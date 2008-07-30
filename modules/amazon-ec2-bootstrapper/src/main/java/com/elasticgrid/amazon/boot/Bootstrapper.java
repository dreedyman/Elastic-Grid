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

package com.elasticgrid.amazon.boot;

import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.ReservationDescription;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Bootstrapper in charge of fetching the EC2 launch parameters and generating the EG configuration files.
 * The launch parameters are available in the <tt>/tmp/user-data</tt> file.
 *
 * @author Jerome Bernard
 */
public class Bootstrapper {
    public static final String LAUNCH_PARAMETER_ACCESS_ID = "AWS_ACCESS_ID";
    public static final String LAUNCH_PARAMETER_SECRET_KEY = "AWS_SECRET_KEY";
    public static final String LAUNCH_PARAMETER_SQS_SECURED = "AWS_SQS_SECURED";

    public static final String EG_PARAMETER_ACCESS_ID = "aws.accessId";
    public static final String EG_PARAMETER_SECRET_KEY = "aws.secretKey";
    public static final String EG_PARAMETER_SQS_SECURED = "aws.sqs.secured";

    public static final String EG_GROUP_MONITOR = "eg-monitor";
    public static final String EG_GROUP_AGENT = "eg-agent";

    public static final String LAUNCH_PARAMETERS_FILE = "/tmp/user-data";
    public static final String ELASTIC_GRID_CONFIGURATION_FILE = "config/eg.properties";

    private String egHome = System.getProperty("EG_HOME");

    public Bootstrapper() throws IOException, EC2Exception {
        // retreive EC2 parameters
        Properties launchParameters = fetchLaunchParameters();
        Properties egParameters = translateProperties(launchParameters);

        // figure out who is the monitor host
        System.out.printf("Searching for Elastic Grid monitor host...\n");
        Jec2 ec2 = new Jec2(
                egParameters.getProperty(EG_PARAMETER_ACCESS_ID), egParameters.getProperty(EG_PARAMETER_SECRET_KEY));
        List<ReservationDescription> reservations = ec2.describeInstances(Collections.<String>emptyList());
        boolean monitorFound = false;
        for (ReservationDescription reservation : reservations) {
            List<String> groups = reservation.getGroups();
            if (groups.contains(EG_GROUP_MONITOR)) {
                // this is a monitor reservation -- instance
                List<ReservationDescription.Instance> instances = reservation.getInstances();
                if (instances.size() == 0)
                    continue;
                ReservationDescription.Instance monitor = null;
                for (ReservationDescription.Instance instance : instances) {
                    monitor = instances.get(0);
                    if ("running".equals(monitor.getState()))
                        break;
                }
                if (monitor == null) {
                    System.err.println("Could not find any monitor host. Aborting boot process.");
                    System.exit(-1);
                }
                if (instances.size() > 1) {
                    System.err.printf("More than once instance found (found %d). Using instance %s as monitor.\n",
                            instances.size(), monitor.getDnsName());
                }
                String monitorHost = monitor.getPrivateDnsName();
                if (monitorHost.equals(InetAddress.getByName("localhost").getHostName())) {
                    System.out.printf("This host is going to be the new monitor!");
                    monitorFound = true;
                } else {
                    System.out.printf("Using monitor host: %s\n", monitorHost);
                    egParameters.put("eg.monitor.host", monitorHost);
                    monitorFound = true;
                }
                FileUtils.writeStringToFile(new File(egHome + File.separator + "config", "monitor-host"), monitorHost);
            }
        }
        if (!monitorFound) {
            System.err.println("Could not find monitor host!");
            return;
        }

        // save configuration
        File file = saveConfiguration(egParameters);
        System.out.printf("Elastic Grid configuration file generated in '%s'\n", file.getAbsolutePath());
    }

    private Properties fetchLaunchParameters() throws IOException {
        Properties launchProperties = new Properties();
        InputStream launchFile = null;
        try {
            launchFile = new FileInputStream(new File(LAUNCH_PARAMETERS_FILE));
            launchProperties.load(launchFile);
            return launchProperties;
        } finally {
            IOUtils.closeQuietly(launchFile);
        }
    }

    private Properties translateProperties(Properties launchParameters) {
        // translate properties
        Properties egParameters = new Properties();
        for (Map.Entry property : launchParameters.entrySet()) {
            String key = (String) property.getKey();
            if (LAUNCH_PARAMETER_ACCESS_ID.equals(key))
                egParameters.put(EG_PARAMETER_ACCESS_ID, property.getValue());
            if (LAUNCH_PARAMETER_SECRET_KEY.equals(key))
                egParameters.put(EG_PARAMETER_SECRET_KEY, property.getValue());
            if (LAUNCH_PARAMETER_SQS_SECURED.equals(key))
                egParameters.put(EG_PARAMETER_SQS_SECURED, property.getValue());
        }
        return egParameters;
    }

    private File saveConfiguration(Properties egParameters) throws IOException {
        // write EG configuration
        if (egHome == null) {
            System.err.println("Could not find EG_HOME environment variable. Please fix this.");
            System.exit(-1);
        }
        File config = new File(egHome, ELASTIC_GRID_CONFIGURATION_FILE);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(config);
            egParameters.store(stream, "Elastic Grid Configuration File - Generated file, please do NOT edit!");
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return config;
    }

    public static void main(String[] args) throws IOException, EC2Exception {
        System.out.printf("Preparing Elastic Grid environment...\n");
        new Bootstrapper();
    }
}
