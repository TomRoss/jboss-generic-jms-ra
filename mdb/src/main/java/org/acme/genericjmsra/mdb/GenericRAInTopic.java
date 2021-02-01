package org.acme.genericjmsra.mdb;

import org.jboss.ejb3.annotation.DeliveryActive;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.MessageDriven;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tomr on 09/11/2015.
 */

@MessageDriven(name = "GenericRATopicMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/topic/inTopic"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.security.principal=${tibco.user};java.naming.security.credentials=${tibco.password};java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://aza:7222"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${tibco.tcf}"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "${mdb.user}"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "${mdb.password}")

})
@DeliveryActive(true)
@ResourceAdapter("genericjms-xa")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class GenericRAInTopic implements MessageListener {
    private static final Logger LOG = Logger.getLogger(GenericRAInTopic.class);
    private static AtomicInteger mdbCnt = new AtomicInteger(1);

    @Resource(name = "${tibco.tcf.fqn}}")
    private TopicConnectionFactory topicConnectionFactory;

    @Resource(name = "${tibco.external.context}")
    private Context externalContext;

    private String  outTopicName = "java:/tibco/jms/topic/outTopic";
    @Resource(name = "${tibco.out.topic.fqn}")
    private Topic outTopic;
    private TopicConnection topicConnection = null;
    private TopicPublisher topicPublisher = null;
    private TopicSession topicSession = null;



    private TextMessage txtMsg = null;
    private int mdbID = 0;
    private int msgCnt = 0;

    public GenericRAInTopic() {

        mdbID = mdbCnt.getAndIncrement();

    }

    @Override
    public void onMessage(Message message) {

        try {
            if (message instanceof TextMessage) {

                LOG.infof("MDB[%d] Got message - '%s'.",mdbID,message);

                txtMsg = (TextMessage) message;

                txtMsg.getText();

                topicConnection = topicConnectionFactory.createTopicConnection();

                topicSession = topicConnection.createTopicSession(true, Session.SESSION_TRANSACTED);

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Creating publisher on topic '%s'.",mdbID,outTopic.getTopicName());
                }

                topicPublisher = topicSession.createPublisher(outTopic);

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Publisher on topic '%s' created.",mdbID,outTopic.getTopicName());
                }

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] Sending message '%s' to topic '%s'",mdbID,message,outTopic.getTopicName());
                }

                topicPublisher.send(message);

                if (LOG.isDebugEnabled()){

                    LOG.debugf("MDB[%d] message '%s' sent to topic '%s'",mdbID,message,outTopic.getTopicName());
                }

                msgCnt++;
            }

        } catch (JMSException jmsException) {

            LOG.errorf("MDB[%d] JMSException - ",mdbID, jmsException);

        } finally{

            try {

                if(LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Closing topic publisher.",mdbID);

                if (topicPublisher != null)
                    topicPublisher.close();

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Topic publisher closed.",mdbID);

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Clsoing topic session.");

                if (topicSession != null)
                    topicSession.close();

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Topic session closed.",mdbID);

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Closing topic connection.",mdbID);

                if (topicConnection != null)
                    topicConnection.close();

                if (LOG.isTraceEnabled())
                    LOG.tracef("MDB[%d] Topic connection closed.",mdbID);

            } catch (JMSException jmsException){

                LOG.errorf(jmsException,"Cleaning up JMS resource." , mdbID);

                throw new RuntimeException(jmsException);
            }

        }
    }

    @PostConstruct
    public void init(){

        try {

            outTopic = (Topic) externalContext.lookup(outTopicName);

        } catch (NamingException namingException) {

            LOG.error(namingException);

        } finally {

            try {
                externalContext.close();
            } catch (NamingException namingException){

            }
        }
    }

    @PreDestroy
    public void cleanUp(){

        LOG.infof("MDB[%d] Shutting down.Processed %d messages.",mdbID,msgCnt);


    }

}


