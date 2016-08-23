package com.hea3ven.idolscraper.page

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL

open class ScrapeImgPageHandler : PageHandler {
	override fun canHandle(url: URL) = true
	override fun handle(task: ScrapingTask, url: URL) {
		task.log("Downloading the page")
		val doc: Document
		try {
			doc = Jsoup.connect(url.toString()).get()!!
		} catch (e: IllegalArgumentException) {
			task.log("Invalid url")
			return
		} catch (e: Exception) {
			val ss = StringWriter()
			e.printStackTrace(PrintWriter(ss))
			task.log("Unknown error: " + ss.buffer.toString())
			return
		}
		getImages(doc, task)
		getFrames(url, doc, task)
	}

	protected open fun getImages(doc: Document, task: ScrapingTask) {
		doc.select("img")
				.map { it.attr("src") }
				.filter { it != null && it.trim().length > 0 }
				.forEach {
					task.addImage(it)
				}
	}

	private fun getFrames(url: URL, doc: Document, task: ScrapingTask) {
		doc.select("frame").forEach {
			val newUrl = URL(url, it.attr("src"))
			val pageHandler = getPageHandler(newUrl)
			pageHandler.handle(task, newUrl)
		}
	}

}