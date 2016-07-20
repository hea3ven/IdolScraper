package com.hea3ven.idolscraper

import com.hea3ven.idolscraper.page.ScrapingTask
import java.io.BufferedInputStream
import java.net.URLConnection
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.management.openmbean.InvalidKeyException

val strategies = mapOf(
		"original" to PreserveOriginalSaveStrategy(),
		"png" to ConvertFormatSaveStrategy("png"),
		"jpg" to ConvertFormatSaveStrategy("jpg"))

private val fileNumberLock = object {}

fun getSaveStrategy(name: String): SaveStrategy {
	return strategies[name] ?: throw InvalidKeyException()
}

interface SaveStrategy {
	fun save(task: ScrapingTask, conn: URLConnection, stream: BufferedInputStream)
}

class PreserveOriginalSaveStrategy : SaveStrategy {
	override fun save(task: ScrapingTask, conn: URLConnection, stream: BufferedInputStream) {
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
			task.log("        Saved as " + fileName)
		}
	}
}

class ConvertFormatSaveStrategy(val format: String) : SaveStrategy {
	override fun save(task: ScrapingTask, conn: URLConnection, stream: BufferedInputStream) {
		synchronized(fileNumberLock) {
			val nextFileNo = getNextFileNumber(task.destDir)
			val fileName = nextFileNo.toString().padStart(3, '0') + "." + format
			val img = ImageIO.read(stream)
			ImageIO.write(img, format, task.destDir.resolve(fileName).toFile())
			task.log("        Saved as " + fileName)
		}
	}
}
