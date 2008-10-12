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

package com.elasticgrid.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Jerome Bernard
 */
public interface Cluster<N extends Node> extends Serializable {
    String getName();
    Cluster<N> name(String name);
    boolean isRunning();
    Set<N> getNodes();
    Set<N> getMonitorNodes();
    Set<N> getAgentNodes();
    Cluster<N> addNodes(List<N> nodes);
    List<Application> getApplications();
    Application application(String name);

    enum Status {
        RUNNING, STOPPED
    }
}
