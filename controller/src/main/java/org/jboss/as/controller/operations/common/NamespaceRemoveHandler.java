/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.controller.operations.common;


import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAMESPACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAMESPACES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.Locale;

import org.jboss.as.controller.Cancellable;
import org.jboss.as.controller.ModelUpdateOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.operations.validation.ParameterValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;

/**
 * Handler for the root resource remove-namespace operation.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class NamespaceRemoveHandler implements ModelUpdateOperationHandler, DescriptionProvider {

    public static final String OPERATION_NAME = "remove-namespace";

    public static final NamespaceRemoveHandler INSTANCE = new NamespaceRemoveHandler();

    public static ModelNode getRemoveNamespaceOperation(ModelNode address, String prefix) {
        ModelNode op = new ModelNode();
        op.get(OP).set(OPERATION_NAME);
        op.get(OP_ADDR).set(address);
        op.get(NAMESPACE).set(prefix);
        return op;
    }

    private final ParameterValidator typeValidator = new ModelTypeValidator(ModelType.STRING);

    /**
     * Create the RemoveNamespaceHandler
     */
    private NamespaceRemoveHandler() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cancellable execute(OperationContext context, ModelNode operation, ResultHandler resultHandler) {
        try {
            ModelNode param = operation.get(NAMESPACE);
            ModelNode namespaces = context.getSubModel().get(NAMESPACES);
            ModelNode toRemove = null;
            String failure = typeValidator.validateParameter(NAMESPACE, param);
            if (failure == null) {
                ModelNode newList = new ModelNode().setEmptyList();
                String prefix = param.asProperty().getName();
                if (namespaces.isDefined()) {
                    for (Property namespace : namespaces.asPropertyList()) {
                        if (!prefix.equals(namespace.getName())) {
                            toRemove = newList.add(namespace.getName(), namespace.getValue());
                            break;
                        }
                    }
                }

                if (toRemove != null) {
                    namespaces.set(newList);
                    ModelNode compensating = NamespaceAddHandler.getAddNamespaceOperation(operation.get(OP_ADDR), toRemove);
                    resultHandler.handleResultComplete(compensating);
                }
                else {
                    failure = "No namespace with URI " + prefix + "found";
                }
            }

            if (failure != null) {
                resultHandler.handleFailed(new ModelNode().set(failure));
            }
        }
        catch (Exception e) {
            resultHandler.handleFailed(new ModelNode().set(e.getLocalizedMessage()));
        }
        return Cancellable.NULL;
    }

    @Override
    public ModelNode getModelDescription(Locale locale) {
        return CommonDescriptions.getRemoveNamespaceOperation(locale);
    }

}
