package com.example.testo.Notifications;

import android.app.Notification;

import androidx.core.app.NotificationCompat;

public class InboxStyleObject {
    private NotificationCompat.InboxStyle inboxStyle;
    private int count;

    public NotificationCompat.InboxStyle getInboxStyle() {
        return inboxStyle;
    }

    public void setInboxStyle(NotificationCompat.InboxStyle inboxStyle) {
        this.inboxStyle = inboxStyle;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }
}
