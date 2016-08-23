package com.hea3ven.idolscraper.page

import java.net.URL

val handlers = arrayOf(
		TwitterMediaPageHandler(),
		DispatchPageHandler(),
		ScrapeImgPageHandler())

fun getPageHandler(url: URL): PageHandler {
	for (handler in handlers)
		if (handler.canHandle(url))
			return handler
	throw RuntimeException("No handler for " + url)
}

interface PageHandler {
	fun canHandle(url: URL): Boolean
	fun handle(task: ScrapingTask, url: URL)
}
