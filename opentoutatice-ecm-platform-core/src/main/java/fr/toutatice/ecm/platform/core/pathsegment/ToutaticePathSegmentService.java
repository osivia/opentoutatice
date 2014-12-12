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

import java.util.regex.Pattern;

import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;

public class ToutaticePathSegmentService implements PathSegmentService {

	public Pattern stupidRegexp = Pattern.compile("^[- .,;?!:/\\\\'\"]*$");
	
	@Override
	public String generatePathSegment(DocumentModel doc) throws ClientException {
		String s = doc.getTitle();
		if (s == null) {
			s = "";
		}
		
		return IdUtils.generateId(s, "-", true, 24);		
	}

	@Override
	public String generatePathSegment(String s) throws ClientException {
		return IdUtils.generateId(s, "-", true, 24);
	}

}
