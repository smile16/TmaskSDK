package com.zkxltech.marksdk.Utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.lang.Exception
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.log

/**
 *创建作者       : albus
 *创建时间       : 2019/11/19
 *Fuction(类描述):
 */
class BPMImage {
    //BMP图像的色深
    val BIT_COUNT_BLACKWHITE = 1
    val BIT_COUNT_16COLORS = 4
    val BIT_COUNT_256COLORS = 8
    val BIT_COUNT_16BITCOLORS = 16
    val BIT_COUNT_24BITCOLORS = 24
    val BIT_COUNT_32BITCOLORS = 32
    //BMP图像的扫描方向，水平或垂直
    val SCAN_DIRECTION_HORIZONTAL = 0
    val SCAN_DIRECTION_VERTICAL = 1
    //BMP图像垂直扫描方向，向上或向下
    val SCAN_COL_UP = 0
    val SCAN_COL_DOWN = 1
    private var fileHeader: BmpFileHeader? = null//文件头
    private var infoHeader: BmpInfoHeader? = null//信息头
    private var colorTable: ByteArray? = null//颜色表
    private var orignalPixData: ByteArray? = null//原始颜色数据
    private var pixData: IntArray? = null//解析后的颜色数据
    private var bitmap: Bitmap? = null
    private val BMP_FILE_HEADER_SIZE = 14
    private val BMP_INFO_HEADER_SIZE = 40
    fun getFileHeader(): BmpFileHeader? {
        return fileHeader
    }

    fun getInfoHeader(): BmpInfoHeader? {
        return infoHeader
    }

    fun getPixData(): IntArray? {
        return pixData
    }

