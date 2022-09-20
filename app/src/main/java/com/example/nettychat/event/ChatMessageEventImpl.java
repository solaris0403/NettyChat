/*
 * Copyright (C) 2022  即时通讯网(52im.net) & Jack Jiang.
 * The MobileIMSDK_TCP (MobileIMSDK v6.x TCP版) Project. 
 * All rights reserved.
 * 
 * > Github地址：https://github.com/JackJiang2011/MobileIMSDK
 * > 文档地址：  http://www.52im.net/forum-89-1.html
 * > 技术社区：  http://www.52im.net/
 * > 技术交流群：185926912 (http://www.52im.net/topic-qqgroup.html)
 * > 作者公众号：“即时通讯技术圈】”，欢迎关注！
 * > 联系作者：  http://www.52im.net/thread-2792-1-1.html
 *  
 * "即时通讯网(52im.net) - 即时通讯开发者社区!" 推荐开源工程。
 * 
 * ChatMessageEventImpl.java at 2022-7-28 17:17:23, code by Jack Jiang.
 */
package com.example.nettychat.event;

import net.x52im.mobileimsdk.server.protocal.ErrorCode;

import android.util.Log;
import android.widget.Toast;

import com.example.mylibrary.message.Message;
import com.example.mylibrary.event.ChatMessageEvent;
import com.example.nettychat.MainActivity;

/**
 * 与IM服务器的数据交互事件在此ChatTransDataEvent子类中实现即可。
 *
 * @author Jack Jiang(http://www.52im.net/thread-2792-1-1.html)
 * @version 1.1
 */
public class ChatMessageEventImpl implements ChatMessageEvent {
	private final static String TAG = ChatMessageEventImpl.class.getSimpleName();

	private MainActivity mainGUI = null;

	@Override
	public void onReceiveMessage(Message message) {
		if (mainGUI != null) {
			Toast.makeText(mainGUI, "说：" + message.getContent(), Toast.LENGTH_SHORT).show();
			this.mainGUI.showIMInfo_black("说：" + message.getContent());
		}
	}

	/**
	 * 服务端反馈的出错信息回调事件通知。
	 *
	 * @param errorCode 错误码，定义在常量表 ErrorCode.ForS 类中
	 * @param errorMsg  描述错误内容的文本信息
	 * @see <a href="http://docs.52im.net/extend/docs/api/mobileimsdk/server/net/openmob/mobileimsdk/server/protocal/ErrorCode.ForS.html">ErrorCode.ForS类</a>
	 */
	@Override
	public void onErrorResponse(int errorCode, String errorMsg) {
		Log.d(TAG, "【DEBUG_UI】收到服务端错误消息，errorCode=" + errorCode + ", errorMsg=" + errorMsg);
		if (errorCode == ErrorCode.ForS.RESPONSE_FOR_UNLOGIN)
			;//this.mainGUI.showIMInfo_brightred("服务端会话已失效，自动登陆/重连将启动! ("+errorCode+")");
		else
			this.mainGUI.showIMInfo_red("Server反馈错误码：" + errorCode + ",errorMsg=" + errorMsg);
	}

	public ChatMessageEventImpl setMainGUI(MainActivity mainGUI) {
		this.mainGUI = mainGUI;
		return this;
	}
}
