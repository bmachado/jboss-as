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

package org.jboss.as.managedbean;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Cancellable;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelQueryOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * Domain extension used to initialize the managed bean subsystem element handlers.
 *
 * @author John E. Bailey
 * @author Emanuel Muckenhuber
 */
public class ManagedBeansExtension implements Extension {

    public static final String SUBSYSTEM_NAME = "managed-beans";
    public static final String NAMESPACE = "urn:jboss:domain:managedbeans:1.0";

    private static final ManagedBeanSubsystemElementParser parser = new ManagedBeanSubsystemElementParser();
    private static final DescriptionProvider DESCRIPTION = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return new ModelNode();
        }
    };

    /** {@inheritDoc} */
    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration registration = context.registerSubsystem(SUBSYSTEM_NAME);
        final ModelNodeRegistration nodeRegistration = registration.registerSubsystemModel(DESCRIPTION);
        nodeRegistration.registerOperationHandler(ADD, ManagedBeansSubsystemAdd.INSTANCE, DESCRIPTION, false);
        nodeRegistration.registerOperationHandler(DESCRIBE, ManagedBeansDescribeHandler.INSTANCE, ManagedBeansDescribeHandler.INSTANCE, false);
        registration.registerXMLElementWriter(parser);

    }

    /** {@inheritDoc} */
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(NAMESPACE, parser);
    }

    private static ModelNode createAddOperation() {
        final ModelNode update = new ModelNode();
        update.get(OP).set(ADD);
        update.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
        return update;
    }

    static class ManagedBeanSubsystemElementParser implements XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        /** {@inheritDoc} */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            ParseUtils.requireNoAttributes(reader);
            ParseUtils.requireNoContent(reader);
            final ModelNode update = new ModelNode();
            update.get(OP).set(ADD);
            update.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
            list.add(createAddOperation());
        }

        /** {@inheritDoc} */
        @Override
        public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
            //TODO seems to be a problem with empty elements cleaning up the queue in FormattingXMLStreamWriter.runAttrQueue
            //context.startSubsystemElement(NewManagedBeansExtension.NAMESPACE, true);
            context.startSubsystemElement(ManagedBeansExtension.NAMESPACE, false);
            writer.writeEndElement();

        }
    }

    private static class ManagedBeansDescribeHandler implements ModelQueryOperationHandler, DescriptionProvider {
        static final ManagedBeansDescribeHandler INSTANCE = new ManagedBeansDescribeHandler();
        @Override
        public Cancellable execute(OperationContext context, ModelNode operation, ResultHandler resultHandler) {
            ModelNode node = new ModelNode();
            node.add(createAddOperation());

            resultHandler.handleResultFragment(Util.NO_LOCATION, node);
            resultHandler.handleResultComplete(new ModelNode());
            return Cancellable.NULL;
        }

        @Override
        public ModelNode getModelDescription(Locale locale) {
            return CommonDescriptions.getSubsystemDescribeOperation(locale);
        }
    }

}
