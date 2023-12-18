package com.mayi.colorplugin.dialog

import com.android.tools.idea.util.toIoFile
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.xml.XmlFile
import com.mayi.colorplugin.toast.ToastMessage
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.android.facet.ResourceFolderManager
import java.awt.Color
import java.awt.GridLayout
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.util.*
import javax.swing.*
import javax.swing.border.Border


class FormNewColorSwing(private var event: AnActionEvent, private var dialog: DialogWrapper?) {
    private var north: JPanel = JPanel()
    private var center: JPanel = JPanel()
    private var south: JPanel = JPanel()

    //为了让位于底部的按钮可以拿到组件内容，这里把表单组件做成类属性
    private var argbText: JLabel = JLabel("")
    private var btnCopy: JButton = JButton("Copy Color")
    private var previewBg: JPanel = JPanel()
    private var nameContent: JTextField = JTextField()
    private var colorContent: JTextField = JTextField()
    private var alphaContent: JTextField = JTextField()
    private val moduleComBox: JComboBox<String> = JComboBox()
    private val fileNameComBox: JComboBox<String> = JComboBox()

    private val moduleList = arrayListOf<Module>()
    private val fileNameList = arrayListOf<VirtualFile>()

    private var currentModule: Module? = null
    private var currentFile: VirtualFile? = null

    fun initNorth(): JPanel {
        //定义表单的主体部分，放置到IDEA会话框的中央位置，垂直布局
        north.setLayout(GridLayout(0, 5))
        val blackLine: Border = BorderFactory.createLineBorder(Color.black)
        north.setBorder(blackLine)

        // 生成的argb 十六进制
        val argbLabel = JLabel(" argb value：")
        north.add(argbLabel)
        argbText.setForeground(Color.RED) //设置字体颜色
        north.add(argbText)

        // 预览
        val bgTitle = JLabel("preview argb：")
        north.add(bgTitle)
        north.add(previewBg)

        // 复制button
        north.add(btnCopy)

        btnCopy.addActionListener {
            clickCopyButton()
        }

        return north
    }

    fun initCenter(): JPanel {
        //定义表单的主体部分，放置到IDEA会话框的中央位置，垂直布局
        val gridLayout = GridLayout(5, 2)
        center.setLayout(gridLayout)

        val colorName = JLabel("Resource Name：")
        center.add(colorName)
        center.add(nameContent)

        val colorTitle = JLabel("Resource RGB Value：")
        center.add(colorTitle)
        center.add(colorContent)

        val alphaTitle = JLabel("Resource Alpha(%)：")
        center.add(alphaTitle)
        center.add(alphaContent)

        val moduleSet = JLabel("Module: ")
        center.add(moduleSet)
        center.add(moduleComBox)

        val fileName = JLabel("File Name: ")
        center.add(fileName)
        center.add(fileNameComBox)

        moduleComBox.addActionListener {
            currentModule = moduleList[moduleComBox.selectedIndex]
            addColorFiles()
        }

        fileNameComBox.addActionListener {
            currentFile = fileNameList[fileNameComBox.selectedIndex]
        }

        if (isAndroidProject(event.project)) {
            addModuleList(event.project)
        }

        return center
    }

    fun initSouth(): JPanel {
        //定义表单的提交按钮，放置到IDEA会话框的底部位置
        val submit = JButton("Preview")
        submit.setHorizontalAlignment(SwingConstants.CENTER) //水平居中
        submit.setVerticalAlignment(SwingConstants.CENTER) //垂直居中
        south.add(submit)
        submit.addActionListener {
            //获取到颜色值和透明度
            val color = getColorString()

            if (!checkData(color)) {
                return@addActionListener
            }

            val alpha = getAlphaPercent() // 十进制透明度
            var hex = Integer.toHexString(alpha).uppercase(Locale.getDefault()) // 转换为十六进制
            if (hex.length == 1) hex = "0$hex"

            // 预览色值字符串
            val colorStr = "#".plus(hex).plus(color)
            argbText.setText(colorStr)

            // 预览色值效果
            val bgColor = Color(Integer.parseInt(color, 16))
            previewBg.setBackground(Color(bgColor.red, bgColor.green, bgColor.blue, alpha))
        }

        val create = JButton("Create Color")
        create.setHorizontalAlignment(SwingConstants.CENTER); //水平居中
        create.setVerticalAlignment(SwingConstants.CENTER); //垂直居中
        south.add(create)
        create.addActionListener {
            //获取到颜色值和透明度
            val color = getColorString()

            if (!checkData(color)) {
                return@addActionListener
            }

            val alpha = getAlphaPercent() // 十进制透明度
            var hex = Integer.toHexString(alpha).uppercase(Locale.getDefault()) // 转换为十六进制
            if (hex.length == 1) hex = "0$hex"

            // 预览色值字符串
            val colorStr = "#".plus(hex).plus(color)
            val name = getColorName()
            if (name.isNullOrEmpty()) {
                JOptionPane.showMessageDialog(null, "color name is not empty", "错误提示", JOptionPane.ERROR_MESSAGE)
                return@addActionListener
            }

            currentFile?.let { it1 ->
                currentModule?.project?.let { it2 ->
                    ApplicationManager.getApplication().runWriteAction {
                        CommandProcessor.getInstance().executeCommand(it2, {
                            addColorMap(it1, it2, name, colorStr)
                        }, "", null)
                    }
                }
            }
        }
        return south
    }

