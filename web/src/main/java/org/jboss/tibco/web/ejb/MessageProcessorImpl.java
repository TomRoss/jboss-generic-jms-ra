/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.tibco.web.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;

/**
 *
 * @author tomr
 */
@Stateless
public class MessageProcessorImpl implements MessageProcessorLocal{
    private static final Logger LOG = Logger.getLogger(MessageProcessorImpl.class);
    private static final Lock lock = new ReentrantLock();

    @Resource(name = "${tibco.external.context}")
    private InitialContext externalContext;

    @Resource(name = "${tibco.qcf}")
    private QueueConnectionFactory qcf;
    private QueueConnection queueConnection = null;

    private Queue queue;
    private QueueSession queueSession = null;
    private MessageProducer msgProducer = null;
    private QueueSender queueSender = null;
    private TextMessage txtMsg = null;
    private String queueName = null;
    private StringBuilder messageText = null;

    @Override
    public void sendMessages(String destiationName,String messageText,int messageNumber) throws JMSException, NamingException {

        try {

            if (LOG.isTraceEnabled()){

                LOG.tracef("sendMessages method called [destination='%s':message='%s':message count='%d'].",destiationName,messageText,messageNumber);
            }
            queueConnection = qcf.createQueueConnection();

            queueSession = queueConnection.createQueueSession(true, Session.SESSION_TRANSACTED);

            queue = getObject(queueName);

            queueSender = queueSession.createSender(queue);

            for (int i = 0; i < messageNumber; i++){

                this.messageText.append("This is message '" + i + "' with text - '" + messageText + "'.");

                sendMessage(this.messageText.toString());

                if (LOG.isDebugEnabled()){

                    LOG.debugf("Message sent.");
                }

                this.messageText.setLength(0);
            }

        } finally {

            if ( queueSender != null){
                queueSender.close();
            }

            if (queueSession != null){
                queueSession.close();
            }

            if (queueConnection != null){
                queueConnection.close();
            }
        }
    }

    private void sendMessage(String messageText) throws JMSException{

        txtMsg = queueSession.createTextMessage(messageText);

        queueSender.send(txtMsg);

        txtMsg = null;

    }

    @PostConstruct
    public void init(){

        messageText = new StringBuilder();
    }

    @PreDestroy
    public void cleanUp(){

    }

    public <T> T getObject(String url) throws NamingException {
        Object obj = null;

        LOG.debugf("Looking up JNDI name '" + url + "'.");

        try {

            lock.lock();

            if (externalContext != null){

                obj = externalContext.lookup(url);

            }

        } finally{

            if ( externalContext != null){

                externalContext.close();

            }

            lock.unlock();

        }

        return (T) obj;
    }
}
