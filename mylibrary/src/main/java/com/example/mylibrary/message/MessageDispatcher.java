package com.example.mylibrary.message;

import android.util.Log;

import com.example.mylibrary.IMCoreSDK;
import com.example.mylibrary.utils.LogUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 消息分发
 */
public class MessageDispatcher {
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

    private final ReceiveThread mReceiveThread = new ReceiveThread();
    private final SendThread mSendThread = new SendThread();

    private MessageDispatcher() {
        mReceiveThread.start();
        mSendThread.start();
    }

    public void onReceive(byte[] data) {
        mReceiveThread.add(data);
    }

    public void onSend(Message message) {
        mSendThread.add(message);
    }

    private static class ReceiveThread extends Thread {
        private final BlockingQueue<byte[]> receiveQueue = new LinkedBlockingQueue<>();

        public void add(byte[] data) {
            receiveQueue.add(data);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    byte[] data = receiveQueue.take();
                    //消息的具体处理交由业务层来实现
                    if (IMCoreSDK.getInstance().getMessageHandler() != null) {
                        IMCoreSDK.getInstance().getMessageHandler().onMessageReceive(data);
                    }
                } catch (Exception e) {
                    LogUtils.e("MessageDispatcher消息接收异常：", e);
                }
            }
        }
    }

    private static class SendThread extends Thread {
        private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
        public void add(Message data) {
            sendQueue.add(data);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Message message = sendQueue.take();
                    SendHelper.getInstance().send(message);
                } catch (Exception e) {
                    LogUtils.e("MessageDispatcher消息发送异常：", e);
                }
            }
        }
    }
}
