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

package org.acme.genericjmsra.ejb;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.acme.genericjmsra.jpa.DBRecord;
import org.acme.genericjmsra.util.MessageRecord;
import org.jboss.logging.Logger;

@Stateless(name = "DBManager")
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class DBManagerImpl implements DBManager{
    private static final Logger LOG = Logger.getLogger(DBManagerImpl.class);
    private static AtomicInteger beanCnt = new AtomicInteger(0);
    private int beanID = 0;
    @PersistenceContext(unitName="postgres-db")
    private EntityManager em;
    private Calendar calendar = null;

    @Override
    public void insertRecord(MessageRecord msgRecord) {

        DBRecord record = new DBRecord(msgRecord.getMessageUUID(),msgRecord.getMdbID(),null,msgRecord.getMessageText());

        record.setMessageCreated(new Timestamp(calendar.getTimeInMillis()));

        if (LOG.isDebugEnabled()){
            LOG.debugf("Persisting record %s ",record.toString());
        }

        em.persist(record);

        em.flush();

    }

    @Override
    public void updateRecord(MessageRecord msgRecord) {

        DBRecord record = em.find(DBRecord.class,msgRecord.getMessageUUID());

        if (record != null){

            LOG.info("Found record");

            record.setUpdator(msgRecord.getMdbID());

            record.setMessageUpdated(new Timestamp(calendar.getTimeInMillis()));

        }
    }

    @PostConstruct
    public void init(){
        beanID = beanCnt.incrementAndGet();
        LOG.infof("[%d] Calendar created.",beanID);
        calendar = Calendar.getInstance();
    }

    @PreDestroy
    public void cleanUp(){
        beanCnt.decrementAndGet();
        LOG.infof("[%d] Destroying bean instance.",beanID);
    }
}
