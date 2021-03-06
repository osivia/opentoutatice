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
package fr.toutatice.ecm.platform.web.adapters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.picture.api.adapters.DefaultPictureAdapter;

public class ToutaticePictureAdapter extends DefaultPictureAdapter {

	@Override
	protected void addViews(List<Map<String, Object>> pictureTemplates,
			String filename, String title) throws IOException, ClientException {
		/* positionner à null le paramètre pictureTemplates pour que les valeurs par défaut soient utilisées
		 * (voir le ticket Jira #7654 - https://jira.nuxeo.com/browse/SUPNXP-7654)
		 */
		List<Map<String, Object>> picTpls = (null != pictureTemplates && pictureTemplates.isEmpty()) ? null : pictureTemplates;
		super.addViews(picTpls, filename, title);
	}

}
