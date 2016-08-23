package com.hea3ven.idolscraper.page

import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import java.net.URL

class TwitterMediaPageHandler : PageHandler {
	val urlRegex = Regex("https?://twitter.com/[^/]*/status/([^/]*)")
	var twitterConfigured = false

	override fun canHandle(url: URL): Boolean {
		return url.toString().matches(urlRegex)
	}

	override fun handle(task: ScrapingTask, url: URL) {
		task.log("Connecting with twitter")
		val twitter = TwitterFactory.getSingleton()
		if (!twitterConfigured) {
			twitterConfigured = true
			twitter.setOAuthConsumer("ZCJBFSC9TWPC4R577OGQ", "ejz2QhhU9xWEfAV9Fg9Ulz7Wuakbnl9Z13oRlFY64")
			twitter.oAuthAccessToken = AccessToken("15209064-f8PPgvr2GpBws1S8hVGRUsKdr5jRg8m8Ag64OJ2Mo",
					"rE8rAFCn9BmB9cu5Lruzdfupf9xGYBPpEJSIYounSI")
		}

		val match = urlRegex.matchEntire(url.toString())
		if (match == null) {
			task.log("Could not get tweet id from url")
			return
		}

		val status = twitter.tweets().showStatus(match.groupValues[1].toLong())
		status.extendedMediaEntities.forEach {
			when (it.type) {
				"animated_gif" -> {
					task.addImage(it.videoVariants[0].url)
				}
				"video" -> {
					val videoUrl = it.videoVariants.filter { it.contentType == "video/mp4" }.maxBy { it.bitrate }?.url
					if (videoUrl != null) {
						task.addImage(videoUrl)
					}
				}
				"photo" -> {
					task.addImage(it.mediaURL)
				}
			}
		}
		// TODO: get vine from status.urlEntities
	}
}