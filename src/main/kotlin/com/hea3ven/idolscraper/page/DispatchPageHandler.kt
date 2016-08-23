package com.hea3ven.idolscraper.page

import org.jsoup.nodes.Document
import java.net.URL

class DispatchPageHandler : ScrapeImgPageHandler() {
	override fun canHandle(url: URL): Boolean {
		return url.toString().contains("1boon.kakao.com/dispatch/")
	}

	override fun getImages(doc: Document, task: ScrapingTask) {
		doc.select("meta[property=og:image]")
				.map { it.attr("content") }
				.filter { it != null && it.trim().length > 0 }
				.forEach {
					task.addImage(it)
				}
	}
}