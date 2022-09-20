package com.example.mylibrary.event;

import net.x52im.mobileimsdk.server.protocal.s.PKickoutInfo;

public interface ChatBaseEvent {
    void onLoginResponse(int errorCode);
    void onLinkClose(int errorCode);
    void onKickout(PKickoutInfo kickoutInfo);
}
