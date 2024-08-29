package io.github.apwlq.sow.client

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import javax.imageio.ImageIO

class Network {

    fun convertToBufferedImage(data: ByteArray): BufferedImage? {
        return try {
            ByteArrayInputStream(data).use { bais ->
                ImageIO.read(bais)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun receiveMulticast(group: InetAddress, port: Int, bufferSize: Int): ByteArray? {
        val socket = MulticastSocket(port)
        socket.joinGroup(group)

        val receivedData = mutableListOf<ByteArray>()
        var totalSize = 0

        try {
            while (true) {
                val buffer = ByteArray(bufferSize)
                val packet = DatagramPacket(buffer, buffer.size)

                socket.receive(packet)

                val packetData = packet.data.copyOf(packet.length)
                receivedData.add(packetData)
                totalSize += packet.length

                // 패킷 크기가 버퍼 크기보다 작으면 전송이 완료되었다고 가정
                if (packet.length < bufferSize) {
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            socket.leaveGroup(group)
            socket.close()
        }

        // 모든 수신 데이터를 하나의 바이트 배열로 결합
        val combinedData = ByteArray(totalSize)
        var offset = 0
        for (chunk in receivedData) {
            System.arraycopy(chunk, 0, combinedData, offset, chunk.size)
            offset += chunk.size
        }
        return combinedData
    }
}
