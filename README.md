## Testing Cellar Configuration Replication with Apache Aries Blueprint

Issue reproduction steps and samples for use with Karaf 4.1.1 and Cellar 4.1.0 regarding config sync.

#### Setup

1. Download the Karaf 4.1.1 zip and unzip two separate distributions. 
Name them `karaf-4.1.1_C1` and `karaf-4.1.1_C2` respectively. 

1. From the `configs/etc` directory of this repo, drop `karaf.management.cfg` and `karaf.shell.cfg`
into the `etc` directory of **only** `karaf-4.1.1_C2`. 

1. In two separate terminal windows, boot up both instances of Karaf. For C2's terminal session, 
if `KARAF_DEBUG` is set, you have two options:
    * Run `unset KARAF_DEBUG`
    * Modify the debug port in relevant scripts found in `<KARAF_HOME>/bin`
    
1. On both nodes, do the following:
   1. download and deploy [org.apache.felix.utils-1.10.0.jar](http://search.maven.org/remotecontent?filepath=org/apache/felix/org.apache.felix.utils/1.10.0/org.apache.felix.utils-1.10.0.jar)
      before installing Cellar. 
   1. Run `feature:repo-add cellar`
   1. Run `feature:install cellar`
   1. In `etc/org.apache.karaf.cellar.node.cfg`, set the `config.listener` property to `true`
   1. Run `log:set TRACE org.apache.karaf.cellar.config`
   
   _Note: Do a `bundle:list` and check if the cellar bundle for feature sync got installed.
   If not, you likely forget to deploy `org.apache.felix.utils-1.10.0.jar` for one or both of the
   nodes. Omitting this step breaks Cellar: feature sync won't work because the bundle won't install,
   hence the synchronizer won't be found, and configurations will not replicate. 

1. In this repo, consider the `blueprint.xml` file under `src/main/resources/OSGI-INF/blueprint`. 
Build this project twice: 
   1. Once using the managed-service-factory configuration with the `<bean>` and `<service>` 
   commented out **(default)**.
   1. Again using the singleton service configuration with `<cm:managed-service-factory ...>`
   commented out. 
   
   _Note: It is highly recommended that the JARs are renamed appropriately. Our artifacts were:_
      * `cellar-msf-testing-1.0-FACTORY`
      * `cellar-msf-testing-1.0-SINGLETON`
  
#### Testing the Singleton Service Configuration Replication

This validates that basic service configuration replication works as expected. 

1. Drop the _SINGLETON_ artifact into both nodes' `deploy` directories.
1. On both nodes, ensure the bundle is _Active_ in `bundle:list`, then tail the logs. 
1. An INFO log should appear approximately once every 5 seconds. 
The name should be "NAME_FROM_BLUEPRINT". 
1. Consider the config file in this repo: `configs/service-singleton/com.connexta.cellar.LogLoopImpl.cfg`,
which contains a name property set to "NAME_FROM_CONFIG". 
   1. **Only on node C1**, drop a copy of the config into `etc`.
   1. **Verify on both nodes** that the INFO log changed from "NAME_FROM_BLUEPRINT" to "NAME_FROM_CONFIG".
   1. **Verify on both nodes** that `etc/com.connexta.cellar.LogLoopImpl.cfg` is present and has
   name="NAME_FROM_CONFIG".
   1. **Only on node C2**, change the name in the config file to something else (i.e. 
   "ANOTHER_NAME_FROM_CONFIG"). Save the file. 
   1. **Verify on both nodes** that the INFO log changed from "NAME_FROM_CONFIG" to 
   "ANOTHER_NAME_FROM_CONFIG".
   1. **Verify on both nodes** that `etc/com.connexta.cellar.LogLoopImpl.cfg` is present and has
      name="ANOTHER_NAME_FROM_CONFIG".

#### Testing the Managed-Service-Factory Configuration Replication

This validates that factory configuration replication **does not** work as expected. 

1. Remove the _SINGLETON_ artifact from both nodes' `deploy` directories and ensure they no longer
show up in `bundle:list`. 
1. Drop the _FACTORY_ artifact into both nodes' `deploy` directories. 
1. Consider the config file in this repo: `configs/service-factory/com.connexta.cellar.LogLoopImpl-01.cfg`,
which contains a name property set to "NAME_FROM_CONFIG_01". 
   1. Create several copies incrementing the numeric components of the filename and internal name property. 
   1. **Only on node C1**, drop config file `01` into `etc`.
   1. **Verify on node C2** that the service instance **was not** replicated, as no logs are being generated. 
   1. **Only on node C2**, drop config file `02` into `etc`. 
   1. **Verify on node C1** that the service instance **was not** replicated, as only logs from 
   config file `01` are visible. 
   
   _Note: As of this repo's current state, deleting the configs from `etc` or even stopping/uninstalling
   the bundle will not clean up the logging threads. Karaf needs to be rebooted._