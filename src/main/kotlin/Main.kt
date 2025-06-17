import impl.CRC32
import `interface`.ICRC32

fun main() {
    val hash = ""
    val crc32: ICRC32 = CRC32()

    val start = System.currentTimeMillis()
    val result = crc32.reverseMID(hash)

    val elapsed = System.currentTimeMillis() - start
    println("Hash: $hash")
    println("Result: $result")
    println("Elapsed: $elapsed ms")
}
