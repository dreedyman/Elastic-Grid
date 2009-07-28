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
package com.elasticgrid.rackspace.cloudservers;

import com.elasticgrid.rackspace.common.RackspaceConnection;
import com.elasticgrid.rackspace.common.RackspaceException;
import com.rackspace.cloudservers.jibx.Servers;
import org.apache.http.HttpException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.jibx.runtime.JiBXException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Implementation based on the XML documents.
 * @author Jerome Bernard
 */
public class XMLCloudServers extends RackspaceConnection implements CloudServers {
    private static final Logger logger = Logger.getLogger(XMLCloudServers.class.getName());

    /**
     * Initializes the Rackspace Cloud Servers connection with the Rackspace login information.
     *
     * @param username the Rackspace username
     * @param apiKey   the Rackspace API key
     */
    public XMLCloudServers(String username, String apiKey) throws RackspaceException, IOException {
        super(username, apiKey);
    }

    public List<Server> getServers() throws CloudServersException {
		HttpGet request = new HttpGet(getServerManagementURL() + "/servers");
		try {
			Servers response = makeRequestInt(request, Servers.class);
            List<Server> servers = new ArrayList<Server>(response.getServers().size());
            for (com.rackspace.cloudservers.jibx.Server server : response.getServers())
                servers.add(new Server(server));
            return servers;
		} finally {
			// TODO: release connection!
		}
    }

    public List<Server> getServersWithDetails() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void createServer(String serverName, String imageID, String flavorID) {
        createServer(serverName, imageID, flavorID, null);
    }

    public void createServer(String serverName, String imageID, String flavorID, Map<String, String> metadata) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected <T> T makeRequestInt(HttpRequestBase request, Class<T> respType)
            throws CloudServersException {
		try {
			return makeRequest(request, respType);
		} catch (RackspaceException e) {
			throw new CloudServersException(e);
		} catch (JiBXException e) {
			throw new CloudServersException("Problem parsing returned message.", e);
		} catch (MalformedURLException e) {
			throw new CloudServersException(e.getMessage(), e);
		} catch (IOException e) {
			throw new CloudServersException(e.getMessage(), e);
		} catch (HttpException e) {
            throw new CloudServersException(e.getMessage(), e);
        }
    }
}
