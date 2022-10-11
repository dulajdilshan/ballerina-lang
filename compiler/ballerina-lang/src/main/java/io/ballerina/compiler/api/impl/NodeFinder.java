/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.compiler.api.impl;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangClientDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorCauseBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorFieldBindingPatterns;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorMessageBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangFieldBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangNamedArgBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangSimpleBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAssertion;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomCharOrEscape;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCapturingGroups;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSet;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSetRange;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharacterClass;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReDisjunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReFlagExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReFlagsOnOff;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReSequence;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorCauseMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorFieldMatchPatterns;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMessageMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangFieldMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangListMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMappingMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangNamedArgMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangRestMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangSimpleMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangClientDeclarationStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatchStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the enclosing AST node for the given position.
 *
 * @since 2.0.0
 */
class NodeFinder extends BaseVisitor {

    private LineRange range;
    private BLangNode enclosingNode;
    private BLangNode enclosingContainer;
    private final boolean allowExprStmts;

    NodeFinder(boolean allowExprStmts) {
        this.allowExprStmts = allowExprStmts;
    }

    void lookup(BLangPackage module, LineRange range) {
        List<TopLevelNode> topLevelNodes = new ArrayList<>(module.topLevelNodes);
        BLangTestablePackage tests = module.getTestablePkg();

        if (tests != null) {
            topLevelNodes.addAll(tests.topLevelNodes);
        }

        lookupTopLevelNodes(topLevelNodes, range);
    }

    BLangNode lookup(BLangCompilationUnit unit, LineRange range) {
        return lookupTopLevelNodes(unit.topLevelNodes, range);
    }

    BLangNode lookupEnclosingContainer(BLangPackage module, LineRange range) {
        this.enclosingContainer = module;
        lookup(module, range);
        return this.enclosingContainer;
    }

    BLangNode lookupEnclosingContainer(BLangCompilationUnit compilationUnit, LineRange range) {
        this.enclosingContainer = compilationUnit;
        lookup(compilationUnit, range);
        return this.enclosingContainer;
    }

    private BLangNode lookupTopLevelNodes(List<TopLevelNode> nodes, LineRange range) {
        this.range = range;
        this.enclosingNode = null;

        for (TopLevelNode node : nodes) {
            BLangNode bLangNode = (BLangNode) node;
            if (PositionUtil.isRangeWithinNode(this.range, node.getPosition())
                    && !isLambdaFunction(node) && !isClassForService(node)) {
                bLangNode.accept(this);
                return this.enclosingNode;
            }
        }
        return this.enclosingNode;
    }

    private boolean lookupNodes(List<? extends BLangNode> nodes) {
        for (BLangNode node : nodes) {
            if (lookupNode(node)) {
                return true;
            }
        }
        return false;
    }

    private boolean lookupNode(BLangNode node) {
        if (node == null) {
            return false;
        }
        if (PositionUtil.isRangeWithinNode(this.range, node.pos)) {
            node.accept(this);
            return this.enclosingNode != null;
        }
        return false;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangClientDeclarationStatement clientDeclStmt) {
        lookupNode(clientDeclStmt.getClientDeclaration());
    }

    @Override
    public void visit(BLangClientDeclaration clientDeclNode) {
        lookupNode((BLangNode) clientDeclNode.getUri());
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // Compare the target lookup pos with the function symbol pos to ensure that we are not looking for the
        // container of the function.
        if (!this.range.equals(funcNode.symbol.pos.lineRange())) {
            this.enclosingContainer = funcNode;
        }

        if (lookupNodes(funcNode.requiredParams)) {
            return;
        }
        if (lookupNode(funcNode.restParam)) {
            return;
        }
        if (lookupNode(funcNode.returnTypeNode)) {
            return;
        }
        lookupNode(funcNode.body);
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        this.enclosingContainer = blockFuncBody;
        lookupNodes(blockFuncBody.stmts);
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        lookupNode(exprFuncBody.expr);
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
        lookupNodes(externFuncBody.annAttachments);
    }

    @Override
    public void visit(BLangService serviceNode) {
        if (lookupNodes(serviceNode.annAttachments)) {
            return;
        }
        if (lookupNodes(serviceNode.attachedExprs)) {
            return;
        }
        lookupNode(serviceNode.serviceClass);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        lookupNode(typeDefinition.typeNode);
    }

