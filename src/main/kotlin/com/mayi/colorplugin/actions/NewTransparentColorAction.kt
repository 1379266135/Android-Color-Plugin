package com.mayi.colorplugin.actions

import com.android.tools.idea.util.toIoFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.xml.XmlFile
import com.mayi.colorplugin.dialog.NewColorDialog


class NewTransparentColorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = NewColorDialog(e)
        dialog.isResizable = true
        dialog.show()

        /*ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(e.project, {
                createColor(e, "#201b1b1b")

            }, "create colors file", null)
        }*/
    }

    /*private fun createColor(event: AnActionEvent, colorValue: String) {
        val project = event.project
        val baseDir = project?.baseDir
        val srcDir = VfsUtil.createDirectoryIfMissing(baseDir, "src")
        val mainDir = VfsUtil.createDirectoryIfMissing(srcDir, "main")
        val resDir = VfsUtil.createDirectoryIfMissing(mainDir, "res")
        val valuesDir = VfsUtil.createDirectoryIfMissing(resDir, "values")
        val colorsFile = valuesDir.findChild("color.xml")

        if (project != null) {
            if (colorsFile != null) {
                addColorMap(colorsFile, project, "syc_h", "#30fb5777")
            } else {
                createColorsFile(valuesDir, project, event)
            }
        }
    }

    private fun createColorsFile(parentDir: VirtualFile, project: Project, event: AnActionEvent) {
        val file = parentDir.findOrCreateChildData(event, "color.xml")
        val xmlContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources xmlns:tools=\"http://schemas.android.com/tools\">" +
                "\n" +
                "</resources>"
        VfsUtil.saveText(file, xmlContent)
        PsiDocumentManager.getInstance(project).commitAllDocuments()

    }

    private fun addColorMap(file: VirtualFile, project: Project, colorName: String, colorValue: String) {
        val psiFile = PsiManager.getInstance(project).findFile(file) as? XmlFile
        psiFile?.let { psi ->
            val factory = XmlElementFactory.getInstance(project)
            val xmlTag = factory.createTagFromText("    <color name=\"$colorName\">$colorValue</color>")
            psi.rootTag?.addSubTag(xmlTag, false)

            try {
                // 使用 VfsUtil 存储文本内容
//                        VfsUtil.saveText(psi.virtualFile, psi.text)
                // 格式化 XML 文档
                CodeStyleManager.getInstance(project).reformat(psi)
                VirtualFileManager.getInstance().syncRefresh();
                // 刷新项目
//                        refreshProject(project)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 刷新项目
    private fun refreshProject(project: Project) {
        val baseDir: VirtualFile = project.baseDir
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(baseDir.toIoFile())
    }*/
}