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
package fr.toutatice.ecm.platform.core.publish;

import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;

public class ToutaticeNullPublishedDocument implements PublishedDocument {

	private static final long serialVersionUID = 1L;

	@Override
	public DocumentRef getSourceDocumentRef() {
		return null;
	}

	@Override
	public String getSourceRepositoryName() {
		return null;
	}

	@Override
	public String getSourceServer() {
		return null;
	}

	@Override
	public String getSourceVersionLabel() {
		return null;
	}

	@Override
	public String getPath() {
		return "/NULL_PUBLISHED_DOCUMENT_PATH";
	}

	@Override
	public String getParentPath() {
		return null;
	}

	@Override
	public boolean isPending() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.LOCAL;
	}

}
