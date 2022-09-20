package com.example.mylibrary.event;

import net.x52im.mobileimsdk.server.protocal.Protocal;

import java.util.ArrayList;

public interface MessageQoSEvent {
    void messagesLost(ArrayList<Protocal> lostMessages);
    void messagesBeReceived(String theFingerPrint);
}
