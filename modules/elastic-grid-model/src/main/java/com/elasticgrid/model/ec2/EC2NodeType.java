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
package com.elasticgrid.model.ec2;

import com.elasticgrid.model.NodeType;

/**
 * Enum of the type of Amazon EC2 instances.
 */
public enum EC2NodeType implements NodeType {
    SMALL("m1.small"),
	LARGE("m1.large"),
	EXTRA_LARGE("m1.xlarge"),
	MEDIUM_HIGH_CPU("c1.medium"),
	EXTRA_LARGE_HIGH_CPU("c1.xlarge");

    private String name;

    EC2NodeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "EC2NodeType{" +
                "name='" + name + '\'' +
                '}';
    }
}
