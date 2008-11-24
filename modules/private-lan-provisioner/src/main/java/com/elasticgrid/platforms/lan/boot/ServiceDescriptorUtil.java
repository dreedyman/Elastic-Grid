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

package com.elasticgrid.platforms.lan.boot;

import com.sun.jini.start.ServiceDescriptor;
import java.io.IOException;
import java.io.File;
import org.rioproject.boot.BootUtil;
import org.rioproject.boot.RioServiceDescriptor;

public class ServiceDescriptorUtil extends org.rioproject.boot.ServiceDescriptorUtil {

    public static ServiceDescriptor getRestApi(String policy, String restApiConfig) throws IOException {
        return getRestApi(policy, restApiConfig, null);
    }

    public static ServiceDescriptor getRestApi(String policy, String restApiConfig, String[] overrides) throws IOException {
        return getRestApi(policy, restApiConfig, overrides, "8182");
    }

    public static ServiceDescriptor getRestApi(String policy, String restApiConfig, String[] overrides, String port) throws IOException {
        return getRestApi(policy, restApiConfig, overrides, BootUtil.getHostAddress(), port);
    }

    public static ServiceDescriptor getRestApi(String policy, String restApiConfig, String[] overrides, String hostAddress, String port) throws IOException {
        String egHome = System.getProperty("EG_HOME");
        if (egHome == null)
            throw new RuntimeException("EG_HOME property not declared");
        String[] configArgs = getArray(restApiConfig, overrides);
        String restApiClasspath = egHome + File.separator + "deploy" + File.separator + "rest-api-oar" + File.separator + "lib";
        String restApiCodebase = BootUtil.getCodebase(new String[]{"cybernode-dl.jar",
                                              "rio-dl.jar",
                                              "jsk-dl.jar"},
                                 hostAddress, port);
        String implClass = "com.elasticgrid.rest.RestJSB";
        return new RioServiceDescriptor(restApiCodebase, policy, restApiClasspath, implClass, configArgs);
    }

}
