import java.io.DataInputStream
import java.io.DataOutputStream

class ServidorHilo(
    private val IN: DataInputStream,
    private val OUT: DataOutputStream,
    private val nombreCliente: String) : Thread() {
    override fun run() {
    }
}