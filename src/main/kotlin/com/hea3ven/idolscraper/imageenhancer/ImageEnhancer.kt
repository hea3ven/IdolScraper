package com.hea3ven.idolscraper.imageenhancer

val enhancers = arrayOf(
		RegexEnhancer(Regex("(https?://.*\\.tistory\\.com/)[^/]*(/.*)"), "$1original$2"),
		RegexEnhancer(Regex("(https?://)post(files.*\\.naver\\.net/.*)\\?.*"), "$1blog$2"),
		RegexEnhancer(Regex("(https?://pbs\\.twimg\\.com/media/.*)(:[a-z]*)?"), "$1:orig"),
		DefaultEnhancer())

fun enhanceImageUrl(url: String): String {
	var newUrl = url
	for (enhancer in enhancers)
		newUrl = enhancer.enhance(newUrl)
	return newUrl
}

interface ImageEnhancer {
	fun enhance(url: String): String
}