/**
 * Elastic Grid
 * Copyright (C) 2008-2009 Elastic Grid, LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.elasticgrid.platforms.lan;

import com.elasticgrid.cluster.spi.CloudPlatformManager;
import com.elasticgrid.model.ClusterException;
import com.elasticgrid.model.NodeProfileInfo;
import com.elasticgrid.model.lan.LANCluster;
import com.elasticgrid.platforms.lan.discovery.LANClusterLocator;
import com.sun.jini.start.LifeCycle;
import org.rioproject.core.jsb.ServiceBeanContext;
import org.rioproject.jsb.ServiceBeanActivation;
import org.rioproject.jsb.ServiceBeanAdapter;
import org.rioproject.watch.GaugeWatch;
import org.rioproject.watch.SamplingWatch;
import org.rioproject.watch.PeriodicWatch;
import org.rioproject.watch.Calculable;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cloud Platform Manager for the LAN.
 * @author Jerome Bernard
 */
public class LANCloudPatformManagerJSB extends ServiceBeanAdapter implements CloudPlatformManager<LANCluster> {
    private LANCloudPlatformManager cloud;

    /**
     * Component name we use to find items in the Configuration
     */
    static final String EG_CONFIG_COMPONENT = "com.elasticgrid";
    /**
     * Component name we use to find items in the Configuration
     */
    static final String CONFIG_COMPONENT = EG_CONFIG_COMPONENT + ".platforms.lan";
    /**
     * The componant name for accessing the service's configuration
     */
    static String configComponent = CONFIG_COMPONENT;
    /**
     * Logger name
     */
    static final String LOGGER = "com.elasticgrid.platforms.lan";
    /**
     * Cluster Manager logger.
     */
    static final Logger logger = Logger.getLogger(LOGGER);

    /**
     * Create a {@link CloudPlatformManager} launched from the ServiceStarter framework
     *
     * @param configArgs Configuration arguments
     * @param lifeCycle  The LifeCycle object that started the REST API
     * @throws Exception if bootstrapping fails
     */
    public LANCloudPatformManagerJSB(String[] configArgs, LifeCycle lifeCycle) throws Exception {
        super();
        bootstrap(configArgs);
    }

    /**
     * Get the ServiceBeanContext and bootstrap the {@link CloudPlatformManager}.
     */
    protected void bootstrap(String[] configArgs) throws Exception {
        try {
            context = ServiceBeanActivation.getServiceBeanContext(
                    getConfigComponent(),
                    "LAN Cloud Platform Manager",
                    configArgs,
                    getClass().getClassLoader());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Getting ServiceElement", e);
            throw e;
        }
        try {
            start(context);
            ServiceBeanActivation.LifeCycleManager lMgr =
                    (ServiceBeanActivation.LifeCycleManager) context.getServiceBeanManager().getDiscardManager();
            if (lMgr != null) {
                lMgr.register(getServiceProxy(), context);
            } else {
                logger.log(Level.WARNING, "LifeCycleManager is null, unable to register");
            }
            PeriodicWatch clustersWatch = new PeriodicWatch("# of Clusters") {
                public void checkValue() {
                    int nbClusters = 0;
                    try {
                        nbClusters = cloud.findClusters().size();
                        addWatchRecord(new Calculable(id, nbClusters, System.currentTimeMillis()));
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Could not compute the number of running clusters", e);
                    }
                }
            };
            context.getWatchRegistry().register(clustersWatch);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Register to LifeCycleManager", e);
            throw e;
        }
    }

    @Override
    public void initialize(ServiceBeanContext context) throws Exception {
        super.initialize(context);
        cloud = (LANCloudPlatformManager) new LANCloudPlatformManagerFactory().getInstance();
    }

    @Override
    protected Object createProxy() {
        return new LANCloudPlatformManagerProxy((CloudPlatformManager<LANCluster>) getExportedProxy(), getUuid());
    }

    @Override
    public void advertise() throws IOException {
        super.advertise();
        logger.info("Advertised Private LAN Cloud Platform");
    }

    /**
     * Get the component name to use for accessing the services configuration properties
     *
     * @return The component name
     */
    public static String getConfigComponent() {
        return configComponent;
    }

    public String getName() throws RemoteException {
        return cloud.getName();
    }

    public void startCluster(String clusterName, List<NodeProfileInfo> clusterTopology)
            throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
        cloud.startCluster(clusterName, clusterTopology);
    }

    public void stopCluster(String clusterName) throws ClusterException, RemoteException {
        cloud.stopCluster(clusterName);
    }

    public Collection<LANCluster> findClusters() throws ClusterException, RemoteException {
        return cloud.findClusters();
    }

    public LANCluster cluster(String name) throws RemoteException, ClusterException {
        return cloud.cluster(name);
    }

    public void resizeCluster(String clusterName, List<NodeProfileInfo> clusterTopology)
            throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
        cloud.resizeCluster(clusterName, clusterTopology);
    }

    public void setClusterLocator(LANClusterLocator clusterLocator) {
        cloud.setClusterLocator(clusterLocator);
    }
}
