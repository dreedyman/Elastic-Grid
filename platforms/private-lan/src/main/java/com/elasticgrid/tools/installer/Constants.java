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
package com.elasticgrid.tools.installer;

public interface Constants {
    String OFFLINE_MODE = "installer.offline";

    String AWS_ACCESS_ID = "aws.accessId";
    String AWS_SECRET_KEY = "aws.secretKey";
    String AWS_KEYPAIR = "aws.ec2.key";
    String AWS_AMI32 = "aws.ec2.ami32";
    String AWS_AMI64 = "aws.ec2.ami64";

    String EG_DROP_BUCKET = "eg.s3.dropBucket";
    String EG_OVERRIDES_BUCKET = "eg.s3.overridesBucket";
}
