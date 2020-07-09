package Model;

import android.net.Uri;

public class User {
    String Id;
    String name;
    String uriPath;
    String activeStatus;
    boolean checkMessages;
    String phoneNumber;

    public User(){

    }
    public User(String Id,String name,String uriAvailable,String activeStatus,boolean checkMessages,String phoneNumber){
        this.Id=Id;
        this.name=name;
        this.uriPath=uriAvailable;
        this.activeStatus=activeStatus;
        this.checkMessages=checkMessages;
        this.phoneNumber=phoneNumber;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUriPath() {
        return uriPath;
    }

    public void setUriPath(String uriPath) {
        this.uriPath = uriPath;
    }

    public boolean isCheckMessages() {
        return checkMessages;
    }

    public void setCheckMessages(boolean checkMessages) {
        this.checkMessages = checkMessages;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
