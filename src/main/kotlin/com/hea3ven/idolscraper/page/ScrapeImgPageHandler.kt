package com.hea3ven.idolscraper.page

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

open class ScrapeImgPageHandler : PageHandler {
	override fun canHandle(url: String) = true
	override fun handle(task: ScrapingTask, url: String) {
		task.log("Downloading the page")
		val doc: Document
		try {
			doc = Jsoup.connect(url).get()
		} catch (e: IllegalArgumentException) {
			task.log("Invalid url")
			return
		}
		return doc.select("img")
				.map { it.attr("src") }
				.filter { it != null && it.trim().length > 0 }
				.forEach {
					task.addImage(it)
				}
	}

}