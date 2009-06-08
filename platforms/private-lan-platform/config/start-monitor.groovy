/*
 * This configuration is used by the com.sun.jini.start utility to start
 * Elastic Grid ProvisionMonitor, Webster and a Jini Lookup Service
 */

import com.elasticgrid.boot.ServiceDescriptorUtil
import org.rioproject.config.Component
import com.sun.jini.start.ServiceDescriptor

@Component ('com.sun.jini.start')
class StartMonitorConfig {

  ServiceDescriptor[] getServiceDescriptors() {
    String jiniHome = System.getProperty('JINI_HOME')
    String egHome = System.getProperty('EG_HOME')

    def websterRoots = [jiniHome + '/lib-dl', ';',
            jiniHome + '/lib', ';',
            egHome + '/lib', ';',
            egHome + '/deploy']

    String policyFile = egHome + '/policy/policy.all'

    def serviceDescriptors = [
            /* Webster, set to serve up 4 directories */
            ServiceDescriptorUtil.getWebster(policyFile, '9010', (String[]) websterRoots),
            /* Jini Lookup Service */
            ServiceDescriptorUtil.getLookup(policyFile, getLookupConfigArgs(egHome)),
            /* Elastic Grid Provision Monitor */
            ServiceDescriptorUtil.getMonitor(policyFile, getMonitorConfigArgs(egHome)),
            /* Elastic Grid Private LAN Cloud Platform */
            ServiceDescriptorUtil.getPrivateLANCloudPlatform(policyFile, getPrivateLANCloudPlatformConfigArgs(egHome)),
            /* Elastic Grid Amazon EC2 Cloud Platform */
            ServiceDescriptorUtil.getEC2CloudPlatform(policyFile, getEC2CloudPlatformConfigArgs(egHome)),
            /* Elastic Grid Cluster Manager */
            ServiceDescriptorUtil.getClusterManager(policyFile, getClusterManagerConfigArgs(egHome)),
            /* Elastic Grid REST API */
            ServiceDescriptorUtil.getRestApi(policyFile, getRestConfigArgs(egHome))
    ]

    return (ServiceDescriptor[]) serviceDescriptors
  }

  String[] getMonitorConfigArgs(String egHome) {
    return ["${egHome}/config/monitor.groovy"] as String[]
  }

  String[] getPrivateLANCloudPlatformConfigArgs(String egHome) {
    return ["${egHome}/config/private-lan-cloud-platform.groovy"] as String[]
  }

  String[] getEC2CloudPlatformConfigArgs(String egHome) {
    return ["${egHome}/config/amazon-ec2-cloud-platform.groovy"] as String[]
  }

  String[] getClusterManagerConfigArgs(String egHome) {
    return ["${egHome}/config/cluster-manager.groovy"] as String[]
  }

  String[] getRestConfigArgs(String egHome) {
    return ["${egHome}/config/rest-api.groovy"] as String[]
  }

  String[] getLookupConfigArgs(String egHome) {
    return ["${egHome}/config/reggie.groovy"] as String[]
  }

}