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
package com.elasticgrid.tools.cli;

import com.elasticgrid.model.NodeProfileInfo;
import com.elasticgrid.model.NodeProfile;
import com.elasticgrid.model.NodeType;
import com.elasticgrid.model.ec2.EC2NodeType;
import com.elasticgrid.utils.amazon.AWSUtils;
import com.elasticgrid.config.EC2Configuration;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import java.util.List;
import java.util.Properties;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractClusterHandler extends AbstractHandler {
    public static final String AGENT_OVERRIDE_FILE_NAME = "start-agent-override.groovy";
    public static final String MONITOR_OVERRIDE_FILE_NAME = "start-monitor-override.groovy";

    protected boolean processOption(String option,
                                  List<NodeProfileInfo> nodeProfileInfo,
                                  NodeProfile nodeProfile,
                                  PrintStream out) {
        int num;
        try {
            num = getNumber(option);
        } catch (NumberFormatException e) {
            out.println("Not a valid number: " + option);
            return false;
        }
        String type = getType(option);
        if (type == null) {
            out.println("Bad type: " + option);
            return false;
        }
        addNodeProfileInfo(nodeProfileInfo,
                           nodeProfile,
                           translateToNodeType(type),
                           num);
        return true;
    }

    private int getNumber(String s) {
        int number;
        int ndx = s.indexOf(":");
        if (ndx > 0)
            s = s.substring(0, ndx);
        number = Integer.parseInt(s);
        return number;
    }

    private String getType(String s) {
        String type = null;
        int ndx = s.indexOf(":");
        if (ndx > 0) {
            s = s.substring(ndx + 1);
            String[] types = {"s", "m", "l", "xl"};
            for (String t : types) {
                if (t.equals(s)) {
                    type = s;
                    break;
                }
            }
        } else {
            type = "s";
        }
        return type;
    }

    private void addNodeProfileInfo(List<NodeProfileInfo> nodeProfileInfo,
                                    NodeProfile nodeProfile,
                                    NodeType nodeType,
                                    int number) {
        nodeProfileInfo.add(new NodeProfileInfo(nodeProfile, nodeType, number));
    }

    protected String getClusterName(BufferedReader br, PrintStream out)
        throws IOException {
        return getResponse("Enter name of cluster: ", br, out);
    }

    private String getResponse(String ask, BufferedReader br, PrintStream out)
        throws IOException {
        out.print(ask);
        return br.readLine();
    }

    protected NodeType translateToNodeType(String type) {
        NodeType nType;
        if (type.equals("s")) {
            nType = EC2NodeType.SMALL;
        } else if (type.equals("m")) {
            nType = EC2NodeType.MEDIUM_HIGH_CPU;
        } else if (type.equals("l")) {
            nType = EC2NodeType.LARGE;
        } else {
            nType = EC2NodeType.EXTRA_LARGE;
        }
        return nType;
    }

    protected String getValidResponse(String ask,
                                    String[] validResponse,
                                    String def,
                                    BufferedReader br,
                                    PrintStream out)
        throws IOException {
        String response = null;
        while (true) {
            out.print(ask);
            String s = br.readLine();
            if (s == null)
                break;
            for (String v : validResponse) {
                if (v.equals(s)) {
                    response = s.equals("")?def:s;
                    break;
                }
            }
            if (response != null)
                break;
            else
                out.println("Invalid response [" + s + "]");
        }
        return response == null ? def : response;
    }

    protected int getIntegerResponse(String ask,
                                   BufferedReader br,
                                   PrintStream out)
        throws IOException {
        int response = -1;
        while (true) {
            out.print(ask);
            String s = br.readLine();
            if (s == null)
                break;
            if (s.length() == 0)
                break;
            try {
                response = Integer.parseInt(s);
                if (response < 0)
                    out.println("Invalid number [" + s + "]");
                else
                    break;
            } catch (NumberFormatException e) {
                out.println("Invalid number [" + s + "]");
            }
        }
        return response;
    }

    protected void setHasOverride(NodeProfile np, List<NodeProfileInfo> list) {
        for (NodeProfileInfo npi : list) {
            if (npi.getNodeProfile().equals(np))
                npi.setOverride(true);
        }
    }

    protected int countAgents(List<NodeProfileInfo> list) {
        int count = 0;
        for (NodeProfileInfo npi : list) {
            if (npi.getNodeProfile().equals(NodeProfile.AGENT))
                count += npi.getNumber();
        }
        return count;
    }

    protected int countMonitors(List<NodeProfileInfo> list) {
        int count = 0;
        for (NodeProfileInfo npi : list) {
            if (npi.getNodeProfile().equals(NodeProfile.MONITOR))
                count += npi.getNumber();
        }
        return count;
    }

    protected int countMonitorAgents(List<NodeProfileInfo> list) {
        int count = 0;
        for (NodeProfileInfo npi : list) {
            if (npi.getNodeProfile().equals(NodeProfile.MONITOR_AND_AGENT))
                count += npi.getNumber();
        }
        return count;
    }

    protected void uploadOverrides(File dir, String clusterName, PrintStream out) throws
                                                               IOException,
            S3ServiceException,
            NoSuchAlgorithmException {
        Properties awsConfig = AWSUtils.loadEC2Configuration();
        String awsAccessID = awsConfig.getProperty(EC2Configuration.AWS_ACCESS_ID);
        String awsSecretKey = awsConfig.getProperty(EC2Configuration.AWS_SECRET_KEY);
        if (awsAccessID == null) {
            throw new IllegalArgumentException("Could not find AWS Access ID");
        }
        if (awsSecretKey == null) {
            throw new IllegalArgumentException("Could not find AWS Secret Key");
        }
        AWSCredentials credentials = new AWSCredentials(awsAccessID, awsSecretKey);

        S3Service s3Service = new RestS3Service(credentials);
        String overridesBucket = awsConfig.getProperty(EC2Configuration.EG_OVERRIDES_BUCKET);
        out.println("Uploading overrides from ["+dir.getPath()+"] " +
                                "to S3 bucket ["+overridesBucket+"] ...");
        S3Bucket s3OverridesBucket = s3Service.getOrCreateBucket(overridesBucket);

        for(File file : dir.listFiles()) {
            if(file.getName().endsWith(".groovy")) {
                S3Object s3o = new S3Object(s3OverridesBucket, file);
                out.println("Sending "+file.getName()+"...");
                s3o.setKey(clusterName+"/"+file.getName());
                s3Service.putObject(s3OverridesBucket, s3o);
            }
        }
    }

}