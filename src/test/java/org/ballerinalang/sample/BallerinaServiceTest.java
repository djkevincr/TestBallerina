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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Sample case to test Ballerina Service inside Dockerized environment.
 */
public class BallerinaServiceTest {

    private BallerinaDockerClient dockerClient;
    private static final String IMAGE_NAME = "helloworld";
    private static final String IMAGE_VERSION = "latest";
    private static final String DOCKER_CONTAINER_NAME = IMAGE_NAME + ":" + IMAGE_VERSION;
    private static final int SERVICE_PORT = 9090;
    private static final int SERVICE_STARTUP_TIMEOUT = 60;
    private static final String BALLERINA_VERSION = "0.95.6";
    private String url = "http://localhost:{port}/hello";
    private GenericContainer ballerinaService;

    @Before
    public void setup() throws BallerinaDockerClientException, IOException, InterruptedException {
        // Set Base Ballerina image version - public docker hub released version.
        System.setProperty("ballerina.version", BALLERINA_VERSION);
        this.dockerClient = new DefaultBallerinaDockerClient();
        Path servicePath = Paths.get(BallerinaServiceTest.class.getClassLoader()
                .getResource("helloWorldService.bal").getPath());
        // Build child docker image using Service bal file and Base image.
        String createdImageName = dockerClient.createMainImage("TestPackage", null, servicePath,
                IMAGE_NAME, IMAGE_VERSION);
        assert createdImageName != null;
        // Spin up container from created docker image in previous step.
        this.ballerinaService = new GenericContainer(DOCKER_CONTAINER_NAME)
                .withExposedPorts(SERVICE_PORT)
                .waitingFor(new ServiceStartupLogWaitStrategy())
                .withStartupTimeout(Duration.ofSeconds(SERVICE_STARTUP_TIMEOUT));
        // Start the container.
        this.ballerinaService.start();
    }


    @Test
    public void testService() {
        // Write Custom test cases accordingly.
        String invocationURL = this.url.replace("{port}",
                ballerinaService.getMappedPort(SERVICE_PORT).toString());
        Assert.assertEquals(HTTPUtil.sendGet(invocationURL), "Hello, World!");
    }

    @After
    public void tearDown(){
        // Stop container.
        this.ballerinaService.stop();
    }


}
