package com.example.mylibrary.message;

import com.example.mylibrary.utils.IMObserver;

public class Message{
    private IMObserver observer;
    private byte type;//消息类型
    private long mid;//消息mid
    private String content;//消息内容

    public IMObserver getObserver() {
        return observer;
    }

    public void setObserver(IMObserver observer) {
        this.observer = observer;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
