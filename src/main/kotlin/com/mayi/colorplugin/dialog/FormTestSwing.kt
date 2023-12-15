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
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.xml.XmlFile
import com.mayi.colorplugin.toast.ToastMessage
import org.jdesktop.swingx.VerticalLayout
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.android.facet.ResourceFolderManager
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.util.*
import javax.swing.*
import javax.swing.border.Border


class FormTestSwing(private var event: AnActionEvent) {
    private var north: JPanel = JPanel()
    private var center: JPanel = JPanel()
    private var south: JPanel = JPanel()

    //为了让位于底部的按钮可以拿到组件内容，这里把表单组件做成类属性
    private var r1: JLabel = JLabel("argb Value：")
    private var r2: JLabel = JLabel("")
    private var btnCopy: JButton = JButton("Copy Color")
    private var previewBg: JPanel = JPanel()

    private var colorName: JLabel = JLabel("Resource Name：")
    private var nameContent: JTextField = JTextField()

    private var colorTitle: JLabel = JLabel("Resource RGB Value：")
    private var colorContent: JTextField = JTextField()

    private var alphaTitle: JLabel = JLabel("Resource Alpha(%)：")
    private var alphaContent: JTextField = JTextField()

    private var moduleSet: JLabel = JLabel("Module: ")
    private val moduleComBox: JComboBox<String> = JComboBox()

    private var fileName: JLabel = JLabel("File Name: ")
    private val fileNameComBox: JComboBox<String> = JComboBox()

    private val moduleList = arrayListOf<Module>()
    private val fileNameList = arrayListOf<VirtualFile>()

    private var currentModule: Module? = null
    private var currentFile: VirtualFile? = null

    fun initNorth(): JPanel {

        //定义表单的标题部分，放置到IDEA会话框的顶部位置
        /*val title = JLabel("透明度转换")
        title.setFont(Font("微软雅黑", Font.PLAIN, 26)) //字体样式
        title.setHorizontalAlignment(SwingConstants.CENTER) //水平居中
        title.setVerticalAlignment(SwingConstants.CENTER) //垂直居中
        north.add(title)*/

        return north
    }

