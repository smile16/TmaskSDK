package com.zkxltech.marksdk.Utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import androidx.constraintlayout.solver.widgets.Rectangle
import com.zkxltech.marksdk.UpdateHelper
import com.zkxltech.marksdk.view.BaseNFCActivity
import java.io.*
import java.nio.channels.FileChannel


/**
 *创建作者       : albus
 *创建时间       : 2019/11/19
 *Fuction(类描述):
 */
object ImageUtils {
    val MODEL_M210 = 0 // 208*112 指M210型号
    val MODEL_M290 = 1 // 232*88
    val MODEL_M410 = 2 // 360*280
    val MODEL_E213 = 3 // 212*104
    val MODEL_E213R = 6 // 212*104
    val MODEL_E290 = 4 // 296*128
    val MODEL_E290R = 7 // 296*128
    val MODEL_E420 = 5 // 400*300
    val MODEL_E420R = 8 // 400*300
    var action_len = 0
    var action_oid = 65535
    var action_pckid = 0
    //led灯闪烁时间
    var ledGlintTime: Int = 10
    //商品是否有促销信息 0 无 1 有
    var promotionType: Int = 0
    private var TAG: String = "albus"

    //BMP图像的扫描方向，水平或垂直
    val SCAN_DIRECTION_HORIZONTAL = 0
    val SCAN_DIRECTION_VERTICAL = 1
    /**
     * @param tpathbw       黑白底图
     * @param tpathwr       红色底图
     * @param type          价签型号
     *  @param ledt led灯闪烁时间(单位:分钟) 不传则默认为10
     *  @param promotionType 商品是否有促销信息 0 无  1 有
     * */
    fun imageToByte(tpathbw: String?, tpathwr: String?, type: Int): ByteArray? {
        return imageToByte(tpathbw, tpathwr, type, ledGlintTime, promotionType)
    }


    //fuction:将图片拆分成两张
    fun convertToBW(bitmap: Bitmap): Array<Bitmap?> {
        val width = bitmap.width
        val height = bitmap.height
        //创建定长的空数组
        val bitMap = arrayOfNulls<Bitmap>(2)
        val pixels = IntArray(width * height)
        val pixels_bw = IntArray(width * height)
        val pixels_wr = IntArray(width * height)
        /**
         * @param pixels 像素数组接收位图的颜色
         *@param 0 偏移第一个索引写入像素[]
         *       左上-->如果说是从图片左上角开始写入  刚此参数为0
         *       右上-->如果从图片右上角开始写入  则此参数为width
         *       中间-->如果从图片最中间开始写入  则此参数为width/2+height/2*width
         *       左下-->如果从图片左下角开始写入  则此参数为width*(height-1)
         *       右下-->如果从图片右下角开始写入  则此参数为width*height
         *@param width 跨步的像素[]项的数量之间跳转行(必须是>=位图的宽度)。可以是负的。
         *        pixels中换行的的长度   基本是位图的宽度
         *@param 0 x要读取的第一个像素的x坐标位图
         *@param 0 y第一个要读取的像素的y坐标位图
         *@param width 宽度从每一行读取的像素数
         *@param height 高度要读取的行数
         *
         *bitmap.getPixels可以进行图片拼接
         * 假设两张图片大小都为 w * h ，getPixels()方法中设置参数pixels[2*w*h],
         * 参数offset = 0，stride = 2*w读取第一张图片，再次运行getPixels()方法，
         *设置参数offset = w，stride = 2*w，读取第二张图片，再将pixels[]绘制到
         * 画布上就可以看到两张图片已经拼接起来了．
         */
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitMap[0] = Bitmap.createBitmap(bitmap).copy(Bitmap.Config.ARGB_8888, true)
        bitMap[0]?.getPixels(pixels_bw, 0, width, 0, 0, width, height)
        bitMap[1] = Bitmap.createBitmap(bitmap).copy(Bitmap.Config.ARGB_8888, true)
        bitMap[1]?.getPixels(pixels_wr, 0, width, 0, 0, width, height)
        val temp = 128
        var flag = false
        var alpha = 0xFF shl 24
        for (i in 0..(height - 1)) {
            for (j in 0..(width - 1)) {
                val grey = pixels[width * i + j]
                alpha = (grey and 0xFF000000.toInt()) shr 24
                var red = (grey and 0x00FF0000.toInt()) shr 16
                var green = (grey and 0x0000FF00.toInt()) shr 8
                var blue = (grey and 0x000000FF.toInt())
                if (red > temp) red = 255 else red = 0
                if (green > temp) green = 255 else green = 0
                if (blue > temp) blue = 255 else blue = 0
                pixels[width * i + j] = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
                if (red > 0 && green == 0 && blue == 0) {
                    //如果判断图片中有红色  则证明图片是三色图
                    flag = true
                    pixels_wr[width * i + j] = -16777216
                    pixels_bw[width * i + j] = -1
                } else {
                    if (pixels[width * i + j] == -1) {
                        pixels_bw[width * i + j] = -1

                    } else {
                        pixels_bw[width * i + j] = -16777216
                    }
                    pixels_wr[width * i + j] = -1
                }
            }
        }

        /**
         * setPixels(int[] pixels, int offset, int stride,int x, int y, int width, int height)
         *@param    pixels(pixels_bw) 	设置像素数组，对应点的像素被放在数组中的对应位置，像素的argb值全包含在该位置中
         *@param    offset(0)	        设置偏移量，我们截图的位置就靠此参数的设置
         *@param    stride(width)	    设置一行打多少像素，通常一行设置为bitmap的宽度，
         *@param    x(0)	     		设置开始绘图的x坐标
         *@param    y(0)	     		设置开始绘图的y坐标
         *@param    width(width)	    设置绘制出图片的宽度
         *@param    height(height)	    设置绘制出图片的高度
         */

        bitMap[0]?.setPixels(pixels_bw, 0, width, 0, 0, width, height)

        if (flag) {
            //证明这张图是三色图
            bitMap[1]?.setPixels(pixels_wr, 0, width, 0, 0, width, height)
        } else {
            bitMap[1]?.recycle()
        }
        return bitMap
    }


