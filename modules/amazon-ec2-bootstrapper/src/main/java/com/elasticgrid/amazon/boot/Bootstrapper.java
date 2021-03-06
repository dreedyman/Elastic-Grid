/**
 * Elastic Grid
 * Copyright (C) 2008-2010 Elastic Grid, LLC.
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
package com.elasticgrid.amazon.boot;

import com.elasticgrid.config.EC2Configuration;
import com.elasticgrid.model.Discovery;
import com.xerox.amazonws.ec2.EC2Exception;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Bootstrapper in charge of fetching the EC2 launch parameters and generating the EG configuration files.
 * The launch parameters are available in the <tt>/tmp/user-data</tt> file.
 *
 * @author Jerome Bernard
 */
public class Bootstrapper {
    public static final String LAUNCH_PARAMETER_CLUSTER_NAME = "CLUSTER_NAME";
    public static final String LAUNCH_PARAMETER_DROP_BUCKET = "DROP_BUCKET";
    public static final String LAUNCH_PARAMETER_OVERRIDES_URL = "OVERRIDES_URL";
    public static final String LAUNCH_PARAMETER_YUM_PACKAGES = "YUM_PACKAGES";
    public static final String LAUNCH_PARAMETER_ACCESS_ID = "AWS_ACCESS_ID";
    public static final String LAUNCH_PARAMETER_SECRET_KEY = "AWS_SECRET_KEY";
    public static final String LAUNCH_PARAMETER_EC2_SECURED = "AWS_EC2_SECURED";
    public static final String LAUNCH_PARAMETER_SQS_SECURED = "AWS_SQS_SECURED";
    public static final String LAUNCH_PARAMETER_EC2_KEYPAIR = "AWS_EC2_KEYPAIR";
    public static final String LAUNCH_PARAMETER_EC2_AMI32 = "AWS_EC2_AMI32";
    public static final String LAUNCH_PARAMETER_EC2_AMI64 = "AWS_EC2_AMI64";

    public static final String LAUNCH_PARAMETERS_FILE = "/tmp/user-data";
    public static final String ELASTIC_GRID_CONFIGURATION_FILE = "eg.properties";

    private String egHome = System.getProperty("EG_HOME");
    private String egConfig = egHome + File.separatorChar + "config";

    public Bootstrapper() throws IOException, EC2Exception {
        // retrieve EC2 launch parameters
        Properties launchParameters = fetchLaunchParameters();
        if (launchParameters.getProperty(LAUNCH_PARAMETER_CLUSTER_NAME) == null) {
            System.err.println("No cluster name given. Assuming 'elastic-grid' as cluster name.");
            launchParameters.setProperty(LAUNCH_PARAMETER_CLUSTER_NAME, "elastic-grid");
        }
        Properties egParameters = translateProperties(launchParameters);

        // save configuration into an Elastic Grid configuration file
        File file = saveConfiguration(egParameters);
        System.out.printf("Elastic Grid configuration file generated in '%s'\n", file.getAbsolutePath());

        String securityGroups = FileUtils.readFileToString(new File("/tmp/security-groups"));
        System.out.printf("Security groups for this EC2 instances are '%s'\n", securityGroups);
        String profile = "monitor-agent";
        boolean hasMonitor = securityGroups.contains(Discovery.MONITOR.getGroupName());
        boolean hasAgent = securityGroups.contains(Discovery.AGENT.getGroupName());
        if (!hasAgent && !hasMonitor) {
            System.err.println("Missing node profile information! Make sure this node has required security groups!");
            System.exit(-1);
        } else if (hasAgent && hasMonitor) {
            profile = "monitor-and-agent";
        } else if (hasMonitor) {
            profile = "monitor";
        } else {
            profile = "agent";
        }

        FileUtils.writeStringToFile(new File("/tmp/eg-node-to-start"), profile);
        System.out.printf("Local machine is morphed into a %s\n", profile);

        String overridesURL = "";
        if (launchParameters.containsKey(LAUNCH_PARAMETER_OVERRIDES_URL))
            overridesURL = launchParameters.getProperty(LAUNCH_PARAMETER_OVERRIDES_URL);
        FileUtils.writeStringToFile(new File("/tmp/overrides"), overridesURL);
    }

    /**
     * At boot time, a file is generated into {@link #LAUNCH_PARAMETERS_FILE}.
     * This method retrieve the content of the file and parse it as if it was a Java configuration file.
     *
     * @return the {@link Properties} object
     * @throws IOException if the file generated at boot time can't be found or read
     */
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

