package com.example.nettychat.event;

import com.example.mylibrary.IMCoreSDK;
import com.example.mylibrary.message.IMessageHandler;
import com.example.mylibrary.message.Message;
import com.example.mylibrary.utils.IMThreadPool;

public class MessageHandler implements IMessageHandler {
    /**
     * Socket发回来的数据，至于对发回来的数据进行何种方式的解析，应该由应用层来处理
     * 存数据库、丢掉
     * 注意：该方法不是主线程，如果要更新IU，需要切到UI线程，而且该线程应该快速处理
     */
    @Override
    public void onMessageReceive(byte[] data) {
        final Message message = new Message();
        message.setContent(new String(data));
        if (IMCoreSDK.getInstance().getChatMessageEvent() != null) {
            IMThreadPool.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    IMCoreSDK.getInstance().getChatMessageEvent().onReceiveMessage(message);
                }
            });
        }
    }

    /**
     * 消息最终发送前的处理发放，对消息的加密、拦截都在这里
     * @param message
     * @return 发送的字节，如果是null或者0，则不发送
     */
    @Override
    public byte[] onMessageSend(Message message) {
        return message.getContent().getBytes();
    }
}