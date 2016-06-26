package com.hea3ven.idolscraper.ui

import com.hea3ven.idolscraper.imageenhancer.ImageEnhancerManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.image.BufferedImage
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder

class MainWindow : JFrame("Idol Scraper") {
	private val urlTxt: JTextField

	private val scanBtn: JButton

	private val imgEnhancerMgr = ImageEnhancerManager()

	private val dirChooser = JFileChooser().apply {
		dialogTitle = "Target destination"
		fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
		isAcceptAllFileFilterUsed = false
	}

	init {
		defaultCloseOperation = EXIT_ON_CLOSE
		size = Dimension(600, 900)

		layout = GridBagLayout()

		val urlLbl = JLabel("URL:")
		urlLbl.border = EmptyBorder(6, 5, 6, 5)
		add(urlLbl, GridBagConstraints().apply {
			gridy = 0
			fill = GridBagConstraints.HORIZONTAL
			weightx = 0.0
			weighty = 0.0
		})

		urlTxt = JTextField()
		add(urlTxt, GridBagConstraints().apply {
			gridy = 0
			gridx = 1
			gridwidth = 2
			fill = GridBagConstraints.HORIZONTAL
			weightx = 1.0
			weighty = 0.0
		})

		val destLbl = JLabel("Destination:")
		destLbl.border = EmptyBorder(0, 5, 6, 5)
		add(destLbl, GridBagConstraints().apply {
			gridy = 1
			fill = GridBagConstraints.HORIZONTAL
			weightx = 0.0
			weighty = 0.0
		})

		val destPnl = JPanel()
		destPnl.layout = BoxLayout(destPnl, BoxLayout.X_AXIS)

		val destTxt = JTextField()
		destPnl.add(destTxt)

		val destBtn = JButton()
		destBtn.action = object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				if (dirChooser.showOpenDialog(this@MainWindow) == JFileChooser.APPROVE_OPTION) {
					destTxt.text = dirChooser.selectedFile.absolutePath
				}
			}
		}
		destBtn.text = "..."
		destPnl.add(destBtn)

		add(destPnl, GridBagConstraints().apply {
			gridy = 1
			gridx = 1
			gridwidth = 2
			fill = GridBagConstraints.HORIZONTAL
			weightx = 1.0
			weighty = 0.0
		})

		val padPnl = JPanel()
		add(padPnl, GridBagConstraints().apply {
			gridy = 2
			gridx = 1
			fill = GridBagConstraints.HORIZONTAL
			weightx = 1.0
		})

		scanBtn = JButton()
		scanBtn.preferredSize = Dimension(72, 24)
		val scanBtnPnl = JPanel()
		scanBtnPnl.border = EmptyBorder(0, 5, 0, 5)
		scanBtnPnl.add(scanBtn)
		add(scanBtnPnl, GridBagConstraints().apply {
			gridy = 2
			gridx = 2
			fill = GridBagConstraints.NONE
			weightx = 0.0
			weighty = 0.0
		})

		val logPanel = JTextArea()
		val logPanelPnl = JPanel()
		logPanelPnl.layout = BorderLayout()
		logPanelPnl.border = EmptyBorder(5, 5, 5, 5)
		logPanelPnl.add(logPanel)
		add(logPanelPnl, GridBagConstraints().apply {
			gridy = 3
			gridx = 0
			gridwidth = 3
			fill = GridBagConstraints.BOTH
			weightx = 1.0
			weighty = 1.0
		})

		scanBtn.action = object : AbstractAction() {

			override fun actionPerformed(e: ActionEvent?) {
				val destDir = Paths.get(destTxt.text)
				if (destTxt.text.length == 0 || !Files.exists(destDir) || !Files.isDirectory(destDir)) {
					logPanel.text += "Invalid destination directory"
					return
				}

				var nextFileNo: Int = 0
				Files.newDirectoryStream(destDir).use {
					nextFileNo = it.filter { Files.isRegularFile(it) }
							.map { it.fileName?.toString() }
							.filterNotNull()
							.map { it.replace(Regex("\\.[^.]*$"), "").trimStart('0') }
							.filter { it.length > 0 && it.all(Char::isDigit) }
							.map { it.toInt() }
							.max()?.plus(1) ?: 0
				}
				val worker = object : SwingWorker<Any, String>() {
					override fun doInBackground() {
						publish("Downloading the page")
						val doc: Document
						try {
							doc = Jsoup.connect(urlTxt.text).get()
						} catch (e: IllegalArgumentException) {
							publish("Invalid url")
							return
						}
						doc.select("img")
								.map { it.attr("src") }
								.filter { it != null && it.trim().length > 0 }
								.map { imgEnhancerMgr.enhance(it) }
								.forEach {
									downloadImage(it)
								}
						publish("Done")
						return
					}

					private fun downloadImage(urlString: String) {

						val url: URL
						try {
							url = URL(urlString)
						} catch(e: MalformedURLException) {
							publish("Bad url " + urlString)
							return
						}
						publish("Downloading image " + url)
						val img: BufferedImage?
						try {
							img = ImageIO.read(url)
						} catch(e: Exception) {
							publish("        Error: " + e.toString())
							return
						}
						publish("        Size " + img.width + "x" + img.height)

						if (img.width < 1000 && img.height < 800) {
							publish("        Too small, not saving")
							return
						}
						val fileName = (nextFileNo++).toString().padStart(3, '0') + ".png"
						ImageIO.write(img, "png", destDir.resolve(fileName).toFile())
						publish("        Saved as " + fileName)
					}

					override fun process(chunks: MutableList<String>) {
						chunks.forEach { logPanel.text += it + "\n" }
					}

					override fun done() {
						super.done()
						scanBtn.isEnabled = true
					}

				}
				scanBtn.isEnabled = false
				worker.execute()
			}
		}
		scanBtn.text = "Scan"
	}

}

