package com.zkxltech.marksdk.Utils

import android.nfc.tech.NfcA
import com.zkxltech.marksdk.Bean.CRC8
import com.zkxltech.marksdk.Bean.Package
import com.zkxltech.marksdk.UpdateHelper
import com.zkxltech.marksdk.task.asynctask.IPublishProgress
import java.io.IOException

/**
 *创建作者       : albus
 *创建时间       : 2019/11/21
 *Fuction(类描述):
 */
class WriteNFCDataUtils {
    private val timeout = 30
    private val delay = 15
    private var action_oid = 65535
    private var action_pckid = 0
    private var crc8: Byte? = null
    var nfca_paclen = 64
    var  tag_type=0
    fun setPcklen(len: Int) {
        this.nfca_paclen = len
    }

    /**
     * @param maskTemperature 当前价签的温度
     * @param len 写入数据的长度
     * @param mfc
     * */
    fun writeStartPackage(
        mfc: NfcA,
        len: Int?,
        maskTemperature: Int,
        type: Int,
        bmpPixData: ByteArray
    ): Int {
        tag_type = type / 3
        tag_type = tag_type shl 4
        tag_type = tag_type or (type % 3)
        var connected = false //NFC是否连接成功
        var renew = false
        var trytime = 1
        val delayTime = (len!! / 512 + 1) * 6
        var readStatus = 0//有三种读取状态 0:初传  1:续传
        var startPackage: ByteArray? = null

        while (trytime < 5) {
            trytime++
            if (!connected) {
                connected = ConnectUtils().connectNfcA(delay, 3, timeout, mfc)
                if (connected) {
                    //连接成功
                    LogUtils.d("NFC连接成功")
                    UpdateHelper.dataThreadDispatch.addListData("NFC连接成功")
                } else {
                    //连接失败
                    LogUtils.d("NFC连接失败")
                    UpdateHelper.dataThreadDispatch.addListData("NFC连接失败")
                }
            }
            try {
                Thread.sleep(delay.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            if (!connected) {
                continue
            }
            if (readStatus == 0 || readStatus == 2) {
                val readNFCData = ReadNFCData()
                //数据写入连接时 返回的结果
                val result = readNFCData.readNfcA(0.toByte(), mfc)
                if (result == null || result.size < 16) {
                    connected = false
                    continue
                }
                if (readStatus == 0) {
                    if (Package.status.set(result, result.size - 16)) {
                        //var eink_type: Byte = 0//目标型号中的高四位
                        //var eink_pixel: Byte = 0//目标型号中的低四位
                        //var eink: Byte = 0//目标型号
                        if ((Package.status.eink_type.toInt() == 0 && Package.status.eink.toInt() != tag_type)
                            ||( Package.status.eink_type.toInt() != 0 && Package.status.eink_pixel.toInt() != (tag_type and 0x0F))
                        ) {
                            LogUtils.e("型号不匹配!!!")
                            UpdateHelper.dataThreadDispatch.addListData("型号不匹配!!!")
                            return -3
                        }
                        crc8 = CRC8.calcCrc8(bmpPixData)
                        if (Package.status.oid === action_oid && action_oid != 0xFFFF) {
                            if (Package.status.xcode === 0xFF.toByte() || Package.status.xcode === 0xFE.toByte()) {//就绪：0xFF,待续传：0xFE
                                LogUtils.d("续传中...${Package.status.recv}")
                                UpdateHelper.dataThreadDispatch.addListData("续传中...${Package.status.recv}")
                                readStatus = 1
                                renew = true
                                action_pckid = Package.status.recv
                                //将得到的action_id  赋值给Packagek中的oid 参数
                                startPackage = Package.start.get(
                                    action_oid,
                                    bmpPixData.size,
                                    maskTemperature.toByte(),
                                    crc8!!,
                                    0xFE.toByte(),
                                    nfca_paclen
                                )
                            } else if (Package.status.xcode === 0xF0.toByte() || Package.status.xcode === 0xE0.toByte()) {//开始刷屏：0xF0,刷屏完成：0xE0
                                LogUtils.d("正在显示或已显示完成!!!")
                                UpdateHelper.dataThreadDispatch.addListData("正在显示或已显示完成!!!")
                                return 1
                            }
                        }
                        if (action_oid == 0xFFFF || Package.status.oid === 0xFFFF) {
                            action_oid = Package.status.oid + 1 and 0xFFFF//将操作ID自加1
                        }
                        if (readStatus == 0) {
                            action_pckid = 0
                            readStatus = 1
                            //将得到的action_id  赋值给Packagek中的oid 参数
                            startPackage = Package.start.get(
                                action_oid,
                                bmpPixData.size,
                                maskTemperature.toByte(),
                                crc8!!,
                                0xFF.toByte(),
                                nfca_paclen
                            )
                        }
                    } else {
                        if (readStatus == 0) {
                            action_pckid = 0
                            readStatus = 1
                            //将得到的action_id  赋值给Packagek中的oid 参数
                            startPackage = Package.start.get(
                                action_oid,
                                bmpPixData.size,
                                maskTemperature.toByte(),
                                crc8!!,
                                0xFF.toByte(),
                                nfca_paclen
                            )
                        } else {
                            connected = false
                        }
                    }
                }

                if (readStatus == 2) {
                    if (Package.status.set(result, result.size - 16)) {
                        if (Package.status.oid === action_oid) {
                            if (Package.status.xcode === 0xFE.toByte() || Package.status.xcode === 0xFF.toByte()) {//就绪：0xFF,待续传：0xFE
                                //起始包执行成功!
                                LogUtils.d("起始包执行成功")
                                UpdateHelper.dataThreadDispatch.addListData("起始包执行成功")
                                return 0
                            }
                        }
                        return -1
                    } else {
                        connected = false
                    }
                }
            }

            if (readStatus == 1) {
                if (writeNfcA(startPackage!!, mfc) === true) {
                    readStatus = 2
                    //写入起始包数据成功！
                    LogUtils.d("写入起始包数据成功!!!")
                    UpdateHelper.dataThreadDispatch.addListData("写入起始包数据成功!!!")
                } else {
                    //起始包发送失败！
                    LogUtils.d("起始包发送失败!!!")
                    UpdateHelper.dataThreadDispatch.addListData("起始包发送失败!!!")
                    connected = false
                }
            }
            if (renew == false) {
                LogUtils.d("等待就绪:$delayTime ms")
                UpdateHelper.dataThreadDispatch.addListData("等待就绪:$delayTime ms")
                return 2
            }
        }
        //起始包执行失败！
        return -2
    }

    fun writeNfcA(data: ByteArray, mfc: NfcA): Boolean {
        if (data.size <= 0) return false
        val cmd = ByteArray(data.size + 2)
        cmd[0] = 0x3A.toByte() // MF write command
        cmd[1] = data.size.toByte()
        //data:源数组 srcPos:源数组中的起始位置 cmd:测试目标数组 destPost:测试目标数组起始位置 length:要复制的数组元素的数量
        System.arraycopy(data, 0, cmd, 2, data.size)
        if (mfc == null) return false

        try {
            val result = mfc.transceive(cmd)
            if (result != null) {
                if (result!![0].toInt() == 0x0A) {
                    return true
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            LogUtils.e("NFC连接被断开")
            UpdateHelper.dataThreadDispatch.addListData("NFC连接被断开")
        } finally {

        }

        return false
    }

    /**
     * fuction:写入数据包
     * @param mfc
     * @param publishProgress  进度条
     * @param len Data数据长度
     * @param maskTemperature 价签温度
     * @param activity 上下文
     * @param type 价签类型
     * @param imagedata 要写入的数据
     * */
    fun writeDataPackage(
        mfc: NfcA,
        publishProgress: IPublishProgress<Int>?,
        imagedata: ByteArray
    ): Boolean {
        if (imagedata.size < 1) {
            return false
        }
        var trytime = 1
        var pck_len = 0
        var connected = true

        val datas = ByteArray(imagedata.size)
        System.arraycopy(imagedata, 0, datas, 0, imagedata.size)
        var onePack = ByteArray(nfca_paclen + 4)
        LogUtils.d("开始写入数据包")
        UpdateHelper.dataThreadDispatch.addListData("开始写入数据包")
        var i = action_pckid * nfca_paclen//为已上传的数据长度
        if (datas != null) {
            if (i > datas.size) {
                LogUtils.d("数据包数据已经写入!!!")
                UpdateHelper.dataThreadDispatch.addListData("数据包数据已经写入!!!")
                return true //数据全部写入完成
            }
            i = 0
            while (i < datas.size) {
                trytime = 1
                while (trytime <= 5) {
                    trytime++
                    if (!connected) {
                        connected = ConnectUtils().connectNfcA(delay, 3, timeout, mfc)
                        if (connected == true) {
                            LogUtils.d("已重连，包号：$action_pckid")
                            UpdateHelper.dataThreadDispatch.addListData("已重连，包号：$action_pckid")
                        } else {
                            LogUtils.d("重连失败，包号：$action_pckid")
                            UpdateHelper.dataThreadDispatch.addListData("重连失败，包号：$action_pckid")
                        }
                    }
                    if (!connected) continue
                    i = action_pckid * nfca_paclen
                    if (i >= datas.size) {
                        return true
                    }
                    if (datas.size - i >= nfca_paclen) {
                        pck_len = nfca_paclen
                    } else {
                        pck_len = datas.size - i
                    }
                    onePack = Package.data.get(action_pckid, datas, i, pck_len)
                    if (UpdateHelper.getIsAddPackageData()) {
                        UpdateHelper.dataThreadDispatch.addListData("状态包数据=${ByteUtil.toHexString(onePack)}")
                    }
                    LogUtils.d("状态包数据=${ByteUtil.toHexString(onePack)}")

                    if (writeNfcA(onePack, mfc) == true) {
                        break
                    }
                    connected = false
                }
                if (trytime > 5 || connected == false) {
                    LogUtils.e("写入数据包错误！出错包号：$action_pckid")
                    UpdateHelper.dataThreadDispatch.addListData("写入数据包错误！出错包号：$action_pckid")

                    return false
                }
                action_pckid++
                i = pck_len + i
                publishProgress?.showProgress(action_pckid + 1)
            }
        }
        LogUtils.e("写入数据包成功!!!")
        UpdateHelper.dataThreadDispatch.addListData("写入数据包成功!!!")
        return true
    }

    fun writeEndPackage(mfc: NfcA): Boolean {
        val endPackage = Package.end.get()
        var connected = true
        var trytime = 1
        LogUtils.d("开始写入结束包")
        UpdateHelper.dataThreadDispatch.addListData("开始写入结束包")
        while (trytime <= 5) {
            trytime++
            if (!connected) {
                connected = ConnectUtils().connectNfcA(delay, 3, timeout, mfc)
                if (connected == true) {
                    LogUtils.e("已重连")
                    UpdateHelper.dataThreadDispatch.addListData("已重连")
                } else {
                    LogUtils.e("重连失败")
                    UpdateHelper.dataThreadDispatch.addListData("重连失败")
                }
            }
            if (!connected) continue
            if (UpdateHelper.getIsAddPackageData()) {
                UpdateHelper.dataThreadDispatch.addListData("结束包数据:=${ByteUtil.toHexString(endPackage)} ")
            }
            LogUtils.d("结束包数据:=${ByteUtil.toHexString(endPackage)} ")
            if (writeNfcA(endPackage, mfc)) {
                break
            }
            connected = false
        }
        if (trytime > 5) {
            LogUtils.e("写入结束包数据错误！")
            UpdateHelper.dataThreadDispatch.addListData("写入结束包数据错误！ ")
            return false
        } else {
            LogUtils.d("写入结束包数据成功！")
            UpdateHelper.dataThreadDispatch.addListData("写入结束包数据成功！ ")
            return true
        }
    }
}