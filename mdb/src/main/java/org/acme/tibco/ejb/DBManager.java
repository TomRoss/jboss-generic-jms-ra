package org.acme.tibco.ejb;

import org.acme.tibco.jpa.DBRecord;

import javax.ejb.Local;

@Local
public interface DBManager {

    public void insertRecord(int workerID, String type, String name);
    public DBRecord findRecord(int indx);
    public void updateRecord(int indx, String name);
}
