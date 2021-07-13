package org.acme.genericjmsra.mdb;



import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import javax.ejb.TransactionManagement;
import javax.ejb.EJB;

import org.jboss.ejb3.annotation.DeliveryActive;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import javax.naming.Context;
import java.util.concurrent.atomic.AtomicInteger;


@MessageDriven(name = "GenericRAOutQueueMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/outQueue"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://aza:7222"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${tibco.qcf}"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "${mdb.user}"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "${mdb.password}"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${in.mdb.max.session}"),
        @ActivationConfigProperty(propertyName = "reconnectAttempts", propertyValue = "${in.mdb.reconnect.attempts}")

})
@DeliveryActive(true)
//@DeliveryGroup(“group2”)
@ResourceAdapter("genericjms-xa")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class GenericRAOutQueue implements MessageListener {
    private static final Logger LOG = Logger.getLogger(GenericRAOutQueue.class);
    private static AtomicInteger mdbCnt = new AtomicInteger(1);

    private TextMessage textMessage = null;

    private int mdbID = 0;
    private int msgCnt = 0;
    private long startOnMessage = 0;
    private long finisOnMessage = 0;


    @Override
    public void onMessage(Message message) {
        startOnMessage = System.currentTimeMillis();
        try {

            if (LOG.isDebugEnabled()) {

                LOG.debugf("MDB[%s] Got message %s", mdbID, message.toString());

            }

            textMessage = (TextMessage) message;


            if (message instanceof TextMessage) {

                msgCnt++;

            }

        //} catch (JMSException jmsException) {

        } finally {
            finisOnMessage = System.currentTimeMillis();
            LOG.infof("MDB[%s] Message consumed in %d milliseconds",mdbCnt,(finisOnMessage - startOnMessage));

        }
    }

    @PostConstruct
    public void init(){

        mdbID = mdbCnt.getAndIncrement();

        LOG.infof("MDB[%d] Constructed",mdbID);

    }

    @PreDestroy
    public void cleanUp(){

        LOG.infof("MDB[%d] Shutting down.Processed %d messages.",mdbID,msgCnt);


    }

    private void doDelay(long delay){

        try {

            Thread.sleep(delay);

        } catch (InterruptedException interruptedException){

            LOG.warnf("MDB[%s] This should not happen",mdbID);

        }
    }
}
