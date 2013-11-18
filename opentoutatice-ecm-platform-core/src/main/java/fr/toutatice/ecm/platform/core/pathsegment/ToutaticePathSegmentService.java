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

}
