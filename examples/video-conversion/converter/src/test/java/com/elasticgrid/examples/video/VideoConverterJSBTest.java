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

package com.elasticgrid.examples.video;

import com.elasticgrid.amazon.sqs.SQSEvent;
import net.jini.id.UuidFactory;
import org.apache.commons.io.IOUtils;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * @author Jerome Bernard
 */
public class VideoConverterJSBTest {
    private VideoConverterJSB converter;
    private Encoder encoder;
    private String awsAccessId, awsSecretKey;
    private S3Service s3;
    private static final String TEST_VIDEO = "T0003386.mp4";
    private static final String VIDEOS_DIRECTORY = "src/test/resources/videos";

    @Test(groups = "qa")
    public void testConversion() throws VideoConversionException, TimeoutException, InterruptedException, RemoteException {
        File original = new File(VIDEOS_DIRECTORY, TEST_VIDEO);
        List<File> files = converter.convertVideos(original, "flv");
        assert files.size() == 4;
    }

    @Test(groups = "qa")
    public void testConversionThroughS3() throws RemoteException, VideoConversionException, TimeoutException, InterruptedException {
        VideoConverter cli = new S3VideoConverterProxy(converter, UuidFactory.generate(), "convqueue", "viv-src", awsAccessId, awsSecretKey);
        cli.convert(new File(VIDEOS_DIRECTORY, TEST_VIDEO), "flv");
        SQSEvent conversionEvent = new SQSEvent(this, null, null, TEST_VIDEO, null);
        converter.handle(conversionEvent);
    }

    @BeforeTest(dependsOnMethods = { "setupAWS", "setupEncoder" })
    public void setupVideoConverter() throws Exception {
        converter = new VideoConverterJSB();
        converter.initialize(encoder, s3, "viv-src", "viv-dest", System.getProperty("java.io.tmpdir"));
    }

    @BeforeTest
    public void setupEncoder() {
        System.setProperty("org.rioproject.home", "src/test/resources");
        encoder = new MencoderEncoder(false);
    }

    @BeforeTest
    public void setupAWS() throws IOException, S3ServiceException {
        Properties awsProperties = new Properties();
        File awsPropertiesFile = new File(System.getProperty("user.home") + File.separatorChar + ".eg", "aws.properties");
        InputStream stream = null;
        try {
            stream = new FileInputStream(awsPropertiesFile);
            awsProperties.load(stream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Can't load Amazon Web Services property file from $HOME/.eg/aws.properties");
        } finally {
            IOUtils.closeQuietly(stream);
        }
        awsAccessId = (String) awsProperties.get("aws.accessId");
        awsSecretKey = (String) awsProperties.get("aws.secretKey");
        s3 = new RestS3Service(new AWSCredentials(awsAccessId, awsSecretKey));
    }

//    @AfterTest
//    public void cleanupVideoConverter() {
//        ((VideoConverterJSB) converter).destroy(false);
//    }
}
