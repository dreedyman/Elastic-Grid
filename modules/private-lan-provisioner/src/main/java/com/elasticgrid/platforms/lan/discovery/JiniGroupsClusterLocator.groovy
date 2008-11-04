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
import com.elasticgrid.platforms.lan.discovery.LANClusterLocator
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.util.logging.Logger
import org.rioproject.core.provision.ServiceBeanInstantiator
import org.rioproject.core.provision.ServiceStatement
import org.rioproject.monitor.ProvisionMonitor
import org.springframework.stereotype.Service
import org.rioproject.resources.client.JiniClient
import net.jini.lookup.LookupCache
import net.jini.discovery.LookupDiscoveryManager
import net.jini.lookup.ServiceDiscoveryManager
import net.jini.core.discovery.LookupLocator
import net.jini.lease.LeaseRenewalManager
import net.jini.core.lookup.ServiceTemplate
import org.rioproject.cybernode.Cybernode
import net.jini.config.Configuration
import net.jini.config.ConfigurationProvider
import net.jini.core.lookup.ServiceItem
import net.jini.core.entry.Entry
import org.rioproject.cybernode.CybernodeAdmin
import org.rioproject.core.ServiceElement
import org.rioproject.core.ServiceBeanConfig

/**
 * {@ClusterLocator} based on EC2 Security Groups, as described on Elastic Grid Blog post:
 * http://blog.elastic-grid.com/2008/06/30/how-to-do-some-service-discovery-on-amazon-ec2/
 */
@Service("clusterLocator")
class JiniGroupsClusterLocator extends LANClusterLocator {
//    def Jec2 ec2
//    def Map<String, Cluster> oldClusterDefinitions = new HashMap<String, Cluster>()
//    public static final String EG_GROUP_MONITOR = "eg-monitor"
//    public static final String EG_GROUP_AGENT = "eg-agent"
    def JiniClient jiniClient
    def ServiceDiscoveryManager sdm
    def LookupCache monitorsCache
    def LookupCache cybernodesCache
    private static final Logger logger = Logger.getLogger(JiniGroupsClusterLocator.class.name)

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
        // retrieve the list of instances running
//        logger.log Level.INFO, "Searching for Elastic Grid nodes in cluster '$clusterName'..."
//        List<ReservationDescription> reservations
//        try {
//            reservations = ec2.describeInstances(Collections.emptyList())
//        } catch (EC2Exception e) {
//            throw new ClusterException("Can't locate cluster $clusterName", e)
//        }
        // filter nodes which are not part of the cluster
//        def clusterReservation = reservations.findAll { ReservationDescription reservation ->
//            reservation.groups.contains "elastic-grid-cluster-$clusterName" as String
//        }
        // build of list of all the instances
//        List nodes = clusterReservation.collect { ReservationDescription reservation ->
//            reservation.instances.findAll { it.isRunning() }.collect { ReservationDescription.Instance instance ->
//                boolean monitor = reservation.groups.contains(NodeProfile.MONITOR.toString())
//                boolean agent = reservation.groups.contains(NodeProfile.AGENT.toString())
//                def profile = null;
//                if (!agent && !monitor) {
//                    logger.log Level.WARNING, "Instance ${instance.instanceId} has no Elastic Grid profile!"
//                    return
//                } else if (agent && monitor) {
//                    logger.log Level.WARNING,
//                            "Instance ${instance.instanceId} is both a monitor and an agent. Using it as a monitor!"
//                    profile = NodeProfile.MONITOR
//                } else if (monitor) {
//                    profile = NodeProfile.MONITOR
//                } else if (agent) {
//                    profile = NodeProfile.AGENT
//                }
//                return new EC2NodeImpl(profile)
//                        .instanceID(instance.instanceId)
//                        .address(InetAddress.getByName(instance.dnsName))
//            }
//        }.flatten()
//        logger.log Level.INFO, "Found ${nodes.size()} nodes in cluster '$clusterName'..."
        // notify listeners of potential cluster topology changes
//        Cluster<EC2Node> cluster = new EC2ClusterImpl(name: clusterName).addNodes(nodes)
//        if (oldClusterDefinitions.containsKey(clusterName)) {
//            Cluster old = oldClusterDefinitions.get(clusterName)
//            if (!old.equals(cluster)) {
//                oldClusterDefinitions.put(clusterName, cluster)
//                fireClusterChangedEvent new ClusterChangedEvent(this, cluster)
//            }
//        } else {
//            oldClusterDefinitions.put(clusterName, cluster)
//            fireClusterChangedEvent new ClusterChangedEvent(this, cluster)
//        }
//        fireClusterChangedEvent new ClusterChangedEvent(this, cluster)
//        return nodes
        return null
    }

    public LANNode findMonitor(String clusterName) throws ClusterMonitorNotFoundException {
//        logger.log Level.INFO, "Searching for monitor node in cluster '$clusterName'..."
//        def List<EC2Node> nodes = findNodes(clusterName)
//        def found = false
//        def node = nodes.find { NodeProfile.MONITOR == it.profile}
//        if (node)
//            return node
//        else
//            throw new ClusterMonitorNotFoundException(clusterName)
        return null
    }

    private ProvisionMonitor findLocalMonitor() {
        Registry registry = LocateRegistry.getRegistry()
        return (ProvisionMonitor) registry.lookup("Monitor")
    }

}