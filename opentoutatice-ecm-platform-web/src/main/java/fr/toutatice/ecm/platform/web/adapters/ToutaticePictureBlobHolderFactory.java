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

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureBlobHolderFactory;

/**
 * Cette surcharge a été rendue nécessaire pour le bon fonctionnement de la vue "toutatice_view" sur un document de type "PictureBook".
 * Le ticket Jira suivant identifie le souci: https://jira.nuxeo.com/browse/SUPNXP-7421
 */
public class ToutaticePictureBlobHolderFactory extends PictureBlobHolderFactory {

	@Override
	public BlobHolder getBlobHolder(DocumentModel doc) {
		BlobHolder bh = null;
		
		if ("PictureBook".equals(doc.getType())) {
			bh = new ToutaticePictureBookBlobHolder(doc, "");
		} else {
			bh = super.getBlobHolder(doc);
		}
		
		return bh;
	}

}
