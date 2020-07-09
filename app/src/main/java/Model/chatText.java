package Model;

public class chatText {
    String senderId;
    String recieverId;
    String message;
    String id;
    boolean isSeen;
    boolean isDelivered;
    boolean isSent;

    public chatText(String id,String senderId, String recieverId, String message,boolean isSeen,boolean isDelivered,boolean isSent) {
        this.id=id;
        this.senderId = senderId;
        this.recieverId = recieverId;
        this.message = message;
        this.isSeen=isSeen;
        this.isDelivered=isDelivered;
        this.isSent=isSent;
    }
    public chatText(){

    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecieverId() {
        return recieverId;
    }

    public void setRecieverId(String recieverId) {
        this.recieverId = recieverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }
}
