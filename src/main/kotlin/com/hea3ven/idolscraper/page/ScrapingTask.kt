package com.hea3ven.idolscraper.page

import com.hea3ven.idolscraper.FileNameStrategy
import com.hea3ven.idolscraper.SaveStrategy
import com.hea3ven.idolscraper.imageenhancer.enhanceImageUrl
import java.net.URL
import java.nio.file.Path

class ScrapingTask(val destDir: Path, val saveStrategy: SaveStrategy, val fileNameStrategy: FileNameStrategy) {
	private val imagesUrls = mutableListOf<String>()
	val images: List<String>
		get() = imagesUrls

	fun addImage(url: String) {
		imagesUrls.add(enhanceImageUrl(url))
	}

	var log: (String?) -> Unit = { print(it) }

	lateinit var url: URL
}