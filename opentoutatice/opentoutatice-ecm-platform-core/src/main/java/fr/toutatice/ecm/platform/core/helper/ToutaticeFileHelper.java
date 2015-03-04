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
package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.mimetype.MimetypeDetectionException;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;

public class ToutaticeFileHelper {
	private static final Log log = LogFactory.getLog(ToutaticeFileHelper.class);
	
	private static ToutaticeFileHelper instance;
	private static MimetypeRegistry mimeService;

	private static final String CST_DEFAULT_MIME_TYPE = "application/octet-stream";
	private static final List<String> ACCEPTED_IMAGE_MIME_TYPES = new ArrayList<String>();
	static {
		ACCEPTED_IMAGE_MIME_TYPES.add("image/jpeg");
		ACCEPTED_IMAGE_MIME_TYPES.add("image/gif");
		ACCEPTED_IMAGE_MIME_TYPES.add("image/png");
		ACCEPTED_IMAGE_MIME_TYPES.add("image/svg+xml");
		ACCEPTED_IMAGE_MIME_TYPES.add("image/tiff");
		ACCEPTED_IMAGE_MIME_TYPES.add("image/tiff-fx");
	}
	
	private ToutaticeFileHelper() {
		// singleton
	}

	public static ToutaticeFileHelper instance() {
		if (null == instance) {
			instance = new ToutaticeFileHelper();
			getMimeService();
		}
		return instance;
	}
	
	public String getFileMimeType(String fileName, Blob blob) {
		String detectedMimeType = CST_DEFAULT_MIME_TYPE;
		
		try {
			String dmt = mimeService.getMimetypeFromFilenameAndBlobWithDefault(fileName, blob, null);
			if (StringUtils.isNotBlank(dmt)) {
				detectedMimeType = dmt;
			}
		} catch (MimetypeDetectionException e) {
			log.warn("Failed to determine the mime type of the file '" + fileName + "', error: " + e.getMessage());
		}

		return detectedMimeType;
	}
	
	public boolean isImageTypeFile(String fileName, Blob blob) {
        String mimeType = ToutaticeFileHelper.instance().getFileMimeType(fileName, blob);
        return ACCEPTED_IMAGE_MIME_TYPES.contains(mimeType);
	}
	
	/**
	 * Initialize the service attribute
	 */
	 private static void getMimeService() {
		 try {
			 if (null == mimeService) {
				 mimeService = Framework.getService(MimetypeRegistry.class);
			 }
		 } catch (Exception e) {
			 log.error("Failed to get the mime service, exception message: " + e.getMessage());
		 }
	 }
}
