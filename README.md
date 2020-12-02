This is a simple example demonstrating a deployment of MDBs on JBoss EAP listening on a queue and a topic in TIBCO EMS. The project contains 3 MDB that can be deployed in JBoss EAP 7.3 that connect to TIBCO EMS system (ver 8.5). The project also contains JBoss CLI to configure JBoss Generic JMS RA and JBoss standalone-full.xml file for reference. 

Before we start we need to create a properties file that contain all the TIBCO references
~~~
# tibco.properties
external.jms.host={tibco host here}
external.jms.port={tibco ems port}
# the project uses user quickuser with password quick123+
# an alternative tibco username/password can be supplied
external.jms.username=quickuser
external.jms.password=quick123+
arjuna.node.id=tibco-111
~~~
To deploy project first it is necessary to create JBoss EAP instance to host it.
~~~
cd $JBOSS_HOME
cp -rp standalone standalone-generic-ra
~~~
Start JBoss EAP instance with `tibco.proeprties` file
~~~
cd $JBOSS_HOME/bin
./standalone.sh -Djboss.server.base.dir=$JBOSS_HOME/standalone-generic-ra --server-config=standalone-full.xml --properties=tibco.properties
~~~
Run CLI script to configure the EAP instance
~~~
jboss-cli.sh --connect --file=generic-ra-tibco.cli 
~~~
To deploy the project 
~~~
cd $PROJECT_DIR
unzip generic-jms-ra.zip 
cd generic-jms-ra
mvn clean package wildfly:deploy 
~~~
Tibco EMS comes with a sample of simple Java apps that can be used to test if the project was deployed correctly. It is assumed that TIBCO sample bundle has been installed and compiled. The command below will send a message to inQueue on TIBCO broker running on `localhost`
~~~
java tibjmsMsgProducer -server tcp://localhost:7222 -queue inQueue -user quickuser -password quick123+  "hello there"
~~~
If the project was deployed correctly then the following output should appear in the `generic-ra.log` in the `$JBOSS_HOME/standalone-generic-ra/log`.
~~~
2020-12-02 10:10:18,717 INFO  org.acme.genericjmsra.mdb.GenericRAInQueue:(default-threads - 4) MDB[1] Constructed
2020-12-02 10:10:18,722 INFO  org.acme.genericjmsra.mdb.GenericRAInQueue:(default-threads - 4) MDB[1] Got message - 'TextMessage={ Header={ JMSMessageID={ID:EMS-SERVER.C055FC74D9D429:1} JMSDestination={Queue[inQueue]} JMSReplyTo={null} JMSDeliveryMode={PERSISTENT} JMSRedelivered={false} JMSCorrelationID={null} JMSType={null} JMSTimestamp={Wed Dec 02 10:10:18 GMT 2020} JMSDeliveryTime={Wed Dec 02 10:10:18 GMT 2020} JMSExpiration={0} JMSPriority={4} } Properties={ JMSXDeliveryCount={Integer:1} } Text={hello there} }'.
2020-12-02 10:10:18,765 INFO  org.acme.genericjmsra.mdb.GenericRAInQueue:(default-threads - 4) MDB[1] Ceated connection in 41 milliseconds
2020-12-02 10:10:18,854 INFO  org.acme.genericjmsra.mdb.GenericRAOutQueue:(default-threads - 5) MDB[1] Constructed
2020-12-02 10:10:18,855 INFO  org.acme.genericjmsra.mdb.GenericRAOutQueue:(default-threads - 5) MDB[1] Got message TextMessage={ Header={ JMSMessageID={ID:EMS-SERVER.C055FC74D9D42D:9} JMSDestination={Queue[outQueue]} JMSReplyTo={null} JMSDeliveryMode={PERSISTENT} JMSRedelivered={false} JMSCorrelationID={null} JMSType={null} JMSTimestamp={Wed Dec 02 10:10:18 GMT 2020} JMSDeliveryTime={Wed Dec 02 10:10:18 GMT 2020} JMSExpiration={0} JMSPriority={4} } Properties={ JMSXDeliveryCount={Integer:1} } Text={hello there} }
~~~

