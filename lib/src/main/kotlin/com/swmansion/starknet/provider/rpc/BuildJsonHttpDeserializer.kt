package com.swmansion.starknet.provider.rpc

import com.swmansion.starknet.data.serializers.JsonRpcErrorSerializer
import com.swmansion.starknet.provider.exceptions.RequestFailedException
import com.swmansion.starknet.provider.exceptions.RpcRequestFailedException
import com.swmansion.starknet.service.http.HttpResponseDeserializer
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.util.function.Function

@Serializable
private data class JsonRpcResponse<T>(
    @SerialName("id")
    val id: Int,

    @SerialName("jsonrpc")
    val jsonRpc: String,

    @SerialName("result")
    val result: T? = null,

    @SerialName("error")
    val error: JsonRpcError? = null,
)

@Serializable(with = JsonRpcErrorSerializer::class)
internal data class JsonRpcError(
    @SerialName("code")
    val code: Int,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: String? = null,
)

private fun <T> extractResult(jsonRpcResponse: JsonRpcResponse<T>, totalPayload: String, payload: String = totalPayload): T {
    if (jsonRpcResponse.error != null) {
        throw RpcRequestFailedException(
            code = jsonRpcResponse.error.code,
            message = jsonRpcResponse.error.message,
            data = jsonRpcResponse.error.data,
            payload = payload,
        )
    }

    if (jsonRpcResponse.result == null) {
        throw RequestFailedException(message = "Response did not contain a result", payload = totalPayload)
    }
    return jsonRpcResponse.result
}

@JvmSynthetic
internal fun <T> buildJsonHttpDeserializer(
    deserializationStrategy: KSerializer<T>,
    deserializationJson: Json,
): HttpResponseDeserializer<T> {
    return Function { response ->
        if (!response.isSuccessful) {
            throw RequestFailedException(
                payload = response.body,
            )
        }
        val jsonRpcResponse =
            deserializationJson.decodeFromString(
                JsonRpcResponse.serializer(deserializationStrategy),
                response.body,
            )

        extractResult(jsonRpcResponse, response.body)
    }
}

internal fun <T> buildJsonBatchHttpDeserializer(
    deserializationStrategies: List<KSerializer<T>>,
    deserializationJson: Json,
): HttpResponseDeserializer<List<T>> {
    // TODO: In case of batch request, exception should not be thrown.
    // Instead, we want to return wrapped responses.
    // This enables access to successful results or encountered errors.

    return Function { response ->
        if (!response.isSuccessful) {
            throw RequestFailedException(
                payload = response.body,
            )
        }

        val jsonArray = Json.parseToJsonElement(response.body).jsonArray
        val jsonRpcResponses = deserializationStrategies.mapIndexed { index, strategy ->
            deserializationJson.decodeFromString(
                JsonRpcResponse.serializer(strategy),
                jsonArray[index].toString(),
            )
        }

        val results = jsonRpcResponses.sortedBy { it.id }.zip(jsonArray)
            .map { (jsonRpcResponse, json) ->
                val payload = json.toString()
                extractResult(jsonRpcResponse, response.body, payload)
            }
        results
    }
}
