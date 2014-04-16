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
 */
package fr.toutatice.ecm.platform.service.customize.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.SchemaDescriptor;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.CompositeType;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author david
 * 
 */
public class CustomizeTypesServiceImpl extends DefaultComponent implements CustomizeTypesService {

    private static final long serialVersionUID = -3335398967270359400L;

    private static final Log log = LogFactory.getLog(CustomizeTypesServiceImpl.class);

    private static final int TTC_NB_FACETS = 1;

    private static final String BASE_TYPE_EXT_POINT = "basettc";
    private static final String DOC_TYPE_EXT_POINT = "doctype";
    private static final String EXCLUDED_RULES_EXT_POINT = "excludedrules";

    public static final String TYPES_RULE = "types";
    public static final String FACETS_RULE = "facets";
    private Map<String, List<String>> excludedRules;

    /* To log */
    private List<String> exclTypes = new ArrayList<String>();

    private SchemaManager schemaManager;

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        schemaManager = Framework.getService(SchemaManager.class);
        excludedRules = new HashMap<String, List<String>>(0);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (EXCLUDED_RULES_EXT_POINT.equals(extensionPoint)) {
            RulesDescriptor rulesDescriptor = (RulesDescriptor) contribution;
            /* Dependencies induce the deployment of this contribution before the basettc one */
            setExcludedRules(rulesDescriptor);
        }
        if (BASE_TYPE_EXT_POINT.equals(extensionPoint)) {
            DocumentTypeDescriptor baseDocTypeDescriptor = (DocumentTypeDescriptor) contribution;
            addToutaticeDocType(baseDocTypeDescriptor);
        }
        if (DOC_TYPE_EXT_POINT.equals(extensionPoint)) {
            DocumentTypeDescriptor docTypeDescriptor = (DocumentTypeDescriptor) contribution;
            addDocTypeContrib(docTypeDescriptor);
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
            exclTypes.add(type.getName());
        }

        return isExcluded;

    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.ecm.platform.service.customize.types.CustomizeTypesService#addToutaticeDocType(org.nuxeo.ecm.core.schema.DocumentTypeDescriptor)
     */
    @Override
    public void addToutaticeDocType(DocumentTypeDescriptor baseDocTypeDescriptor) {
        SchemaDescriptor[] schemasDescriptor = baseDocTypeDescriptor.schemas;
        Set<String> schemas = SchemaDescriptor.getSchemaNames(schemasDescriptor);
        String[] facetsToAddTab = baseDocTypeDescriptor.facets;

        DocumentType[] types = schemaManager.getDocumentTypes();
        for (DocumentType type : types) {

            if (!verifyExcludedRule(type)) {

                if (facetsToAddTab != null && facetsToAddTab.length > 0) {

                    List<String> facetsToAdd = Arrays.asList(facetsToAddTab);
                    List<String> allFacets = new ArrayList<String>();

                    String[] allFacetsTab = (String[]) facetsToAdd.toArray(new String[TTC_NB_FACETS]);

                    Set<String> facetsOfType = type.getFacets();
                    facetsToAdd = clearFacetsList(facetsToAdd, facetsOfType);

                    if (facetsToAdd.size() > 0) {
                        allFacets.addAll(facetsToAdd);
                    }
                    if (facetsOfType != null && facetsOfType.size() > 0) {
                        allFacets.addAll(facetsOfType);
                    }
                    allFacetsTab = (String[]) allFacets.toArray(new String[allFacets.size()]);
                    type.setDeclaredFacets(allFacetsTab);

                }

                if (schemas != null && schemas.size() > 0) {
                    for (String schemaName : schemas) {
                        if (!type.hasSchema(schemaName)) {
                            Schema schema = schemaManager.getSchema(schemaName);
                            type.addSchema(schema);
                        }
                    }
                }

                schemaManager.registerDocumentType(type);
            }
        }

        log.info("EXCLUDED TYPES: " + exclTypes.toString());
    }

    private List<Schema> getSchemasInFacets(String[] facetsNames) {
        List<Schema> schemasInFacets = new ArrayList<Schema>();
        for (String facetName : facetsNames) {
            CompositeType facet = schemaManager.getFacet(facetName);
            Collection<Schema> schemas = facet.getSchemas();
            schemasInFacets.addAll(schemas);
        }
        return schemasInFacets;
    }

    private List<String> clearFacetsList(List<String> facetsToAdd, Set<String> facetsOfType) {
        List<String> clearedFacets = new ArrayList<String>(facetsToAdd.size());
        clearedFacets.addAll(facetsToAdd);
        for (String facetToAdd : facetsToAdd) {
            for (String facetOfType : facetsOfType) {
                if (facetToAdd.equals(facetOfType)) {
                    clearedFacets.remove(facetToAdd);
                }
            }
        }
        return clearedFacets;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.ecm.platform.service.customize.types.CustomizeTypesService#addDocTypeContrib(org.nuxeo.ecm.core.schema.DocumentTypeDescriptor)
     */
    @Override
    public void addDocTypeContrib(DocumentTypeDescriptor docTypeDescriptor) throws Exception {
        String docTypeName = docTypeDescriptor.name;
        DocumentType typeToExtend = schemaManager.getDocumentType(docTypeName);

        String[] facetsToAddTab = docTypeDescriptor.facets;
        if (facetsToAddTab != null && facetsToAddTab.length > 0) {
            String[] contribFacets = facetsToAddTab;
            Set<String> facetsToExtend = typeToExtend.getFacets();
            if (facetsToExtend != null && facetsToExtend.size() > 0) {
                String[] facetsToExtendTab = (String[]) facetsToExtend.toArray(new String[facetsToExtend.size()]);
                contribFacets = (String[]) ArrayUtils.addAll(contribFacets, facetsToExtendTab);
            }
            typeToExtend.setDeclaredFacets(contribFacets);
        }

        List<Schema> schemasInFacets = getSchemasInFacets(facetsToAddTab);
        if (schemasInFacets != null && schemasInFacets.size() > 0) {
            for (Schema schema : schemasInFacets) {
                typeToExtend.addSchema(schema);
            }
        }

        SchemaDescriptor[] schemaDescriptors = docTypeDescriptor.schemas;
        Set<String> schemas = SchemaDescriptor.getSchemaNames(schemaDescriptors);
        if (schemas != null && schemas.size() > 0) {
            for (String schemaName : schemas) {
                Schema schema = schemaManager.getSchema(schemaName);
                typeToExtend.addSchema(schema);
            }
        }

        schemaManager.registerDocumentType(typeToExtend);
    }

}
