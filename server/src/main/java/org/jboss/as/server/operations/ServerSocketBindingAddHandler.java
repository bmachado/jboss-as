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
package org.jboss.as.server.operations;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FIXED_PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MULTICAST_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MULTICAST_PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.operations.common.SocketBindingAddHandler;
import org.jboss.as.controller.operations.validation.InetAddressValidator;
import org.jboss.as.controller.operations.validation.IntRangeValidator;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.server.RuntimeOperationContext;
import org.jboss.as.server.RuntimeOperationHandler;
import org.jboss.as.server.services.net.NetworkInterfaceBinding;
import org.jboss.as.server.services.net.NetworkInterfaceService;
import org.jboss.as.server.services.net.SocketBinding;
import org.jboss.as.server.services.net.SocketBindingManager;
import org.jboss.as.server.services.net.SocketBindingService;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceTarget;

/**
 * Handler for the server socket-binding resource's add operation.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class ServerSocketBindingAddHandler extends SocketBindingAddHandler implements RuntimeOperationHandler {

    public static final ServerSocketBindingAddHandler INSTANCE = new ServerSocketBindingAddHandler();


    private final ParametersValidator runtimeValidator = new ParametersValidator();

    private ServerSocketBindingAddHandler() {
        runtimeValidator.registerValidator(INTERFACE, new StringLengthValidator(1, Integer.MAX_VALUE, true, false));
        runtimeValidator.registerValidator(PORT, new IntRangeValidator(0, 65535, false, false));
        runtimeValidator.registerValidator(FIXED_PORT, new ModelTypeValidator(ModelType.BOOLEAN, true, false));
        runtimeValidator.registerValidator(MULTICAST_ADDRESS, new InetAddressValidator(true, false));
        runtimeValidator.registerValidator(MULTICAST_PORT, new IntRangeValidator(0, 65535, true, false));
    }

    @Override
    protected void installSocketBinding(String name, ModelNode operation, OperationContext context,
            ResultHandler resultHandler, ModelNode compensatingOp) {
        if (context instanceof RuntimeOperationContext) {

            // Resolve any expressions and re-validate
            final ModelNode resolvedOp = operation.resolve();
            final String failure = runtimeValidator.validate(resolvedOp);
            if (failure == null) {

                final RuntimeOperationContext runtimeContext = (RuntimeOperationContext) context;
                final ServiceTarget serviceTarget = runtimeContext.getServiceTarget();


                final String intf = resolvedOp.get(INTERFACE).isDefined() ? resolvedOp.get(INTERFACE).asString() : null;
                final int port = resolvedOp.get(PORT).asInt();
                final boolean fixedPort = resolvedOp.get(FIXED_PORT).asBoolean(false);
                final String mcastAddr = resolvedOp.get(MULTICAST_ADDRESS).isDefined() ? resolvedOp.get(MULTICAST_ADDRESS).asString() : null;
                final int mcastPort = resolvedOp.get(MULTICAST_PORT).isDefined() ? resolvedOp.get(MULTICAST_PORT).asInt() : 0;
                try {
                    InetAddress mcastInet = mcastAddr == null ? null : InetAddress.getByName(mcastAddr);

                    final SocketBindingService service = new SocketBindingService(name, port, fixedPort, mcastInet, mcastPort);
                    final ServiceBuilder<SocketBinding> builder = serviceTarget.addService(SocketBinding.JBOSS_BINDING_NAME.append(name), service);
                    if (intf != null) {
                        builder.addDependency(NetworkInterfaceService.JBOSS_NETWORK_INTERFACE.append(intf), NetworkInterfaceBinding.class, service.getInterfaceBinding());
                    }
                    builder.addDependency(SocketBindingManager.SOCKET_BINDING_MANAGER, SocketBindingManager.class, service.getSocketBindings())
                        .setInitialMode(Mode.ON_DEMAND)
                        .install();
                    resultHandler.handleResultComplete(compensatingOp);
                } catch (UnknownHostException e) {
                    resultHandler.handleFailed(new ModelNode().set(e.getLocalizedMessage()));
                }
            }
            else {
                resultHandler.handleFailed(new ModelNode().set(failure));
            }
        }
        else {
            resultHandler.handleResultComplete(compensatingOp);
        }
    }

}
