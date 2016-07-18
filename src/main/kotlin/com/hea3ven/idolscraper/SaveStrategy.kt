package com.hea3ven.idolscraper

import com.hea3ven.idolscraper.page.ScrapingTask
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.net.URLConnection
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.management.openmbean.InvalidKeyException

val strategies = mapOf<String, SaveStrategy>("original" to PreserveOriginalSaveStrategy(),
		"png" to ConvertFormatSaveStrategy("png"),
		"jpg" to ConvertFormatSaveStrategy("jpg"))

private val fileNumberLock = object {}

fun getSaveStrategy(name: String): SaveStrategy {
	return strategies[name] ?: throw InvalidKeyException()
}

interface SaveStrategy {
	fun save(task: ScrapingTask, conn: URLConnection, img: BufferedImage, stream: BufferedInputStream)
}

class PreserveOriginalSaveStrategy : SaveStrategy {
	override fun save(task: ScrapingTask, conn: URLConnection, img: BufferedImage,
			stream: BufferedInputStream) {
		synchronized(fileNumberLock) {
			val format = when (conn.contentType) {
				"image/jpg", "image/jpeg", "image/pjpeg" -> "jpg"
				"image/png" -> "png"
				"image/gif", "video/gif" -> "gif"
				"video/webm" -> "webm"
				"video/mp4", "video/mpeg" -> "mp4"
				else -> {
					"jpg"
				}
			}
			val nextFileNo = getNextFileNumber(task.destDir)
			val fileName = nextFileNo.toString().padStart(3, '0') + "." + format
			Files.copy(stream, task.destDir.resolve(fileName))
//			publish("        Saved as " + fileName)
		}
	}
}

class ConvertFormatSaveStrategy(val format: String) : SaveStrategy {
	override fun save(task: ScrapingTask, conn: URLConnection, img: BufferedImage,
			stream: BufferedInputStream) {
		synchronized(fileNumberLock) {
			val nextFileNo = getNextFileNumber(task.destDir)
			val fileName = nextFileNo.toString().padStart(3, '0') + "." + format
			ImageIO.write(img, format, task.destDir.resolve(fileName).toFile())
//			publish("        Saved as " + fileName)
		}
	}
}
