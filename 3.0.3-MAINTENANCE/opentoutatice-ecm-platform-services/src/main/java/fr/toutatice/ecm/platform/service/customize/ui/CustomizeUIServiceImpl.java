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
 */
package fr.toutatice.ecm.platform.service.customize.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ELException;
import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.contentview.jsf.ContentView;
import org.nuxeo.ecm.platform.contentview.jsf.ContentViewLayout;
import org.nuxeo.ecm.platform.contentview.jsf.ContentViewService;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRowDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetReference;
import org.nuxeo.ecm.platform.forms.layout.api.impl.LayoutRowDefinitionImpl;
import org.nuxeo.ecm.platform.forms.layout.api.impl.WidgetReferenceImpl;
import org.nuxeo.ecm.platform.forms.layout.service.WebLayoutManager;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderDefinition;
import org.nuxeo.ecm.platform.query.api.WhereClauseDefinition;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


/**
 * @author david
 * 
 */
public class CustomizeUIServiceImpl extends DefaultComponent implements CustomizeUIService {

    private static final long serialVersionUID = 6462426331447513648L;
    private static final Log log = LogFactory.getLog(CustomizeUIServiceImpl.class);

    private static final String QUERY_WITH_NO_PROXY = "AND ((ecm:name NOT LIKE '%.proxy') OR (ecm:name LIKE '%.remote.proxy'))";
    private static final String CONTENT_CATEGORY = "content";
    private static final String PORTAL_LAYOUT = "cv_ContentOrderedReadNWrite@cvListingLayout";

    private static final String LAYOUTS_PT_EXT = "layouts";
    private static final String CATEGORIES_PT_EXT = "categories";

    private TypeManager typeManager;
    private ContentViewService contentViewService;
    private WebLayoutManager webLayoutManager;
    private SchemaManager schemaManager;

    private Collection<Type> allowedTypesUnderPublishSpace;
    private static List<String> modifiedContentViewsLayouts = new ArrayList<String>();

    private List<LayoutsDescriptor> allLayoutsDescriptor;
    private List<String> categories;

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        typeManager = Framework.getService(TypeManager.class);
        contentViewService = Framework.getService(ContentViewService.class);
        webLayoutManager = Framework.getService(WebLayoutManager.class);
        schemaManager = Framework.getService(SchemaManager.class); 
        
