package com.zkxltech.marksdk

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Build
import android.os.Environment
import com.zkxltech.marksdk.Utils.ConnectUtils
import com.zkxltech.marksdk.Utils.ImageUtils
import com.zkxltech.marksdk.Utils.LogUtils
import com.zkxltech.marksdk.Utils.WriteNFCDataUtils
import com.zkxltech.marksdk.`interface`.NFCCommunicationInterface
import com.zkxltech.marksdk.task.MyAsyncTask
import com.zkxltech.marksdk.task.asynctask.*
import com.zkxltech.marksdk.thread.DataThreadDispatch
import io.reactivex.Observable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.ObservableOnSubscribe
import io.reactivex.functions.Consumer


/**
 *创建作者       : albus
 *创建时间       : 2019/11/19
 *Fuction(类描述):
 */
object UpdateHelper {
    private var isAddPackageData = true
    val dataThreadDispatch by lazy { DataThreadDispatch() }
    private  var isLogPrint = true
    private  var imageWRpath: String? = null
    private  var imageWBpath: String? = null
    private  var action_len: Int? = null
    private var maskTemperature: Int = 0
    private  var listener: NFCCommunicationInterface? = null
    private  var t1: Long = 0
    private  var t2: Long = 0
    private val writeNFCDataUtils by lazy { WriteNFCDataUtils() }
    private var imageWBPath =
        Environment.getExternalStorageDirectory().getPath() + "/new_bw_imageview.bmp"
    private var imageWRPath =
        Environment.getExternalStorageDirectory().getPath() + "/new_wr_imageview.bmp"
    private  lateinit var imagedata: ByteArray

    //设置黑白图片存储位置
    fun setImageWBPath(imageBwPath: String) {
        this.imageWBPath = imageBwPath
    }

    //设置红色图片存储位置
    fun setImageWRPath(imageWrPath: String) {
        this.imageWRPath = imageWrPath
    }

    fun setIsLogPrint(showLog:Boolean){
        this.isLogPrint=showLog
    }

    fun getIsLogPrint():Boolean{
        return isLogPrint
    }
    fun setIsAddPackageData(isAdd: Boolean) {
        this.isAddPackageData = isAdd
    }
    fun getIsAddPackageData():Boolean {
       return isAddPackageData
    }

    //更新价签界面
    fun upDateMarkUI(
        type: Int,
        imagePath: String,
        mfc: NfcA,
        listener: NFCCommunicationInterface
    ) {
        upDateMarkUI(type, imagePath, mfc, 20, listener)
    }

    /**
     * @param type 价签型号
     * @param imagePath 图片路径
     * @param mfc
     * @param temperature 价签温度
     * @param listener
     * */
    //方法重载
    fun upDateMarkUI(
        type: Int,
        imagePath: String,
        mfc: NfcA?,
        temperature: Int,
        listener: NFCCommunicationInterface
    ) {
        this.listener = listener
        //先判断价签型号
        this.maskTemperature = temperature
        val file = File(imagePath)
        if (!file.exists()) {
            dataThreadDispatch.addListData("图片文件不存在,请重新选择图片路径!!!")
            LogUtils.e("图片文件不存在,请重新选择图片路径!!!")
            return
        }
        val decodeFile = getLocalBitmap(imagePath)
        //需要将用户给的图片 拆分成两张  一张黑白 一张彩色   然后将拆分后的图片保存在本地
        val bitmap = ImageUtils.convertToBW(decodeFile)
        dataThreadDispatch.addListData("图片拆分成功")
        LogUtils.d("图片拆分成功")
        val tmpBW = File(imageWBPath)
        if (tmpBW.exists()) {
            tmpBW.delete()
        }
        tmpBW.createNewFile()
        ImageUtils.saveTagBitmapToBMP(bitmap[0], tmpBW)
        imageWBpath = tmpBW.path//黑白图片路径
        dataThreadDispatch.addListData("拆分后的黑白图片保存本地成功 保存地址:${imageWBpath}")
        LogUtils.d("拆分后的黑白图片保存本地成功 保存地址:${imageWBpath}")
        if (bitmap[1] != null) {
            if (!bitmap[1]?.isRecycled!!) {
                val tmpWR = File(imageWRPath)
                if (tmpWR.exists()) {
                    tmpWR.delete()
                }
                tmpWR.createNewFile()
                ImageUtils.saveTagBitmapToBMP(bitmap[1], tmpWR)
                imageWRpath = tmpWR.path
                dataThreadDispatch.addListData("拆分后的彩色图片保存本地成功 保存地址:${imageWRPath}")
                LogUtils.d("拆分后的彩色图片保存本地成功 保存地址:${imageWRPath}")
            }
        }
        imagedata = ImageUtils.imageToByte(imageWBpath, imageWRpath, type)!!
        if (imagedata != null) {
            if (imageWRpath != null) {
                action_len = imagedata?.size * 2 + 20
            } else {
                action_len = imagedata?.size + 12
            }
        } else {
            dataThreadDispatch.addListData("用户选择图片转换为byte数据为空")
            LogUtils.e("用户选择图片转换为byte数据为空")
            return
        }
        //异步更新任务
        if (mfc == null) {
            dataThreadDispatch.addListData("NFC未连接 请先连接NFC进行通信!")
            LogUtils.d("NFC未连接 请先连接NFC进行通信!")
            return
        }
//        AysncUpdate(mfc, type)
    }

