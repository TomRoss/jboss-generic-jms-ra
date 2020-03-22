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

package org.acme.tibco.ejb;

import org.acme.tibco.jpa.DBRecord;
import org.jboss.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class DBManagerImpl implements DBManager{
    private static final Logger LOG = Logger.getLogger(DBManagerImpl.class);

    @PersistenceContext(unitName="postgres-db")
    private EntityManager em;

    @Override
    public void insertRecord(int workerID,String type, String name) {

        DBRecord dbr = new DBRecord(workerID,type,name);

        LOG.infof("Record %s inserted",name);

        if (LOG.isDebugEnabled()){
            LOG.debugf("Inserting record %s ",dbr.toString());
        }

        em.persist(dbr);

        em.flush();

    }

    @Override
    public DBRecord findRecord(int indx) {

        DBRecord db_record = em.find(DBRecord.class,indx);
        return db_record;
    }

    public void updateRecord(int indx, String name){

        DBRecord dbr = findRecord(indx);

        dbr.setRecordName(name);

        em.merge(dbr);

    }
}
