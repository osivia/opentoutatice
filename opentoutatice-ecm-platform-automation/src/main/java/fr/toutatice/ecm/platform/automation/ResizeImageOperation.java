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
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.automation.helper.ToutaticeXPathPropertyHelper;

@Operation(id = ResizeImageOperation.ID, category = Constants.CAT_CONVERSION, label = "Image resizing operation", description = "Operation to resize the images")
public class ResizeImageOperation {
	public static final String ID = "ImageResize.Operation";

	public static final Log log = LogFactory.getLog(ResizeImageOperation.class);
	private static final float ROUND_RANGE = new Float(0.05);
	
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
	@SuppressWarnings({"unchecked", "rawtypes"})
	public DocumentModel run(DocumentModel doc) throws PropertyException, ClientException {
		ToutaticeXPathPropertyHelper inProperty = new ToutaticeXPathPropertyHelper(doc, xpath_img_in);

		if (inProperty.getValue() != null) {
			if (inProperty.isList()) {
				List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
				
				// retailler toutes les images d'une liste ("xpath_img_in") et les affecter à une méta-donnée de type liste ("xpath_img_out")
				List images = (List) inProperty.getValue();
				
				for (Object image : images) {
					Object resizedImage = resize(image, img_width, img_heidth, enlarge);
					out.add((Map<String, Object>) resizedImage);
				}
				
				doc.setPropertyValue(xpath_img_out, (Serializable) out);
			} else {
				// retailler une image ("xpath_img_in") et l'affecter à une méta-donnée scalaire ("xpath_img_out")
				Object image = inProperty.getValue();
				Object resizedImage = resize(image, img_width, img_heidth, enlarge);
				if (null != resizedImage) {
					doc.setPropertyValue(xpath_img_out, (Serializable) resizedImage);
				}
			}
			
		}
		
		return doc;

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
		

}
