package com.mayi.colorplugin.dialog

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent

class NewColorDialog() : DialogWrapper(true) {

    //swing样式类，定义在4.3.2
    private val formTestSwing = FormTestSwing()

    init {
        title = "新增颜色值"
        init()
    }

    override fun createNorthPanel(): JComponent? {
        return formTestSwing.initNorth()
    }

    override fun createCenterPanel(): JComponent? {
        return formTestSwing.initCenter()
    }

    override fun createSouthPanel(): JComponent {
        return formTestSwing.initSouth()
    }
}