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

package com.elasticgrid.model.internal;

import com.elasticgrid.model.Application;
import com.elasticgrid.model.Cluster;
import com.elasticgrid.model.Node;
import com.elasticgrid.model.NodeProfile;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jerome Bernard
 */
public abstract class AbstractCluster<N extends Node> implements Cluster<N> {
    private String name;
    @SuppressWarnings("unchecked")
    private Set<N> nodes = setOfNodes();
    private List<Application> applications = Factories.listOfApplications();

    protected abstract N createNode(NodeProfile profile);

    @SuppressWarnings("unchecked")
    private static Set setOfNodes() {
        return Collections.synchronizedSet(new HashSet());
    }

    public Cluster<N> name(String name) {
        setName(name);
        return this;
    }

    public boolean isRunning() {
        return nodes != null && nodes.size() > 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<N> getNodes() {
        return nodes;
    }

    @SuppressWarnings("unchecked")
    public N node(NodeProfile profile, InetAddress address) {
        N node = (N) createNode(profile).address(address);
        nodes.add(node);
        return node;
    }

    public void setNodes(Set<N> nodes) {
        this.nodes = nodes;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public Application application(String name) {
        Application application = new ApplicationImpl().name(name);
        applications.add(application);
        return application;
    }

    public Cluster<N> addNodes(List<N> nodes) {
        this.nodes.addAll(nodes);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AbstractCluster");
        sb.append("{name='").append(name).append('\'');
        sb.append(", nodes=").append(nodes.size());
        sb.append(", applications=").append(applications.size());
        sb.append('}');
        return sb.toString();
    }
}