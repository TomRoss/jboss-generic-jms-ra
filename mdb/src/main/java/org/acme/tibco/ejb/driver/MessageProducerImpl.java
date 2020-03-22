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

package org.acme.tibco.ejb.driver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.QueueSession;
import javax.jms.TopicSession;
import javax.jms.TopicPublisher;
import javax.jms.QueueSender;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.enterprise.concurrent.ManagedExecutorService;

import org.jboss.logging.Logger;

@Singleton
public class MessageProducerImpl implements MessageProducer{
    private static final Logger LOG = Logger.getLogger(MessageProducerImpl.class);
    public static final int MESSAGE_COUNT = 100;

    @Resource(name= "${tibco.qcf.fqn}")
    private QueueConnectionFactory qcf;
    @Resource(name= "${tibco.tcf.fqn}")
    private TopicConnectionFactory tcf;
    private QueueConnection queueConnection = null;
    private TopicConnection topicConnection = null;
    @Resource(name = "${tibco.in.queue.fqn}")
    private Queue queue;
    @Resource(name = "${tibco.in.topic.fqn}")
    private Topic topic;
    private QueueSender queueSender = null;
    private QueueSession queueSession = null;
    private TopicSession topicSession = null;
    private TopicPublisher topicPublisher = null;

    @Resource(name = "DefaultManagedExecutorService")
    private ManagedExecutorService mes;


    @Schedule(second = "*/45", minute = "*", hour = "*", info = "MyTimer", persistent = false)
    public void sendMessges(){

        mes.submit(new SendMessages());
        mes.submit(new PublishMessages());

    }

    class SendMessages implements Runnable{

        TextMessage textMessage = null;

        @Override
        public void run() {

            try{

                queueConnection = qcf.createQueueConnection();

                queueSession = queueConnection.createQueueSession(true,Session.SESSION_TRANSACTED);

                for (int i = 0; i < MESSAGE_COUNT; i++) {

                    textMessage = queueSession.createTextMessage();

                    queueSender = queueSession.createSender(queue);

                    queueSender.send(textMessage);

                    textMessage = null;
                }
            } catch (JMSException jmsException) {

                LOG.errorf(jmsException,"");

            } catch (Exception ex){

                LOG.errorf(ex,"");

            } finally {

                try {
                    if (queueSender != null) {
                        queueSender.close();
                        queueSender = null;
                    }

                    if (queueSession != null) {
                        queueSession.close();
                        queueSession = null;
                    }

                    if (queueConnection != null) {
                        queueConnection.close();
                    }
                } catch (JMSException jmsException) {

                    LOG.warnf(jmsException,"");
                }
            }
        }
    }

    class PublishMessages implements Runnable{
        TextMessage textMessage = null;
        @Override
        public void run() {

            try {

                topicConnection = tcf.createTopicConnection();

                topicSession = topicConnection.createTopicSession(true,Session.SESSION_TRANSACTED);

                for (int i =0 ; i < MESSAGE_COUNT; i++) {

                    textMessage = topicSession.createTextMessage();

                    topicPublisher = topicSession.createPublisher(topic);

                    topicPublisher.publish(textMessage);

                    textMessage = null;
                }

            } catch (JMSException jmsException) {

                LOG.errorf(jmsException,"");

            } finally {

                try {
                    if (topicPublisher != null) {
                        topicPublisher.close();
                        topicPublisher = null;
                    }

                    if (topicSession != null) {
                        topicSession.close();
                        topicSession = null;
                    }

                    if (topicConnection != null) {
                        topicConnection.close();
                    }

                } catch (JMSException jmsException) {

                    LOG.warnf(jmsException,"");
                }
            }
        }
    }


    @PostConstruct
    public void init(){
        LOG.info("Creating Producer Bean.");
    }

    @PreDestroy
    public void cleanUp(){

        if (!mes.isShutdown()){

            LOG.info("Shutting down the executor service.");

            mes.shutdown();

        }

        LOG.info("Destroying Sender Bean.");

    }
}
