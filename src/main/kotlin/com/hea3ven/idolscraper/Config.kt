package com.hea3ven.idolscraper

import java.io.File
import java.util.prefs.Preferences

object Config {
	private lateinit var prefs: Preferences

	fun init() {
		prefs = Preferences.userRoot().node("/idolscraper")
	}

	fun getDestinationDir(): File? {
		val destDir = prefs.get("dest_dir", "")
		return File(if (destDir != "") destDir else "")
	}

	fun setDestinationDir(file: File?) {
		prefs.put("dest_dir", file?.absolutePath ?: "")
	}

	fun getFormat(): String? {
		val format = prefs.get("format", "png")
		return if (format != "") format else null
	}

	fun setFormat(format: String?) {
		prefs.put("format", format ?: "")
	}
}