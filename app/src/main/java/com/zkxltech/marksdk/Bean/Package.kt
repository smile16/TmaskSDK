package com.zkxltech.marksdk.Bean

import kotlin.experimental.and
import kotlin.experimental.or

/**
 *创建作者       : albus
 *创建时间       : 2019/11/21
 *Fuction(类描述):
 */
class Package {
    /**
     * 配置包
     */
    object cfg {
        val HEARTBEAT_CH00 = 0
        val SYNNCTIME_CH01 = 1
        val cfg_magic = 0xE4.toByte()//包头
        var bitrate_heartbeat: String?=null//  1MBPS : 0 , 250KBPS : 2
        var bitrate_synctime: String?=null
        var bitr_heartbeat: Byte = 0
        var bitr_synctime: Byte = 0
        var rf24_enable: Boolean = false
        var built_in: Byte = 0
        var oid_clear: Boolean = false
        var rf24_augment: Boolean = false
        var uid = ByteArray(4)
        var mac = ByteArray(4)
        var ch = ByteArray(2)
        var heartbeat: Int = 0

        operator fun set(_data: ByteArray, pos: Int, crc: Boolean): Byte {
            if (pos >= _data.size) return -1
            if (_data.size - pos < 16) return -2
            if (_data[pos + 0] != cfg_magic) return -3
            if (_data[15] != CRC8.calcCrc8(_data, 1, 14) && crc == true) return 0

            System.arraycopy(_data, pos + 1, uid, 0, 4)
            heartbeat = (_data[pos + 5] and 0xFF.toByte()).toInt()
            //System.arraycopy(_data, pos + 6, mac, 0, 4);
            mac[0] = _data[pos + 6]
            mac[1] = _data[pos + 7]
            mac[2] = _data[pos + 8]
            mac[3] = _data[pos + 9]
            System.arraycopy(_data, pos + 10, ch, 0, 2)
            val temp = _data[pos + 14].toInt()
            bitr_heartbeat = (temp shr 2 and 0x01).toByte()
            bitr_synctime = (temp shr 3 and 0x01).toByte()
            built_in = (temp shr 4 and 0x03).toByte()
            rf24_enable = if (temp shr 6 and 0x01 != 0) true else false
            rf24_augment = if (temp shr 7 and 0x01 != 0) true else false
            setStr(bitr_heartbeat, bitr_synctime)
            return 1
        }

        private fun setStr(ihb: Byte, ist: Byte) {
            when (ihb) {
                0.toByte() -> bitrate_heartbeat = "1MBPS"
                1.toByte() -> bitrate_heartbeat = "250KBPS"
            }

            when (ist) {
                0.toByte() -> bitrate_synctime = "1MBPS"
                1.toByte()-> bitrate_synctime = "250KBPS"
            }
        }

        operator fun get(
            uid: ByteArray?,
            mac: ByteArray?,
            hbch: Int,
            sych: Int,
            temp: Int,
            hbbitrate: Int,
            sybitrate: Int,
            heartbeat: Int,
            rf24: Boolean,
            augment: Boolean,
            _built_in: Int,
            _oid_clear: Boolean
        ): ByteArray? {
            val datas = ByteArray(16)
            if (uid == null || mac == null) return null
            if (uid.size != 4 || mac.size != 4) return null

            datas[0] = cfg_magic
            System.arraycopy(uid, 0, datas, 1, 4)
            datas[5] = heartbeat.toByte()
            //System.arraycopy(mac, 0, datas, 6, 4);
            datas[6] = mac[0]
            datas[7] = mac[1]
            datas[8] = mac[2]
            datas[9] = mac[3]

            datas[10] = hbch.toByte()
            datas[11] = sych.toByte()
            datas[12] = temp.toByte()
            datas[13] = 0x00

            datas[14] = 0x00
            datas[14] = datas[14] or (if (augment == true) 0x01 else 0x00).toByte()
            datas[14] = (datas[14] .toInt() shl 1).toByte()
            datas[14] = datas[14] or (if (rf24 == true) 0x01 else 0x00).toByte()
            datas[14] = (datas[14].toInt() shl 2).toByte()
            datas[14] = datas[14] or (_built_in and 0x03).toByte()
            datas[14] = (datas[14].toInt() shl 1).toByte()
            datas[14] = datas[14] or (sybitrate and 0x01).toByte()
            datas[14] = (datas[14].toInt() shl 1).toByte()
            datas[14] = datas[14] or (hbbitrate and 0x01).toByte()
            datas[14] = (datas[14].toInt()shl 2).toByte()
            datas[14] = datas[14] or (if (_oid_clear == true) 0x01 else 0x00).toByte()

            datas[15] = CRC8.calcCrc8(datas, 1, 14)
            return datas
        }
    }

