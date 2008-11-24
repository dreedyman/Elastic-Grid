/**
 * Copyright (C) 2007-2008 Elastic Grid, LLC.
 *
 * This file is part of Elastic Grid.
 *
 * Elastic Grid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * Elastic Grid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Elastic Grid.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.elasticgrid.platforms.lan.discovery

import com.elasticgrid.model.ClusterException
import com.elasticgrid.model.ClusterMonitorNotFoundException
import com.elasticgrid.model.ClusterNotFoundException
import com.elasticgrid.model.lan.LANNode
import com.elasticgrid.model.lan.impl.LANNodeImpl
import com.elasticgrid.platforms.lan.discovery.LANClusterLocator
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.util.logging.Logger
import net.jini.config.Configuration
import net.jini.config.ConfigurationProvider
import net.jini.core.discovery.LookupLocator
import net.jini.core.lookup.ServiceItem
import net.jini.core.lookup.ServiceTemplate
import net.jini.discovery.LookupDiscoveryManager
import net.jini.lease.LeaseRenewalManager
import net.jini.lookup.LookupCache
import net.jini.lookup.ServiceDiscoveryManager
import net.jini.lookup.entry.Host
import org.rioproject.core.ServiceBeanConfig
import org.rioproject.core.ServiceElement
import org.rioproject.cybernode.Cybernode
import org.rioproject.cybernode.CybernodeAdmin
import org.rioproject.monitor.ProvisionMonitor
import org.rioproject.resources.client.JiniClient
import org.springframework.stereotype.Service
import com.elasticgrid.model.NodeProfile
import com.elasticgrid.model.Application
import com.elasticgrid.model.internal.ApplicationImpl

/**
 * {@ClusterLocator}  based on EC2 Security Groups, as described on Elastic Grid Blog post:
 * http://blog.elastic-grid.com/2008/06/30/how-to-do-some-service-discovery-on-amazon-ec2/
 */
@Service
class JiniGroupsClusterLocator extends LANClusterLocator {
  def JiniClient jiniClient
  def ServiceDiscoveryManager sdm
  def LookupCache monitorsCache
  def LookupCache cybernodesCache
  private final Logger logger = Logger.getLogger(JiniGroupsClusterLocator.getClass().getName())

  public JiniGroupsClusterLocator() {
    String[] groups = JiniClient.parseGroups(System.getProperty('org.rioproject.groups', "all"));
    LookupLocator[] locators = JiniClient.parseLocators(System.getProperty('org.rioproject.locator'));
    Configuration config = ConfigurationProvider.getInstance(['-'] as String[])
    jiniClient = new JiniClient(new LookupDiscoveryManager(groups, locators, null, config));
    ServiceTemplate monitors = new ServiceTemplate(null, [ProvisionMonitor.class] as Class[], null);
    ServiceTemplate cybernodes = new ServiceTemplate(null, [Cybernode.class] as Class[], null);
    sdm = new ServiceDiscoveryManager(jiniClient.getDiscoveryManager(), new LeaseRenewalManager(), config);
    monitorsCache = sdm.createLookupCache(monitors, null, null);
    cybernodesCache = sdm.createLookupCache(cybernodes, null, null);
  }

  public List<String> findClusters() {
    logger.info "Searching for all clusters..."
    ServiceItem[] items = cybernodesCache.lookup(null, Integer.MAX_VALUE)
    def Set clusters = new HashSet()
    items.each { ServiceItem item ->
      def CybernodeAdmin cybernode = item.service.admin
      def ServiceElement serviceElement = cybernode.serviceElement
      def ServiceBeanConfig config = serviceElement.serviceBeanConfig
      config.groups.each { clusters << it }
    }
    logger.info "Found clusters $clusters"
    return clusters as List;
  }

  public List<LANNode> findNodes(String clusterName) throws ClusterNotFoundException, ClusterException {
    logger.info "Searching for Elastic Grid nodes in cluster '$clusterName'..."

    def ServiceItem[] agentsItems = cybernodesCache.lookup(new CybernodeGroupFilter(clusterName), Integer.MAX_VALUE);
    def ServiceItem[] monitorsItems = monitorsCache.lookup(new MonitorGroupFilter(clusterName), Integer.MAX_VALUE);

    def List<LANNode> nodes = new ArrayList<LANNode>()
    monitorsItems.each { ServiceItem item ->
      def attributes = item.attributeSets
      def Host hostEntry = (Host) attributes.find { it instanceof Host }
      nodes << new LANNodeImpl()
              .instanceID(item.serviceID.toString())
              .profile(NodeProfile.MONITOR)
              .address(InetAddress.getByName(hostEntry.hostName))
    }
    agentsItems.each { ServiceItem item ->
      def attributes = item.attributeSets
      def Host hostEntry = (Host) attributes.find { it instanceof Host }
      def matches = agentsItems.findAll { it.attributeSets.find { it instanceof Host } == hostEntry }
      if (matches.size() > 1)
        nodes << new LANNodeImpl()
                .instanceID(item.serviceID.toString())
                .profile(NodeProfile.AGENT)
                .address(InetAddress.getByName(hostEntry.hostName))
    }
    logger.info "Found nodes $nodes"
    return nodes as List;
  }

  public LANNode findMonitor(String clusterName) throws ClusterMonitorNotFoundException {
    logger.info "Searching for monitor node in cluster '$clusterName'..."
    def ServiceItem[] monitorsItems = monitorsCache.lookup(new MonitorGroupFilter(clusterName), Integer.MAX_VALUE);
    ServiceItem item = (ServiceItem) monitorsItems[0]
    def attributes = item.attributeSets
    def Host hostEntry = (Host) attributes.find { it instanceof Host }
    return (LANNode) new LANNodeImpl()
            .instanceID(item.serviceID.toString())
            .profile(NodeProfile.MONITOR)
            .address(InetAddress.getByName(hostEntry.hostName))
  }

  public List<? extends Application> findApplications (String clusterName) throws ClusterException {
    def applications = [
            new ApplicationImpl().name('test'),
            new ApplicationImpl().name('hello')
    ]
    return applications as List
  }

  private ProvisionMonitor findLocalMonitor() {
    Registry registry = LocateRegistry.getRegistry()
    return (ProvisionMonitor) registry.lookup("Monitor")
  }

}