package org.acme.tibco.mdb;



import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.ejb3.annotation.DeliveryActive;
import org.jboss.ejb3.annotation.ResourceAdapter;

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

import org.acme.tibco.ejb.DBManager;

/**
 * Created by tomr on 27/07/15.
 */


@MessageDriven(name = "GenericRAQueueMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "${tibco.in.queue}"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://aza:7222"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${tibco.qcf}"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${tibco.in.maxSession}"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "${tibco.user}"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "${tibco.password}"),
        @ActivationConfigProperty(propertyName = "reconnectAttempts", propertyValue = "15")

})

@DeliveryActive(value = true)
//@DeliveryGroup(“group2”)
@ResourceAdapter("genericjms-xa")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class GenericRAInQueue implements MessageListener {
    private static final Logger LOG = Logger.getLogger(GenericRAInQueue.class);
    private static AtomicInteger mdbCnt = new AtomicInteger(1);

    @Resource(name = "${tibco.qcf.fqn}")
    private QueueConnectionFactory queueConnectionFactory;

    @Resource(name = "${tibco.external.context}")
    private Context externalContext;

    @Resource(lookup = "java:global/jboss-generic-jmsra-ear-1.0/jboss-generic-jms-ra-mdb-1.0/DBManagerImpl")
    private DBManager ejb;
    @Resource
    private MessageDrivenContext mdbCtx;
    @Resource(name = "${tibco.out.queue.fqn}")
    private Queue outQueue = null;
    private QueueConnection queueConnection = null;
    private QueueSender queueSender = null;
    private QueueSession queueSession = null;
    private TextMessage textMessage = null;
    private int mdbID = 0;
    private int msgCnt = 0;

    public GenericRAInQueue() {

    }

    @Override
    public void onMessage(Message message) {

        try {
            if (message instanceof TextMessage) {

                LOG.infof("MDB[%d] Got message - '%s'.",mdbID,message);

                textMessage = (TextMessage) message;

                textMessage.getText();

                try {

                    ejb.insertRecord(mdbID, "queue","inserted");

                } catch (Exception ex){

                    LOG.errorf(ex,"MDB[%d] Error in SQL");

                }

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Createing connection.",mdbID);
                }

                queueConnection = queueConnectionFactory.createQueueConnection("admin","quick123+");

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Connection created.",mdbID);
                }

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Createing session.",mdbID);
                }

                queueSession = queueConnection.createQueueSession(true, Session.SESSION_TRANSACTED);

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Session created.",mdbID);
                }


                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Creating Queue Sender.",mdbID);
                }

                queueSender = queueSession.createSender(outQueue);

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Queue Sender created.",mdbID);
                }


                if (LOG.isDebugEnabled()){
                    LOG.debugf("MDB[%d] Sending message '%s'.",mdbID,message.toString());
                }

                queueSender.send(message);

                if (LOG.isDebugEnabled()){
                    LOG.debugf("MDB[%d] Message '%s sent to queue '%s'.",mdbID,message.toString(),outQueue.getQueueName());
                }

                msgCnt++;
            }

        } catch (JMSException jmsException) {

            LOG.errorf(jmsException,"MDB[%d] JMSException - ",mdbID);

            throw new RuntimeException(jmsException);

        } finally {

            try {

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Closing queue sender.",mdbID);

                if (queueSender != null)
                    queueSender.close();

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] QueueSender closed.",mdbID);

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Closing queue session.",mdbID);

                if (queueSession != null)
                    queueSession.close();

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] QueueSession closed.",mdbID);


                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Clsoing queue connection.",mdbID);

                if (queueConnection != null)
                    queueConnection.close();

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Queue connection closed.",mdbID);

            } catch (JMSException jmsException){

                LOG.warnf(jmsException,"[%d] Cleaning up JMS resource.",mdbID);
            }

        }
    }

    @PostConstruct
    public void init(){

        mdbID = mdbCnt.getAndIncrement();

        LOG.infof("MDB[%d] MDB created. MDB count %d ",mdbID,mdbCnt.get());

    }


    @PreDestroy
    public void cleanUp(){

        LOG.infof("MDB[%d] Shutting down.Processed %d messages.",mdbID,msgCnt);

        mdbCnt.decrementAndGet();

    }
}
