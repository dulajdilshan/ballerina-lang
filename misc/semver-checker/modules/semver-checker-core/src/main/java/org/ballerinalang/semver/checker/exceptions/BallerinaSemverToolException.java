/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.semver.checker.exceptions;

/**
 * Base class for all exceptions in the Ballerina server checker tool.
 * All the exceptions that are thrown in this tool should be inherited from this base type and, all the other
 * exceptions will be unhandled exceptions.
 *
 * @since 2201.2.0
 */
public class BallerinaSemverToolException extends Exception {

    public BallerinaSemverToolException(String message) {
        super(message);
    }

    public BallerinaSemverToolException(String message, Throwable cause) {
        super(message, cause);
    }

    public BallerinaSemverToolException(Throwable e) {
        super(e);
    }
}
