package com.hea3ven.idolscraper.ui

import javax.swing.SwingUtilities

fun main(args: Array<String>) {
	SwingUtilities.invokeLater {
		val wnd = MainWindow()
		wnd.isVisible = true
	}
}