        allLayoutsDescriptor = new ArrayList<LayoutsDescriptor>();
        categories = new ArrayList<String>();
        categories.add(CONTENT_CATEGORY);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (LAYOUTS_PT_EXT.equals(extensionPoint)) {
            LayoutsDescriptor layoutsDescriptor = (LayoutsDescriptor) contribution;
            allLayoutsDescriptor.add(layoutsDescriptor);
        } else if(CATEGORIES_PT_EXT.equals(extensionPoint)){
            CategoryDescriptor categoryDescriptor = (CategoryDescriptor) contribution;
            categories.add(categoryDescriptor.getCategory());
        }
    }

    @Override
    public void applicationStarted(ComponentContext context) throws Exception {
        Set<String> publishSpaces = schemaManager.getDocumentTypeNamesForFacet(ToutaticeNuxeoStudioConst.CST_DOC_FACET_TTC_PUBLISH_SPACE);
        
        for(String publishSpace : publishSpaces){
            
            Type psType = typeManager.getType(publishSpace);
            allowedTypesUnderPublishSpace = getAllowedTypesUnderPublishSpace(new ArrayList<Type>(), psType);
        adaptContentViews();
        for (LayoutsDescriptor layoutsDescriptor : allLayoutsDescriptor) {
            overrideLayoutsTemplate(layoutsDescriptor);
            }
            
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.ecm.platform.service.cutomize.ui.CustomizeUIService#adaptContentViews()
     */
    @Override
    public void adaptContentViews() throws Exception {

        if (allowedTypesUnderPublishSpace != null) {
            Collection<Type> types = typeManager.getTypes();
            for (Type type : types) {
                if (allowedTypesUnderPublishSpace.contains(type)) {
                    addPublishWidgetToResultLayout(type);
                }
                setNoProxyQueryToContentViews(type);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.toutatice.ecm.platform.service.customize.ui.CustomizeUIService#overrideLayoutsTemplate(fr.toutatice.ecm.platform.service.customize.ui.LayoutsDescriptor
     * )
     */
    @Override
    public void overrideLayoutsTemplate(LayoutsDescriptor layoutsDescriptor) {
        
        LayoutDescriptor[] layoutsToOverride = layoutsDescriptor.getLayouts();
        
        if (ArrayUtils.isNotEmpty(layoutsToOverride)) {
            
            for (LayoutDescriptor layoutToOverride : layoutsToOverride) {
                
                String layoutNameToOverride = layoutToOverride.getName();
                TemplateDescriptor[] templatesDescriptor = layoutToOverride.getTemplates();

                for (TemplateDescriptor templateDescriptor : templatesDescriptor) {
                    String overrideTemplateName = templateDescriptor.getName();
                    String mode = templateDescriptor.getMode();

                    LayoutDefinition layoutToOverrideDef = webLayoutManager.getLayoutDefinition(layoutNameToOverride);
                    Map<String, String> templatesByMode = layoutToOverrideDef.getTemplates();

                    // for (String mode : modes) {
                    templatesByMode.put(mode, overrideTemplateName);
                    // }
                    layoutToOverrideDef.setTemplates(templatesByMode);
                }
            }
        }

    }

    /**
     * Méthode permettant d'ajouter la colonne (et le widget) de status de publication locale
     * aux tableaux de résultats de la vue Contenu.
     */
    private void addPublishWidgetToResultLayout(Type type) throws Exception {

        for(String cvCategory : categories){
        
            String[] typeContentViewNames = type.getContentViews(cvCategory);
            
            if (ArrayUtils.isNotEmpty(typeContentViewNames)) {

            for (String typeContentViewName : typeContentViewNames) {
                ContentView contentView = contentViewService.getContentView(typeContentViewName);

                if (contentView != null) {
                    List<ContentViewLayout> resultLayouts = contentView.getResultLayouts();

                    for (ContentViewLayout resultLayout : resultLayouts) {

                        String layoutName = resultLayout.getName();

                        if (!modifiedContentViewsLayouts.contains(layoutName) && !PORTAL_LAYOUT.equals(layoutName)) {
                            modifiedContentViewsLayouts.add(layoutName);

                            LayoutDefinition layoutDefinition = webLayoutManager.getLayoutDefinition(layoutName);
                            LayoutRowDefinition[] rows = layoutDefinition.getRows();
                                
                                if(widgetNotYetPresent(layoutDefinition, rows)){

                            List<WidgetReference> widgets = new ArrayList<WidgetReference>(1);
                            WidgetReference widgetRef = new WidgetReferenceImpl("publishing_status");
                            widgets.add(widgetRef);

                            Map<String, Map<String, Serializable>> properties = new HashMap<String, Map<String, Serializable>>();
                            Map<String, Serializable> property = new HashMap<String, Serializable>();
                            property.put("useFirstWidgetLabelAsColumnHeader", true);
                            property.put("columnStyleClass", "iconColumn");
                            properties.put("any", property);

                            LayoutRowDefinitionImpl layoutRowDefinitionImpl = new LayoutRowDefinitionImpl("publishing_status", properties, widgets, true, true);
                            LayoutRowDefinition[] modifiedRows = (LayoutRowDefinition[]) Arrays.copyOf(rows, rows.length + 1);
                            modifiedRows[rows.length] = layoutRowDefinitionImpl;

                            layoutDefinition.setRows(modifiedRows);
                                
                                }
                        }
                    }
                } else {
                    log.error(typeContentViewName + " not defined for type " + type.getLabel());
                }
            }
        }
            
        }
    }
    
    /**
     * 
     * @param rows
     * @return true if publishing_status widget yet added in rows.
     */
    private boolean widgetNotYetPresent(LayoutDefinition layoutDefinition, LayoutRowDefinition[] rows) {
        boolean added = false;

        if (ArrayUtils.isNotEmpty(rows)) {
            List<LayoutRowDefinition> rowsList = Arrays.asList(rows);
            Iterator<LayoutRowDefinition> iterator = rowsList.iterator();

            while (iterator.hasNext() && !added) {
                LayoutRowDefinition row = iterator.next();
                if ("publishing_status".equals(row.getName())) {
                    added = true;
                }
                if (!added) {
                    WidgetReference[] widgetReferences = row.getWidgetReferences();

                    if (ArrayUtils.isNotEmpty(widgetReferences)) {

                        WidgetReference widgetReference = widgetReferences[0];
                        WidgetDefinition widgetDefinition = layoutDefinition.getWidgetDefinition(widgetReference.getName());

                        if (widgetDefinition != null) {
                            Map<String, Map<String, Serializable>> properties = widgetDefinition.getProperties();

                            Map<String, Serializable> anyProperties = properties.get("any");
                            Map<String, Serializable> viewProperties = properties.get("view");

                            Map<String, Serializable> wgtProperties = new HashMap<String, Serializable>();
                            if (viewProperties != null) {
                                wgtProperties.putAll(viewProperties);
                            }
                            if (anyProperties != null) {
                                wgtProperties.putAll(anyProperties);
                            }

                            Serializable template = wgtProperties.get("template");
                            if (template != null) {
                                String tmpl = (String) template;
                                added = StringUtils.equals(tmpl, "/widgets/toutatice_widget_document_listing_icon.xhtml");
                            }


                        }

                    }
                }
            }
        }

        return !added;
    }

    /**
     * Méthode permettant de supprimer les proxies locaux des résultats
     * des requêtes de toutes les contentViews
     */
    private void setNoProxyQueryToContentViews(Type type) throws Exception {

        /*
         * Pour éviter les logs d'erreur "FacesContext null" au démarrage
         * de Nuxeo.
         */
        ToutaticeMockFacesContext mockFacesContext = new ToutaticeMockFacesContext() {

            @Override
            public Object evaluateExpressionGet(FacesContext context, String expression, Class expectedType) throws ELException {
                if (expression.startsWith("#{")) {
                    return "";
                }
                return null;
            }
        };
        mockFacesContext.setCurrent();

        for(String cvCategory : categories){

            String[] typeContentViewNames = type.getContentViews(cvCategory);
    
            if (ArrayUtils.isNotEmpty(typeContentViewNames)) {
                
            for (String typeContentViewName : typeContentViewNames) {
                ContentView contentView = contentViewService.getContentView(typeContentViewName);
                    
                if (contentView != null) {
                    /*
                     * Passage d'une liste et de paramètres vides pour éviter une partie
                     * des logs d'erreur au démarrage de Nuxeo.
                     */
                    PageProvider<?> pageProvider = contentView.getPageProvider(null, new ArrayList<SortInfo>(), null, null, new Object[0]);
                    PageProviderDefinition pageProviderDefinition = pageProvider.getDefinition();
                    String pattern = pageProviderDefinition.getPattern();
                    if (pattern != null) {
                        pattern += QUERY_WITH_NO_PROXY;
                        pageProviderDefinition.setPattern(pattern);
                    } else {
                        WhereClauseDefinition whereClause = pageProviderDefinition.getWhereClause();
                        if (whereClause != null) {
                            String query = whereClause.getFixedPart();
                            query += QUERY_WITH_NO_PROXY;
                            whereClause.setFixedPart(query);
                        }
                    }
                    pageProviderDefinition.setEnabled(true);
                }
            }
        }
        
        }
        mockFacesContext.relieveCurrent();
    }

    /**
     * Méthode permettant de récupérer tous les sous-types possiblement présents sous un PortalSite.
     */
    private Collection<Type> getAllowedTypesUnderPublishSpace(Collection<Type> allowedSubTypesUnderPublishSpace, Type type) {
        Collection<Type> allowedSubTypes = typeManager.getAllowedSubTypes(type.getId());
        if (CollectionUtils.isNotEmpty(allowedSubTypes)) {
            for (Type allowedType : allowedSubTypes) {
                if (!allowedSubTypesUnderPublishSpace.contains(allowedType)) {
                    allowedSubTypesUnderPublishSpace.add(allowedType);
                    getAllowedTypesUnderPublishSpace(allowedSubTypesUnderPublishSpace, allowedType);
                }
            }
        }
        return allowedSubTypesUnderPublishSpace;
    }

}
