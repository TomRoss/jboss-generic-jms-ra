/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.genericjmsra.jpa;


import java.util.Date;
import java.util.Objects;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

import org.jboss.logging.Logger;

@Table(name = "dbrecord")
@Entity
public class DBRecord {
    private static final long serialVersionUID = -4610973432199628746L;
    private static final Logger LOG = Logger.getLogger(DBRecord.class);

    @Id
    @Column(name = "message_uuid",nullable = false,unique = true)
    private String message_uuid;

    @Column(name = "creator_id",nullable = false)
    private String creator;

    @Column(name = "updator_id",nullable = true)
    private String updator;

    @Column(name = "message_text",nullable = false)
    private String messageText;

    @Column(name = "message_created",nullable = false)
    public Date messageCreated;

    @Column(name = "message_updated",nullable = true )
    public Date messageUpdated;

    public DBRecord() {

    }

    public DBRecord(String message_uuid, String creator, String updator, String messageText) {
        this.message_uuid = message_uuid;
        this.creator = creator;
        this.updator = updator;
        this.messageText = messageText;
    }

    public String getMessage_uuid() {
        return message_uuid;
    }

    public void setMessage_uuid(String message_uuid) {
        this.message_uuid = message_uuid;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getUpdator() {
        return updator;
    }

    public void setUpdator(String updator) {
        this.updator = updator;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Date getMessageCreated() {
        return messageCreated;
    }

    public void setMessageCreated(Date messageCreated) {
        this.messageCreated = messageCreated;
    }

    public Date getMessageUpdated() {
        return messageUpdated;
    }

    public void setMessageUpdated(Date messageUpdated) {
        this.messageUpdated = messageUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBRecord dbRecord = (DBRecord) o;
        return getMessage_uuid().equals(dbRecord.getMessage_uuid()) &&
                getCreator().equals(dbRecord.getCreator()) &&
                Objects.equals(getUpdator(), dbRecord.getUpdator()) &&
                getMessageText().equals(dbRecord.getMessageText()) &&
                getMessageCreated().equals(dbRecord.getMessageCreated()) &&
                Objects.equals(getMessageUpdated(), dbRecord.getMessageUpdated());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessage_uuid(), getCreator(), getUpdator(), getMessageText(), getMessageCreated(), getMessageUpdated());
    }
}
