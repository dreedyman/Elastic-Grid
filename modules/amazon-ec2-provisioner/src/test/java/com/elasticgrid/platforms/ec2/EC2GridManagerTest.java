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

package com.elasticgrid.platforms.ec2;

import com.elasticgrid.cluster.NodeInstantiator;
import com.elasticgrid.model.ClusterAlreadyRunningException;
import com.elasticgrid.model.ClusterException;
import com.elasticgrid.model.NodeProfile;
import com.elasticgrid.model.ec2.impl.EC2NodeImpl;
import com.elasticgrid.model.ec2.EC2Node;
import com.elasticgrid.platforms.ec2.discovery.EC2ClusterLocator;
import org.easymock.EasyMock;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class EC2GridManagerTest {
    private EC2CloudPlatformManager cloudPlatformManager;
    private NodeInstantiator<EC2Node> mockEC2;
    private EC2ClusterLocator mockLocator;

    @Test(expectedExceptions = ClusterAlreadyRunningException.class)
    public void testStartingARunningGrid() throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
        EasyMock.expect(mockLocator.findNodes("test"))
                .andReturn(null);
        EasyMock.expect(mockEC2.startInstances(null, 1, 1, Arrays.asList("elastic-grid-cluster-test", "eg-monitor", "elastic-grid"),
                "CLUSTER_NAME=test,AWS_ACCESS_ID=null,AWS_SECRET_KEY=null,AWS_SQS_SECURED=true",
                null, true, InstanceType.SMALL))
                .andReturn(null);
        EasyMock.expect(mockEC2.getGroupsNames())
                .andReturn(Arrays.asList("elastic-grid-cluster-test", "eg-monitor", "eg-agent", "elastic-grid"))
                .times(3);
        EasyMock.expect(mockLocator.findNodes("test"))
                .andReturn(Arrays.asList(new EC2NodeImpl(NodeProfile.MONITOR).instanceID("123")));
        EasyMock.replay(mockEC2);
        org.easymock.classextension.EasyMock.replay(mockLocator);
        cloudPlatformManager.startCluster("test", 1, 0);
        cloudPlatformManager.startCluster("test", 1, 0);
    }

    @BeforeTest
    @SuppressWarnings("unchecked")
    public void setUpClusterManager() {
        cloudPlatformManager = new EC2CloudPlatformManager();
        mockEC2 = EasyMock.createMock(NodeInstantiator.class);
        mockLocator = org.easymock.classextension.EasyMock.createMock(EC2ClusterLocator.class);
        cloudPlatformManager.setNodeInstantiator(mockEC2);
        cloudPlatformManager.setClusterLocator(mockLocator);
    }

    @AfterTest
    public void verifyMocks() {
        EasyMock.verify(mockEC2);
        org.easymock.classextension.EasyMock.verify(mockLocator);
        EasyMock.reset(mockEC2);
        org.easymock.classextension.EasyMock.reset(mockLocator);
    }
}