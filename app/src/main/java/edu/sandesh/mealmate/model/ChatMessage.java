package edu.sandesh.mealmate.model;

import java.util.Date;

public class ChatMessage {
    
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    
    private String message;
    private int messageType;
    private Date timestamp;
    
    public ChatMessage(String message, int messageType) {
        this.message = message;
        this.messageType = messageType;
        this.timestamp = new Date();
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getMessageType() {
        return messageType;
    }
    
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isUserMessage() {
        return messageType == TYPE_USER;
    }
    
    public boolean isBotMessage() {
        return messageType == TYPE_BOT;
    }
} 