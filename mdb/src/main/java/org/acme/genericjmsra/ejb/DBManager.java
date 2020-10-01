package org.acme.genericjmsra.ejb;

import javax.ejb.Local;
import org.acme.genericjmsra.util.MessageRecord;

@Local
public interface DBManager {

    public void insertRecord(MessageRecord msgRecord);
    public void updateRecord(MessageRecord msgRecord);

}
