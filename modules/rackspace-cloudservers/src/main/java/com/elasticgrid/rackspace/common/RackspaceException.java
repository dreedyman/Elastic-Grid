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
package com.elasticgrid.rackspace.common;

/**
 * A wrapper exception to simplify catching errors related to Rackspace activity.
 *
 * @author Jerome Bernard
 */
public class RackspaceException extends Exception {
    private int code;
    private String details;

    public RackspaceException(String message) {
        this(message, null);
    }

    public RackspaceException(int code, String message, String details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public RackspaceException(Throwable cause) {
        super(cause);
        if (cause instanceof RackspaceException) {
            this.code = ((RackspaceException) cause).code;
            this.details = ((RackspaceException) cause).details;
        }
    }

    public RackspaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "Error " + getCode() + ": " + super.getMessage() + "\nDetails: " + getDetails();
    }
}
