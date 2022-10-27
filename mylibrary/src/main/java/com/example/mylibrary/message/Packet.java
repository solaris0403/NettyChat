package com.example.mylibrary.message;

import java.io.Serializable;
import java.util.UUID;

public class Packet implements Serializable {
    private String mid;// 消息唯一标识
    private int type;//消息类型
    private String from;//发送方
    private String to;//接收方
    private long timestamp;// 消息发送时间，单位：毫秒
    private String content;// 消息内容
    private int contentType;// 消息内容类型
    private String data; // 扩展字段，以key/value形式存储的json字符串

    public Packet(Builder builder) {
        if (builder == null){
            throw new IllegalArgumentException("packet null");
        }
        this.mid = builder.mid;
        this.type = builder.type;
        this.from = builder.from;
        this.to = builder.to;
        this.timestamp = builder.timestamp;
        this.content = builder.content;
        this.contentType = builder.contentType;
        this.data = builder.data;
    }

    public String getMid() {
        return mid;
    }

    public int getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public int getContentType() {
        return contentType;
    }

    public String getData() {
        return data;
    }

    public static class Builder{
        private final String mid;// 消息唯一标识
        private int type;//消息类型
        private String from;//发送方
        private String to;//接收方
        private long timestamp;// 消息发送时间，单位：毫秒
        private String content;// 消息内容
        private int contentType;// 消息内容类型
        private String data; // 扩展字段，以key/value形式存储的json字符串
        public Builder() {
            this.mid = String.valueOf(UUID.randomUUID());
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder setTo(String to) {
            this.to = to;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setContentType(int contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Packet build(){
            return new Packet(this);
        }
    }
}
