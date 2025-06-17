import impl.CRC32

fun main() {
    val hash = ""
    val start = System.currentTimeMillis()
    val result = CRC32().reverseMID(hash)
    val elapsed = System.currentTimeMillis() - start
    println("Hash: $hash")
    println("Result: $result")
    println("Elapsed: $elapsed ms")
}