    // 校验数据是否符合标准
    private fun checkData(color: String): Boolean {
        if (color.length > 6) {
            JOptionPane.showMessageDialog(null, "颜色值不能超过6位数", "错误提示", JOptionPane.ERROR_MESSAGE)
            return false
        }

        val regex = Regex("^[a-zA-Z0-9]+$")
        if (!regex.matches(color)) {
            JOptionPane.showMessageDialog(null, "颜色值只能是数字和字母组合", "错误提示", JOptionPane.ERROR_MESSAGE)
            return false
        }

        val alphaRegex = Regex("^\\d+$")
        if (!alphaRegex.matches(alphaContent.getText())) {
            JOptionPane.showMessageDialog(null, "透明度只能是数字", "错误提示", JOptionPane.ERROR_MESSAGE)
            return false
        }

        return true
    }

    private fun isAndroidProject(project: Project?): Boolean {
        project?.let {
            val modules = ModuleManager.getInstance(project).modules
            for (module in modules) {
                if (AndroidFacet.getInstance(module) != null) {
                    // 找到 Android 模块，当前项目是 Android 项目
                    return true
                }
            }
            // 未找到 Android 模块，当前项目不是 Android 项目
            return false
        }
        return false
    }

    private fun addModuleList(project: Project?) {
        project?.let {
            val modules = ModuleManager.getInstance(project).modules
            for (module in modules) {
                val facet = AndroidFacet.getInstance(module)

                if (facet != null && (facet.configuration.isAppProject || facet.configuration.isLibraryProject)) {
                    if (module.name.endsWith(".main")) {
                        moduleList.add(module)
                        moduleComBox.addItem(module.name)
                    }
                }
            }
        }
    }

    private fun addColorFiles() {
        if (currentModule == null) {
            return
        }
        val facet = AndroidFacet.getInstance(currentModule!!)
        val resourceFolderManager = facet?.let { ResourceFolderManager.getInstance(it) }
        val resDirectories = resourceFolderManager?.folders
        // 在所有的 res 目录中找到 values 目录
        val valuesFolder = resDirectories
                ?.asSequence()
                ?.mapNotNull { it.findChild("values") }
                ?.firstOrNull()
        valuesFolder?.let {
            for (xmlFile in it.children) {
                fileNameList.add(xmlFile)
                fileNameComBox.addItem(xmlFile.name)
            }
        }
    }

    private fun addColorMap(file: VirtualFile, project: Project, colorName: String, colorValue: String) {
        val psiFile = PsiManager.getInstance(project).findFile(file) as? XmlFile
        psiFile?.let { psi ->
            val factory = XmlElementFactory.getInstance(project)
            val xmlTag = factory.createTagFromText("<color name=\"$colorName\">$colorValue</color>")
            try {
                psi.rootTag?.addSubTag(xmlTag, false)
                // 使用 VfsUtil 存储文本内容
//                VfsUtil.saveText(psi.virtualFile, psi.text)
                // 格式化 XML 文档
                CodeStyleManager.getInstance(project).reformat(psi)
                // 强制刷新文件
                VirtualFileManager.getInstance().syncRefresh();
                // 刷新项目
//                refreshProject(project)
            } catch (e: Exception) {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("transparent.color")
                        .createNotification("提示信息", "Exception message: ${e.message}", NotificationType.INFORMATION)
                        .notify(project)
                e.printStackTrace()
            } finally {
                dialog?.exitCode?.let { dialog?.close(it) }
            }
        }
    }

    // 刷新项目
    private fun refreshProject(project: Project) {
        val baseDir: VirtualFile = project.baseDir
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(baseDir.toIoFile())
    }

    private fun getColorName(): String {
        return nameContent.getText()
    }

    private fun getColorString(): String {
        var color = colorContent.getText()

        if (color == null || color.equals("")) {
            color = "ffffff"
            colorContent.text = color
        }
        return color
    }

    private fun getAlphaPercent(): Int {
        if (alphaContent.getText().isNullOrEmpty()) {
            alphaContent.text = "100"
        }

        var percent = Math.round(alphaContent.getText().toFloat()) / 100.0 // 百分比
        if (percent > 1) {
            percent = 1.0
        } else if (percent < 0) {
            percent = 0.0
        }
        return Math.round((1 - percent) * 255).toInt()
    }

    private fun clickCopyButton() {
        if (argbText.text.isNullOrEmpty()) {
            ToastMessage(center, "还未生成结果")
            return
        }
        val stringSelection = StringSelection(argbText.text)
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
        ToastMessage(center, "复制成功")
        dialog?.exitCode?.let { dialog?.close(it) }
    }
}