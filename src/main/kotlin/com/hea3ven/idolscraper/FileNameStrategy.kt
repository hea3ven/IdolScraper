package com.hea3ven.idolscraper

import com.hea3ven.idolscraper.page.ScrapingTask
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ContentDisposition
import java.io.Closeable
import java.net.URLConnection
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock

private val strategies = mapOf(
		"original" to OriginalFileNameStrategy(),
		"numbered" to NumberedFileNameStrategy())

fun getFileNameStrategy(name: String): FileNameStrategy {
	return strategies[name] ?: throw IllegalArgumentException()
}

interface FileNameStrategy {
	fun get(task: ScrapingTask, conn: URLConnection,
			format: String): FileNameReservation

}

open class FileNameReservation(val path: Path) : Closeable {
	override fun close() {
	}
}

class OriginalFileNameStrategy : FileNameStrategy {
	override fun get(task: ScrapingTask, conn: URLConnection, format: String): FileNameReservation {
			return FileNameReservation(task.destDir.resolve(getFileName(conn)))
	}

	private fun  getFileName(conn: URLConnection): String {
		if (conn.getHeaderField("Content-Disposition") != null) {
			val contentDisp = ContentDisposition(conn.getHeaderField("Content-Disposition"))
			return contentDisp.getParameter("filename")
		}
		val fileRx = Regex("([^/]*\\.\\w+).*$")
		return fileRx.find( conn.url.file)?.groupValues?.get(1) ?: throw RuntimeException("Could not figure out the original file name")
	}

}

private val fileNumberLock = ReentrantLock()

class NumberedFileNameStrategy : FileNameStrategy {
	override fun get(task: ScrapingTask, conn: URLConnection,
			format: String): FileNameReservation {
		fileNumberLock.lock()
		val nextFileNo = getNextFileNumber(task.destDir)
		val fileName = nextFileNo.toString().padStart(3, '0') + "." + format
		return object : FileNameReservation(task.destDir.resolve(fileName)) {
			override fun close() {
				fileNumberLock.unlock()
			}
		}
	}

}

