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
package fr.toutatice.ecm.platform.core.pathsegment;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;

public class ToutaticePathSegmentService implements PathSegmentService {

	protected final int maxSize = 24;
	
	/**
	 * Generates path segment based on document's title.
	 */
	@Override
	public String generatePathSegment(DocumentModel doc) throws ClientException {
		String title = doc.getTitle();
		if (title == null) {
			title = StringUtils.EMPTY;
		}
		
		return IdUtils.generateId(title, "-", true, maxSize);		
	}
	
	/**
	 * Generates path segment:
	 * - value is lowercased
	 * - length is 24 at maximum
	 * - empty spaces are replaced by '-'.
	 */
	@Override
	public String generatePathSegment(String value) throws ClientException {
		return IdUtils.generateId(value, "-", true, maxSize);
	}
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public int getMaxSize() {
        return maxSize;
    }

}
