package starknet.data.types

import com.swmansion.starknet.data.types.StarknetChainId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class StarknetChainIdTest {
    companion object {
        @JvmStatic
        fun getChainIdData(): List<ChainIdData> {
            return listOf(
                ChainIdData(StarknetChainId.MAIN, "SN_MAIN", "0x534e5f4d41494e"),
                ChainIdData(StarknetChainId.GOERLI, "SN_GOERLI", "0x534e5f474f45524c49"),
                ChainIdData(StarknetChainId.SEPOLIA, "SN_SEPOLIA", "0x534e5f5345504f4c4941"),
                ChainIdData(
                    StarknetChainId.INTEGRATION_SEPOLIA,
                    "SN_INTEGRATION_SEPOLIA",
                    "0x534e5f494e544547524154494f4e5f5345504f4c4941",
                ),
            )
        }
    }
    data class ChainIdData(
        val chainId: StarknetChainId,
        val name: String,
        val hex: String,
    )

    @ParameterizedTest
    @MethodSource("getChainIdData")
    fun `from short string`(data: ChainIdData) {
        val chainId = StarknetChainId.fromShortString(data.name)
        assertEquals(data.chainId, chainId)
    }

    @Test
    fun `from hex`() {
        val chainId = StarknetChainId.fromHex("0x534e5f4d41494e")
        assertEquals(StarknetChainId.MAIN, chainId)
    }
}
