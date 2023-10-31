package com.mayi.colorplugin.toast

import java.awt.Color
import java.awt.GridBagLayout
import java.awt.Window
import java.awt.geom.RoundRectangle2D
import javax.swing.*


class ToastMessage : JDialog {
    private val serialVersionUID = 1L
    private var spamProtect = false
    private val milliseconds = 1500

    constructor(caller: JComponent?, toastString: String?) : super() {
        if (spamProtect) {
            return
        }
        isUndecorated = true
        setAlwaysOnTop(true)
        setFocusableWindowState(false)
        layout = GridBagLayout()
        val panel = JPanel()
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10))
        panel.setBackground(Color(160, 160, 160))
        val toastLabel = JLabel(toastString)
        toastLabel.setForeground(Color.WHITE)
        panel.add(toastLabel)
        add(panel)
        pack()
        val window: Window = SwingUtilities.getWindowAncestor(caller)
        val xcoord: Int = window.locationOnScreen.x + window.width / 2 - width / 2
        val ycoord: Int = window.locationOnScreen.y + (window.height * 0.75).toInt() - height / 2
        setLocation(xcoord, ycoord)
        shape = RoundRectangle2D.Double(0.0, 0.0, width.toDouble(), height.toDouble(), 30.0, 30.0)
        isVisible = true
        object : Thread() {
            override fun run() {
                try {
                    spamProtect = true
                    sleep(milliseconds.toLong())
                    dispose()
                    spamProtect = false
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}