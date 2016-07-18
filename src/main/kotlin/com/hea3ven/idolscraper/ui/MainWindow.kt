package com.hea3ven.idolscraper.ui

import com.hea3ven.idolscraper.Config
import com.hea3ven.idolscraper.getSaveStrategy
import com.hea3ven.idolscraper.imageenhancer.ImageEnhancerManager
import com.hea3ven.idolscraper.page.ScrapingTask
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.*
import java.awt.event.ActionEvent
import java.io.BufferedInputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder

class MainWindow : JFrame("Idol Scraper") {
	private val imgEnhancerMgr = ImageEnhancerManager()

	private val dirChooser = JFileChooser().apply {
		dialogTitle = "Target destination"
		fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
		isAcceptAllFileFilterUsed = false
		currentDirectory = Config.getDestinationDir()
	}

	init {
		defaultCloseOperation = EXIT_ON_CLOSE
		size = Dimension(600, 900)

		layout = GridBagLayout()

		val urlLbl = JLabel("URL:")
		urlLbl.border = EmptyBorder(0, 5, 0, 0)
		val urlTxt = JTextField()

		val destLbl = JLabel("Destination:")
		destLbl.border = EmptyBorder(0, 5, 0, 0)
		val destTxt = JTextField(Config.getDestinationDir()?.absolutePath ?: "")
		val destBtn = JButton()
		destBtn.action = object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				if (dirChooser.showOpenDialog(this@MainWindow) == JFileChooser.APPROVE_OPTION) {
					destTxt.text = dirChooser.selectedFile.absolutePath
					Config.setDestinationDir(dirChooser.selectedFile)
				}
			}
		}
		destBtn.text = "..."

		val formatLbl = JLabel("Format:")
		formatLbl.border = EmptyBorder(0, 5, 0, 0)
		val formatCb = JComboBox(arrayOf("original", "png", "jpg"))
		formatCb.preferredSize = Dimension(100, 22)
		formatCb.border = EmptyBorder(2, 2, 2, 5)
		formatCb.selectedItem = Config.getFormat()
		formatCb.action = object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				Config.setFormat(formatCb.selectedItem as String?)
			}
		}

		val scanBtn = JButton()
		scanBtn.preferredSize = Dimension(72, 22)

		val logTxtArea = JTextArea()
		logTxtArea.isEditable = false
		val logTxtAreaScrlPn = JScrollPane(logTxtArea)

		scanBtn.action = object : AbstractAction() {

			override fun actionPerformed(e: ActionEvent?) {
				val destDir1 = Paths.get(destTxt.text)
				if (destTxt.text.length == 0 || !Files.exists(destDir1) || !Files.isDirectory(destDir1)) {
					logTxtArea.text += "Invalid destination directory"
					return
				}

				val task = ScrapingTask(destDir1, getSaveStrategy(formatCb.selectedItem as String))

				val worker = object : SwingWorker<Any, String>() {
					override fun doInBackground() {
						try {
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
						} catch(e: Exception) {
							val ss = StringWriter()
							e.printStackTrace(PrintWriter(ss))
							publish("Unknown error: " + ss.toString())
						}
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
						try {
							val conn = url.openConnection()
							BufferedInputStream(conn.inputStream).use { stream ->
								stream.mark(Int.MAX_VALUE)
								val img = ImageIO.read(stream)
								if (img == null) {
									publish("        Error: unable to download")
									return
								}
								stream.reset()
								publish("        Size " + img.width + "x" + img.height)

								if (img.width < 800 || img.height < 800) {
									publish("        Too small, not saving")
									return
								}
								task.saveStrategy.save(task, conn, img, stream)
							}
						} catch(e: Exception) {
							publish("        Error: " + e.toString())
							return
						}
					}

					override fun process(chunks: MutableList<String>) {
						val vertScroll = logTxtAreaScrlPn.verticalScrollBar
						val doScroll = vertScroll.value == vertScroll.maximum
						chunks.forEach { logTxtArea.text += it + "\n" }
						if (doScroll)
							vertScroll.value = vertScroll.maximum
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

		add(urlLbl, GridBagConstraints().apply {
			gridy = 0
			fill = GridBagConstraints.HORIZONTAL
			weightx = 0.0
			weighty = 0.0
		})

		add(JPanel().apply {
			border = EmptyBorder(5, 2, 2, 5)
			layout = BorderLayout()
			add(urlTxt, BorderLayout.CENTER)
		}, GridBagConstraints().apply {
			gridy = 0
			gridx = 1
			gridwidth = 2
			fill = GridBagConstraints.HORIZONTAL
			weightx = 1.0
			weighty = 0.0
		})
		add(destLbl, GridBagConstraints().apply {
			gridy = 1
			fill = GridBagConstraints.HORIZONTAL
			weightx = 0.0
			weighty = 0.0
		})

		add(JPanel().apply {
			layout = GridBagLayout()
			border = EmptyBorder(2, 2, 2, 5)
			add(destTxt, GridBagConstraints().apply {
				weightx = 1.0
				fill = GridBagConstraints.HORIZONTAL
			})
			add(destBtn, GridBagConstraints().apply {
				weightx = 0.0
			})
		}, GridBagConstraints().apply {
			gridy = 1
			gridx = 1
			gridwidth = 2
			fill = GridBagConstraints.HORIZONTAL
			weightx = 1.0
			weighty = 0.0
		})
		add(formatLbl, GridBagConstraints().apply {
			gridy = 2
			fill = GridBagConstraints.HORIZONTAL
			weightx = 0.0
			weighty = 0.0
		})
		add(JPanel().apply {
			layout = FlowLayout(FlowLayout.LEFT, 0, 0)
			add(formatCb)
		}, GridBagConstraints().apply {
			gridy = 2
			gridx = 1
			fill = GridBagConstraints.HORIZONTAL
			weightx = 0.0
			weighty = 0.0
		})

		add(JPanel(), GridBagConstraints().apply {
			gridy = 3
			gridx = 1
			fill = GridBagConstraints.HORIZONTAL
			weightx = 1.0
		})

		add(JPanel().apply {
			add(scanBtn)
		}, GridBagConstraints().apply {
			gridy = 3
			gridx = 2
			fill = GridBagConstraints.NONE
			weightx = 0.0
			weighty = 0.0
		})

		add(JPanel().apply {
			layout = BorderLayout()
			border = EmptyBorder(0, 5, 5, 5)
			add(logTxtAreaScrlPn, BorderLayout.CENTER)
		}, GridBagConstraints().apply {
			gridy = 4
			gridx = 0
			gridwidth = 3
			fill = GridBagConstraints.BOTH
			weightx = 1.0
			weighty = 1.0
		})
	}
}

