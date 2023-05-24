// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.


package org.ballerinalang.testerina.natives;

import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;


/**
 * Common utility functions for the Testerina module.
 *
 * @since 2201.7.0
 */
public class CommonUtils {
    public static Object sleep(BDecimal seconds) {
        try {
            Thread.sleep(seconds.intValue() * 1000);
        } catch (InterruptedException e) {
            return BLangExceptionHelper.getRuntimeException(
                    RuntimeErrors.OPERATION_NOT_SUPPORTED_ERROR, "Invalid duration: " + e.getMessage());
        }
        return null;
    }

    public static BDecimal currentTimeInMillis() {
        long currentTime = System.currentTimeMillis();
        return BDecimal.valueOf(currentTime);
    }

    public static Object isFunctionParamConcurrencySafe(BFunctionPointer func) {
        FunctionType functionType = (FunctionType) func.getType();
        Parameter[] functionParameters = functionType.getParameters();
        for (Parameter functionParameter : functionParameters) {
            Type parameterType = functionParameter.type;
            if (isSubTypeOfReadOnlyOrIsolatedObjectUnion(parameterType)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean isSubTypeOfReadOnlyOrIsolatedObjectUnion(Type type) {
        if (type.isReadOnly()) {
            return true;
        }

        if (type instanceof ObjectType) {
            return ((ObjectType) type).isIsolated();
        }

        if (!(type instanceof UnionType)) {
            return false;
        }

        for (Type memberType : ((UnionType) type).getMemberTypes()) {
            if (!isSubTypeOfReadOnlyOrIsolatedObjectUnion(memberType)) {
                return false;
            }
        }
        return true;
    }
}
