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
