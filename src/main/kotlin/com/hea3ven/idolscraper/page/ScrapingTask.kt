package com.hea3ven.idolscraper.page

import com.hea3ven.idolscraper.SaveStrategy
import com.hea3ven.idolscraper.imageenhancer.enhanceImageUrl
import java.nio.file.Path

class ScrapingTask(val destDir: Path, val saveStrategy: SaveStrategy) {
	private val imagesUrls = mutableListOf<String>()
	val images: List<String>
		get() = imagesUrls

	fun addImage(url: String) {
		imagesUrls.add(enhanceImageUrl(url))
	}

	var log: (String?) -> Unit = { print(it) }
}