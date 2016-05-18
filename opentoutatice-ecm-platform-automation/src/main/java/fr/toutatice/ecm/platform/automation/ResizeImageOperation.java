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
package fr.toutatice.ecm.platform.automation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.automation.helper.ToutaticeXPathPropertyHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;

@Operation(id = ResizeImageOperation.ID, category = Constants.CAT_CONVERSION, label = "Image resizing operation", description = "Operation to resize the images")
public class ResizeImageOperation {
	public static final String ID = "ImageResize.Operation";

	public static final Log log = LogFactory.getLog(ResizeImageOperation.class);
	private static final float ROUND_RANGE = new Float(0.05);
	
	private static final List<Class<?>> FILTERED_SERVICES_LIST = new ArrayList<Class<?>>() {
        private static final long serialVersionUID = 1L;

        {
            add(EventService.class);
            add(VersioningService.class);
        }
    };
	
	@Context
	protected ImagingService service;

	@Context
	protected CoreSession session;

	@Param(name="xpath_img_in", required=true)
	protected String xpath_img_in;

	@Param(name="xpath_img_out", required=true)
	protected String xpath_img_out;

	@Param(name="img_width", required=true)
	protected int img_width;

	@Param(name="img_heidth", required=true)
	protected int img_heidth;

	@Param(name="enlarge", required=false)
	protected boolean enlarge = false;

	@OperationMethod
	public DocumentModel run(DocumentModel doc) throws PropertyException, ClientException {
	    ResizeImgeSilently runner = new ResizeImgeSilently(session, doc, xpath_img_in, xpath_img_out, img_width, img_heidth, enlarge);
	    runner.silentRun(true, FILTERED_SERVICES_LIST);
        return runner.getDocument();
	}

	@SuppressWarnings("unchecked")
	private Object resize(Object image, int img_width, int img_heidth, boolean enlarge) throws ClientException {
		String imageName = "";
		Blob imageBlob = null;
		Object resizedImage = image;
		
		if (image == null) {
			return null;
		}
		
		if (image instanceof Map) {
			// donnée complexe
			Map<String, Object> imageMap = (Map<String, Object>) image;
			imageBlob = (Blob) imageMap.get("file");
			imageName = (String) imageMap.get("filename");
		} else if (image instanceof Blob) {
			// un blob
			imageBlob = (Blob) image;
		} else {
			log.debug("Paramètre d'entrée inconsistant '" + xpath_img_in + "'. Doit désigner une donnée de type blob ou bien une donnée complexe du type Map<String, Blob>");
		}
		
		if (null != imageBlob) {
			
			ImageInfo info = getImageService().getImageInfo(imageBlob);
			int width = info.getWidth();
			int height = info.getHeight();
			float wScale = (float) img_width / width;
			float hscale = (float) img_heidth / height;
			float scale = Math.min(wScale, hscale);
	
			if (roundToOne(scale) < 1 || (roundToOne(scale) > 1) && enlarge) {
				imageBlob = getImageService().resize(imageBlob, 
						"jpg", 
						(int) (width * scale),
						(int) (height * scale), 
						info.getDepth());
				imageBlob.setMimeType("image/jpeg");
				
				if (image instanceof Map) {
					resizedImage = new HashMap<String, Object>();
					((Map<String, Object>) resizedImage).put("filename", imageName);
					((Map<String, Object>) resizedImage).put("file", imageBlob);
				} else {
					resizedImage = imageBlob;
				}
			}
		}
		
		return resizedImage;
	}
	
	private float roundToOne(float nbr) {
		float rounded = nbr;
		
		if ( ((1 - ROUND_RANGE) < nbr) && ((1 + ROUND_RANGE) > nbr) ) {
			rounded = 1;
		}
		return rounded;
	}
	
	private ImagingService getImageService() throws ClientException {
		if (null == this.service) {
			try {
				service = Framework.getService(ImagingService.class);
			} catch (Exception e) {
				throw new ClientException("Failed to get ImagingService, error: " + e.getMessage());
			}
		}
		
		return this.service;
	}
	
	private class ResizeImgeSilently extends ToutaticeSilentProcessRunnerHelper {
	    
	    private DocumentModel doc;
	    private String xpath_img_in;
	    private String xpath_img_out;
	    private int img_width;
	    private int img_heigth;
	    private boolean enlarge;

        public ResizeImgeSilently(CoreSession session, DocumentModel doc, String xpath_img_in, String xpath_img_out,
                int img_width, int img_heigth, boolean enlarge) {
            super(session);
            this.doc = doc;
            this.xpath_img_in = xpath_img_in;
            this.xpath_img_out = xpath_img_out;
            this.img_width = img_width;
            this.img_heigth = img_heigth; 
            this.enlarge = enlarge;
        }
        
        public DocumentModel getDocument() throws ClientException {
            return this.session.getDocument(this.doc.getRef());             
        }

        @Override
        public void run() throws ClientException {
            ToutaticeXPathPropertyHelper inProperty = new ToutaticeXPathPropertyHelper(this.doc, this.xpath_img_in);

            if (inProperty.getValue() != null) {
                if (inProperty.isList()) {
                    List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
                    
                    // retailler toutes les images d'une liste ("xpath_img_in") et les affecter à une méta-donnée de type liste ("xpath_img_out")
                    List<?> images = (List<?>) inProperty.getValue();
                    
                    for (Object image : images) {
                        Object resizedImage = resize(image, this.img_width, this.img_heigth, this.enlarge);
                        out.add((Map<String, Object>) resizedImage);
                    }
                    
                    this.doc.setPropertyValue(this.xpath_img_out, (Serializable) out);
                    this.session.saveDocument(this.doc);
                } else {
                    // retailler une image ("xpath_img_in") et l'affecter à une méta-donnée scalaire ("xpath_img_out")
                    Object image = inProperty.getValue();
                    Object resizedImage = resize(image, this.img_width, this.img_heigth, this.enlarge);
                    if (null != resizedImage) {
                        this.doc.setPropertyValue(this.xpath_img_out, (Serializable) resizedImage);
                        this.session.saveDocument(this.doc);
                    }
                }
                
            }
            
        }
	    
	}
		

}