    fun initCenter(): JPanel {

        // 设置默认宽高
        center.preferredSize = Dimension(350, 150)

        //定义表单的主体部分，放置到IDEA会话框的中央位置，垂直布局
        center.setLayout(VerticalLayout(10))

        //row1：预览转换结果
        val rowTop = JPanel()
        rowTop.setLayout(FlowLayout(FlowLayout.LEFT, 10, 5))
        val blackline: Border = BorderFactory.createLineBorder(Color.black)
        rowTop.setBorder(blackline);

        rowTop.add(r1)
        r2.size = Dimension(40, 15)
        r2.setForeground(Color(139, 181, 20)) //设置字体颜色
        rowTop.add(r2)

        val bgTitle = JLabel("预览效果：")
        rowTop.add(bgTitle)

        previewBg.preferredSize = Dimension(40, 15)
        rowTop.add(previewBg)

        rowTop.add(btnCopy)
        btnCopy.addActionListener {
            clickCopyButton()
        }
        center.add(rowTop)

        // color name
        val rowName = JPanel()
        rowName.setLayout(FlowLayout(FlowLayout.LEFT, 10, 5))
        rowName.add(colorName)
        rowName.add(nameContent)
        center.add(rowName)

        //row2：颜色值+文本框
        val rowRGB = JPanel()
        rowRGB.setLayout(FlowLayout(FlowLayout.LEFT, 10, 5))
        rowRGB.add(colorTitle)
        rowRGB.add(colorContent)
        center.add(rowRGB)

        //row3：透明度百分比+文本框
        val rowAlpha = JPanel()
        rowAlpha.setLayout(FlowLayout(FlowLayout.LEFT, 10, 5))
        rowAlpha.add(alphaTitle)
        rowAlpha.add(alphaContent)
        center.add(rowAlpha)

        // row 项目目录
        val rowModule = JPanel()
        rowModule.setLayout(FlowLayout(FlowLayout.LEFT, 10, 5))
        rowModule.add(moduleSet)
        rowModule.add(moduleComBox)
        center.add(rowModule)
        moduleComBox.addActionListener {
            currentModule = moduleList[moduleComBox.selectedIndex]
            addColorFiles()
        }

        // row 项目目录
        val rowFile = JPanel()
        rowFile.setLayout(FlowLayout(FlowLayout.LEFT, 10, 5))
        rowFile.add(fileName)
        rowFile.add(fileNameComBox)
        center.add(rowFile)
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

            if (color.length > 6) {
                JOptionPane.showMessageDialog(null, "颜色值不能超过6位数", "错误提示", JOptionPane.ERROR_MESSAGE)
                return@addActionListener
            }

            val regex = Regex("^[a-zA-Z0-9]+$")
            if (!regex.matches(color)) {
                JOptionPane.showMessageDialog(null, "颜色值只能是数字和字母组合", "错误提示", JOptionPane.ERROR_MESSAGE)
                return@addActionListener
            }

            val alphaRegex = Regex("^\\d+$")
            if (!alphaRegex.matches(alphaContent.getText())) {
                JOptionPane.showMessageDialog(null, "透明度只能是数字", "错误提示", JOptionPane.ERROR_MESSAGE)
                return@addActionListener
            }

            val alpha = getAlphaPercent() // 十进制透明度
            var hex = Integer.toHexString(alpha).uppercase(Locale.getDefault()) // 转换为十六进制
            if (hex.length == 1) hex = "0$hex"

            // 预览色值字符串
            val colorStr = "#".plus(hex).plus(color)
            r2.setText(colorStr)

            // 预览色值效果
            val bgColor = Color(Integer.parseInt(color, 16))
            previewBg.setBackground(Color(bgColor.red, bgColor.green, bgColor.blue, alpha))
        }

        val create = JButton("Generate Transparent Color")
        create.setHorizontalAlignment(SwingConstants.CENTER); //水平居中
        create.setVerticalAlignment(SwingConstants.CENTER); //垂直居中
        south.add(create)
        create.addActionListener {
            //获取到颜色值和透明度
            val color = getColorString()

            if (color.length > 6) {
                JOptionPane.showMessageDialog(null, "颜色值不能超过6位数", "错误提示", JOptionPane.ERROR_MESSAGE)
                return@addActionListener
            }

            val regex = Regex("^[a-zA-Z0-9]+$")
            if (!regex.matches(color)) {
                JOptionPane.showMessageDialog(null, "颜色值只能是数字和字母组合", "错误提示", JOptionPane.ERROR_MESSAGE)
                return@addActionListener
            }

            val alphaRegex = Regex("^\\d+$")
            if (!alphaRegex.matches(alphaContent.getText())) {
                JOptionPane.showMessageDialog(null, "透明度只能是数字", "错误提示", JOptionPane.ERROR_MESSAGE)
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
//            createColor(event, name, colorStr)
        }
        return south
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

    private fun createColor(event: AnActionEvent, colorName: String, colorValue: String) {
        val project = event.project
        val baseDir = project?.baseDir
        val srcDir = VfsUtil.createDirectoryIfMissing(baseDir, "src")
        val mainDir = VfsUtil.createDirectoryIfMissing(srcDir, "main")
        val resDir = VfsUtil.createDirectoryIfMissing(mainDir, "res")
        val valuesDir = VfsUtil.createDirectoryIfMissing(resDir, "values")
        val colorsFile = valuesDir.findChild("color.xml")

        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(project, {
                if (project != null) {
                    if (colorsFile != null) {
                        addColorMap(colorsFile, project, colorName, colorValue)
                    } else {
                        val tempFile = createColorsFile(valuesDir, project, event)
                        addColorMap(tempFile, project, colorName, colorValue)
                    }
                }
            }, "create and add color", null)
        }
    }