    fun getOrignalPixData(): ByteArray? {
        return orignalPixData
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

    constructor(buffer: ByteArray) {
        val fileBuffer = buffer.clone()
        bitmap = BitmapFactory.decodeByteArray(buffer, 0, fileBuffer.size)
        var headerLength = BMP_FILE_HEADER_SIZE
        val fileHeaderBytes = ByteArray(headerLength)
        System.arraycopy(fileBuffer, 0, fileHeaderBytes, 0, headerLength)
        fileHeader = BmpFileHeader(fileHeaderBytes)
        var infoLength = BMP_INFO_HEADER_SIZE;
        val infoHeaderBytes = ByteArray(infoLength)
        System.arraycopy(fileBuffer, headerLength, infoHeaderBytes, 0, infoLength);
        infoHeader = BmpInfoHeader(infoHeaderBytes)
        var colorTableLength = 0
        when (infoHeader?.getBitCount()) {
            BIT_COUNT_BLACKWHITE -> {
                colorTableLength = 2 * 4
            }
            BIT_COUNT_16COLORS -> {
                colorTableLength = 16 * 4
            }
            BIT_COUNT_256COLORS -> {
                colorTableLength = 256 * 4
            }
            BIT_COUNT_16BITCOLORS,
            BIT_COUNT_24BITCOLORS,
            BIT_COUNT_32BITCOLORS -> {
                colorTableLength = 0;
            }
        }
        if (colorTableLength > 0) {
            this.colorTable = ByteArray(colorTableLength)
            System.arraycopy(
                fileBuffer,
                headerLength + infoLength,
                this.colorTable,
                0,
                colorTableLength
            )
        }
        this.orignalPixData = ByteArray(infoHeader?.getPixSize()!!)
        System.arraycopy(
            fileBuffer,
            headerLength + infoLength + colorTableLength,
            this.orignalPixData,
            0,
            infoHeader?.getPixSize()!!
        )
        var width = infoHeader?.getWidth()
        var height = infoHeader?.getHeight()
        pixData = IntArray(height!! * width!!)
        var oft = 0
        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                pixData!![oft++] = bitmap?.getPixel(x, y)!!
            }
        }
    }

    fun getBMPPixData(iScanDirection: Int, bColorOverturn: Boolean): ByteArray? {
        try {
            var width = bitmap?.width//图片实际宽度
            var height = bitmap?.height//图片高度
            var pix: ByteArray? = null
            var currentValue = 0
            var currentX = 0
            if (iScanDirection == SCAN_DIRECTION_HORIZONTAL) {


                //每一扫描行的字节数必需是4的整倍数，当不够4的整数倍时，需要加0补齐。
                //例如：你使用的图片是40x60的，点阵的大小不是40x60而是64*60（40/8=5，不是4的整数倍，需要补3个字节的0)。
                pix =
                    ByteArray(if (width!! % 8 != 0) (height!! * ((width / 8) + 1)) else (height!! * (width / 8)))
                for (y in 0..(height!! - 1)) {
                    for (x in 0..(width - 1) step 8) {
                        currentValue = 0
                        for (i in 0..7) {

                            currentValue = currentValue shl 1
                            if ((x + i) < width) {
                                currentValue =
                                    (pixData?.get((y * width) + x + i)!! and 0x01) or currentValue
                            }
                        }
                        if (bColorOverturn == true) {
                            pix[currentX++] =
                                ((currentValue.inv()) and 0xFF).toByte()//~非运算
                        } else {
                            pix[currentX++] = (((currentValue) and 0xFF).toByte())
                        }
                    }
                }
            } else {
                //每一扫描行的字节数必需是4的整倍数，当不够4的整数倍时，需要加0补齐。
                //例如：你使用的图片是40x60的，点阵的大小不是40x60而是64*60（40/8=5，不是4的整数倍，需要补3个字节的0)。
                val stringBuffer = StringBuffer()
                pixData?.forEach {
                    stringBuffer.append(it)
                }
                pix =
                    ByteArray(if (height!! % 8 != 0) (width!! * ((height / 8) + 1)) else (width!! * (height / 8)))
                for (x in 0 until width) {
                    var y = 0
                    while (y < height) {
                        currentValue = 0
                        for (i in 0..7) {
                            currentValue = currentValue shl 1
                            if (y + i < height) {
                                currentValue =
                                    currentValue or ((pixData?.get(x + (y + i) * width))?.and(0x01))?.toByte()?.toInt()!!
                            }
                        }
                        if (bColorOverturn == true)
                            pix[currentX++] = (currentValue.inv() and 0xFF).toByte()
                        else
                            pix[currentX++] = (currentValue and 0xFF).toByte()
                        y += 8
                    }
                }
            }
            return pix
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * Title: BMP文件的头结构
     * Description: BMP文件的头结构固定是14个字节，低字节在前,其定义如下：
     * byte[2] bfType= { (byte) 'B', (byte) 'M' };地址0x0000~0x0001，位图类型，windows系统固定值为BM
     * byte[4] bfSize; 地址0x0002~0x0005，整个文件大小，包括这14个字节
     * byte[2] bfReserved1; 地址0x0006~0x0007，保留字,固定为0
     * byte[2] bfReserved2; 地址0x0008~0x0009，保留字,固定为0
     * byte[4] bfOffBits= BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE + 8;地址0x000A~0x000D，位图数据开始的偏移量 为从文件头到实际的位图数据的偏移字节数
     */
    inner class BmpFileHeader {
        //        val BMP_FILE_HEADER_SIZE = 14
        // Header data
        val data = ByteArray(BMP_FILE_HEADER_SIZE)
        // BMP file size
        var size: Int = 0
            private set
        var offset: Int = 0
            private set

        internal constructor(_data: ByteArray) {
            this.size = ((_data[2] and 0xFF.toByte()).toInt()) or
                    (((_data[3] and 0xFF.toByte()).toInt()) shl 8) or
                    ((_data[4] and 0xFF.toByte()).toInt() shl 16) or
                    ((_data[5] and 0xFF.toByte()).toInt() shl 24)
            this.offset = ((_data[10] and 0xFF.toByte()).toInt()) or
                    (((_data[11] and 0xFF.toByte()).toInt()) shl 8) or
                    ((_data[12] and 0xFF.toByte()).toInt() shl 16) or
                    ((_data[13] and 0xFF.toByte()).toInt() shl 24)
            System.arraycopy(_data, 0, this.data, 0, 14)
        }

        internal constructor(size: Int, offset: Int) {
            this.size = size
            this.offset = offset

            data[0] = 'B'.toByte()
            data[1] = 'M'.toByte()

            var value = size
            data[2] = value.toByte()
            value = value.ushr(8)
            data[3] = value.toByte()
            value = value.ushr(8)
            data[4] = value.toByte()
            value = value.ushr(8)
            data[5] = value.toByte()

            value = offset
            data[10] = value.toByte()
            value = value.ushr(8)
            data[11] = value.toByte()
            value = value.ushr(8)
            data[12] = value.toByte()
            value = value.ushr(8)
            data[13] = value.toByte()
        }


    }


    /**
     * Title: BMP文件内容的头结构
     * Description: BMP文件内容的头结构固定是40个字节，低字节在前，其定义如下：
     * byte[4] biSize= BITMAPINFOHEADER_SIZE; 地址0x000E~0x0011，本信息头结构的大小，windows系统固定值为28h=40d
     * byte[4] biWidth; 地址0x0012~0x0015，指定图象的宽度，单位是象素
     * byte[4] biHeight; 地址0x0016~0x0019，指定图象的高度，单位是象素
     * byte[2] biPlanes; 地址0x001A~0x001B，图像的位面数，固定值为1
     * byte[2] biBitCount; 地址0x001C~0x001D，BMP图像的色深，指定表示颜色时要用到的位数，常用的值为1(黑白二色图), 4(16色图), 8(256色),
     * 24(真彩色图),32(增强型真彩色)当biBitCount=1时，8个像素占1个字节;当biBitCount=4时，2个像素占1个字节;当biBitCount=8时，
     * 1个像素占1个字节;当biBitCount=24时,1个像素占3个字节;
     * byte[4] biCompression; 地址0x001E~0x0021,数据压缩方式，0表示不压缩，1表示RLE8压缩，2表示RLE4压缩，3表示每个像素值由指定的掩码决定
     * byte[4] biSizeImage; 地址0x0022~0x0025,BMP图像数据区大小，必须是4的倍数，图像数据大小不是4的倍数时用0填充补足;指定实际的位图数据占用的字节数
     *                      Windows规定一个扫描行所占的字节数必须是4的倍数(即以long为单位),不足的以0填充，
     *                      biSizeImage = ((((biWidth * biBitCount) + 31) & ~31) / 8) * biHeight;
     * byte[4] biXPelsPerMeter; 地址0x0026~0x0029,指定目标设备的水平分辨率，单位是每米的象素个数
     * byte[4] biYPelsPerMeter; 地址0x002A~0x002D,指定目标设备的垂直分辨率，单位是每米的象素个数
     * byte[4] biClrUsed; 地址0x002E~0x0031,指定本图象实际用到的颜色数，0表示使用全部颜色，对于256色位图来说，此值为100h=256d
     * byte[4] biClrImportant; 地址0x0032~0x0035,指定本图象中重要的颜色数，如果该值为零，则认为所有的颜色都是重要的
     *
     */
    inner class BmpInfoHeader {
        private val data = ByteArray(BMP_INFO_HEADER_SIZE)

        fun getData(): ByteArray {
            return this.data
        }

        private var width: Int = 0

        fun getWidth(): Int {
            return this.width
        }

        private var height: Int = 0

        fun getHeight(): Int {
            return this.height
        }

        private var bitCount: Int = 0

        fun getBitCount(): Int {
            return this.bitCount
        }

        private var pixSize: Int = 0

        fun getPixSize(): Int {
            return pixSize
        }

        fun setPixSize(pixSize: Int) {
            this.pixSize = pixSize
        }

        constructor(_data: ByteArray) {
            System.arraycopy(_data, 0, this.data, 0, 40)
            val a1 = (_data[4].toInt() and 0xFF)
            val a2 = ((_data[5].toInt() and 0xFF) shl 8)
            val a3 = ((_data[6].toInt() and 0xFF) shl 16)
            val a4 = ((_data[7].toInt() and 0xFF) shl 24)
            this.width = a1 or a2 or a3 or a4
            val b1 = ((_data[8].toInt() and 0xFF))
            val b2 = ((_data[9].toInt() and 0xFF) shl 8)
            val b3 = ((_data[10].toInt() and 0xFF) shl 16)
            val b4 = ((_data[11].toInt() and 0xFF) shl 24)
            this.height = b1 or b2 or b3 or b4
            val c1 = ((_data[14].toInt() and 0xFF))
            val c2 = ((_data[15].toInt() and 0xFF) shl 8)
            this.bitCount = c1 or c2
            val d1 = ((_data[20].toInt() and 0xFF))
            val d2 = ((_data[21].toInt() and 0xFF) shl 8)
            val d3 = ((_data[22].toInt() and 0xFF) shl 16)
            val d4 = ((_data[23].toInt() and 0xFF) shl 24)
            this.pixSize = d1 or d2 or d3 or d4
        }

        fun BmpInfoHeader(width: Int, height: Int, bitCount: Int) {
            this.width = width
            this.height = height
            this.bitCount = bitCount

            data[0] = 40

            var value = width
            data[4] = value.toByte()
            value = value.ushr(8)
            data[5] = value.toByte()
            value = value.ushr(8)
            data[6] = value.toByte()
            value = value.ushr(8)
            data[7] = value.toByte()

            value = height
            data[8] = value.toByte()
            value = value.ushr(8)
            data[9] = value.toByte()
            value = value.ushr(8)
            data[10] = value.toByte()
            value = value.ushr(8)
            data[11] = value.toByte()

            data[12] = 1

            data[14] = bitCount.toByte()

            value = width * height * 3
            if (width % 4 != 0)
                value += width % 4 * height
            data[20] = value.toByte()
            value = value shl 8
            data[21] = value.toByte()
            value = value shl 8
            data[22] = value.toByte()
            value = value shl 8
            data[23] = value.toByte()
        }


    }
}