    fun AysncUpdate(
        mfc: NfcA,
        type: Int,
        activity: Activity
    ) {
        AysncUpdate(
            mfc,
            type,
            activity,
            64
        )
    }

    /**
     * @param mfc
     * @param type 价签型号
     * @param activity  传入当前的Activity
     * @param pck_len 自定义数据长度
     * */
    fun AysncUpdate(
        mfc: NfcA,
        type: Int,
        activity: Activity,
        pck_len: Int
    ) {
        MyAsyncTask.newBuilder<Tag, Int, Int>().setPreExecute(object : IPreExecute {
            override fun onPreExecute() {
                //执行线程前的调用
                listener?.NFCCommunicationAgo()
            }
        }).setDoInBackground(object : IDoInBackground<Tag, Int, Int> {
            override fun doInBackground(
                publishProgress: IPublishProgress<Int>?,
                vararg params: Tag?
            ): Int {
                //接收输入参数  执行任务中的耗时操作(必须复写 从而执行任务中的耗时操作)    返回任务线程执行结果
                try {
                    val proc =
                        (imagedata?.size!! / 60) + 3 + if (imagedata?.size % 60 != 0) 1 else 0
                    try {
                        var action_result = false
                        writeNFCDataUtils.setPcklen(pck_len)
                        val res = writeNFCDataUtils.writeStartPackage(
                            mfc,
                            action_len,
                            maskTemperature,
                            type,
                            imagedata
                        )//写入起始数据包
                        publishProgress?.showProgress(1)
                        if (res == 0) {
                            //如果返回的是0  说明数据是中断后再次写入成功的  此时可以直接写入数据包
                        } else if (res == 2) {
                            Thread.sleep(1000)
                        } else {
                            return res
                        }
                        action_result = writeNFCDataUtils.writeDataPackage(
                            mfc,
                            publishProgress,
                            imagedata
                        )//写入数据包
                        if (!action_result) return -2
                        action_result =
                            writeNFCDataUtils.writeEndPackage(mfc)//写入结束包
                        publishProgress?.showProgress(proc - 1)
                        if (!action_result) return -3
//                        listener?.NFCCommunicationProgress(100)
                        publishProgress?.showProgress(proc)
                        return 2
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        //关闭NFC连接
                        dataThreadDispatch.addListData("关闭NFC连接")
                        LogUtils.d("关闭NFC连接")
                        ConnectUtils().closeNFC(mfc)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                return -4
            }
        }).setViewActive(object : IIsViewActive {
            override fun isViewActive(): Boolean {
                //* 增加一个函数接口，在方法doInBackground执行结束开始回调方法onPostExecute之前，
                //*用来判断所属Activity是否依然处于活跃状态，
                //* 如果处于活跃状态则回调方法onPostExecute，
                //* 如果处于非活跃状态则不回调，避免回调后操作UI产生空指针异常
                return isViewActive(activity)
            }
        }).setProgressUpdate(object : IProgressUpdate<Int> {
            override fun onProgressUpdate(vararg values: Int?) {
                //在主线程显示任务执行进度
                var proc = imagedata.size / 60 + 3 + if (imagedata.size % 60 != 0) 1 else 0
                var progress = values[0]?.times(100)!! / proc
                listener?.NFCCommunicationProgress(progress)
            }
        }).setPostExecute(object : IPostExecute<Int> {
            override fun onPostExecute(result: Int?) {
                //接收线程任务执行结果 将执行结果显示到UI组件
                LogUtils.d("操作成功($result)")
                dataThreadDispatch.addListData("操作成功($result)")
                var msg: String? = null
                t2 += System.currentTimeMillis() - t1
                if (result == 1 || result == 2) {
                    msg =
                        "耗时：${t2 / 1000}.${t2 % 1000 / 100} ${t2 % 100 / 10}  ${t2 % 10}$msg"
                    LogUtils.d("操作成功,($msg)")
                    dataThreadDispatch.addListData("操作成功($msg)")
                    if (msg != null) {
                        listener?.NFCCommunicationSucessResult("操作成功,($msg)")
                    }
                } else if (result != null) {
                    if (result < 0) {
                        msg = "操作被断开,请重新靠近刷卡区域,待续传..."
                        LogUtils.d("操作被断开,请重新靠近刷卡区域,待续传...")
                        dataThreadDispatch.addListData("操作被断开,请重新靠近刷卡区域,待续传...")
                        if (msg != null) {
                            listener?.NFCCommunicationFail(msg)
                        }
                    }
                }
            }

        }).start() //start()方法  内部封装了execute()方法  触发异步任务 运行在主线程
    }

    private fun getLocalBitmap(path: String): Bitmap {
        var bitmap: Bitmap? = null
        try {
            val fis = FileInputStream(path)
            bitmap = BitmapFactory.decodeStream(fis)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            /* try catch  可以解决OOM后出现的崩溃，然后采取相应的解决措施，如缩小图片，较少内存使用
            * 但这不是解决OOM的根本方法，因为这个地方是压缩骆驼的最后一颗稻草，
            * 解决方法是dump内存，找到内存异常原因。*/
        } catch (error: OutOfMemoryError) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle()
                bitmap = null
            }
            System.gc()
        }
        return bitmap!!
    }

    fun isViewActive(activity: Activity): Boolean {
        return !(activity.isFinishing() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())
    }


}