/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.ballerinalang.test.statements.dostatement;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in Do Statements.
 */
public class DoStmtsTest {

    private CompileResult programFile, negativeFile1, negativeFile2, negativeFile3;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/dostatement/do-stmt.bal");
        negativeFile1 = BCompileUtil.compile("test-src/statements/dostatement/do-stmt-negative-1.bal");
        negativeFile2 = BCompileUtil.compile("test-src/statements/dostatement/do-stmt-negative-2.bal");
        negativeFile3 =
                BCompileUtil.compile("test-src/statements/dostatement/do-stmt-negative-unreachable.bal");
    }

    @Test
    public void testOnFailStatement() {
        BRunUtil.invoke(programFile, "testOnFailStatement");
    }

    @Test(description = "Check not incompatible types.")
    public void testNegative1() {
        int index = 0;
        BAssertUtil.validateError(negativeFile1, index++, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 15, 12);
        BAssertUtil.validateError(negativeFile1, index++, "incompatible types: expected 'ErrorTypeA', " +
                "found 'ErrorTypeB'", 15, 12);
        BAssertUtil.validateError(negativeFile1, index++, "incompatible error definition type: " +
                "'ErrorTypeB' will not be matched to 'ErrorTypeA'", 37, 12);
        BAssertUtil.validateError(negativeFile1, index++, "incompatible types: expected " +
                "'(ErrorTypeA|ErrorTypeB)', found 'ErrorTypeA'", 37, 12);
        Assert.assertEquals(negativeFile1.getDiagnostics().length, index);
    }

    @Test(description = "Check unreachable statements.")
    public void testNegativeUnreachable() {
        int index = 0;
        BAssertUtil.validateError(negativeFile3, index++, "unreachable code", 15, 6);
        BAssertUtil.validateWarning(negativeFile3, index++, "unused variable 'e'", 17, 12);
        BAssertUtil.validateWarning(negativeFile3, index++, "unused variable 'e'", 30, 12);
        BAssertUtil.validateWarning(negativeFile3, index++, "unused variable 'e'", 43, 12);
        BAssertUtil.validateWarning(negativeFile3, index++, "unused variable 'e'", 57, 12);
        BAssertUtil.validateError(negativeFile3, index++, "unreachable code", 60, 7);
        BAssertUtil.validateError(negativeFile3, index++, "unreachable code", 72, 3);
        BAssertUtil.validateError(negativeFile3, index++, "this function must return a result", 73, 1);
        Assert.assertEquals(negativeFile3.getDiagnostics().length, index);
    }

    @Test(description = "Check on fail scope.")
    public void testNegative2() {
        int index = 0;
        Assert.assertEquals(negativeFile2.getErrorCount(), 5);
        BAssertUtil.validateError(negativeFile2, index++, "type 'string' not allowed here; " +
                "expected an 'error' or a subtype of 'error'.", 6, 11);
        BAssertUtil.validateError(negativeFile2, index++, "incompatible error definition " +
                "type: 'other' will not be matched to 'string'", 8, 12);
        BAssertUtil.validateError(negativeFile2, index++, "incompatible types: expected " +
                "'other', found 'string'", 8, 12);
        BAssertUtil.validateError(negativeFile2, index++, "invalid error variable; expecting " +
                "an error type but found 'string' in type definition", 8, 12);
        BAssertUtil.validateError(negativeFile2, index++, "undefined symbol 'd'", 26, 12);
    }

    @AfterClass
    public void tearDown() {
        programFile = null;
        negativeFile1 = null;
        negativeFile2 = null;
        negativeFile3 = null;
    }
}
