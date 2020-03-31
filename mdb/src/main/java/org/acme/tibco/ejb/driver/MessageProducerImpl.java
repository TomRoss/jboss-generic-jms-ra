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
import javax.jms.*;
import javax.enterprise.concurrent.ManagedExecutorService;

import org.jboss.logging.Logger;

import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class MessageProducerImpl implements MessageProducer {
    private static final Logger LOG = Logger.getLogger(MessageProducerImpl.class);
    public static final int MESSAGE_COUNT = 100;

    @Resource(name = "${tibco.jms.context.fqn}")
    private QueueConnectionFactory ccf;

    @Resource(name = "${tibco.in.queue.fqn}")
    private Queue inQueue;

    @Resource(name = "${tibco.test.topic.fqn}")
    private Topic testTopic;

    @Resource(name = "${tibco.in.topic.fqn}")
    private Topic inTopic;

    @Resource(name = "DefaultManagedExecutorService")
    private ManagedExecutorService mes;

    private AtomicInteger nonDurableMessagesTotal = new AtomicInteger(0);
    private AtomicInteger durableMessagesTotal = new AtomicInteger(0);
    private AtomicInteger messagesTotal = new AtomicInteger(0);

    @Schedule(second = "*/30", minute = "*", hour = "*", info = "MyTimer", persistent = false)
    public void sendMessges() {

        mes.submit(new SendMessages());
        mes.submit(new PublishMessages());
        mes.submit(new PublishDurableMessages());

    }

    class SendMessages implements Runnable {

        TextMessage textMessage = null;

        @Override
        public void run() {

            try (JMSContext jmsContext = ccf.createContext(Session.AUTO_ACKNOWLEDGE)) {

                JMSProducer jmsProducer = jmsContext.createProducer();

                for (int i = 0; i < MESSAGE_COUNT; i++) {

                    textMessage = jmsContext.createTextMessage("Simple test message");

                    jmsProducer.send(inQueue, textMessage);

                    messagesTotal.incrementAndGet();
                }

            } catch (JMSRuntimeException jmsRuntimeException) {

                LOG.errorf(jmsRuntimeException, "ERROR - JMSException");

            } catch (Exception ex) {

                LOG.errorf(ex, "ERROR- Caught general exception");

            } finally {

                try {

                    LOG.infof("Sent messages sent to queue '%s'", inQueue.getQueueName());

                } catch (JMSException jmsException) {

                    LOG.warnf("Can't get queue name");
                }
            }
        }
    }

    class PublishMessages implements Runnable {
        TextMessage textMessage = null;

        @Override
        public void run() {

            try (JMSContext jmsContext = ccf.createContext(Session.AUTO_ACKNOWLEDGE)) {

                JMSProducer jmsProducer = jmsContext.createProducer();

                for (int i = 0; i < MESSAGE_COUNT; i++) {

                    textMessage = jmsContext.createTextMessage("Simple test message");

                    jmsProducer.send(testTopic, textMessage);

                    nonDurableMessagesTotal.incrementAndGet();
                }

            } catch (JMSRuntimeException jmsRuntimeException) {

                LOG.errorf("Error publishing messages to topic");

            } finally {
                try {

                    LOG.infof("Published messages on topic '%s'", testTopic.getTopicName());

                } catch (JMSException jmsException) {

                    LOG.warnf("Can't get topic name");

                }
            }
        }
    }

    class PublishDurableMessages implements Runnable {
        TextMessage textMessage = null;

        @Override
        public void run() {

            try (JMSContext jmsContext = ccf.createContext(Session.AUTO_ACKNOWLEDGE)) {

                JMSProducer jmsProducer = jmsContext.createProducer();

                for (int i = 0; i < MESSAGE_COUNT; i++) {

                    textMessage = jmsContext.createTextMessage("Simple test message");

                    jmsProducer.send(inTopic, textMessage);

                    durableMessagesTotal.incrementAndGet();

                }

            } catch (JMSRuntimeException jmsRuntimeException) {

                LOG.errorf("Error publishing messages to topic");

            } finally {

                try {

                    LOG.infof("Published messages on topic '%s'", inTopic.getTopicName());

                } catch (JMSException jmsException) {

                    LOG.warnf("Can't get topic name");

                }
            }
        }
    }


    @PostConstruct
    public void init() {
        LOG.info("Creating Producer Bean.");
    }

    @PreDestroy
    public void cleanUp() {

        LOG.info("Destroying Sender Bean.");

        LOG.infof("Sent or published - messages = %d, non-durable messages = %d, durable messages = %d", messagesTotal.get(), nonDurableMessagesTotal.get(), durableMessagesTotal.get());

        if (!mes.isShutdown()) {

            LOG.info("Shutting down the executor service.");

            mes.shutdown();

        }

    }
}
