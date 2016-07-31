package com.hea3ven.idolscraper.page

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SeniroPageHandler : ScrapeImgPageHandler() {
	override fun canHandle(url: String): Boolean {
		return url.contains("blog.naver.com/seniro94")
	}

	override fun handle(task: ScrapingTask, url: String) {
		task.log("Getting the correct url")
		val doc: Document
		try {
			doc = Jsoup.connect(url).get()
		} catch (e: IllegalArgumentException) {
			task.log("Invalid url")
			return
		}
		val newUrl = "http://blog.naver.com" + doc.select("frame").first().attr("src")
		task.log("url is " + newUrl)
		super.handle(task, newUrl)
	}
}