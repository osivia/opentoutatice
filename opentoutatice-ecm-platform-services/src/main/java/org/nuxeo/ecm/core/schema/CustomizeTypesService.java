/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *    
 */
package org.nuxeo.ecm.core.schema;

import java.io.Serializable;

import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.platform.types.Type;

/**
 * @author david
 *
 */
public interface CustomizeTypesService extends Serializable {
	
    /**
     * Ajout des schémas toutatice, uid et de la facet Versionable
     * à tous les types de document.
     */
    void addToutaticeDocType(DocumentTypeDescriptor baseDocTypeDescriptor) throws Exception;
	
}
