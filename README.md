maskSDK
电子价签SDK 电子价签系统对接API 手册 Android版

V 1.0.0

历史版本 示例价签型号 sdk版本 文档 修改说明 修改 修改时间 M290 V1.0 V1.0.0 1.初始版本,已添加写入价签功能 严闯 2019.012.12

目 录

文档说明 1 1.1. 系统说明 1 1.2. 写作目的 1 1.3. 术语与缩写解释 1

函数（指令）说明 1 2.1. 函数(指令)集 1 2.1.1. NFC使用相关函数 1

流程示意图 7 3.1. 电子价签写入流程 9

权限及SDK依赖添加 13 4.1. 权限添加 13 4.2. sdk依赖添加 表目录 未找到图形项目表。 图目录 未找到图形项目表。

文档说明 1.1. 系统说明 在整个系统中由APP端通过NFC发送指令控制电子价签更新界面数据。 1.2. 写作目的 本文讲解APP与硬件设备(电子价签)的连接及APP与硬件设备(电子价签)之间的交互指令。 1.3. 术语与缩写解释

maskTemperature：价签当前温度。 ImageWBPath：用户上传图片被拆分后黑白图片保存路径 ImageWRPath: 用户上传图片被拆分后红色图片保存路径 Action_len： APP与价签通信时 自定义上传数据长度 2. 函数（指令）说明 2.1. 函数(指令)集 电子价签的指令函数仅为一类，写入数据更新界面时的帮忙类。 2.1.1. NFC使用相关函数 UpdataHelper类,提供拆分图片 更新价签类

/** * @param type 价签型号 * @param imagePath 图片路径 * @param mfc * @param temperature 价签温度 * @param listener 回调 * */ fun upDateMarkUI( type: Int, imagePath: String, mfc: NfcA?, temperature: Int, listener: NFCCommunicationInterface ) //重载方法 可不传价签温度 此时默认为20 fun upDateMarkUI( type: Int, imagePath: String, mfc: NfcA, listener: NFCCommunicationInterface )

连接上NFC后调用

在activity或fragment销毁时调用
/******************************************************************************* /** * @param mfc * @param type 价签型号 * @param activity 传入当前的Activity
* @param pck_len 自定义数据长度(可不传 提供重载方法 不传时默认长度为64) * /******************************************************************************/

fun AysncUpdate( mfc: NfcA, type: Int, activity: Activity, pck_len: Int )
以上为基本使用

设置是否打印SDK日志功能 true:打印 false:不打印 默认为打印

fun setIsLogPrint(showLog:Boolean)

如果需要在APP界面上以文字的形式 展现给用户看当前价签更新进度 1 需要先开启消息队列接收消息 UpdateHelper.dataThreadDispatch.mIsRunning=true //设置为true 才会将当前进度添加至消息队列 2 根据需要 看是否需要添加写入价签的byte数据至消息队列 如果需要传入True 不需要传入false fun setIsAddPackageData(isAdd: Boolean)

完成上面两步后 就可以从UpdateHelper.dataThreadDispatch.getListData()中取出消息 展示给用户

取出完成或当前Activity被销毁时 UpdateHelper.dataThreadDispatch.shutDown() 关闭消息队列

流程示意图 3.1. 电子价签写入流程 注意：

如果是同批次刷新同一种型号的价签 同一张图片 upDateMarkUI方法只需要调用一次 后续可连续进行更新写入 如果是不同型号或者不同图片 upDateMarkUI和AysncUpdate 都需要重新调用

权限及SDK依赖添加 4.1. 权限添加

4.2 依赖添加 step 1: allprojects { repositories { ... maven { url 'https://jitpack.io' } } }

step:2 dependencies { implementation 'com.github.smile16:maskSDK:Tag' }
