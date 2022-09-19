package com.example.im_lib;

/**
 * 消息转发器，负责将接收到的消息转发到应用层
 */
public class MsgDispatcher {
    private OnEventListener mOnEventListener;

    public MsgDispatcher() {

    }

    public void setOnEventListener(OnEventListener listener) {
        this.mOnEventListener = listener;
    }

    /**
     * 接收消息，并通过OnEventListener转发消息到应用层
     *
     * @param msg
     */
    public void receivedMsg(MessageProtobuf.Msg msg) {
        if (mOnEventListener != null) {
            mOnEventListener.dispatchMsg(msg);
        }
    }
}
