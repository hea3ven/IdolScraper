package com.hea3ven.idolscraper.ui

import com.hea3ven.idolscraper.Config
import javax.swing.SwingUtilities

fun main(args: Array<String>) {
	Config.init()
	SwingUtilities.invokeLater {
		val wnd = MainWindow()
		wnd.isVisible = true
	}
}

