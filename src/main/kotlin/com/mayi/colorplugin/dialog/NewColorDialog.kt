package com.mayi.colorplugin.dialog

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent

class NewColorDialog(e: AnActionEvent) : DialogWrapper(true) {

    //swing样式类，定义在4.3.2
    private val formTestSwing = FormTestSwing(e)

    init {
        title = "New Transparent Color Value Resource"
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