package org.jboss.tibco.mdb;

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
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import java.net.URL;

/**
 * Created by tomr on 27/07/15.
 */


@MessageDriven(name = "GenericRAQueueMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/inQueue"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://aza:7222"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${tibco.qcf}"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "${tibco.user}"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "${tibco.password}"),
        @ActivationConfigProperty(propertyName = "reconnectAttempts", propertyValue = "15")

})
@DeliveryActive(true)
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

    private String outQueueName = "java:/jms/tibco/queue/outQueue";
    private Queue outQueue = null;
    private QueueConnection queueConnection = null;
    private QueueSender queueSender = null;
    private QueueSession queueSession = null;
    private TextMessage textMessage = null;
    private int mdbID = 0;
    private int msgCnt = 0;

    public GenericRAInQueue() {

        mdbID = mdbCnt.getAndIncrement();

    }

    @Override
    public void onMessage(Message message) {

        try {
            if (message instanceof TextMessage) {

                LOG.infof("MDB[%d] Got message - '%s'.",mdbID,message);

                textMessage = (TextMessage) message;

                textMessage.getText();

               /* if (LOG.isDebugEnabled()){

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
                    LOG.debugf("MDB[%d] Sending message '%s%'.",mdbID,message.toString());
                }

                queueSender.send(message);

                if (LOG.isDebugEnabled()){
                    LOG.debugf("MDB[%d] Message '%s% sent to queue '%s'.",mdbID,message.toString(),outQueue.getQueueName());
                }*/

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
        Context ctx = null;

        try {
            //Object o = externalContext.lookup("jms/cf/XAQueueConnectionFactory");

            //QueueConnectionFactory oq = (QueueConnectionFactory) o;

            //Properties env = new Properties();
            //java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://aza:7222"),
           // env.setProperty("java.naming.security.principal","");
            //env.setProperty("java.naming.security.credentials","");
            //env.setProperty("java.naming.factory.initial","com.tibco.tibjms.naming.TibjmsInitialContextFactory");
            //env.setProperty("java.naming.provider.url","tcp://");

            ctx = new InitialContext();

            Object obj = ctx.lookup(outQueueName);

            //URL url = javax.jms.Queue.class.getProtectionDomain().getCodeSource().getLocation();
            //Class c = obj.getClass().getSuperclass();

            outQueue = (Queue) obj;

            LOG.debugf("MDB[%d] Out destination found");

        } catch (NamingException namingException){

            LOG.errorf(namingException,"'MDB[%d] Error",mdbID);

        } finally {

            try {

                ctx.close();

            } catch (NamingException namingEx) {

                LOG.warnf(namingEx, "MDB[%d] Can't close initial context");

            }
        }

    }

    @PreDestroy
    public void cleanUp(){

        LOG.infof("MDB[%d] Shutting down.Processed %d messages.",mdbID,msgCnt);


    }
}
