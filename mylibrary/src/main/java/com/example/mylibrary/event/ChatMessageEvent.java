package com.example.mylibrary.event;

public interface ChatMessageEvent {
    void onRecieveMessage(String fingerPrintOfProtocal, String userid, String dataContent, int typeu);
    void onErrorResponse(int errorCode, String errorMsg);
}