    /**
     * 状态包
     */
    object status {
        private val default_magic = 0xDF.toByte()//包头
        private val nfc_magic = 0xE0.toByte()//包头
        private val rf24_magic = 0xE1.toByte()//包头
        var oid: Int = 0//操作ID
        var sver: Byte = 0//版本
        var temp: Byte = 0//上次刷屏温度
        var volt: Byte = 0//电压
        var eink_type: Byte = 0//目标型号中的高四位
        var eink_pixel: Byte = 0//目标型号中的低四位
        var eink: Byte = 0//目标型号
        var xcode: Byte = 0//NFC状态 就绪：0xFF,待续传：0xFE,无效：0xFC,失败：0xF8,开始刷屏：0xF0,刷屏完成：0xE0
        var recv: Int = 0//已接收包号、断点包号
        var len: Int = 0//上次操作的数据长度，高字节在前，低字节在后
        var pck: Int = 0//本次操作的分包长度
        var strCode: String?=null//状态码文字显示

        operator fun set(_data: ByteArray, pos: Int): Boolean {
            val t = 0
            if (pos >= _data.size) return false
            if (_data.size - pos < 16) return false
            if (_data[pos + 0] != nfc_magic && _data[pos + 0] != rf24_magic && _data[pos + 0] != default_magic) return false
            xcode = _data[pos + 1]
            oid = (_data[pos + 3] and 0xFF.toByte()).toInt()
            oid = oid shl 8
            oid = oid or ((_data[pos + 2] and 0xFF.toByte()).toInt())
            len = (_data[pos + 4] and 0xFF.toByte()).toInt()
            len = len shl 8
            len = len or ((_data[pos + 5] and 0xFF.toByte()).toInt())
            temp = _data[pos + 6]

            eink = _data[pos + 7]
            eink_type = (eink.toInt() shr 4 and 0x0F).toByte()
            eink_pixel = (eink.toInt() shr 0 and 0x0F).toByte()

            sver = _data[pos + 8]

            recv = _data[pos + 9].toInt()
            recv = recv shl 8
            recv = recv or _data[pos + 10].toInt()
            volt = _data[pos + 11]
            val pcklen = _data[12].toInt() and 0x000000ff
            pck=pcklen
            setStrCode(xcode)
            return true
        }

        private fun setStrCode(icode: Byte) {
            when (icode) {
                //                就绪：0xFF,待续传：0xFE,无效：0xFC,失败：0xF8,开始刷屏：0xF0,刷屏完成：0xE0
                0xFF.toByte() -> strCode = "就绪"
                0xFE.toByte() -> strCode = "不完整"
                0xFC.toByte() -> strCode = "无效操作"
                0xF8.toByte() -> strCode = "校验失败"
                0xF0.toByte() -> strCode = "正在显示"
                0xE0.toByte() -> strCode = "显示完成"
            }
        }
    }

    /**
     * 起始包
     */
    object start {
        private val magic = 0xE2.toByte()//包头
        var oid: Int = 0//操作ID
        var crc: Byte = 0
        var xcode: Byte = 0
        var len: Int = 0
        var temp: Byte = 0
        var pck_len:Int=0//本次操作的分包长度
        /**
         * 获取起始数据包
         *
         * @param _oid  操作编号
         * @param _len 操作长度
         * @param _crc8 CRC8
         * @return 14byte起始数据包
         */
        operator fun get(_oid: Int, _len: Int, _temp: Byte, _crc8: Byte, _xcode: Byte,_pcklen:Int): ByteArray {
            val datas = ByteArray(9)
            xcode = (_xcode.toInt() and 0x000000ff).toByte()
            oid = _oid and 0x000000ff
            crc = (_crc8.toInt() and 0x000000ff).toByte()
            len = _len and 0x000000ff
            temp = (_temp.toInt() and 0x000000ff).toByte()
            pck_len=_pcklen and 0x000000ff
            datas[0] = magic
            datas[1] = xcode
            datas[3] = (oid shr 8 and 0xFF).toByte()
            datas[2] = (oid and 0xFF).toByte()
            datas[4] = (_len shr 8 and 0xFF).toByte()
            datas[5] = (_len and 0xFF).toByte()

            datas[6] = temp
            datas[7] = crc
            datas[8]= pck_len.toByte()
            return datas
        }
    }

    /**
     * 数据包
     */
    object data {
        /**
         * 获取数据包
         *
         * @param packageID 2byte, 包号，高为在前，低位在后
         * @param _data     14byte,数据内容
         * @return 16byte组合数据包
         */
        operator fun get(packageID: Int, _data: ByteArray, oft: Int, _len: Int): ByteArray {
            val datas = ByteArray(_len + 2)
            val bid =
                byteArrayOf((packageID shr 8 and 0xFF).toByte(), (packageID and 0xFF).toByte())
            System.arraycopy(bid, 0, datas, 0, 2)
            System.arraycopy(_data, oft, datas, 2, _len)
            return datas
        }
    }

    /**
     * 结束包
     */
    object end {
        private val magic = 0xE3.toByte()//包头

        fun get(): ByteArray {
            val datas = ByteArray(1)
            datas[0] = magic
            return datas
        }
    }
}