package fr.toutatice.ecm.platform.web.publication.finder;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.publisher.helper.RootSectionFinder;
import org.nuxeo.ecm.platform.publisher.helper.RootSectionFinderFactory;

public class ToutaticeRootSectionsFinderFactory implements	RootSectionFinderFactory {

	@Override
	public RootSectionFinder getRootSectionFinder(CoreSession session) {
		return new ToutaticeRootSectionsFinder(session);
	}

}
