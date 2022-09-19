package com.example.mylibrary.event;

public interface MessageQoSEvent {
    void messagesLost(ArrayList<Protocal> lostMessages);
    void messagesBeReceived(String theFingerPrint);
}
