# jboss-generic-jms-ra
A very simple JBoss Generic JMS RA TIBCO EMS integration example.

To build the project run `mvn clean package`

Before deploy project in jboss EAP:

`cd $JBOSS_HOME`

`cp -rp standalone standalone-tibco`

`cd $JBOSS_HOME/bin`

`./standalone.sh -Djboss.server.base.dir=$JBOSS_HOME/standalone-tibco --server-config=standalone-full.xml`

`cd $PROJECT_HOME`

`cd mdb/src/main/resource/META-INF`

`jboss-cli.sh --connect --file=setup.cli`

`cd $PROJECT_HOME`

`mvn clean package wildfly:deploy`

At this stage the clients should be visible on htequeues in TIBCO admin console

~~~
tcp://aza:7222> show queues
All Msgs            Persistent Msgs
Queue Name                        SNFGXIBCT  Pre  Rcvrs     Msgs    Size        Msgs    Size
$sys.admin                        +--------    5*     0        0     0.0 Kb        0     0.0 Kb
$sys.lookup                       ---------    5*     0        0     0.0 Kb        0     0.0 Kb
$sys.redelivery.delay             +--------    5*     0        0     0.0 Kb        0     0.0 Kb
$sys.undelivered                  +--------    5*     0        0     0.0 Kb        0     0.0 Kb
* $TMP$.EMS-SERVER.E0460ED6B523.1   ---------    5      1        0     0.0 Kb        0     0.0 Kb
  inQueue                           ---------    5*     1        0     0.0 Kb        0     0.0 Kb
  outQueue                          ---------    5*     1        0     0.0 Kb        0     0.0 Kb
  sourceQueue                       ---------    5*     0        0     0.0 Kb        0     0.0 Kb
  targetQueue                       ---------    5*     0        2     0.7 Kb        2     0.7 Kb
  testQueue                         ---------    5*     0        0     0.0 Kb        0     0.0 Kb
~~~


