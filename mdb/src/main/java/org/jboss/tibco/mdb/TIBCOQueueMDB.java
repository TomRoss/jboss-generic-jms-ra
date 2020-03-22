package org.jboss.tibco.mdb;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tomr on 27/07/15.
 */


@MessageDriven(name = "TibcoQueueMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "${tibco.in.queue}"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://${tibco.host}:${tibco.port}"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${tibco.qcf}"),
        @ActivationConfigProperty(propertyName = "reconnectAttempts",propertyValue = "15"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "${tibco.user}"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "${tibco.password}")

})
@ResourceAdapter("genericjms-xa")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class TIBCOQueueMDB implements MessageListener {
    private static final Logger LOG = Logger.getLogger(TIBCOQueueMDB.class);
    private static AtomicInteger mdbCnt = new AtomicInteger(1);

    @Resource(name = "${tibco.qcf}")
    private QueueConnectionFactory queueConnectionFactory;

    @Resource(lookup = "${tibco.external.context}")
    private InitialContext externalContext;
    private String outQueueName = "java:/tibco/jms/queue/outQueue";
    private Queue outQueue;
    private QueueConnection queueConnection = null;
    private QueueSender queueSender = null;
    private QueueSession queueSession = null;
    private TextMessage textMessage = null;
    private int mdbID = 0;
    private int msgCnt = 0;

    public TIBCOQueueMDB() {

        mdbID = mdbCnt.getAndIncrement();

    }

    @Override
    public void onMessage(Message message) {

        try {
            if (message instanceof TextMessage) {

                LOG.infof("MDB[%d] Got message - '%s'.",mdbID,message);

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Createing connection.",mdbID);
                }

                queueConnection = queueConnectionFactory.createQueueConnection();

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
                    LOG.debugf("MDB[%d] Sending message '%s%'.",message.toString());
                }

                queueSender.send(message);

                if (LOG.isDebugEnabled()){
                    LOG.debugf("MDB[%d] Message '%s% sent to queue '%s'.",message.toString(),outQueue.getQueueName());
                }

                msgCnt++;
            }

        } catch (JMSException jmsException) {

            LOG.errorf(jmsException,"MDB[%d] JMSException",mdbID);

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
                    LOG.tracef("MDB[%d] Closing queue connection.",mdbID);

                if (queueConnection != null)
                    queueConnection.close();

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Queue connection closed.",mdbID);

            } catch (JMSException jmsException){

                LOG.errorf(jmsException,"MDB[%d] Cleaning up JMS resource %s." ,mdbID);
            }

        }
    }

    @PostConstruct
    public void init(){

       LOG.infof("MDB[%d] Looking up %s.",mdbID,outQueueName);

        try {

            outQueue = (Queue) externalContext.lookup(outQueueName);

        } catch (NamingException namingException) {

            LOG.errorf(namingException,"MDB[%d] Error looking up JNDI name '%s'.",mdbID,outQueueName);

        } finally {

            try {

                externalContext.close();

            } catch (NamingException namingException){

                LOG.warnf(namingException,"MDB[%d] Error while closing exteranl context.",mdbID);
            }

        }
    }

    @PreDestroy
    public void cleanUp(){

        LOG.infof("MDB[%d] Shutting down.Processed %d messages.",mdbID,msgCnt);

        mdbCnt.decrementAndGet();

    }
}
