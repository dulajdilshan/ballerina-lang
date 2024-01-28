/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.worker;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * worker alternative receive related tests.
 */
public class WorkerAlternateReceiveTest {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/workers/workers_alt_receive.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        negativeResult = BCompileUtil.compile("test-src/workers/workers_alt_receive_negative.bal");
    }

    @Test(dataProvider = "functionProvider")
    public void workerAlternateReceiveTest(String funcName) {
        BRunUtil.invoke(result, funcName, new Object[0]);
    }

    @DataProvider
    public static String[] functionProvider() {
        return new String[] {
                "workerAlternateReceiveTest",
                "workerAlternateReceiveTest2",
                "alternateReceiveWithSenderPanic",
                "alternateReceiveWithMultiplePanic",
                "alternateReceiveWithSenderError",
                "alternateReceiveWithMultipleError",
                "alternateReceiveWithPanicAndError",
                "alternateReceiveWithReceiverPanic",
                "alternateReceiveWithReceiverError",
                "alternateReceiveWithSameWorkerSend",
                "alternateReceiveWithSameWorkerSendError1",
                "alternateReceiveWithSameWorkerSendError2",
                "alternateReceiveWithSameWorkerSendPanic",
                "multilpleAlternateReceive1",
                "multilpleAlternateReceive2"
        };
    }

    @Test(description = "Test negative scenarios of alternate receive action.")
    public void testAltWorkerReceiveNegative() {
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'", 53,
                20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'string', found '" +
                "(int|ballerina/lang.error:0.0.0:NoMessage)'", 54, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'", 55,
                20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'", 56,
                20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'string', found 'int'", 57,
                20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'string', found '" +
                "(decimal|string|int|boolean)'", 58, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'string', found '" +
                "(decimal|string|int|boolean|ballerina/lang.error:0.0.0:NoMessage)'", 59, 20);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
