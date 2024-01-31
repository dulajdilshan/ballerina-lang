/*
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.ballerinalang.test.types.xml;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for XMLNS Definitions.
 *
 * @since 2201.9.0
 */
public class XMLNSTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xmlns.bal");
    }

    @Test
    public void testXMLNSDeclUsingConstant() {
        BRunUtil.invoke(result, "testXMLNSDeclUsingConstant");
    }

    @Test
    public void testXMLNSDeclStmtUsingConstant() {
        BRunUtil.invoke(result, "testXMLNSDeclStmtUsingConstant");
    }

    @Test
    public void testXMLNSUsageInModuleVar() {
        BRunUtil.invoke(result, "testXMLNSUsageInModuleVar");
    }

    @Test
    public void testXMLNSNegativeDefinition() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/types/xml/xmlns_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 22, 7);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 23, 7);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'D'", 24, 7);
        BAssertUtil.validateError(negativeResult, i++, "cannot bind prefix 'ns3' to the empty namespace name", 25, 1);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 28, 11);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'F'", 29, 11);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
