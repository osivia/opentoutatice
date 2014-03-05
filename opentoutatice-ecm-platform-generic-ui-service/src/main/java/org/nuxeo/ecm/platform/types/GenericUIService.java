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
package org.nuxeo.ecm.platform.types;

import java.io.Serializable;

/**
 * @author david
 *
 */
public interface GenericUIService extends Serializable {
	
    /**
     * Méthode permettant d'jouter dans des vues des documents des layouts génériques
     * ou transverses à tous les documents.
     */
	void addGenericUIElements(GenericLayoutsDescriptor genericLayoutsDescriptor) throws Exception;
	
}
