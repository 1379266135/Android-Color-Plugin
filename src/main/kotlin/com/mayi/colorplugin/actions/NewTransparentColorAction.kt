package com.mayi.colorplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.mayi.colorplugin.dialog.NewColorDialog

class NewTransparentColorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = NewColorDialog()
        dialog.isResizable = true
        dialog.show()
    }
}