package com.hea3ven.idolscraper.imageenhancer

class ImageEnhancerManager {
	val enhancers = arrayOf<ImageEnhancer>(
			RegexEnhancer(Regex("(https?://.*\\.tistory.com/)[^/]*(/.*)"), "$1original$2"),
			DefaultEnhancer())

	fun enhance(url: String): String {
		var newUrl = url
		for (enhancer in enhancers)
			newUrl = enhancer.enhance(newUrl)
		return newUrl
	}
}

