package com.hea3ven.idolscraper

import java.io.File
import java.util.prefs.Preferences

object Config {
	private lateinit var prefs: Preferences

	fun init() {
		prefs = Preferences.userRoot().node("/idolscraper")
	}

	fun getDestinationDir(): File? {
		val destDir = prefs.get("dest_dir", null)
		return if (destDir != null) File(destDir) else null
	}

	fun setDestinationDir(file: File?) {
		prefs.put("dest_dir", file?.absolutePath ?: null)
	}
}