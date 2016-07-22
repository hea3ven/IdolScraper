package com.hea3ven.idolscraper.ui

import com.hea3ven.idolscraper.Config
import com.hea3ven.idolscraper.PreserveOriginalSaveStrategy
import com.hea3ven.idolscraper.getFileNameStrategy
import com.hea3ven.idolscraper.getSaveStrategy
import com.hea3ven.idolscraper.page.ScrapingTask
import com.hea3ven.idolscraper.page.getPageHandler
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder

class MainWindow : JFrame("Idol Scraper") {

	private val dirChooser = JFileChooser().apply {
		dialogTitle = "Target destination"
		fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
		isAcceptAllFileFilterUsed = false
		selectedFile = Config.getDestinationDir()
	}

	init {
		defaultCloseOperation = EXIT_ON_CLOSE
		size = Dimension(600, 900)

		layout = GridBagLayout()

		val urlLbl = JLabel("URL:")
		urlLbl.border = EmptyBorder(0, 5, 0, 0)
		val urlTxt = JTextField()
		urlTxt.addFocusListener(object : FocusListener {
			override fun focusLost(e: FocusEvent?) {
			}

			override fun focusGained(e: FocusEvent?) {
				urlTxt.select(0, urlTxt.text.length)
			}
		})

		val destLbl = JLabel("Destination:")
		destLbl.border = EmptyBorder(0, 5, 0, 0)
		val destTxt = JTextField(Config.getDestinationDir()?.absolutePath ?: "")
		destTxt.addFocusListener(object : FocusListener {
			override fun focusLost(e: FocusEvent?) {
			}

			override fun focusGained(e: FocusEvent?) {
				destTxt.select(0, destTxt.text.length)
			}
		})
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
		formatCb.preferredSize = Dimension(100, 25)
		formatCb.border = EmptyBorder(2, 2, 2, 5)
		formatCb.selectedItem = Config.getFormat()
		formatCb.action = object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				Config.setFormat(formatCb.selectedItem as String?)
			}
		}

		val fileNameLbl = JLabel("File Name:")
		fileNameLbl.border = EmptyBorder(0, 5, 0, 0)
		val fileNameCb = JComboBox(arrayOf("original", "numbered"))
		fileNameCb.preferredSize = Dimension(100, 25)
		fileNameCb.border = EmptyBorder(2, 2, 2, 5)
		fileNameCb.selectedItem = Config.getFileNameFormat()
		fileNameCb.action = object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				Config.setFileNameFormat(fileNameCb.selectedItem as String?)
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

				val fileNameStgy = fileNameCb.selectedItem as String
				val formatStgy = formatCb.selectedItem as String
				val task = ScrapingTask(destDir1, getSaveStrategy(formatStgy),
						getFileNameStrategy(fileNameStgy))

				val worker = object : SwingWorker<Any, String>() {
					override fun doInBackground() {
						task.log = { publish(it) }
						val pageHandler = getPageHandler(urlTxt.text)
						pageHandler.handle(task, urlTxt.text)
						try {
							task.images.forEach {
								downloadImage(it)
							}
							task.log("Done")
						} catch(e: Exception) {
							val ss = StringWriter()
							e.printStackTrace(PrintWriter(ss))
							task.log("Unknown error: " + ss.toString())
						}
						return
					}

					private fun downloadImage(urlString: String) {

						val url: URL
						try {
							url = URL(urlString)
						} catch(e: MalformedURLException) {
							task.log("Bad url " + urlString)
							return
						}
						task.log("Downloading image " + url)
						try {
							val conn = url.openConnection()
							BufferedInputStream(conn.inputStream).use { stream ->
								if (conn.contentType == "video/mp4") {
									PreserveOriginalSaveStrategy().save(task, conn, stream)
								} else {
									stream.mark(Int.MAX_VALUE)
									val size = getImageSize(stream)
									if (size == null) {
										task.log("        Error: unable to download")
										return
									}
									stream.reset()
									task.log("        Size " + size.first + "x" + size.second)

									if ((size.first < 800 || size.second < 800)
											&& (size.first * size.second < 600 * 600)) {
										task.log("        Too small, not saving")
										return
									}
									task.saveStrategy.save(task, conn, stream)
								}
							}
						} catch(e: Exception) {
							task.log("        Error: " + e.toString())
							return
						}
					}

					private fun getImageSize(stream: InputStream): Pair<Int, Int>? {
						ImageIO.createImageInputStream(stream).use {
							val readers = ImageIO.getImageReaders(it)
							if (readers.hasNext()) {
								val reader = readers.next()
								try {
									reader.input = it
									return reader.getWidth(0) to reader.getHeight(0)
								} finally {
									reader.dispose()
								}
							}
						}
						return null
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
		add(JPanel().apply {
			layout = FlowLayout(FlowLayout.LEFT, 0, 0)
			add(formatLbl)
			add(formatCb)
			add(fileNameLbl)
			add(fileNameCb)
		}, GridBagConstraints().apply {
			gridy = 2
			gridwidth = 2
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

