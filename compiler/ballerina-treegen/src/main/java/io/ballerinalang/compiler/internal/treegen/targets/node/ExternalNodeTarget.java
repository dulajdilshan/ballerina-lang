/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.internal.treegen.targets.node;

import io.ballerinalang.compiler.internal.treegen.TreeGenConfig;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxNode;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeClass;

import java.util.ArrayList;
import java.util.List;

import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.EXTERNAL_NODE_OUTPUT_DIR_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.EXTERNAL_NODE_PACKAGE_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.EXTERNAL_NODE_TEMPLATE_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.INTERNAL_NODE_PACKAGE_KEY;

/**
 * Generates an external tree Java class for each syntax tree node.
 *
 * @since 1.3.0
 */
public class ExternalNodeTarget extends AbstractNodeTarget {

    public ExternalNodeTarget(TreeGenConfig config) {
        super(config);
    }

    @Override
    protected String getTemplateName() {
        return config.getOrThrow(EXTERNAL_NODE_TEMPLATE_KEY);
    }

    @Override
    protected String getPackageName() {
        return config.getOrThrow(EXTERNAL_NODE_PACKAGE_KEY);
    }

    @Override
    protected String getOutputDir() {
        return config.getOrThrow(EXTERNAL_NODE_OUTPUT_DIR_KEY);
    }

    @Override
    protected String getClassName(TreeNodeClass treeNodeClass) {
        return treeNodeClass.externalClassName();
    }

    @Override
    protected List<String> getImportClasses(SyntaxNode syntaxNode) {
        List<String> importClassList = new ArrayList<>();
        importClassList.add(getClassFQN(config.getOrThrow(INTERNAL_NODE_PACKAGE_KEY), INTERNAL_BASE_NODE_CN));
        return importClassList;
    }
}