    @Override
    public void visit(BLangConstant constant) {
        if (lookupNode(constant.typeNode)) {
            return;
        }
        if (lookupNode(constant.expr)) {
            return;
        }
        setEnclosingNode(constant, constant.name.pos);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (lookupNodes(varNode.annAttachments)) {
            return;
        }
        if (lookupNode(varNode.typeNode)) {
            return;
        }
        if (lookupNode(varNode.expr)) {
            return;
        }
        setEnclosingNode(varNode, varNode.name.pos);
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        if (lookupNode(annotationNode.typeNode)) {
            return;
        }
        setEnclosingNode(annotationNode, annotationNode.name.pos);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        if (lookupNode(annAttachmentNode.expr)) {
            return;
        }
        setEnclosingNode(annAttachmentNode, annAttachmentNode.annotationName.pos);
    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
        lookupNode(tableKeyTypeConstraint.keyType);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        this.enclosingContainer = blockNode;
        lookupNodes(blockNode.stmts);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        lookupNode(varDefNode.var);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        if (lookupNode(assignNode.varRef)) {
            return;
        }
        lookupNode(assignNode.expr);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        if (lookupNode(compoundAssignNode.varRef)) {
            return;
        }
        lookupNode(compoundAssignNode.expr);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        if (lookupNode(retryNode.retryBody)) {
            return;
        }
        if (lookupNode(retryNode.retrySpec)) {
            return;
        }
        lookupNode(retryNode.onFailClause);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        if (lookupNode(retryTransaction.transaction)) {
            return;
        }
        lookupNode(retryTransaction.retrySpec);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        if (lookupNode(retrySpec.retryManagerType)) {
            return;
        }
        lookupNodes(retrySpec.argExprs);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        lookupNode(returnNode.expr);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        lookupNode(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        lookupNode(xmlnsStmtNode.xmlnsDecl);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        lookupNode(exprStmtNode.expr);

        if (this.allowExprStmts) {
            setEnclosingNode(exprStmtNode.expr, exprStmtNode.pos);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (lookupNode(ifNode.expr)) {
            return;
        }
        if (lookupNode(ifNode.body)) {
            return;
        }
        lookupNode(ifNode.elseStmt);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        if (lookupNodes(queryAction.queryClauseList)) {
            return;
        }
        lookupNode(queryAction.doClause);
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (lookupNode((BLangNode) foreach.variableDefinitionNode)) {
            return;
        }
        if (lookupNode(foreach.collection)) {
            return;
        }
        if (lookupNode(foreach.body)) {
            return;
        }
        lookupNode(foreach.onFailClause);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        if (lookupNode(fromClause.collection)) {
            return;
        }
        if (lookupNode((BLangNode) fromClause.variableDefinitionNode)) {
            return;
        }
        this.enclosingNode = fromClause;
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        if (lookupNode(joinClause.collection)) {
            return;
        }
        if (lookupNode((BLangNode) joinClause.variableDefinitionNode)) {
            return;
        }
        lookupNode(joinClause.onClause);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable var : letClause.letVarDeclarations) {
            if (lookupNode((BLangNode) var.definitionNode)) {
                return;
            }
        }
    }

    @Override
    public void visit(BLangOnClause onClause) {
        if (lookupNode(onClause.lhsExpr)) {
            return;
        }
        lookupNode(onClause.rhsExpr);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        lookupNode(orderKeyClause.expression);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        for (OrderKeyNode key : orderByClause.orderByKeyList) {
            if (lookupNode((BLangNode) key)) {
                return;
            }
        }
        this.enclosingNode = orderByClause;
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        lookupNode(selectClause.expression);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        lookupNode(whereClause.expression);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        lookupNode(doClause.body);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        lookupNode(onConflictClause.expression);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        lookupNode(limitClause.expression);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (lookupNode(whileNode.expr)) {
            return;
        }
        if (lookupNode(whileNode.body)) {
            return;
        }
        lookupNode(whileNode.onFailClause);
    }

    @Override
    public void visit(BLangLock lockNode) {
        if (lookupNode(lockNode.body)) {
            return;
        }
        lookupNode(lockNode.onFailClause);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        if (lookupNode(transactionNode.transactionBody)) {
            return;
        }
        lookupNode(transactionNode.onFailClause);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        if (lookupNode(stmt.expr)) {
            return;
        }
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        if (lookupNode(stmt.expr)) {
            return;
        }
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        if (lookupNode(stmt.expr)) {
            return;
        }
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        lookupNodes(forkJoin.workers);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        if (lookupNode(workerSendNode.expr)) {
            return;
        }
        setEnclosingNode(workerSendNode, workerSendNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        setEnclosingNode(workerReceiveNode, workerReceiveNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        lookupNode(rollbackNode.expr);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (lookupNode((BLangNode) field)) {
                return;
            }
        }
        this.enclosingNode = recordLiteral;
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        if (lookupNodes(varRefExpr.expressions)) {
            return;
        }
        lookupNode(varRefExpr.restParam);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : varRefExpr.recordRefFields) {
            if (lookupNode(recordRefField.getBindingPattern())) {
                return;
            }
        }
        lookupNode(varRefExpr.restParam);
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        if (lookupNode(varRefExpr.message)) {
            return;
        }
        if (lookupNodes(varRefExpr.detail)) {
            return;
        }
        if (lookupNode(varRefExpr.cause)) {
            return;
        }
        lookupNode(varRefExpr.restVar);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (setEnclosingNode(varRefExpr, varRefExpr.variableName.pos)) {
            return;
        }

        setEnclosingNode(varRefExpr, varRefExpr.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (setEnclosingNode(fieldAccessExpr, fieldAccessExpr.field.pos)) {
            return;
        }

        lookupNode(fieldAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        if (lookupNode(indexAccessExpr.expr)) {
            return;
        }
        if (lookupNode(indexAccessExpr.indexExpr)) {
            return;
        }
        this.enclosingNode = indexAccessExpr;
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // Looking up args expressions since requiredArgs and restArgs get set only when compilation is successful
        if (lookupNodes(invocationExpr.argExprs)) {
            return;
        }
        if (lookupNode(invocationExpr.expr)) {
            return;
        }

        if (setEnclosingNode(invocationExpr, invocationExpr.name.pos)) {
            return;
        }

        setEnclosingNode(invocationExpr, invocationExpr.pos);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        if (lookupNode(typeInit.userDefinedType)) {
            return;
        }
        if (lookupNodes(typeInit.argsExpr)) {
            return;
        }
        setEnclosingNode(typeInit, typeInit.pos);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        if (lookupNodes(actionInvocationExpr.argExprs)) {
            return;
        }
        if (lookupNodes(actionInvocationExpr.restArgs)) {
            return;
        }
        if (lookupNode(actionInvocationExpr.expr)) {
            return;
        }
        if (setEnclosingNode(actionInvocationExpr, actionInvocationExpr.name.pos)) {
            return;
        }

        setEnclosingNode(actionInvocationExpr, actionInvocationExpr.pos);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        if (lookupNode(ternaryExpr.expr)) {
            return;
        }
        if (lookupNode(ternaryExpr.thenExpr)) {
            return;
        }
        if (lookupNode(ternaryExpr.elseExpr)) {
            return;
        }
        this.enclosingNode = ternaryExpr;
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        lookupNodes(awaitExpr.exprList);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        if (lookupNode(trapExpr.expr)) {
            return;
        }
        this.enclosingNode = trapExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (lookupNode(binaryExpr.lhsExpr)) {
            return;
        }
        if (lookupNode(binaryExpr.rhsExpr)) {
            return;
        }
        if (PositionUtil.withinRange(binaryExpr.pos.lineRange(), this.range)) {
            this.enclosingNode = binaryExpr;
        }
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        if (lookupNode(elvisExpr.lhsExpr)) {
            return;
        }
        lookupNode(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        if (lookupNode(groupExpr.expression)) {
            return;
        }
        this.enclosingNode = groupExpr;
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        for (BLangLetVariable var : letExpr.letVarDeclarations) {
            if (lookupNode((BLangNode) var.definitionNode)) {
                return;
            }
        }

        if (lookupNode(letExpr.expr)) {
            return;
        }
        this.enclosingNode = letExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        if (lookupNodes(listConstructorExpr.exprs)) {
            return;
        }
        this.enclosingNode = listConstructorExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr spreadOpExpr) {
        lookupNode(spreadOpExpr.expr);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        if (lookupNode(tableConstructorExpr.tableKeySpecifier)) {
            return;
        }
        if (lookupNodes(tableConstructorExpr.recordLiteralList)) {
            return;
        }
        if (PositionUtil.withinRange(tableConstructorExpr.pos.lineRange(), this.range)) {
            this.enclosingNode = tableConstructorExpr;
        }
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        lookupNodes(tupleLiteral.exprs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        lookupNodes(arrayLiteral.exprs);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (lookupNode(unaryExpr.expr)) {
            return;
        }
        this.enclosingNode = unaryExpr;
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        lookupNode(typedescExpr.typeNode);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        if (lookupNodes(conversionExpr.annAttachments)) {
            return;
        }
        if (lookupNode(conversionExpr.typeNode)) {
            return;
        }
        if (lookupNode(conversionExpr.expr)) {
            return;
        }
        this.enclosingNode = conversionExpr;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        if (setEnclosingNode(xmlQName, xmlQName.pos)
                || setEnclosingNode(xmlQName, xmlQName.prefix.pos)) {
            return;
        }

        setEnclosingNode(xmlQName, xmlQName.localname.pos);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {

    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        if (lookupNode(xmlElementLiteral.startTagName)) {
            return;
        }
        if (lookupNodes(xmlElementLiteral.attributes)) {
            return;
        }
        if (lookupNodes(xmlElementLiteral.children)) {
            return;
        }
        if (lookupNode(xmlElementLiteral.endTagName)) {
            return;
        }
        this.enclosingNode = xmlElementLiteral;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        if (lookupNode(xmlTextLiteral.concatExpr)) {
            return;
        }
        lookupNodes(xmlTextLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        if (lookupNode(xmlCommentLiteral.concatExpr)) {
            return;
        }
        lookupNodes(xmlCommentLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        if (lookupNode(xmlProcInsLiteral.dataConcatExpr)) {
            return;
        }
        if (lookupNodes(xmlProcInsLiteral.dataFragments)) {
            return;
        }
        lookupNode(xmlProcInsLiteral.target);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        if (lookupNode(xmlQuotedString.concatExpr)) {
            return;
        }
        lookupNodes(xmlQuotedString.textFragments);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        lookupNodes(stringTemplateLiteral.exprs);
        setEnclosingNode(stringTemplateLiteral, stringTemplateLiteral.pos);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        if (lookupNodes(rawTemplateLiteral.strings)) {
            return;
        }
        if (lookupNodes(rawTemplateLiteral.insertions)) {
            return;
        }
        this.enclosingNode = rawTemplateLiteral;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        if (lookupNode(bLangLambdaFunction.function)) {
            return;
        }
        if (PositionUtil.withinRange(bLangLambdaFunction.pos.lineRange(), this.range)) {
            this.enclosingNode = bLangLambdaFunction;
        }
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        if (lookupNodes(bLangArrowFunction.params)) {
            return;
        }
        if (lookupNode(bLangArrowFunction.body)) {
            return;
        }
        this.enclosingNode = bLangArrowFunction;
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        lookupNode(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        if (lookupNode(bLangNamedArgsExpression.expr)) {
            return;
        }
        setEnclosingNode(bLangNamedArgsExpression.name, bLangNamedArgsExpression.name.pos);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        if (lookupNode(assignableExpr.lhsExpr)) {
            return;
        }
        lookupNode(assignableExpr.typeNode);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        if (lookupNode(checkedExpr.expr)) {
            return;
        }
        this.enclosingNode = checkedExpr;
    }

    @Override
    public void visit(BLangFail failExpr) {
        lookupNode(failExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        if (lookupNode(checkPanickedExpr.expr)) {
            return;
        }
        this.enclosingNode = checkPanickedExpr;
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        lookupNode(serviceConstructorExpr.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        if (lookupNode(typeTestExpr.expr)) {
            return;
        }
        if (lookupNode(typeTestExpr.typeNode)) {
            return;
        }
        this.enclosingNode = typeTestExpr;
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        if (lookupNode(typeTestExpr.expr)) {
            return;
        }
        lookupNode(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        if (lookupNode(annotAccessExpr.expr)) {
            return;
        }
        this.enclosingNode = annotAccessExpr;
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        if (lookupNodes(queryExpr.queryClauseList)) {
            return;
        }
        this.enclosingNode = queryExpr;
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        lookupNode(arrayType.elemtype);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        lookupNode(constrainedType.constraint);
    }

    @Override
    public void visit(BLangStreamType streamType) {
        if (lookupNode(streamType.constraint)) {
            return;
        }
        lookupNode(streamType.error);
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        if (lookupNode(tableType.constraint)) {
            return;
        }
        if (lookupNode(tableType.tableKeySpecifier)) {
            return;
        }
        lookupNode(tableType.tableKeyTypeConstraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        setEnclosingNode(userDefinedType, userDefinedType.typeName.pos);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        if (lookupNodes(functionTypeNode.params)) {
            return;
        }
        if (lookupNode(functionTypeNode.restParam)) {
            return;
        }
        lookupNode(functionTypeNode.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        lookupNodes(unionTypeNode.memberTypeNodes);
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        lookupNodes(intersectionTypeNode.constituentTypeNodes);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        if (lookupNodes(classDefinition.annAttachments)) {
            return;
        }
        if (lookupNodes(classDefinition.fields)) {
            return;
        }
        if (lookupNodes(classDefinition.referencedFields)) {
            return;
        }
        if (lookupNode(classDefinition.initFunction)) {
            return;
        }
        if (lookupNodes(classDefinition.functions)) {
            return;
        }
        if (lookupAnnAttachmentsAttachedToFunctions(classDefinition.functions)) {
            return;
        }
        if (lookupNodes(classDefinition.typeRefs)) {
            return;
        }
        setEnclosingNode(classDefinition, classDefinition.name.pos);
    }

    private boolean lookupAnnAttachmentsAttachedToFunctions(List<BLangFunction> functions) {
        for (BLangFunction func : functions) {
            if (lookupNodes(func.annAttachments)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation) {
        if (lookupNodes(resourceAccessInvocation.annAttachments)) {
            return;
        }
        if (lookupNode(resourceAccessInvocation.expr)) {
            return;
        }
        if (lookupNode(resourceAccessInvocation.resourceAccessPathSegments)) {
            return;
        }
        if (lookupNodes(resourceAccessInvocation.requiredArgs)) {
            return;
        }
        if (lookupNodes(resourceAccessInvocation.restArgs)) {
            return;
        }
        setEnclosingNode(resourceAccessInvocation, resourceAccessInvocation.pos);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        if (lookupNodes(objectTypeNode.fields)) {
            return;
        }
        if (lookupNodes(objectTypeNode.functions)) {
            return;
        }
        lookupNodes(objectTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        if (lookupNodes(recordTypeNode.fields)) {
            return;
        }
        lookupNodes(recordTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        lookupNodes(finiteTypeNode.valueSpace);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        if (lookupNodes(tupleTypeNode.memberTypeNodes)) {
            return;
        }
        lookupNode(tupleTypeNode.restParamType);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        lookupNode(errorType.detailType);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        if (lookupNode(errorConstructorExpr.errorTypeRef)) {
            return;
        }
        if (lookupNodes(errorConstructorExpr.positionalArgs)) {
            return;
        }
        if (lookupNodes(errorConstructorExpr.namedArgs)) {
            return;
        }
        this.enclosingNode = errorConstructorExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        if (lookupNode(fieldAccessExpr.expr)) {
            return;
        }
        lookupNode(fieldAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        if (setEnclosingNode(functionVarRef, functionVarRef.field.pos)) {
            return;
        }
        lookupNode(functionVarRef.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        if (lookupNode(mapKeyAccessExpr.expr)) {
            return;
        }
        lookupNode(mapKeyAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        if (lookupNode(arrayIndexAccessExpr.expr)) {
            return;
        }
        lookupNode(arrayIndexAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
        if (lookupNode(tableKeyAccessExpr.expr)) {
            return;
        }
        lookupNode(tableKeyAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        if (lookupNode(xmlAccessExpr.expr)) {
            return;
        }
        lookupNode(xmlAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        for (RecordLiteralNode.RecordField field : mapLiteral.fields) {
            if (lookupNode((BLangNode) field)) {
                return;
            }
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        for (RecordLiteralNode.RecordField field : structLiteral.fields) {
            if (lookupNode((BLangNode) field)) {
                return;
            }
        }
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        if (lookupNodes(bFunctionPointerInvocation.requiredArgs)) {
            return;
        }
        if (lookupNodes(bFunctionPointerInvocation.restArgs)) {
            return;
        }
        setEnclosingNode(bFunctionPointerInvocation, bFunctionPointerInvocation.name.pos);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        if (setEnclosingNode(iExpr, iExpr.name.pos)) {
            return;
        }

        if (lookupNode(iExpr.expr)) {
            return;
        }
        if (lookupNodes(iExpr.requiredArgs)) {
            return;
        }
        lookupNodes(iExpr.restArgs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        lookupNodes(jsonArrayLiteral.exprs);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        if (lookupNode(jsonAccessExpr.expr)) {
            return;
        }
        lookupNode(jsonAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        if (lookupNode(stringAccessExpr.expr)) {
            return;
        }
        lookupNode(stringAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode, xmlnsNode.prefix.pos)) {
            return;
        }
        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode, xmlnsNode.prefix.pos)) {
            return;
        }

        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        lookupNodes(bLangXMLSequenceLiteral.xmlItems);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        if (lookupNode(bLangStatementExpression.stmt)) {
            return;
        }
        lookupNode(bLangStatementExpression.expr);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        if (lookupNodes(bLangTupleVariable.annAttachments)) {
            return;
        }
        if (lookupNode(bLangTupleVariable.typeNode)) {
            return;
        }
        if (lookupNodes(bLangTupleVariable.memberVariables)) {
            return;
        }
        if (lookupNode(bLangTupleVariable.restVariable)) {
            return;
        }
        lookupNode(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        lookupNode(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        for (BLangRecordVariable.BLangRecordVariableKeyValue var : bLangRecordVariable.variableList) {
            if (lookupNode(var.valueBindingPattern)) {
                return;
            }
        }
        if (lookupNode(bLangRecordVariable.restParam)) {
            return;
        }
        if (lookupNode(bLangRecordVariable.expr)) {
            return;
        }
        lookupNodes(bLangRecordVariable.annAttachments);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        lookupNode(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        if (lookupNodes(bLangErrorVariable.annAttachments)) {
            return;
        }
        if (lookupNode(bLangErrorVariable.typeNode)) {
            return;
        }
        if (lookupNode(bLangErrorVariable.message)) {
            return;
        }

        for (BLangErrorVariable.BLangErrorDetailEntry detail : bLangErrorVariable.detail) {
            if (lookupNode(detail.valueBindingPattern)) {
                return;
            }
        }

        if (lookupNode(bLangErrorVariable.detailExpr)) {
            return;
        }
        if (lookupNode(bLangErrorVariable.cause)) {
            return;
        }
        if (lookupNode(bLangErrorVariable.reasonMatchConst)) {
            return;
        }
        if (lookupNode(bLangErrorVariable.restDetail)) {
            return;
        }
        lookupNode(bLangErrorVariable.expr);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        lookupNode(bLangErrorVariableDef.errorVariable);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        setEnclosingNode(workerFlushExpr, workerFlushExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        if (lookupNode(syncSendExpr.expr)) {
            return;
        }
        setEnclosingNode(syncSendExpr, syncSendExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        lookupNodes(waitForAllExpr.keyValuePairs);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        lookupNodes(waitLiteral.keyValuePairs);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        if (lookupNode(recordKeyValue.key)) {
            return;
        }
        lookupNode(recordKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKey recordKey) {
        lookupNode(recordKey.expr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        lookupNode(spreadOperatorField.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        if (lookupNode(waitKeyValue.keyExpr)) {
            return;
        }
        lookupNode(waitKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        setEnclosingNode(xmlElementFilter, xmlElementFilter.elemNamePos);
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        if (lookupNode(xmlElementAccess.expr)) {
            return;
        }
        if (lookupNodes(xmlElementAccess.filters)) {
            return;
        }
        this.enclosingNode = xmlElementAccess;
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        if (lookupNode(xmlNavigation.expr)) {
            return;
        }
        if (lookupNode(xmlNavigation.childIndex)) {
            return;
        }
        if (lookupNodes(xmlNavigation.filters)) {
            return;
        }
        this.enclosingNode = xmlNavigation;
    }

    @Override
    public void visit(BLangMatchStatement matchStatementNode) {
        if (lookupNode(matchStatementNode.expr)) {
            return;
        }
        if (lookupNodes(matchStatementNode.matchClauses)) {
            return;
        }
        lookupNode(matchStatementNode.onFailClause);
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        if (lookupNodes(matchClause.matchPatterns)) {
            return;
        }
        if (lookupNode(matchClause.matchGuard)) {
            return;
        }
        lookupNode(matchClause.blockStmt);
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern) {
        if (lookupNodes(mappingMatchPattern.fieldMatchPatterns)) {
            return;
        }
        lookupNode(mappingMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern) {
        lookupNode(fieldMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern) {
        if (lookupNodes(listMatchPattern.matchPatterns)) {
            return;
        }
        lookupNode(listMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern) {
        if (lookupNode(errorMatchPattern.errorTypeReference)) {
            return;
        }
        if (lookupNode(errorMatchPattern.errorMessageMatchPattern)) {
            return;
        }
        if (lookupNode(errorMatchPattern.errorCauseMatchPattern)) {
            return;
        }
        lookupNode(errorMatchPattern.errorFieldMatchPatterns);
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern errorMessageMatchPattern) {
        lookupNode(errorMessageMatchPattern.simpleMatchPattern);
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern errorCauseMatchPattern) {
        if (lookupNode(errorCauseMatchPattern.simpleMatchPattern)) {
            return;
        }
        lookupNode(errorCauseMatchPattern.errorMatchPattern);
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns errorFieldMatchPatterns) {
        if (lookupNodes(errorFieldMatchPatterns.namedArgMatchPatterns)) {
            return;
        }
        lookupNode(errorFieldMatchPatterns.restMatchPattern);
    }

    @Override
    public void visit(BLangSimpleMatchPattern simpleMatchPattern) {
        if (lookupNode(simpleMatchPattern.constPattern)) {
            return;
        }
        lookupNode(simpleMatchPattern.varVariableName);
    }

    @Override
    public void visit(BLangNamedArgMatchPattern namedArgMatchPattern) {
        lookupNode(namedArgMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
        lookupNode(matchGuard.expr);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        lookupNode(constMatchPattern.expr);
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        lookupNode(varBindingPattern.getBindingPattern());
    }

    @Override
    public void visit(BLangRestMatchPattern restMatchPattern) {
        // ignore
    }

    @Override
    public void visit(BLangMappingBindingPattern mappingBindingPattern) {
        if (lookupNodes(mappingBindingPattern.fieldBindingPatterns)) {
            return;
        }
        lookupNode(mappingBindingPattern.restBindingPattern);
    }

    @Override
    public void visit(BLangFieldBindingPattern fieldBindingPattern) {
        lookupNode(fieldBindingPattern.bindingPattern);
    }

    @Override
    public void visit(BLangRestBindingPattern restBindingPattern) {
        // ignore
    }

    @Override
    public void visit(BLangErrorBindingPattern errorBindingPattern) {
        if (lookupNode(errorBindingPattern.errorMessageBindingPattern)) {
            return;
        }
        if (lookupNode(errorBindingPattern.errorTypeReference)) {
            return;
        }
        if (lookupNode(errorBindingPattern.errorCauseBindingPattern)) {
            return;
        }
        lookupNode(errorBindingPattern.errorFieldBindingPatterns);
    }

    @Override
    public void visit(BLangErrorMessageBindingPattern errorMessageBindingPattern) {
        lookupNode(errorMessageBindingPattern.simpleBindingPattern);
    }

    @Override
    public void visit(BLangErrorCauseBindingPattern errorCauseBindingPattern) {
        if (lookupNode(errorCauseBindingPattern.simpleBindingPattern)) {
            return;
        }
        lookupNode(errorCauseBindingPattern.errorBindingPattern);
    }

    @Override
    public void visit(BLangErrorFieldBindingPatterns errorFieldBindingPatterns) {
        if (lookupNodes(errorFieldBindingPatterns.namedArgBindingPatterns)) {
            return;
        }
        lookupNode(errorFieldBindingPatterns.restBindingPattern);
    }

    @Override
    public void visit(BLangSimpleBindingPattern simpleBindingPattern) {
        lookupNode(simpleBindingPattern.captureBindingPattern);
    }

    @Override
    public void visit(BLangNamedArgBindingPattern namedArgBindingPattern) {
        lookupNode(namedArgBindingPattern.bindingPattern);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
        setEnclosingNode(captureBindingPattern, captureBindingPattern.getIdentifier().getPosition());
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
        if (lookupNodes(listBindingPattern.bindingPatterns)) {
            return;
        }
        lookupNode(listBindingPattern.restBindingPattern);
    }

    @Override
    public void visit(BLangDo doNode) {
        if (lookupNode(doNode.body)) {
            return;
        }
        lookupNode(doNode.onFailClause);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        this.enclosingNode = literalExpr;
    }

    @Override
    public void visit(BLangValueType valueType) {
        this.enclosingNode = valueType;
    }

    @Override
    public void visit(BLangConstRef constRef) {
        this.enclosingNode = constRef;
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess) {
        this.enclosingNode = nsPrefixedFieldBasedAccess;
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        lookupNode((BLangNode) onFailClause.variableDefinitionNode);
        lookupNode(onFailClause.body);

        // Adding this as the last stmt to ensure that var define in on fail clause will also be considered.
        this.enclosingContainer = onFailClause;
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral) {
        lookupNode(regExpTemplateLiteral.reDisjunction);
    }

    @Override
    public void visit(BLangReSequence reSequence) {
        lookupNodes(reSequence.termList);
    }

    @Override
    public void visit(BLangReAtomQuantifier reAtomQuantifier) {
        if (lookupNode(reAtomQuantifier.atom)) {
            return;
        }
        lookupNode(reAtomQuantifier.quantifier);
    }

    @Override
    public void visit(BLangReAtomCharOrEscape reAtomCharOrEscape) {
        lookupNode(reAtomCharOrEscape.charOrEscape);
    }

    @Override
    public void visit(BLangReQuantifier reQuantifier) {
        if (lookupNode(reQuantifier.quantifier)) {
            return;
        }
        lookupNode(reQuantifier.nonGreedyChar);
    }

    @Override
    public void visit(BLangReCharacterClass reCharacterClass) {
        if (lookupNode(reCharacterClass.characterClassStart)) {
            return;
        }
        if (lookupNode(reCharacterClass.negation)) {
            return;
        }
        if (lookupNode(reCharacterClass.charSet)) {
            return;
        }
        lookupNode(reCharacterClass.characterClassEnd);
    }

    @Override
    public void visit(BLangReCharSet reCharSet) {
        lookupNodes(reCharSet.charSetAtoms);
    }

    @Override
    public void visit(BLangReCharSetRange reCharSetRange) {
        if (lookupNode(reCharSetRange.lhsCharSetAtom)) {
            return;
        }
        if (lookupNode(reCharSetRange.dash)) {
            return;
        }
        lookupNode(reCharSetRange.rhsCharSetAtom);
    }

    @Override
    public void visit(BLangReAssertion reAssertion) {
        lookupNode(reAssertion.assertion);
    }

    @Override
    public void visit(BLangReCapturingGroups reCapturingGroups) {
        lookupNode(reCapturingGroups.openParen);
        if (lookupNode(reCapturingGroups.flagExpr)) {
            return;
        }
        if (lookupNode(reCapturingGroups.disjunction)) {
            return;
        }
        lookupNode(reCapturingGroups.closeParen);
    }

    @Override
    public void visit(BLangReDisjunction reDisjunction) {
        lookupNodes(reDisjunction.sequenceList);
    }

    @Override
    public void visit(BLangReFlagsOnOff reFlagsOnOff) {
        lookupNode(reFlagsOnOff.flags);
    }

    @Override
    public void visit(BLangReFlagExpression reFlagExpression) {
        if (lookupNode(reFlagExpression.questionMark)) {
            return;
        }
        if (lookupNode(reFlagExpression.flagsOnOff)) {
            return;
        }
        lookupNode(reFlagExpression.colon);
    }

    private boolean setEnclosingNode(BLangNode node, Location pos) {
        if (PositionUtil.isRangeWithinNode(this.range, pos)
                && (this.enclosingNode == null
                || PositionUtil.isRangeWithinNode(pos.lineRange(), this.enclosingNode.pos))) {
            this.enclosingNode = node;
            return true;
        }
        if (this.enclosingNode == null && PositionUtil.withinRange(node.pos.lineRange(), this.range)) {
            this.enclosingNode = node;
            return true;
        }
        return false;
    }

    private boolean isLambdaFunction(TopLevelNode node) {
        if (node.getKind() != NodeKind.FUNCTION) {
            return false;
        }

        BLangFunction func = (BLangFunction) node;
        return func.flagSet.contains(Flag.LAMBDA);
    }

    private boolean isClassForService(TopLevelNode node) {
        if (node.getKind() != NodeKind.CLASS_DEFN) {
            return false;
        }

        return ((BLangClassDefinition) node).flagSet.contains(Flag.SERVICE);
    }
}
