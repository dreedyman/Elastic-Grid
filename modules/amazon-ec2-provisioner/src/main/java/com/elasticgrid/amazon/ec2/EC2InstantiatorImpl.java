/**
 * Copyright (C) 2007-2008 Elastic Grid, LLC.
 * 
 * Licensed under the GNU Lesser General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elasticgrid.amazon.ec2;

import com.elasticgrid.grid.ec2.InstanceType;
import com.elasticgrid.model.NodeProfile;
import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.ReservationDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static java.lang.String.format;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EC2InstantiatorImpl implements EC2Instantiator {
    private Jec2 jec2;
    private static final Logger logger = Logger.getLogger(EC2Instantiator.class.getName());

    public List<String> startInstances(String imageID, int minCount, int maxCount, List<String> groupSet, String userData, String keyName, boolean publicAddress, Object... options) throws RemoteException {
        InstanceType instanceType = (InstanceType) options[0];
        logger.log(Level.FINER, "Starting {0} Amazon EC2 instance from image ''{1}'': keyName={2}, groups={3}, userdata={4}, instanceType={5}",
                new Object[] { minCount, imageID, keyName, groupSet, userData, instanceType });
        com.xerox.amazonws.ec2.InstanceType type;
        switch (instanceType) {
            case SMALL:
                type = com.xerox.amazonws.ec2.InstanceType.DEFAULT;
                break;
            case MEDIUM_HIGH_CPU:
                type = com.xerox.amazonws.ec2.InstanceType.MEDIUM_HCPU;
                break;
            case LARGE:
                type = com.xerox.amazonws.ec2.InstanceType.LARGE;
                break;
            case EXTRA_LARGE:
                type = com.xerox.amazonws.ec2.InstanceType.XLARGE;
                break;
            case EXTRA_LARGE_HIGH_CPU:
                type = com.xerox.amazonws.ec2.InstanceType.XLARGE_HCPU;
                break;
            default:
                throw new IllegalArgumentException(format("Invalid Amazon EC2 instance type '%s'", instanceType.getName()));
        }
        try {
            ReservationDescription reservation = jec2.runInstances(imageID, minCount, maxCount, groupSet, userData, keyName, type);
            List<ReservationDescription.Instance> instances = reservation.getInstances();
            ReservationDescription.Instance last = instances.get(instances.size() - 1);
            try {
                while (last.isPending()) {
                    Thread.sleep(200);
                    reservation = jec2.describeInstances(Arrays.asList(last.getInstanceId())).get(0);
                    last = reservation.getInstances().get(instances.size() - 1);
                }
            } catch (InterruptedException e) {
                String message = format("Couldn't start properly %d Amazon EC2 instances." +
                        "Make sure instances of reservation ID %s are started and used properly within your grid",
                        minCount, reservation.getReservationId());
                logger.log(Level.SEVERE, message, e);
                throw new RemoteException(message, e);
            }
            List<String> instancesIDs = new ArrayList<String>(instances.size());
            for (ReservationDescription.Instance instance : instances) {
                instancesIDs.add(instance.getInstanceId());
            }
            logger.log(Level.INFO, "Started {0} Amazon EC2 instance from image ''{1}''...", new Object[] { minCount, imageID });
            return instancesIDs;
        } catch (EC2Exception e) {
            throw new RemoteException("Can't start Amazon EC2 instances", e);
        }
    }

    public void shutdownInstance(String instanceID) throws RemoteException {
        logger.info(format("Shutting down Amazon EC2 instance '%s'...", instanceID));
        try {
            jec2.terminateInstances(Arrays.asList(instanceID));
        } catch (EC2Exception e) {
            throw new RemoteException(format("Can't stop Amazon instance '%s'", instanceID), e);
        }
    }

    public List<String> getGroupsNames() throws RemoteException {
        List<GroupDescription> groups = null;
        try {
            groups = jec2.describeSecurityGroups(new String[] {});
        } catch (EC2Exception e) {
            throw new RemoteException("Can't get list of groups names", e);
        }
        List<String> groupNames = new ArrayList<String>(groups.size());
        for (GroupDescription group : groups) {
            groupNames.add(group.getName());
        }
        return groupNames;
    }

    public void createGridGroup(String gridName) throws RemoteException {
        try {
            jec2.createSecurityGroup("elastic-grid-cluster-" + gridName, "Grid " + gridName);
        } catch (EC2Exception e) {
            throw new RemoteException("Can't create security group 'elastic-grid-cluster-" + gridName + "'", e);
        }
    }

    public void createProfileGroup(NodeProfile profile) throws RemoteException {
        try {
            jec2.createSecurityGroup(profile.toString(), "Elastic Grid Node Profile");
        } catch (EC2Exception e) {
            throw new RemoteException("Can't create security group '" + profile.toString() + "'", e);
        }
    }

    @Autowired
    public void setJec2(Jec2 jec2) {
        this.jec2 = jec2;
    }
}
