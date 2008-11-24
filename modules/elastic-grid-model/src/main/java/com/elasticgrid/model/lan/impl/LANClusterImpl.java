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

package com.elasticgrid.model.lan.impl;

import com.elasticgrid.model.NodeProfile;
import com.elasticgrid.model.internal.AbstractCluster;
import com.elasticgrid.model.lan.LANCluster;
import com.elasticgrid.model.lan.LANNode;
import java.util.Set;

/**
 * @author Jerome Bernard
 */
public class LANClusterImpl extends AbstractCluster<LANNode> implements LANCluster {
    protected LANNode createNode(NodeProfile profile) {
        return new LANNodeImpl(profile);
    }

    public boolean equals(Object o) {
        if (!(o instanceof LANCluster))
            return false;
        LANCluster anotherCluster = (LANCluster) o;
        if (!anotherCluster.getName().equals(getName()))
            return false;
        Set<LANNode> otherNodes = anotherCluster.getNodes();
        return getNodes().equals(otherNodes);
    }
}