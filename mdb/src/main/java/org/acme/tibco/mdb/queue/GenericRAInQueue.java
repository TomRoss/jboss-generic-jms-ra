package org.acme.tibco.mdb.queue;


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

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.MapMessage;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.Context;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tomr on 27/07/15.
 */


@MessageDriven(name = "GenericRAInQueueMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "${tibco.in.queue}"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://aza:7222"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${tibco.qcf}"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${tibco.in.queue.maxSession}"),
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

    @Resource(name = "${tibco.ccf.fqn}")
    private QueueConnectionFactory connectionFactory;


    @Resource(name = "${tibco.external.context}")
    private Context externalContext;

    @Resource
    private MessageDrivenContext mdbCtx;
    @Resource(name = "${tibco.out.queue.fqn}")
    //private TemporaryQueue tempQueue = null;
    private Destination tempQueue = null;
    private MapMessage mapMessage = null;
    private int mdbID = 0;
    private int msgCnt = 0;

    public GenericRAInQueue() {

    }

    @Override
    public void onMessage(Message message) {

        try {
            if (message instanceof MapMessage) {

                if (LOG.isDebugEnabled()) {
                    LOG.infof("MDB[%d] Got message - '%s'.", mdbID, message);
                }

                mapMessage = (MapMessage) message;

                try (JMSContext context = connectionFactory.createContext(Session.SESSION_TRANSACTED)) {

                    JMSProducer jmsProducer = context.createProducer();

                    tempQueue = mapMessage.getJMSReplyTo();

                    jmsProducer.send(tempQueue, mapMessage);

                    msgCnt++;
                }

                if (LOG.isInfoEnabled()) {
                    LOG.infof("MDB[%d] Message '%s' sent to destiantion '%s'", mdbID, mapMessage.toString(), tempQueue.toString());
                }
            }

        } catch (JMSException jmsException) {

            LOG.errorf(jmsException, "MDB[%d] ERROR - JMSException - ", mdbID);

            mdbCtx.setRollbackOnly();

            throw new RuntimeException(jmsException);

        } catch (Exception ex) {

            mdbCtx.setRollbackOnly();

            LOG.errorf(ex, "MDB[%d] ERROR - Generic exception", mdbID);

        }

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
