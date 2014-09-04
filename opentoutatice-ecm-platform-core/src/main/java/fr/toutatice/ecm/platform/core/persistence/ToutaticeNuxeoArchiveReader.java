package fr.toutatice.ecm.platform.core.persistence;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.nuxeo.ecm.core.NXCore;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.io.ExportConstants;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.NuxeoArchiveReader;
import org.nuxeo.ecm.core.lifecycle.LifeCycle;
import org.nuxeo.ecm.core.lifecycle.LifeCycleService;
import org.nuxeo.ecm.core.schema.FacetNames;

public class ToutaticeNuxeoArchiveReader extends NuxeoArchiveReader {

	private static final Log log = LogFactory.getLog(ToutaticeNuxeoArchiveReader.class);

	private LifeCycleService LCService;
	private Map<String, String> defaultInitialLifeCycleState;
	
	public ToutaticeNuxeoArchiveReader(File file) throws IOException {
		super(file);
		this.defaultInitialLifeCycleState = new HashMap<String, String>();
	}

	@Override
	public ExportedDocument read() throws IOException {
		ExportedDocument expDoc = super.read();

		/* Filtrer les documents:
		* - Immutable documents
		* - Deleted
		* 
		*  Re-positionner les documents dans l'état initial (project)
		*/
		try {
			if (null != expDoc) {
				Document doc = expDoc.getDocument();
				Element system = doc.getRootElement().element(ExportConstants.SYSTEM_TAG);
				if (null != system) {
					// Récupérer l'état intial par défaut du document
					String initialLifeCycleState = getDefaultLifeCycleStateForDoc(doc);
					
					// Filtrer les documents supprimés
					DefaultElement lcpolicy = (DefaultElement) system.element(ExportConstants.LIFECYCLE_POLICY_TAG);
					DefaultElement lcs = (DefaultElement) system.element(ExportConstants.LIFECYCLE_STATE_TAG);
					if (null != lcs && LifeCycleConstants.DELETED_STATE.equals(lcs.getTextTrim())) {
						// Lire le document suivant
						expDoc = read();
					}
					
					// Filtrer les documents Immutable (proxy, version...)
					Iterator<?> itr = system.elementIterator(ExportConstants.FACET_TAG);
					while (null != itr && itr.hasNext()) {
						DefaultElement facet = (DefaultElement) itr.next();
						String value = facet.getTextTrim();
						if (FacetNames.IMMUTABLE.equals(value)) {
							// Lire le document suivant
							expDoc = read();
							break;
						}
					}
					
					// Forcer le passage dans l'état initial (except for Content Routing documents)
					if (null != initialLifeCycleState
						&& (null != lcpolicy &&  !"documentRouteElement".equals(lcpolicy.getTextTrim())) ) {
						lcs.setText(initialLifeCycleState);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Failed to filter document list while import" + ((StringUtils.isNotBlank(expDoc.getId())) ? " (" + expDoc.getId() + ")" : "") + ", error: "+ e.getMessage());
		}

		return expDoc;
	}
	
	private String getDefaultLifeCycleStateForDoc(Document doc) {
		String defaultState = null;

		DefaultElement type = (DefaultElement) doc.getRootElement().element(ExportConstants.SYSTEM_TAG).element(ExportConstants.TYPE_TAG);
		if (null != type) {
			if (!this.defaultInitialLifeCycleState.containsKey(type.getTextTrim())) {
				if (null != getLFService()) {
					String lcName = getLFService().getLifeCycleNameFor(type.getTextTrim());
					LifeCycle lc = getLFService().getLifeCycleByName(lcName);
					this.defaultInitialLifeCycleState.put(type.getTextTrim(), lc.getDefaultInitialStateName());					
				}
			}
			
			defaultState = this.defaultInitialLifeCycleState.get(type.getTextTrim());
		}
		
		return defaultState;
	}
	
	private LifeCycleService getLFService() {
		if (null == this.LCService) {
			this.LCService = NXCore.getLifeCycleService();
		}
		return this.LCService;
	}

}
