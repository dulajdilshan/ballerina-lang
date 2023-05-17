/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.GroupByClauseNode;
import io.ballerina.compiler.syntax.tree.GroupingKeyVarDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link GroupByClauseNode} context.
 *
 * @since 2201.6.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class GroupByClauseNodeContext extends IntermediateClauseNodeContext<GroupByClauseNode> {

    public GroupByClauseNodeContext() {
        super(GroupByClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, GroupByClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        SeparatedNodeList<Node> groupingKey = node.groupingKey();
        
        
        if (!groupingKey.isEmpty() 
                && groupingKey.get(groupingKey.separatorSize()).kind() == SyntaxKind.GROUPING_KEY_VAR_DECLARATION) {
            Node groupingKeyNode = groupingKey.get(groupingKey.separatorSize());
            return getGroupingKeyVarDeclCompletions(context, node, (GroupingKeyVarDeclarationNode) groupingKeyNode);
        } 
        
        completionItems.addAll(this.expressionCompletions(context));
        this.sort(context, node, completionItems);
        return completionItems;
    }

    private List<LSCompletionItem> getGroupingKeyVarDeclCompletions(BallerinaCompletionContext context, 
                                                                    GroupByClauseNode groupByClauseNode, 
                                                                    GroupingKeyVarDeclarationNode groupingKeyNode) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        int cursor = context.getCursorPositionInTree();
       
        if (this.onBindingPatternContext(cursor, groupingKeyNode)) {
                /*
                Covers the case where the cursor is within the binding pattern context.
                Eg:
                (1) group by var <cursor>
                (2) group by var cu<cursor>
                (3) group by var item = foo(), var <cursor>
                In these cases no suggestions are provided
                */
            return completionItems;
        } else if (this.onTypedBindingPatternContext(cursor, groupingKeyNode)) {
                /*
                Covers the case where the cursor is within the typed binding pattern context.
                Eg:
                (1) group by <cursor>
                (2) group by v<cursor>
                (3) group by var item = foo(), v<cursor>
                */
            NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
            if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                return this.getCompletionItemList(QNameRefCompletionUtil.getTypesInModule(context, qNameRef), context);
            }
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        } else {
                /*
                Covers the case where the cursor is within the expression context.
                Eg:
                (1) group by var item = <cursor>
                */
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, groupByClauseNode, completionItems);
        return completionItems;
    }

    private boolean onBindingPatternContext(int cursor, GroupingKeyVarDeclarationNode node) {
        return cursor > node.typeDescriptor().textRange().endOffset()
                && cursor < node.equalsToken().textRange().startOffset();
    }

    private boolean onTypedBindingPatternContext(int cursor, GroupingKeyVarDeclarationNode node) {
        TypeDescriptorNode typeDescriptorNode = node.typeDescriptor();
        return cursor < typeDescriptorNode.textRange().startOffset() ||
                cursor >= typeDescriptorNode.textRange().startOffset()
                        && cursor <= typeDescriptorNode.textRange().endOffset();
    }

    @Override
    public void sort(BallerinaCompletionContext context,
                     GroupByClauseNode node,
                     List<LSCompletionItem> completionItems) {

        Optional<QueryExpressionNode> queryExprNode = SortingUtil.getTheOutermostQueryExpressionNode(node);
        if (queryExprNode.isEmpty()) {
            return;
        }
        completionItems.forEach(lsCItem -> {
            int rank = 2;
            if (SortingUtil.isSymbolCItemWithinNodeAndCursor(context, lsCItem, queryExprNode.get())) {
                rank = 1;
            }
            lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank) +
                    SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem)));
        });
    }
}
