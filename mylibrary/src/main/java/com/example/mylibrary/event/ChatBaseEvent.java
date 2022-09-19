package com.example.mylibrary.event;

public interface ChatBaseEvent {
    void onLoginResponse(int errorCode);
    void onLinkClose(int errorCode);
    void onKickout(PKickoutInfo kickoutInfo);
}
