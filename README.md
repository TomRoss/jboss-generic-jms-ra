# jboss-generic-jms-ra
JBoss (EAP 7) Generic JMS RA TIBCO EMS integration example

This example assumes that you have a TIBCO EMS broker running and provider module has been added to the JBoss EAP instance.
The example uses `tibco.properties` (file below) to point a external TIBCO broker
 ~~~
# tibco.properties
external.jms.host=localhost
external.jms.port=7222
external.jms.username=admin
external.jms.password=quick123+
arjuna.node.id=example-111

amq.jms.support-bytes-id=false

psql.host=localhost
psql.port=5432
psql.db=jbossdb
psql.user=jboss
psql.password=jboss
psql.url=jdbc:postgresql://localhost:5432/jbossdb

#
# Possible values
# OFF, INFO, DEBUG
#
psql.log.level=10
jdbc.pool.size.min=10
jdbc.pool.size.max=30
jboss.as.log.level=INFO

artemis.ra.client.global.thread.pool.max.size=250
artemis.ra.client.global.scheduled.thread.pool.core.size=250
~~~
To deploy this example in JBoss EAP 7 follow the steps
- create quickuser
~~~
  cd $JBOSS_HOME/bin
  ./add-user.sh -a -u quickuser -p quick123+ -g guest
~~~  
- create standalone-tibco instance
~~~
  cd $JBOSS_HOME 
  cp -rp standalone standalone-tibco
~~~  
- add provider module to the generic JMS RA
  locate `genericjms` used by the JBoss instance
  for examlpe
~~~
  cd $JBOSS_HOME
  find . -name 'generic*'
  ./modules/system/layers/base/org/jboss/genericjms
  ./modules/system/layers/base/org/jboss/genericjms/main/generic-jms-ra-jar-2.0.1.Final-redhat-1.jar
  ./modules/system/layers/base/.overlays/layer-base-jboss-eap-7.2.7.CP/org/jboss/genericjms
  ./modules/system/layers/base/.overlays/layer-base-jboss-eap-7.2.7.CP/org/jboss/genericjms/main/generic-jms-ra-jar-2.0.2.Final-redhat-00001.jar
  cd ./modules/system/layers/base/.overlays/layer-base-jboss-eap-7.2.7.CP/org/jboss/genericjms
  mkdir -p provider/main
  cd provider/main
~~~
   - create `module.xml` file 
~~~
  <?xml version='1.0' encoding='UTF-8'?>
  <module xmlns="urn:jboss:module:1.5" name="org.jboss.genericjms.provider">
      <resources>
          <resource-root path="tibjms.jar"/>
  	<resource-root path="tibrvjms.jar"/>
  	<resource-root path="tibjmsapps.jar"/>
  	<resource-root path="tibjmsadmin.jar"/>
  	<resource-root path="tibemsd_sec.jar"/>
      </resources>
      <dependencies>
          <module name="javax.api"/>
          <module name="javax.jms.api"/>
           <module name="org.jboss.logging" />
           <module name="org.jboss.jts" />
      </dependencies>
  </module>
~~~
 - add TIBCO runtime jars to the provider module. Those are provided by TIBCO and do not come with JBoss EAP.
  
- start JBoss instance with command 
~~~
  ./standalone.sh -Djboss.server.base.dir=$JBOSS_HOME/standalone-tibco --server-config=standalone-full.xml --properties=tibco.properties
~~~ 
- add TIBCO configuration to EAP (tibco.cli file is located in hte project's resource directory)
~~~
  jboss-cli.sh --connect --file=tibco.cli
~~~  
- deploy ear file
~~~
  mvn clean package wildfly:deploy  
~~~


