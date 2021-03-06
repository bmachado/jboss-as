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

package org.jboss.as.server;

import org.jboss.as.server.deployment.module.DeploymentModuleLoader;
import org.jboss.msc.service.ServiceName;

/**
 * A holder class for constants containing the names of the core services.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class Services {

    private Services() {
    }

    /**
     * The service name of the root application server service.
     */
    public static final ServiceName JBOSS_AS = ServiceName.JBOSS.append("as");

    /**
     * The service corresponding to the {@link ServerController} for this instance.
     */
    public static final ServiceName JBOSS_SERVER_CONTROLLER = JBOSS_AS.append("server-controller");

    /**
     * The service corresponding to the {@link DeploymentModuleLoader} for this instance.
     */
    public static final ServiceName JBOSS_DEPLOYMENT_MODULE_LOADER = JBOSS_AS.append("deployment-module-loader");

    /**
     * The internal deployer chains service used by all deployments.
     */
    public static final ServiceName JBOSS_DEPLOYER_CHAINS = JBOSS_AS.append("deployer-chains");
}
