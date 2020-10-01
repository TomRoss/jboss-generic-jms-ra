package org.acme.genericjmsra.mdb;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.ejb.EJB;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import javax.ejb.TransactionManagement;

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

import org.acme.genericjmsra.util.MessageRecord;
import org.acme.genericjmsra.ejb.DBManager;
import org.acme.genericjmsra.ejb.DBManagerImpl;

/**
 * Created by tomr on 27/07/15.
 */


@MessageDriven(name = "GenericRAInQueueMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/inQueue"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://aza:7222"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${tibco.qcf}"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "${tibco.user}"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "${tibco.password}"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${in.mdb.maxsession}"),
        @ActivationConfigProperty(propertyName = "reconnectAttempts", propertyValue = "${in.mdb.reconnect.attempts}")

})
@DeliveryActive(true)
//@DeliveryGroup(“group2”)
@ResourceAdapter("genericjms-xa")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class GenericRAInQueue implements MessageListener {
    private static final Logger LOG = Logger.getLogger(GenericRAInQueue.class);
    private static AtomicInteger mdbCnt = new AtomicInteger(1);

    @Resource(lookup = "${tibco.qcf.fqn}")
    private QueueConnectionFactory queueConnectionFactory;

    @Resource(lookup= "${tibco.external.context}")
    private Context externalContext;

    @Resource(lookup = "${tibco.out.queue.fqn}")
    private Queue outQueue = null;
    private QueueConnection queueConnection = null;
    private QueueSender queueSender = null;
    private QueueSession queueSession = null;
    private TextMessage textMessage = null;

    @EJB(beanName = "DBManager")
    private DBManager ejb;

    private int mdbID = 0;
    private int msgCnt = 0;

    private MessageRecord msgRecord = null;
    private String messageUUID = null;

    public GenericRAInQueue() {


    }

    @Override
    public void onMessage(Message message) {
        long startonMessage = 0;

        if (LOG.isTraceEnabled()) {
            startonMessage = System.currentTimeMillis();
        }

        try {
            if (message instanceof TextMessage) {

                if (LOG.isTraceEnabled()) {
                    LOG.tracef("MDB[%d] Got message - '%s'.", mdbID, message);
                }

                textMessage = (TextMessage) message;

                textMessage.getText();

               if (LOG.isTraceEnabled()){

                    LOG.tracef("MDB[%d] Createing connection.",mdbID);
                }

               long start = System.currentTimeMillis();

                queueConnection = queueConnectionFactory.createQueueConnection("admin","quick123+");

                long finish = System.currentTimeMillis();

                LOG.infof("MDB[%d] Ceated connection in %d milliseconds",mdbID,(finish-start));

                if (LOG.isTraceEnabled()){

                    LOG.tracef("MDB[%d] Connection created.",mdbID);
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
                    LOG.debugf("MDB[%d] Sending message '%s%'.",mdbID,message.toString());
                }

                queueSender.send(message);

                if (LOG.isDebugEnabled()){
                    LOG.debugf("MDB[%d] Message '%s% sent to queue '%s'.",mdbID,message.toString(),outQueue.getQueueName());
                }

                msgRecord = new MessageRecord(message.getJMSMessageID(),"InQueueMDB" + mdbID, textMessage.getText());

                ejb.insertRecord(msgRecord);

                msgRecord = null;

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

            } finally {

                if (LOG.isTraceEnabled()){
                    long finishonMessage = System.currentTimeMillis();
                    LOG.tracef("MDB[%s] onMessage method completed in %d milliseconds",mdbID,(finishonMessage - startonMessage));
                }

            }

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
