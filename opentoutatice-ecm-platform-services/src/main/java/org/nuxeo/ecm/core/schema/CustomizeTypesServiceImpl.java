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
 * mberhaut1
 * dchevrier
 * lbillon
 */
package org.nuxeo.ecm.core.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author david
 * 
 */
public class CustomizeTypesServiceImpl extends DefaultComponent implements
		CustomizeTypesService {

	private static final long serialVersionUID = -3335398967270359400L;

	private static final Log log = LogFactory
			.getLog(CustomizeTypesService.class);

	public static final String BASE_TYPE_EXT_POINT = "basettc";
	public static final String EXCLUDED_RULES_EXT_POINT = "excludedrules";
	public static final String DOC_TYPE_EXT_POINT = "doctype";

	public static final String TYPES_RULE = "types";
	public static final String FACETS_RULE = "facets";

	private Map<String, List<String>> excludedRules;
	/* To log */
	private List<String> allExcludedTypes = new ArrayList<String>();

	private SchemaManager schemaManager;

	@Override
	public void activate(ComponentContext context) throws Exception {
		super.activate(context);
		schemaManager = Framework.getLocalService(SchemaManager.class);
		excludedRules = new HashMap<String, List<String>>(0);
	}

	@Override
	public void registerContribution(Object contribution,
			String extensionPoint, ComponentInstance contributor)
			throws Exception {
		if (EXCLUDED_RULES_EXT_POINT.equals(extensionPoint)) {
			RulesDescriptor rulesDescriptor = (RulesDescriptor) contribution;
			/*
			 * Dependencies induce the deployment of this contribution before
			 * the basettc one
			 */
			setExcludedRules(rulesDescriptor);
		}
		if (BASE_TYPE_EXT_POINT.equals(extensionPoint)) {
			DocumentTypeDescriptor baseDocTypeDescriptor = (DocumentTypeDescriptor) contribution;
			addToutaticeDocType(baseDocTypeDescriptor);
		}
	}

	public void setExcludedRules(RulesDescriptor rules) {
		String[] types = rules.getTypes();
		if (ArrayUtils.isNotEmpty(types)) {
			excludedRules.put(TYPES_RULE, Arrays.asList(types));
		}
		String[] facets = rules.getFacets();
		if (ArrayUtils.isNotEmpty(facets)) {
			excludedRules.put(FACETS_RULE, Arrays.asList(facets));
		}
	}

	private boolean verifyExcludedRule(DocumentType type) {

		List<String> excludedTypes = excludedRules.get(TYPES_RULE);
		boolean hasExcludedTypes = false;
		if (excludedTypes != null) {
			String typeName = type.getName();
			hasExcludedTypes = excludedTypes.contains(typeName);
		}

		List<String> excludedFacets = excludedRules.get(FACETS_RULE);
		boolean hasExcludedFacet = false;
		if (excludedFacets != null) {
			Set<String> facets = type.getFacets();
			int index = 0;
			while (index < excludedFacets.size() && !hasExcludedFacet) {
				hasExcludedFacet = facets.contains(excludedFacets.get(index));
				index++;
			}
		}

		boolean isExcluded = hasExcludedTypes || hasExcludedFacet;

		if (isExcluded) {
			allExcludedTypes.add(type.getName());
		}

		return isExcluded;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.toutatice.ecm.platform.service.customize.types.CustomizeTypesService
	 * #addToutaticeDocType(org.nuxeo.ecm.core.schema.DocumentTypeDescriptor)
	 */
	@Override
	public void addToutaticeDocType(DocumentTypeDescriptor baseDocTypeDescriptor)
			throws Exception {

		/* getDocumentTypes() induces recomputing */
		DocumentType[] types = schemaManager.getDocumentTypes();

		SchemaManagerImpl schemaManagerImpl = ((SchemaManagerImpl) schemaManager);
		List<DocumentTypeDescriptor> excludedDocTypesDesc = new ArrayList<DocumentTypeDescriptor>();
		Map<String, DocumentTypeImpl> excludedDocTypes = new HashMap<String, DocumentTypeImpl>();

		for (DocumentType type : types) {
			String name = type.getName();
			if (!TypeConstants.DOCUMENT.equals(name)) {
				if (!verifyExcludedRule(type)) {
					DocumentTypeDescriptor baseDocTypeDesc = baseDocTypeDescriptor
							.clone();
					baseDocTypeDesc.name = name;

					schemaManagerImpl.registerDocumentType(baseDocTypeDesc);
				} else {
					DocumentTypeDescriptor exDocTypeDesc = schemaManagerImpl
							.getDocumentTypeDescriptor(name);
					excludedDocTypesDesc.add(exDocTypeDesc);
					excludedDocTypes.put(name, (DocumentTypeImpl) type);
				}
			}
		}

		/* To del excluded types from recomputing */
		schemaManagerImpl.allDocumentTypes.removeAll(excludedDocTypesDesc);
		schemaManagerImpl.recompute();
		schemaManagerImpl.allDocumentTypes.addAll(excludedDocTypesDesc);
		schemaManagerImpl.documentTypes.putAll(excludedDocTypes);
		/*
		 * To avoid recomputing in flushPendingRegistration() method of
		 * SchemaManagerImpl
		 */
		schemaManagerImpl.dirty = false;

		log.warn("Excluded Types from toutatice's schema setting: "
				+ allExcludedTypes.toString());

	}

}
