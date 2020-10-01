package org.acme.genericjmsra.util;

import org.jboss.logging.Logger;

import javax.persistence.Column;
import java.util.Date;

public class MessageRecord {
    private static final Logger LOG = Logger.getLogger(MessageRecord.class);

    private String messageUUID = null;

    private String mdbID = null;

    private String messageText = null;

    public MessageRecord(String messageUUID,String mdbID, String messageText) {
        this.messageUUID = messageUUID;
        this.mdbID = mdbID;
        this.messageText = messageText;
    }

    public String getMessageUUID() {
        return messageUUID;
    }

    public void setMessageUUID(String messageUUID) {
        this.messageUUID = messageUUID;
    }

    public String getMdbID() {
        return mdbID;
    }

    public void setMdbID(String mdbID) {
        this.mdbID = mdbID;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    @Override
    public String toString() {
        return "MessageRecord{" +
                "messageUUID='" + messageUUID + '\'' +
                ", mdbID='" + mdbID + '\'' +
                ", messageText='" + messageText + '\'' +
                '}';
    }
}
