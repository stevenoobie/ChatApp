package com.example.testo.Notifications;

public class Data {
    private String user;
    private String icon;
    private String body;
    private String title;
    private String sent_to;
    private String conversationId;
    private String messageId;


    public Data(String user, String icon, String body, String title, String sent_to, String conversationId, String messageId) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sent_to = sent_to;
        this.conversationId = conversationId;
        this.messageId = messageId;

    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent_to() {
        return sent_to;
    }

    public void setSent_to(String sent_to) {
        this.sent_to = sent_to;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }


}
