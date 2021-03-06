/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.as.security;

/**
 * Attributes used by the security subsystem.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
interface CommonAttributes {

    String AUTHENTICATION_MANAGER_CLASS_NAME = "authentication-manager-class-name";
    String DEEP_COPY_SUBJECT_MODE = "deep-copy-subject-mode";
    String DEFAULT_CALLBACK_HANDLER_CLASS_NAME = "default-callback-handler-class-name";
    String JAAS_APPLICATION_POLICY = "jaas-application-policy";
    String MODULE_OPTIONS = "module-options";
    String SUBJECT_FACTORY_CLASS_NAME = "subject-factory-class-name";
}
