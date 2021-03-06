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

import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.WaitingConsumer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * Ballerina Service waiting strategy.
 */
public class ServiceStartupLogWaitStrategy extends GenericContainer.AbstractWaitStrategy {

    private static final String serviceStartUpLog = "ballerina: started HTTP/WS server connector 0.0.0.0:9090.*";

    protected void waitUntilReady() {
        WaitingConsumer waitingConsumer = new WaitingConsumer();
        this.container.followOutput(waitingConsumer);
        Predicate waitPredicate = (outputFrame) -> {
            String trimmedFrameText = ((OutputFrame) outputFrame).getUtf8String().replaceFirst("\n$", "");
            return trimmedFrameText.matches(serviceStartUpLog);
        };

        try {
            waitingConsumer.waitUntil(waitPredicate, this.startupTimeout.getSeconds(), TimeUnit.SECONDS, 1);
        } catch (TimeoutException var4) {
            throw new ContainerLaunchException(
                    "Timed out waiting for log output matching Ballerina Service startup Log  \'" + serviceStartUpLog
                            + "\'");
        }
    }
}