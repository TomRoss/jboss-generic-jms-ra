package org.jboss.tibco.ejb;

import javax.ejb.Local;

@Local
public interface DBManager {

    public void insertRecord(int indx, String name);
    public void updateRecord(int indx);

}
