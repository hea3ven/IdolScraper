package com.hea3ven.idolscraper.ui

import com.hea3ven.idolscraper.Config
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

fun main(args: Array<String>) {
	try {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
	} catch (e: UnsupportedLookAndFeelException) {
	} catch (e: ClassNotFoundException) {
	} catch (e: InstantiationException) {
	} catch (e: IllegalAccessException) {
	}
	Config.init()
	SwingUtilities.invokeLater {
		val wnd = MainWindow()
		wnd.isVisible = true
	}
}

