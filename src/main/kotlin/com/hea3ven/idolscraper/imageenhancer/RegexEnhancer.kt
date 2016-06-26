package com.hea3ven.idolscraper.imageenhancer

class RegexEnhancer(val regex: Regex, val replace: String) : ImageEnhancer {
	override fun enhance(url: String): String {
		return if (regex.matches(url)) regex.replace(url, replace) else url
	}
}