/**
 * 
 */
package fr.toutatice.ecm.platform.service.customize.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.SchemaDescriptor;
import org.nuxeo.ecm.core.schema.SchemaManager;
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

    private static final int TTC_NB_FACETS = 1;

    private static final String BASE_TYPE_EXT_POINT = "basettc";
    private static final String DOC_TYPE_EXT_POINT = "doctype";

    private static final String[] excludedTypes = {"AdministrativeStatus"};

    private SchemaManager schemaManager;

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        schemaManager = Framework.getService(SchemaManager.class);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (BASE_TYPE_EXT_POINT.equals(extensionPoint)) {
            DocumentTypeDescriptor baseDocTypeDescriptor = (DocumentTypeDescriptor) contribution;
            addToutaticeDocType(baseDocTypeDescriptor);
        }
        if (DOC_TYPE_EXT_POINT.equals(extensionPoint)) {
            DocumentTypeDescriptor docTypeDescriptor = (DocumentTypeDescriptor) contribution;
            addDocTypeContrib(docTypeDescriptor);
        }
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

            if (!ArrayUtils.contains(excludedTypes, type.getName())) {

                if (schemas != null && schemas.size() > 0) {
                    for (String schemaName : schemas) {
                        if (!type.hasSchema(schemaName)) {
                            Schema schema = schemaManager.getSchema(schemaName);
                            type.addSchema(schema);
                        }
                    }
                }

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
                schemaManager.registerDocumentType(type);
            }
        }
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

        SchemaDescriptor[] schemaDescriptors = docTypeDescriptor.schemas;
        Set<String> schemas = SchemaDescriptor.getSchemaNames(schemaDescriptors);
        if (schemas != null && schemas.size() > 0) {
            for (String schemaName : schemas) {
                Schema schema = schemaManager.getSchema(schemaName);
                typeToExtend.addSchema(schema);
            }
        }
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
        schemaManager.registerDocumentType(typeToExtend);
    }

}
