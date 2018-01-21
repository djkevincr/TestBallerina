/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.sample;

import org.ballerinalang.containers.docker.BallerinaDockerClient;
import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;
import org.ballerinalang.containers.docker.impl.DefaultBallerinaDockerClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.http.HttpResponse;
import org.testcontainers.shaded.org.apache.http.client.HttpClient;
import org.testcontainers.shaded.org.apache.http.client.methods.HttpGet;
import org.testcontainers.shaded.org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.Duration;


/**
 * Sample case to test Ballerina Service inside Dockerized environment.
 */
public class TestBallerina {

    private static BallerinaDockerClient dockerClient;
    private static final String IMAGE_NAME = "helloworld";
    private static final String IMAGE_VERSION = "latest";
    private static final String DOCKER_CONTAINER_NAME = IMAGE_NAME + ":" + IMAGE_VERSION;
    private static final int SERVICE_PORT = 9090;
    private static final int SERVICE_STARTUP_TIMEOUT = 60;
    private static final String BALLERINA_VERSION = "0.95.6";
    private String url = "http://localhost:{port}/hello";
    public static GenericContainer ballerinaService;

    @BeforeClass
    public static void setup() throws BallerinaDockerClientException, IOException, InterruptedException {
        System.setProperty("ballerina.version", BALLERINA_VERSION);
        dockerClient = new DefaultBallerinaDockerClient();
        Path servicePath = Paths.get(TestBallerina.class.getClassLoader().getResource("helloWorldService.bal").getPath());
        String createdImageName = dockerClient.createMainImage("TestPackage", null, servicePath,
                IMAGE_NAME, IMAGE_VERSION);
        assert createdImageName != null;
        ballerinaService = new GenericContainer(DOCKER_CONTAINER_NAME)
                .withExposedPorts(SERVICE_PORT)
                .waitingFor(new ServiceStartupLogWaitStrategy())
                .withStartupTimeout(Duration.ofSeconds(SERVICE_STARTUP_TIMEOUT));

    }


    @Test
    public void testApp() throws InterruptedException {
        String invocationURL = this.url.replace("{port}",
                this.ballerinaService.getMappedPort(SERVICE_PORT).toString());
        Assert.assertEquals(sendGet(invocationURL), "Hello, World!");
    }

    private String sendGet(String url) {

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        StringBuffer result = new StringBuffer();
        try {
            HttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException ex) {
            //Ignore
        }
        return result.toString();
    }

}