    /**
     * This method translate EC2 lauch parameters into {@link Properties} which can be used through
     * all Elastic Grid code.
     *
     * @param launchParameters the EC2 launch parameters
     * @return the Elastic Grid parameters
     * @throws IOException if the metadata can't be fetched
     */
    private Properties translateProperties(Properties launchParameters) throws IOException {
        // translate properties
        Properties egParameters = new Properties();
        for (Map.Entry property : launchParameters.entrySet()) {
            String key = (String) property.getKey();
            if (LAUNCH_PARAMETER_ACCESS_ID.equals(key))
                egParameters.put(EC2Configuration.AWS_ACCESS_ID, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_ACCESS_ID))
                System.err.println("The Amazon Access ID could not be retrieved!");
            if (LAUNCH_PARAMETER_SECRET_KEY.equals(key))
                egParameters.put(EC2Configuration.AWS_SECRET_KEY, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_SECRET_KEY))
                System.err.println("The Amazon Secret Key could not be retrieved!");
            if (LAUNCH_PARAMETER_CLUSTER_NAME.equals(key))
                egParameters.put(EC2Configuration.EG_CLUSTER_NAME, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_CLUSTER_NAME))
                System.err.println("The name of the cluster could not be retrieved!");
            if (LAUNCH_PARAMETER_DROP_BUCKET.equals(key))
                egParameters.put(EC2Configuration.EG_DROP_BUCKET, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_DROP_BUCKET))
                System.err.println("The name of the Amazon S3 drop bucket could not be retrieved!");
            if (LAUNCH_PARAMETER_OVERRIDES_URL.equals(key))
                egParameters.put(EC2Configuration.EG_OVERRIDES_BUCKET, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_OVERRIDES_URL))
                egParameters.put(EC2Configuration.EG_OVERRIDES_BUCKET, "");
            if (LAUNCH_PARAMETER_EC2_SECURED.equals(key))
                egParameters.put(EC2Configuration.AWS_EC2_SECURED, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_EC2_SECURED))
                egParameters.put(EC2Configuration.AWS_EC2_SECURED, Boolean.TRUE.toString());
            if (LAUNCH_PARAMETER_SQS_SECURED.equals(key))
                egParameters.put(EC2Configuration.AWS_SQS_SECURED, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_SQS_SECURED))
                egParameters.put(EC2Configuration.AWS_SQS_SECURED, Boolean.TRUE.toString());
            if (LAUNCH_PARAMETER_EC2_KEYPAIR.equals(key))
                egParameters.put(EC2Configuration.AWS_EC2_KEYPAIR, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_EC2_KEYPAIR))
                System.err.println("The Amazon keypair name could not be retrieved!");
            /*
            else
                egParameters.put(EC2Configuration.AWS_EC2_KEYPAIR, EC2Utils.getInstanceMetadata("keypair"));    // todo: check the value of the metadata property
            */
            if (LAUNCH_PARAMETER_EC2_AMI32.equals(key))
                egParameters.put(EC2Configuration.AWS_EC2_AMI32, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_EC2_AMI32))
                System.err.println("The 32 bits AMI ID could not be retrieved!");
            if (LAUNCH_PARAMETER_EC2_AMI64.equals(key))
                egParameters.put(EC2Configuration.AWS_EC2_AMI64, property.getValue());
            else if (!launchParameters.containsKey(LAUNCH_PARAMETER_EC2_AMI64))
                System.err.println("The 64 bits AMI ID could not be retrieved!");

            if (LAUNCH_PARAMETER_YUM_PACKAGES.equals(key))
                egParameters.put(EC2Configuration.REDHAT_YUM_PACKAGES, property.getValue());
        }
        return egParameters;
    }

    /**
     * Dump Elastic Grid properties into a configuration file.
     *
     * @param egParameters the properties to dump in a file
     * @return the file where the properties have been dumped into.
     * @throws IOException if there is an error when generating the configuration file
     */
    private File saveConfiguration(Properties egParameters) throws IOException {
        // write EG configuration
        if (egHome == null) {
            System.err.println("Could not find EG_HOME environment variable. Please fix this.");
            System.exit(-1);
        }
        File config = new File(egConfig, ELASTIC_GRID_CONFIGURATION_FILE);
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
