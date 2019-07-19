/**
 * 
 */
package org.opentoutatice.core.recursive.copy.partial.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;
import org.opentoutatice.core.recursive.copy.partial.ResetableSchemasRegister;

import fr.toutatice.ecm.platform.core.listener.ToutaticeDocumentEventListenerHelper;

/**
 * @author david
 *
 */
public class PartialDocumentCopyListener implements EventListener {
	
	private static final Log log = LogFactory.getLog(PartialDocumentCopyListener.class);
	
	@Override
	public void handleEvent(Event event) throws ClientException {
		
		if(DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(event.getName()) && (event.getContext() instanceof DocumentEventContext)) {
			
			DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
			DocumentModel copiedDoc = docCtx.getSourceDocument();
			
			if(copiedDoc != null && ToutaticeDocumentEventListenerHelper.isAlterableDocument(copiedDoc)) {
				
				// Get resetable schemas
				Set<String> schemasNames = ((ResetableSchemasRegister) Framework.getService(ResetableSchemasRegister.class)).getResetableSchemasNames();
				if(schemasNames != null) {
					
					for(String schemaName : schemasNames) {
						
						if(log.isDebugEnabled()) {
							log.debug(String.format("Trying to reset schema [%s] on document [%s] ...", schemaName, copiedDoc.getPathAsString()));
						}
						
						if(copiedDoc.hasSchema(schemaName)) {
							if(log.isDebugEnabled()) {
								log.debug(String.format("Document [%s] has schema [%s]: reseting ...", copiedDoc.getPathAsString(), schemaName));
							}
							
							// Load data for given schema
							Map<String, Object> properties = copiedDoc.getProperties(schemaName);
							if(properties != null) {
								// Reset properties
								Map<String, Object> resetMap = new HashMap<>();
								
								// Hard reset to null
								for(String propName : properties.keySet()) {
									resetMap.put(propName, null);
									
									if(log.isTraceEnabled()) {
										log.trace(String.format("Property [%s] reset to null", propName));
									}
								}
								
								// Update document model
								copiedDoc.setProperties(schemaName, resetMap);
								
								if(log.isDebugEnabled()) {
									log.debug(String.format("Schema [%s] reset on document [%s]", schemaName, copiedDoc.getPathAsString()));
								}
							}
							
						}
					}
				}
			}
		}
		
	}

}
