/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugger.test.adapter;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for conditional breakpoint related debug scenarios.
 */
public class ConditionalBreakpointTest extends BaseTestCase {

    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() {
        String testProjectName = "conditional-breakpoint-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);
    }

    @Test
    public void testConditionalBreakpointScenarios() throws BallerinaTestException {
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 29));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 34, "y == 3"));
        debugTestRunner.addBreakPoint(
            new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 44, "capital == \"London\""));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 47, "x == 5"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 48, "x == 10"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 49, "x != 100"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 51, "e1 === e2"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 52, "e1 !== e3"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 53, "x > 0"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 54, "x >= 0"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 55, "y < 0"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 56, "y <= 0"));
        debugTestRunner.addBreakPoint(
            new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 57, "x > 0 && z > 0"));
        debugTestRunner.addBreakPoint(
            new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 58, "x > 0 || z > 0"));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 59, "x is int"));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(0));

        // conditional breakpoint inside 'while' loop
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(1));

        // conditional breakpoint inside 'foreach' loop
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(2));

        // debug hit for condition equal `==`
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(3));

        // debug hit for condition not equal `!=`
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(5));

        // debug hit for condition triple equal `===`
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(6));

        // debug hit for condition double not equal `!==`
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(7));

//        TODO: Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/29619
//        // debug hit for condition greater than `>`
//        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
//        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
//        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(8));
//
//        // debug hit for condition greater than or equal `>=`
//        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
//        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
//        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(9));
//
//        // debug hit for condition greater less `<`
//        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
//        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
//        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(10));
//
//        // debug hit for condition greater less or equal `<=`
//        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
//        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
//        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(11));
//
//        // debug hit for condition logical AND `&&`
//        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
//        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
//        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(12));
//
//        // debug hit for condition logical OR `||`
//        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
//        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
//        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(13));
//
//        // debug hit for condition type test expression `x is int`
//        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
//        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
//        Assert.assertEquals(debugHitInfo.getLeft(), debugTestRunner.testBreakpoints.get(14));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}
