package com.example.mylibrary.event;

import com.example.mylibrary.message.Message;

public interface ChatMessageEvent {
    void onReceiveMessage(Message message);
    void onErrorResponse(int errorCode, String errorMsg);
}
