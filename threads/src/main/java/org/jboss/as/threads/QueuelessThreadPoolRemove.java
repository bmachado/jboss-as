/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.threads;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.threads.CommonAttributes.BLOCKING;
import static org.jboss.as.threads.CommonAttributes.HANDOFF_EXECUTOR;
import static org.jboss.as.threads.CommonAttributes.KEEPALIVE_TIME;
import static org.jboss.as.threads.CommonAttributes.MAX_THREADS;
import static org.jboss.as.threads.CommonAttributes.PROPERTIES;
import static org.jboss.as.threads.CommonAttributes.THREAD_FACTORY;

import org.jboss.as.controller.Cancellable;
import org.jboss.as.controller.ModelRemoveOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.server.RuntimeOperationContext;
import org.jboss.as.server.RuntimeOperationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

/**
 * Removes a queueless thread pool.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class QueuelessThreadPoolRemove implements RuntimeOperationHandler, ModelRemoveOperationHandler {

    static final OperationHandler INSTANCE = new QueuelessThreadPoolRemove();

    @Override
    public Cancellable execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {

        final ModelNode opAddr = operation.require(OP_ADDR);
        final PathAddress address = PathAddress.pathAddress(opAddr);
        final String name = address.getLastElement().getValue();

        if (context instanceof RuntimeOperationContext) {

            final RuntimeOperationContext runtimeContext = (RuntimeOperationContext) context;
            final ServiceController<?> controller = runtimeContext.getServiceRegistry(). getService(ThreadsServices.threadFactoryName(name));
            if (controller == null) {
                resultHandler.handleResultComplete(null);
                return Cancellable.NULL;
            } else {
                //controller.addListener(new UpdateResultHandler.ServiceRemoveListener<P>(handler, param));
                controller.setMode(Mode.REMOVE);
            }
        }


        // Compensating is add
        final ModelNode model = context.getSubModel();
        final ModelNode compensating = Util.getEmptyOperation(ADD, opAddr);
        if (model.hasDefined(THREAD_FACTORY)) {
            compensating.get(THREAD_FACTORY).set(model.get(THREAD_FACTORY));
        }
        if (model.hasDefined(PROPERTIES)) {
            compensating.get(PROPERTIES).set(model.get(PROPERTIES));
        }
        if (model.hasDefined(MAX_THREADS)) {
            compensating.get(MAX_THREADS).set(model.get(MAX_THREADS));
        }
        if (model.hasDefined(KEEPALIVE_TIME)) {
            compensating.get(KEEPALIVE_TIME).set(model.get(KEEPALIVE_TIME));
        }
        if (model.hasDefined(BLOCKING)) {
            compensating.get(BLOCKING).set(model.get(BLOCKING));
        }
        if (model.hasDefined(HANDOFF_EXECUTOR)) {
            compensating.get(HANDOFF_EXECUTOR).set(model.get(HANDOFF_EXECUTOR));
        }

        resultHandler.handleResultComplete(compensating);

        return Cancellable.NULL;
    }
}
