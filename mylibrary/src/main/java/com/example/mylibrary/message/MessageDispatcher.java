package com.example.mylibrary.message;

import android.util.Log;

import com.example.mylibrary.IMCoreSDK;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 消息分发中心，
 */
public class MessageDispatcher {
    private static final String TAG = MessageDispatcher.class.getSimpleName();
    private static MessageDispatcher instance;

    public static MessageDispatcher getInstance() {
        if (instance == null) {
            synchronized (MessageDispatcher.class) {
                if (instance == null) {
                    instance = new MessageDispatcher();
                }
            }
        }
        return instance;
    }

    private final BlockingQueue<byte[]> mMessagesReceiveQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> mMessagesSendQueue = new LinkedBlockingQueue<>();

    private MessageDispatcher() {
        mReceiveThread.start();
        mSendThread.start();
    }

    public void onReceive(byte[] data) {
        mMessagesReceiveQueue.add(data);
    }

    public void onSend(Message message) {
        mMessagesSendQueue.add(message);
    }

    private Thread mReceiveThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    byte[] data = mMessagesReceiveQueue.take();
                    if (IMCoreSDK.getInstance().getMessageHandler() != null) {
                        IMCoreSDK.getInstance().getMessageHandler().onMessageReceive(data);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "MessageDispatcher 消息接收异常：" + e.getMessage());
                }
            }
        }
    });

    private Thread mSendThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Message message = mMessagesSendQueue.take();
                    if (IMCoreSDK.getInstance().getMessageHandler() != null) {
                        byte[] data = IMCoreSDK.getInstance().getMessageHandler().onMessageSend(message);
                        SendHelper.getInstance().send(data);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "MessageDispatcher 消息发送异常：" + e.getMessage());
                }
            }
        }
    });
}
