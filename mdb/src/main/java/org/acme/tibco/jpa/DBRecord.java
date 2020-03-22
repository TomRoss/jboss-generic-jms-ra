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

package org.acme.tibco.jpa;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
@Table(name = "dbrecord_tbl")
public class DBRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "indx", unique = true, nullable = false)
    private int indx;

    @Column(name = "record_name")
    private String recordName = null;

    @Column(name = "worker_id")
    private int workerID;

    @Column(name = "worker_type")
    private String workerType;

    public DBRecord(String recordName) {
        this.recordName = recordName;
    }

    public DBRecord(int workerID, String recordName) {
        this.workerID = workerID;
        this.recordName = recordName;
    }

    public DBRecord(int workerID, String workerType, String recordName) {
        this.workerType = workerType;
        this.workerID = workerID;
        this.recordName = recordName;
    }

    public int getWorkerID() {
        return workerID;
    }

    public void setWorkerID(int workerID) {
        this.workerID = workerID;
    }

    public int getIndx() {
        return indx;
    }

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public void setIndx(int indx) {
        this.indx = indx;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBRecord)) return false;
        DBRecord dbRecord = (DBRecord) o;
        return getIndx() == dbRecord.getIndx() &&
                Objects.equals(getRecordName(), dbRecord.getRecordName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndx(), getRecordName());
    }

    @Override
    public String toString() {
        return "DBRecord{" +
                "indx=" + indx +
                ", workerID=" + workerID +
                ", workerType=" + workerType +
                ", recordName='" + recordName +
                "}";
    }
}
