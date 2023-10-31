package com.mayi.colorplugin.dialog

import com.mayi.colorplugin.toast.ToastMessage
import org.jdesktop.swingx.VerticalLayout
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.util.*
import javax.swing.*
import javax.swing.border.Border


class FormTestSwing {
    private var north: JPanel = JPanel()
    private var center: JPanel = JPanel()
    private var south: JPanel = JPanel()

    //为了让位于底部的按钮可以拿到组件内容，这里把表单组件做成类属性
    private var r1: JLabel = JLabel("输出：")
    private var r2: JLabel = JLabel("")
    private var btnCopy: JButton = JButton("复制")
    private var previewBg: JPanel = JPanel()

    private var colorTitle: JLabel = JLabel("颜色值：")
    private var colorContent: JTextField = JTextField()

    private var alphaTitle: JLabel = JLabel("透明度百分比：")
    private var alphaContent: JTextField = JTextField()

    fun initNorth(): JPanel {

        //定义表单的标题部分，放置到IDEA会话框的顶部位置
        val title = JLabel("透明度转换")
        title.setFont(Font("微软雅黑", Font.PLAIN, 26)) //字体样式
        title.setHorizontalAlignment(SwingConstants.CENTER) //水平居中
        title.setVerticalAlignment(SwingConstants.CENTER) //垂直居中
        north.add(title)

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

        //row2：颜色值+文本框
        center.add(colorTitle)
        center.add(colorContent)

        //row3：透明度百分比+文本框
        center.add(alphaTitle)
        center.add(alphaContent)

        return center
    }

    fun initSouth(): JPanel {
        //定义表单的提交按钮，放置到IDEA会话框的底部位置
        val submit = JButton("转换")
        submit.setHorizontalAlignment(SwingConstants.CENTER); //水平居中
        submit.setVerticalAlignment(SwingConstants.CENTER); //垂直居中
        south.add(submit)
        submit.addActionListener {
            //获取到颜色值和透明度
            var color = getColorString()

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

        return south
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
        val alpha = Math.round((1 - percent) * 255).toInt() // 十进制透明度
        return alpha
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