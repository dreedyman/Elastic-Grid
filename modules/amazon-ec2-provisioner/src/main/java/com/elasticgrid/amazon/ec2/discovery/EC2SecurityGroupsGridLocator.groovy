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

package com.elasticgrid.amazon.ec2.discovery

import com.elasticgrid.model.GridException
import com.elasticgrid.model.GridNotFoundException
import com.elasticgrid.model.NodeProfile
import com.elasticgrid.model.ec2.EC2Node
import com.elasticgrid.model.ec2.impl.EC2NodeImpl
import com.xerox.amazonws.ec2.EC2Exception
import com.xerox.amazonws.ec2.Jec2
import com.xerox.amazonws.ec2.ReservationDescription
import java.util.logging.Level
import java.util.logging.Logger
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import com.elasticgrid.model.GridMonitorNotFoundException
import com.elasticgrid.grid.discovery.AbstractGridLocator
import com.elasticgrid.grid.discovery.GridChangedEvent
import com.elasticgrid.model.Grid
import com.elasticgrid.model.ec2.impl.EC2GridImpl

/**
 * {@GridLocator} based on EC2 Security Groups, as described on Elastic Grid Blog post:
 * http://blog.elastic-grid.com/2008/06/30/how-to-do-some-service-discovery-on-amazon-ec2/
 */
@Service("gridLocator")
class EC2SecurityGroupsGridLocator extends EC2GridLocator {
    def Jec2 ec2
    def Map<String, Grid> oldGridDefinitions = new HashMap<String, Grid>()
    public static final String EG_GROUP_MONITOR = "eg-monitor"
    public static final String EG_GROUP_AGENT = "eg-agent"
    private static final Logger logger = Logger.getLogger(EC2GridLocator.class.name)

    public List<String> findGrids() {
        logger.info "Searching for all grids..."
        List<ReservationDescription> reservations
        try {
            reservations = ec2.describeInstances(Collections.emptyList())
        } catch (EC2Exception e) {
            throw new GridException("Can't locate grids", e)
        }
        // extract grid names
        def Set grids = new HashSet()
        reservations.each { ReservationDescription reservation ->
            reservation.groups.each { String group ->
                if (group.startsWith("elastic-grid-cluster-")) {
                    if (reservation.instances.any { it.isRunning() })
                        grids << group.substring("elastic-grid-cluster-".length(), group.length())
                }
            }
        }
        return grids as List;
    }

    public List<EC2Node> findNodes(String gridName) throws GridNotFoundException, GridException {
        // retrieve the list of instances running
        logger.log Level.INFO, "Searching for Elastic Grid nodes in grid '$gridName'..."
        List<ReservationDescription> reservations
        try {
            reservations = ec2.describeInstances(Collections.emptyList())
        } catch (EC2Exception e) {
            throw new GridException("Can't locate grid $gridName", e)
        }
        // filter nodes which are not part of the grid
        def gridReservation = reservations.findAll { ReservationDescription reservation ->
            reservation.groups.contains "elastic-grid-cluster-$gridName" as String
        }
        // build of list of all the instances
        List nodes = gridReservation.collect { ReservationDescription reservation ->
            reservation.instances.findAll { it.isRunning() }.collect { ReservationDescription.Instance instance ->
                boolean monitor = reservation.groups.contains(NodeProfile.MONITOR.toString())
                boolean agent = reservation.groups.contains(NodeProfile.AGENT.toString())
                def profile = null;
                if (!agent && !monitor) {
                    logger.log Level.WARNING, "Instance ${instance.instanceId} has no Elastic Grid profile!"
                    return
                } else if (agent && monitor) {
                    logger.log Level.WARNING,
                            "Instance ${instance.instanceId} is both a monitor and an agent. Using it as a monitor!"
                    profile = NodeProfile.MONITOR
                } else if (monitor) {
                    profile = NodeProfile.MONITOR
                } else if (agent) {
                    profile = NodeProfile.AGENT
                }
                return new EC2NodeImpl(profile)
                        .instanceID(instance.instanceId)
                        .address(InetAddress.getByName(instance.dnsName))
            }
        }.flatten()
        logger.log Level.INFO, "Found ${nodes.size()} nodes in grid '$gridName'..."
        // notify listeners of potential grid topology changes
        Grid<EC2Node> grid = new EC2GridImpl(name: gridName).addNodes(nodes)
        if (oldGridDefinitions.containsKey(gridName)) {
            Grid old = oldGridDefinitions.get(gridName)
            if (!old.equals(grid)) {
                oldGridDefinitions.put(gridName, grid)
                fireGridChangedEvent new GridChangedEvent(this, grid)
            }
        } else {
            oldGridDefinitions.put(gridName, grid)
            fireGridChangedEvent new GridChangedEvent(this, grid)
        }
        fireGridChangedEvent new GridChangedEvent(this, grid)
        return nodes
    }

    public EC2Node findMonitor(String gridName) throws GridMonitorNotFoundException {
        logger.log Level.INFO, "Searching for monitor node in grid '$gridName'..."
        def List<EC2Node> nodes = findNodes(gridName)
        def found = false
        def node = nodes.find { NodeProfile.MONITOR == it.profile}
        if (node)
            return node
        else
            throw new GridMonitorNotFoundException(gridName)
    }

    @Autowired
    public void setEc2(Jec2 ec2) {
        this.ec2 = ec2;
    }
}