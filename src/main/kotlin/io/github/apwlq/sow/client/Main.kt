package io.github.apwlq.sow.client

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.File
import java.net.InetAddress
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingUtilities
import java.util.concurrent.atomic.AtomicBoolean
import java.util.jar.JarFile
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val jarFilePath = System.getProperty("java.class.path").split(":").firstOrNull { it.endsWith(".jar") }
    val version = jarFilePath?.let {
        try {
            JarFile(it).use { jar ->
                val manifest = jar.manifest
                manifest.mainAttributes.getValue("Implementation-Version") ?: "unknown"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "unknown"
        }
    } ?: "unknown"

    val screenIndex = args.indexOfFirst { it == "--screen" || it == "-s" }
        .takeIf { it >= 0 && it + 1 < args.size }
        ?.let { args[it + 1].toIntOrNull() }
        ?: 0
    when {
        args.contains("--version") -> {
            println(version)
            stop()
        }
        args.contains("--help") -> {
            println("Usage: java -jar wsm-slaves.jar [--screen screenIndex | -s screenIndex]")
            println("screenIndex: Index of the screen to display the image (default: 0)")
            stop()
        }
        else -> {
            // 여기서 screenIndex를 사용하여 나머지 로직을 계속 진행합니다.
            println("Selected screen index: $screenIndex")
        }
    }

    val multicastGroup = InetAddress.getByName("230.0.0.0")
    val port = 4446
    val mousePort = 4447
    val bufferSize = 65000
    val defaultImagePath = "./splash.png"
    val imageUpdateTimeout = 3600_000L // 10 seconds timeout

    val gd: GraphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices[screenIndex]
    // JFrame 초기화 및 전체화면 설정
    val frame = JFrame("WSM Display").apply {
        isUndecorated = true
        gd.fullScreenWindow = this
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    // JLabel 초기화 및 프레임에 추가
    val label = JLabel().apply {
        frame.contentPane.add(this)
    }

    // 마우스 포인터 숨기기
    val cursor = Toolkit.getDefaultToolkit().createCustomCursor(
        Toolkit.getDefaultToolkit().getImage(""), Point(0, 0), "invisibleCursor"
    )
    frame.cursor = cursor


    // 앱 아이콘 설정
    try {
        val iconStream = Thread.currentThread().contextClassLoader.getResourceAsStream("logo.png")
        val iconImage = ImageIO.read(iconStream)
        frame.iconImage = iconImage
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 기본 이미지 로드
    val defaultImage: BufferedImage = try {
        ImageIO.read(File(defaultImagePath))
    } catch (e: Exception) {
        BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).apply {
            createGraphics().apply {
                color = Color.BLUE
                fillRect(0, 0, width, height)
                dispose()
            }
        }
    }

    // 현재 화면에 표시될 이미지를 저장하는 변수
    var currentImage: BufferedImage = defaultImage

    // 창 종료 시 프로그램 종료 설정
    val running = AtomicBoolean(true)
    frame.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            running.set(false)
        }
    })

    val receive = Network()
    var lastUpdateTime = System.currentTimeMillis()

    // 마우스 커서 이미지를 로드
    val cursorImage: BufferedImage? = try {
        val cursorStream = Thread.currentThread().contextClassLoader.getResourceAsStream("cursor.png")
        ImageIO.read(cursorStream)
    } catch (e: Exception) {
        null
    }

    // Coroutine scope for managing coroutines
    val coroutineScope = CoroutineScope(Dispatchers.Default + Job())
    val img = ImgUtlity()

    // Image update coroutine
    coroutineScope.launch {
        while (running.get()) {
            try {
                val currentTime = System.currentTimeMillis()

                // 이미지 수신
                val imageData = receive.receiveMulticast(multicastGroup, port, bufferSize)

                if (imageData != null) {
                    val image = receive.convertToBufferedImage(imageData)

                    if (image != null) {
                        val screenSize = Toolkit.getDefaultToolkit().screenSize
                        currentImage = img.resizeImageToScreen(image, screenSize.width, screenSize.height)

                        withContext(Dispatchers.Swing) {
                            SwingUtilities.invokeLater {
                                // 현재 이미지에 커서를 그리지 않은 상태로 화면을 업데이트
                                label.icon = ImageIcon(currentImage)
                                frame.repaint()
                            }
                        }

                        lastUpdateTime = currentTime
                    } else {
                        println("Failed to reconstruct image.")
                    }
                }

                if (currentTime - lastUpdateTime > imageUpdateTimeout) {
                    withContext(Dispatchers.Swing) {
                        SwingUtilities.invokeLater {
                            currentImage = defaultImage
                            label.icon = ImageIcon(defaultImage)
                            frame.repaint()
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error in image coroutine: ${e.message}")
            }

            delay(1000) // 1 second delay
        }
    }

    // Mouse position coroutine
    coroutineScope.launch {
        while (running.get()) {
            try {
                val receivedData = receive.receiveMulticast(multicastGroup, mousePort, bufferSize) ?: continue
                val receivedString = String(receivedData).trim()

                val dataParts = receivedString.split(",")
                if (dataParts.size == 2) {
                    val widthRatio = dataParts[0].toDoubleOrNull()
                    val heightRatio = dataParts[1].toDoubleOrNull()

                    if (widthRatio != null && heightRatio != null) {
                        val screenSize = Toolkit.getDefaultToolkit().screenSize
                        val screenWidth = screenSize.width
                        val screenHeight = screenSize.height

                        val mouseX = (widthRatio / 100 * screenWidth).toInt()
                        val mouseY = (heightRatio / 100 * screenHeight).toInt()

                        // 기존의 currentImage에 커서를 그린다.
                        val cursorImageWithMouse = cursorImage?.let {
                            val scaledCursorImage = img.scaleImageToAspectRatio(it, 20, 20)
                            val combinedImage = BufferedImage(currentImage.width, currentImage.height, BufferedImage.TYPE_INT_ARGB)
                            val graphics = combinedImage.createGraphics()
                            graphics.drawImage(currentImage, 0, 0, null)
                            graphics.drawImage(scaledCursorImage, mouseX, mouseY, null)
                            graphics.dispose()
                            combinedImage
                        }

                        withContext(Dispatchers.Swing) {
                            SwingUtilities.invokeLater {
                                if (cursorImageWithMouse != null) {
                                    label.icon = ImageIcon(cursorImageWithMouse)
                                    frame.repaint()
                                }
                            }
                        }
                    } else {
                        println("Received invalid mouse position data: $receivedString")
                    }
                } else {
                    println("Received invalid mouse position data format: $receivedString")
                }
            } catch (e: Exception) {
                println("Error receiving mouse position: ${e.message}")
            }
        }
    }

    // Handle application exit
    Runtime.getRuntime().addShutdownHook(Thread {
        coroutineScope.cancel()
        try {
            coroutineScope.coroutineContext[Job]?.invokeOnCompletion { println("All coroutines completed") }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    })

    frame.isVisible = true
}

fun stop() {
    exitProcess(0)
}
