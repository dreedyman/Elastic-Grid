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

package com.elasticgrid.amazon.ec2.costmodel;

import org.rioproject.costmodel.ResourceCostModel;
import static java.lang.String.format;
import java.util.logging.Logger;

/**
 * {@link ResourceCostModel} for EC2 service.
 * @author Jerome Bernard
 */
public abstract class AbstractEC2InstanceCostModel implements ResourceCostModel {
    private static final Logger logger = Logger.getLogger(EC2SmallInstanceCostModel.class.getName());

    protected abstract double getCostPerHour();

    /**
     * Get the cost per unit
     * @param duration the amount of time in milliseconds that is to be used to compute the cost per unit
     * @return The cost per unit
     */
    public double getCostPerUnit(long duration) {
        double cost = duration * getCostPerHour() / 60 / 60 / 1000;
        logger.info(format("EC2 Cost for %d milliseconds is %f", duration, cost));
        return cost;
    }

    /**
     * Add a TimeBoundary to the ResourceCostModel
     * @param timeBoundary a TimeBoundary indicating the attributes to be applied to the computation of
     * cost per unit for resource use over a duration
     */
    public void addTimeBoundary(TimeBoundary timeBoundary) {
        // implemented to meet ResourceCostModel contract
    }
}