    private fun createColorsFile(parentDir: VirtualFile, project: Project, event: AnActionEvent): VirtualFile {
        val file = parentDir.findOrCreateChildData(event, "color.xml")
        val xmlContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources xmlns:tools=\"http://schemas.android.com/tools\">" +
                "\n" +
                "</resources>"
        VfsUtil.saveText(file, xmlContent)
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return file
    }

    private fun addColorMap(file: VirtualFile, project: Project, colorName: String, colorValue: String) {
        val psiFile = PsiManager.getInstance(project).findFile(file) as? XmlFile
        psiFile?.let { psi ->
            val factory = XmlElementFactory.getInstance(project)
            val xmlTag = factory.createTagFromText("<color name=\"$colorName\">$colorValue</color>")
            try {
                psi.rootTag?.addSubTag(xmlTag, false)
                // 使用 VfsUtil 存储文本内容
                VfsUtil.saveText(psi.virtualFile, psi.text)
                // 格式化 XML 文档
                CodeStyleManager.getInstance(project).reformat(psi)
                // 刷新项目
                refreshProject(project)
            } catch (e: Exception) {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("transparent.color")
                        .createNotification("提示信息", "Exception message: ${e.message}", NotificationType.INFORMATION)
                        .notify(project)
                e.printStackTrace()
            }
        }
    }

    // 刷新项目
    private fun refreshProject(project: Project) {
        val baseDir: VirtualFile = project.baseDir
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(baseDir.toIoFile())
    }

    /*private fun addColorResource(module: Module, colorName: String, colorValue: String) {

        val facet = AndroidFacet.getInstance(module) ?: return
        ToastMessage(center, "addColorResource 2")
        WriteCommandAction.runWriteCommandAction(module.project) {
            // 使用 ResourceFolderManager 获取 res 目录及其子目录
            val resourceFolderManager = ResourceFolderManager.getInstance(facet)
            ToastMessage(center, "addColorResource 3")
            val resDirectories = resourceFolderManager.folders
            ToastMessage(center, "addColorResource resDirectories：$resDirectories")
            // 在所有的 res 目录中找到 values 目录
            val valuesFolder = resDirectories
                    .asSequence()
                    .mapNotNull { it.findChild("values") }
                    .firstOrNull()
            ToastMessage(center, "addColorResource valuesFolder：$valuesFolder")
            valuesFolder?.let {
                // 获取或创建 colors.xml 文件
                val colorsFile = it.findOrCreateChildData(module, "colors.xml")
                ToastMessage(center, "addColorResource colorsFile：$colorsFile")
                // 在 colors.xml 文件中添加颜色资源
                val project = module.project
                val psiFile = PsiManager.getInstance(project).findFile(colorsFile) as? XmlFile
                ToastMessage(center, "addColorResource psiFile：$psiFile")
                val psiFile = PsiFileFactory.getInstance(module.project).createFileFromText(
                        "colors.xml",  // 文件名
                        XmlFileType.INSTANCE,  // 文件类型
                        "<resources><color name=\"$colorName\">$colorValue</color></resources>"  // 文件内容
                ) as? XmlFile

                psiFile?.let { psi ->
                    val factory = XmlElementFactory.getInstance(module.project)
                    val rootTag = psi.rootTag
                    val xmlTag = factory.createTagFromText("<color name=\"$colorName\">$colorValue</color>")
                    ToastMessage(center, "addColorResource rootTag：$rootTag")
                    if (rootTag != null) {
                        rootTag.addSubTag(xmlTag, false)
                    } else {
                        psi.add(factory.createTagFromText("<resources>${xmlTag.text}</resources>"))
                    }
                }
            }
        }
    }*/

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
        if (r2.text.isNullOrEmpty()) {
            ToastMessage(center, "还未生成结果")
            return
        }
        val stringSelection = StringSelection(r2.text)
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
        ToastMessage(center, "复制成功")
    }
}