    /**
     * @param tpathbw       黑白底图
     * @param tpathwr       红色底图
     * @param type          价签型号
     * @param ledt led灯闪烁时间(单位:分钟)
     * @param tpromotion 商品是否有促销信息 0 无  1 有
     * */
    fun imageToByte(
        tpathbw: String?,
        tpathwr: String?,
        type: Int,
        ledt_: Int,
        tpromotion: Int
    ): ByteArray? {
        val file = File(tpathbw)
        if (file.exists()) {
            try {
                val inputStream = FileInputStream(file)
                val available = inputStream.available()
                val buffer = ByteArray(available)
                inputStream.read(buffer)
                inputStream.close()
                var bpmImage = BPMImage(buffer)
                var tbmpPixData: ByteArray? = null
                when (type) {
                    MODEL_M210 ->
                        tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_HORIZONTAL, true)
                    MODEL_M290 ->
                        tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_HORIZONTAL, true)
                    MODEL_M410 ->
                        tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_HORIZONTAL, true)
                    MODEL_E213,
                    MODEL_E213R ->
                        tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_VERTICAL, false)
                    MODEL_E290,
                    MODEL_E290R ->
                        tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_VERTICAL, false)
                    MODEL_E420,
                    MODEL_E420R ->
                        tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_HORIZONTAL, false)
                }
                if (tbmpPixData != null) {
                    if (tpathwr != null) {
                        action_len = tbmpPixData.size * 2 + 20
                    } else {
                        action_len = tbmpPixData.size + 12
                    }

                    action_oid = 0xFFFF
                    action_pckid = 0

                    var width = bpmImage.getBitmap()?.getWidth()//图片实际宽度
                    var height = bpmImage.getBitmap()?.getHeight()//图片高度
                    val rect = Rectangle()
                    rect.x = 0
                    rect.y = 0
                    rect.width = width!!
                    rect.height = height!!
                    val bmpPixData = ByteArray(action_len)
                    var idx = 0
                    if (tpathwr != null) {
                        bmpPixData[idx++] = 0x84.toByte()//闪灯 + 黑白红点阵图形（两张图）
                    } else {
                        bmpPixData[idx++] = 0x82.toByte()//闪灯 + 黑白点阵图形
                    }
                    var ledt = ledt_ and 0xffff
                    bmpPixData[idx] = 0x00
                    if (tpromotion != 0 && tpathwr == null) {
                        bmpPixData[idx] = (if (ledt == 0) 0x00 else 0x01).toByte()
                    }
                    idx++
                    //闪烁工作分钟数  2byte
                    bmpPixData[idx++] = (ledt shr 8 and 0xFF).toByte()
                    bmpPixData[idx++] = (ledt and 0xFF).toByte()
                    //矩形x坐标 2byte
                    bmpPixData[idx++] = (rect.x shr 8 and 0xFF).toByte()
                    bmpPixData[idx++] = (rect.x and 0xFF).toByte()
                    //矩形y坐标 2byte
                    bmpPixData[idx++] = (rect.y shr 8 and 0xFF).toByte()
                    bmpPixData[idx++] = (rect.y and 0xFF).toByte()
                    //矩形宽 2byte
                    bmpPixData[idx++] = (rect.width shr 8 and 0xFF).toByte()
                    bmpPixData[idx++] = (rect.width and 0xFF).toByte()
                    //矩形高 2byte
                    bmpPixData[idx++] = (rect.height shr 8 and 0xFF).toByte()
                    bmpPixData[idx++] = (rect.height and 0xFF).toByte()
                    //tbmpPixData 点阵数据
                    //tbmpPixData 目标源数组  srcPos目标源起始下标   bmpPixData最后获得的数组
                    // idx获得数组起始位置  tbmpPixData.length 需要copy的数据长度
                    System.arraycopy(tbmpPixData, 0, bmpPixData, idx, tbmpPixData.size)
                    idx += tbmpPixData.size   //将数组索引下标移动到最后添加数据的位置
                    if (tpathwr != null) {
                        var wrtmp = File(tpathwr)
                        if (wrtmp.exists()) {
                            val inputStream1 = FileInputStream(wrtmp)
                            val lenght = inputStream1.available()
                            val buffer2 = ByteArray(lenght)
                            inputStream1.read(buffer2)
                            inputStream1.close()
                            bpmImage = BPMImage(buffer2)
                            tbmpPixData = null
                            when (type) {
                                MODEL_M210,
                                MODEL_M290,
                                MODEL_M410 -> {
                                    LogUtils.e("该型号不支持3色！")
                                    UpdateHelper.dataThreadDispatch.addListData("该型号不支持3色！")
                                }
                                MODEL_E213,
                                MODEL_E213R ->
                                    tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_VERTICAL, false)
                                MODEL_E290,
                                MODEL_E290R ->
                                    tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_VERTICAL, false)
                                MODEL_E420,
                                MODEL_E420R ->
                                    tbmpPixData = bpmImage.getBMPPixData(SCAN_DIRECTION_HORIZONTAL, false)
                            }
                            if (tbmpPixData != null) {
                                width = bpmImage.getBitmap()?.getWidth()//图片实际宽度
                                height = bpmImage.getBitmap()?.getHeight()//图片高度
                                rect.x = 0
                                rect.y = 0
                                rect.width = width!!
                                rect.height = height!!
                                bmpPixData[idx++] = (rect.x shr 8 and 0xFF).toByte()
                                bmpPixData[idx++] = (rect.x and 0xFF).toByte()

                                bmpPixData[idx++] = (rect.y shr 8 and 0xFF).toByte()
                                bmpPixData[idx++] = (rect.y and 0xFF).toByte()

                                bmpPixData[idx++] = (rect.width shr 8 and 0xFF).toByte()
                                bmpPixData[idx++] = (rect.width and 0xFF).toByte()

                                bmpPixData[idx++] = (rect.height shr 8 and 0xFF).toByte()
                                bmpPixData[idx++] = (rect.height and 0xFF).toByte()
                                //data:源数组 srcPos:源数组中的起始位置 cmd:测试目标数组
                                // destPost:测试目标数组起始位置 length:要复制的数组元素的数量
                                System.arraycopy(
                                    tbmpPixData, 0, bmpPixData, idx, tbmpPixData.size
                                )

                            } else {
                                UpdateHelper.dataThreadDispatch.addListData("图片二无效！")
                                LogUtils.e("图片二无效")
                            }

                        } else {
                            UpdateHelper.dataThreadDispatch.addListData("文件二不存在！")
                            LogUtils.e("文件二不存在!!!")
                        }

                    }
                    return bmpPixData
                }
                return null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun saveTagBitmapToBMP(bitmap: Bitmap?, file: File) {
        if (bitmap == null)
            return

        val nBmpWidth = if (bitmap.width % 4 == 0) bitmap.width else bitmap.width + bitmap.width % 4
        val nBmpHeight = bitmap.height
        val wWidth = nBmpWidth * 3 + nBmpWidth % 4
        val bufferSize = nBmpHeight * wWidth

        try {
            val fileos = FileOutputStream(file)

            val bfType = 0x4d42
            val bfSize = (14 + 40 + bufferSize).toLong()
            val bfReserved1 = 0
            val bfReserved2 = 0
            val bfOffBits = (14 + 40).toLong()

            writeWord(fileos, bfType)
            writeDword(fileos, bfSize)
            writeWord(fileos, bfReserved1)
            writeWord(fileos, bfReserved2)
            writeDword(fileos, bfOffBits)

            val biSize = 40L
            val biWidth = nBmpWidth.toLong()
            val biHeight = nBmpHeight.toLong()
            val biPlanes = 1
            val biBitCount = 24
            val biCompression = 0L
            val biSizeImage = 0L
            val biXpelsPerMeter = 0L
            val biYPelsPerMeter = 0L
            val biClrUsed = 0L
            val biClrImportant = 0L

            writeDword(fileos, biSize)
            writeLong(fileos, biWidth)
            writeLong(fileos, biHeight)
            writeWord(fileos, biPlanes)
            writeWord(fileos, biBitCount)
            writeDword(fileos, biCompression)
            writeDword(fileos, biSizeImage)
            writeLong(fileos, biXpelsPerMeter)
            writeLong(fileos, biYPelsPerMeter)
            writeDword(fileos, biClrUsed)
            writeDword(fileos, biClrImportant)

            val bmpData = ByteArray(bufferSize)

            var nCol = 0
            var nRealCol = nBmpHeight - 1
            while (nCol < nBmpHeight) {
                var wRow = 0
                var wByteIdex = 0
                while (wRow < nBmpWidth) {
                    if (bitmap.width % 4 != 0) {//此时表示 用户选择的图片是非标准的价签显示图片 所以最后要做补0处理
                        if (wRow >= bitmap.width) {
                            bmpData[nRealCol * wWidth + wByteIdex] = 0 and 0xff
                            bmpData[nRealCol * wWidth + wByteIdex + 1] = 0 and 0xff
                            bmpData[nRealCol * wWidth + wByteIdex + 2] = 0 and 0xff
                        } else {
                            val clr = bitmap.getPixel(wRow, nCol)
                            bmpData[nRealCol * wWidth + wByteIdex] = Color.blue(clr).toByte()
                            bmpData[nRealCol * wWidth + wByteIdex + 1] =
                                Color.green(clr).toByte()
                            bmpData[nRealCol * wWidth + wByteIdex + 2] = Color.red(clr).toByte()
                        }
                    } else {
                        val clr = bitmap.getPixel(wRow, nCol)
                        bmpData[nRealCol * wWidth + wByteIdex] = Color.blue(clr).toByte()
                        bmpData[nRealCol * wWidth + wByteIdex + 1] = Color.green(clr).toByte()
                        bmpData[nRealCol * wWidth + wByteIdex + 2] = Color.red(clr).toByte()
                    }
                    wRow++
                    wByteIdex += 3
                }
                ++nCol
                --nRealCol
            }
            fileos.write(bmpData)
            fileos.flush()
            fileos.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    fun writeWord(stream: FileOutputStream, value: Int) {
        val b = ByteArray(2)
        b[0] = (value and 0xff).toByte()//低8位
        b[1] = (value shr 8 and 0xff).toByte()//高8位
        stream.write(b)
    }

    fun writeDword(stream: FileOutputStream, value: Long) {
        val b = ByteArray(4)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        b[2] = (value shr 16 and 0xff).toByte()
        b[3] = (value shr 24 and 0xff).toByte()
        stream.write(b)
    }


    fun writeLong(stream: FileOutputStream, value: Long) {
        val b = ByteArray(4)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        b[2] = (value shr 16 and 0xff).toByte()
        b[3] = (value shr 24 and 0xff).toByte()
        stream.write(b)
    }


    //将bitmap修改成可读写的状态  且会将图片等比例扩大三倍大小
    fun changeIvState(bitmap: Bitmap, activity: Activity): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Matrix()
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val density = metrics.density
        val postScale = matrix.postScale(density, density)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }


}
