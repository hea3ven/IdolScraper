package com.hea3ven.idolscraper

import com.hea3ven.idolscraper.page.ScrapingTask
import java.io.BufferedInputStream
import java.net.URLConnection
import java.nio.file.Files
import javax.imageio.ImageIO

private val strategies = mapOf(
		"original" to PreserveOriginalSaveStrategy(),
		"png" to ConvertFormatSaveStrategy("png"),
		"jpg" to ConvertFormatSaveStrategy("jpg"))

fun getSaveStrategy(name: String): SaveStrategy {
	return strategies[name] ?: throw IllegalArgumentException()
}

interface SaveStrategy {
	fun save(task: ScrapingTask, conn: URLConnection, stream: BufferedInputStream)
}

class PreserveOriginalSaveStrategy : SaveStrategy {
	override fun save(task: ScrapingTask, conn: URLConnection, stream: BufferedInputStream) {
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
		task.fileNameStrategy.get(task, conn, format).use {
			Files.copy(stream, task.destDir.resolve(it.path))
			task.log("        Saved as " + it.path.fileName.toString())
		}
	}
}

class ConvertFormatSaveStrategy(val format: String) : SaveStrategy {
	override fun save(task: ScrapingTask, conn: URLConnection, stream: BufferedInputStream) {
		task.fileNameStrategy.get(task, conn, format).use {
			val img = ImageIO.read(stream)
			ImageIO.write(img, format, it.path.toFile())
			task.log("        Saved as " + it.path.fileName.toString())
		}
	}
}
