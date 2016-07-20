package com.hea3ven.idolscraper.page

val handlers = arrayOf(
		TwitterMediaPageHandler(),
		ScrapeImgPageHandler())

fun getPageHandler(url: String): PageHandler {
	for (handler in handlers)
		if (handler.canHandle(url))
			return handler
	throw RuntimeException("No handler for " + url)
}

interface PageHandler {
	fun canHandle(url: String): Boolean
	fun handle(task: ScrapingTask, url: String)
}
