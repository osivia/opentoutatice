package fr.toutatice.ecm.platform.automation;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.services.permalink.PermaLinkService;



@Operation(id = GetPermalink.ID, category = Constants.CAT_DOCUMENT, label = "Get permalink", description = "")
public class GetPermalink {
	 protected transient PermaLinkService permaLinkService;
	 
	@Param(name = "codec", required = false)
	protected String codec;

	public static final String ID="Document.permalink";
	
	@OperationMethod
	public Blob run(DocumentModel doc) throws Exception{
		
		
		if (permaLinkService == null) {
            permaLinkService = Framework.getService(PermaLinkService.class);
        }
         
		
		String link = permaLinkService.getPermalink(doc, codec);
		
		// build response blob
		JSONObject jso = new JSONObject();
		if(StringUtils.isNotBlank(link)){
			jso.element("permalink", link);
		}
		Blob res = new StringBlob(jso.toString(),"application/json");
				
		return res;
	}
}
