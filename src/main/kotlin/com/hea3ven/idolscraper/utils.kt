package com.hea3ven.idolscraper

import java.nio.file.Files
import java.nio.file.Path

fun getNextFileNumber(destDir: Path): Int {
	Files.newDirectoryStream(destDir).use {
		return it.filter { Files.isRegularFile(it) }
				.map { it.fileName?.toString() }
				.filterNotNull()
				.map { it.replace(Regex("\\.[^.]*$"), "") }
				.filter { it.length > 0 && it.all(Char::isDigit) }
				.map { it.toInt() }
				.max()?.plus(1) ?: 0
	}
}
