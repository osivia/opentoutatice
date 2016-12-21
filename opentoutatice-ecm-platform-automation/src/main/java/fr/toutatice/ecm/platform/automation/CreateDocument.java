/*
 * (C) Copyright 2016 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 *   kle-helley
 */
package fr.toutatice.ecm.platform.automation;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

@Operation(
		id = CreateDocument.ID,
		category = Constants.CAT_DOCUMENT,
		label = "Create a new document",
		description = "Create a new document in the input folder. If the 'name' parameter is not set, a new name will be derived from the document title, using the same "
				+ "naming strategy as Nuxeo when using the GUI (if the document has a title). This is the only difference between this operation and 'Document.Create', "
				+ "with the latter defaulting the name to 'Untitled'.")
public class CreateDocument {

	public static final String ID = "Document.TTCCreate";
	
	/** DublinCore schema prefix. */
	protected static final String DUBLINCORE_SCHEMA_PREFIX = "dc:";
	/** Title property key. */
	protected static final String PROP_TITLE = "dc:title";

	@Context
	protected CoreSession session;

	@Context
	protected PathSegmentService pathSegmentService;

	@Param(name = "type")
	protected String type;

	@Param(name = "name", required = false)
	protected String name;

	@Param(name = "properties", required = false)
	protected Properties properties;

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(final DocumentModel doc) throws Exception {
	    // Build name from title if any
		if (this.name == null) {
			if ((properties != null) && (properties.get(PROP_TITLE) != null)) {
			    this.name = pathSegmentService.generatePathSegment(properties.get(PROP_TITLE));
			} else {
			    this.name = "Untitled";
			}
		}

		DocumentModel newDoc = this.session.createDocumentModel(doc.getPathAsString(), this.name, this.type);

		if (this.properties != null) {
		    // Get Dublincore properties to save them silently
		    // (to disable DublinCoreListener)
		    Properties dublinCoreProperties = getDublinCoreProperties(this.properties);
		    
		    if(dublinCoreProperties.size() > 0){
		        // Remove DublinCore entries from original properties
		        Set<String> propertiesKeys = this.properties.keySet();
		        propertiesKeys.removeAll(dublinCoreProperties.keySet());
		        
		        // Creates with given DublincoreProperties
		        newDoc = create(this.session, newDoc, this.properties, dublinCoreProperties);
		        
		    } else {
	            // Creates normally
		        newDoc = this.session.createDocument(newDoc);
	        }
		    
		} else {
		    // Creates
		    newDoc = this.session.createDocument(newDoc);
		}
		
		return newDoc;
	}

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(final DocumentRef doc) throws Exception {
		return run(this.session.getDocument(doc));
	}
	
	/**
	 * Extract properties of DublinCore schema from given properties.
	 * 
	 * @param properties
	 * @return properties of DublinCore schema
	 */
	protected Properties getDublinCoreProperties(Properties properties){
	    Properties dcProperties = new Properties();
	    
	    if(properties != null){
	        for(Entry<String, String> property : properties.entrySet()){
	            if(StringUtils.contains(property.getKey(), DUBLINCORE_SCHEMA_PREFIX)){
	                dcProperties.put(property.getKey(), property.getValue());
	            }
	        }
	    }
	    return dcProperties;
	}
	
	/**
	 * Creates document setting Dublincore properties.
	 * 
	 * @param session
	 * @param document
	 * @param properties
	 * @param dublinCoreProperties
	 * @return document
	 * @throws ClientException
	 * @throws IOException
	 */
	protected DocumentModel create(CoreSession session, DocumentModel document, Properties properties, Properties dublinCoreProperties) throws ClientException, IOException {
	    // Create document without given dublincore properties:
	    // DublinCoreListener sets them
	    DocumentHelper.setProperties(session, document, properties);
	    DocumentModel createDocument = session.createDocument(document);
	    
	    // Set dublincore properties and save silently to shortcut DublinCoreListener
	    DocumentHelper.setProperties(session, createDocument, dublinCoreProperties);
	    ToutaticeDocumentHelper.saveDocumentSilently(session, createDocument, false);
	    
	    return createDocument;
	}

}
