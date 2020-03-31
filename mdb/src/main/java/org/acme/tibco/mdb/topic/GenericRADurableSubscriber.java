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

package org.acme.tibco.mdb.topic;

import org.jboss.ejb3.annotation.DeliveryActive;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.atomic.AtomicInteger;

@MessageDriven(name = "GenericRADurableSubscriberMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "${tibco.in.topic}"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "${tibco.durable.subscriber.name}"),
        @ActivationConfigProperty(propertyName = "ClientID", propertyValue = "${tibco.durable.clientid}"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://aza:7222"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${tibco.tcf}"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${tibco.in.topic.maxSession}"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "${tibco.user}"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "${tibco.password}"),
        @ActivationConfigProperty(propertyName = "reconnectAttempts", propertyValue = "15")

})


@DeliveryActive(value = true)
//@DeliveryGroup(“group2”)
@ResourceAdapter("genericjms-xa")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class GenericRADurableSubscriber implements MessageListener {
    private static final Logger LOG = Logger.getLogger(GenericRADurableSubscriber.class);
    private static AtomicInteger mdbCnt = new AtomicInteger(1);

    private int mdbID = 0;
    private int msgCnt = 0;
    @Override
    public void onMessage(Message message) {

        LOG.infof("MDB[%d] Got messages %s",mdbID,message.toString());

        msgCnt++;
    }

    @PostConstruct
    public void init() {

        mdbID = mdbCnt.getAndIncrement();

        LOG.infof("MDB[%d] MDB created. MDB count %d ", mdbID, mdbID);

    }


    @PreDestroy
    public void cleanUp() {

        LOG.infof("MDB[%d] Shutting down.Processed %d messages.", mdbID, msgCnt);

        mdbCnt.decrementAndGet();

    }

}
