/**
 * 
 */
package fr.toutatice.ecm.platform.service.customize.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.platform.contentview.jsf.ContentView;
import org.nuxeo.ecm.platform.contentview.jsf.ContentViewLayout;
import org.nuxeo.ecm.platform.contentview.jsf.ContentViewService;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRowDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.impl.LayoutRowDefinitionImpl;
import org.nuxeo.ecm.platform.forms.layout.service.WebLayoutManager;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderDefinition;
import org.nuxeo.ecm.platform.query.api.WhereClauseDefinition;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.ui.web.jsf.MockFacesContext;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;


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
    private static final String PORTAL_SITE = "PortalSite";

    private static final String LAYOUTS_PT_EXT = "layouts";

    private TypeManager typeManager;
    private ContentViewService contentViewService;
    private WebLayoutManager webLayoutManager;

    private Collection<Type> allowedTypesUnderPortalSite;
    private static List<String> modifiedContentViewsLayouts = new ArrayList<String>();

    private List<LayoutsDescriptor> allLayoutsDescriptor = new ArrayList<LayoutsDescriptor>();

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        typeManager = Framework.getService(TypeManager.class);
        contentViewService = Framework.getService(ContentViewService.class);
        webLayoutManager = Framework.getService(WebLayoutManager.class);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (LAYOUTS_PT_EXT.equals(extensionPoint)) {
            LayoutsDescriptor layoutsDescriptor = (LayoutsDescriptor) contribution;
            allLayoutsDescriptor.add(layoutsDescriptor);
        }
    }

    @Override
    public void applicationStarted(ComponentContext context) throws Exception {
        Type portalSite = typeManager.getType(PORTAL_SITE);
        allowedTypesUnderPortalSite = getAllowedTypesUnderPortalSite(new ArrayList<Type>(), portalSite);
        adaptContentViews();
        for (LayoutsDescriptor layoutsDescriptor : allLayoutsDescriptor) {
            overrideLayoutsTemplate(layoutsDescriptor);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.toutatice.ecm.platform.service.cutomize.ui.CustomizeUIService#adaptContentViews()
     */
    @Override
    public void adaptContentViews() throws Exception {
        if (allowedTypesUnderPortalSite != null) {
            Collection<Type> types = typeManager.getTypes();
            for (Type type : types) {
                if (allowedTypesUnderPortalSite.contains(type)) {
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
        if (layoutsToOverride != null && layoutsToOverride.length > 0) {
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

        String[] typeContentViewNames = type.getContentViews(CONTENT_CATEGORY);
        if (typeContentViewNames != null && typeContentViewNames.length > 0) {
            for (String typeContentViewName : typeContentViewNames) {
                ContentView contentView = contentViewService.getContentView(typeContentViewName);
                if (contentView != null) {
                    ContentViewLayout currentResultLayout = contentView.getCurrentResultLayout();
                    String layoutName = currentResultLayout.getName();
                    if (!modifiedContentViewsLayouts.contains(layoutName) && !PORTAL_LAYOUT.equals(layoutName)) {
                        modifiedContentViewsLayouts.add(layoutName);
                        LayoutDefinition layoutDefinition = webLayoutManager.getLayoutDefinition(layoutName);

                        LayoutRowDefinition[] rows = layoutDefinition.getRows();
                        LayoutRowDefinitionImpl layoutRowDefinitionImpl = new LayoutRowDefinitionImpl("Version en ligne?", "local_publishing_status");
                        LayoutRowDefinition[] modifiedRows = (LayoutRowDefinition[]) Arrays.copyOf(rows, rows.length + 1);
                        modifiedRows[rows.length] = layoutRowDefinitionImpl;

                        layoutDefinition.setRows(modifiedRows);
                    }
                } else {
                    log.error(typeContentViewName + " not defined for type " + type.getLabel());
                }
            }
        }
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
        MockFacesContext mockFacesContext = new MockFacesContext() {
            @Override
            public Object evaluateExpressionGet(FacesContext context, String expression, Class expectedType) throws ELException {
                if (expression.startsWith("#{")) {
                    return "";
                }
                return null;
            }
        };
        mockFacesContext.setCurrent();
        
        String[] typeContentViewNames = type.getContentViews(CONTENT_CATEGORY);

        if (typeContentViewNames != null && typeContentViewNames.length > 0) {
            for (String typeContentViewName : typeContentViewNames) {
                ContentView contentView = contentViewService.getContentView(typeContentViewName);
                if (contentView != null) {
                    /*
                     * Passage d'une liste et de paramètres vides pour éviter une portaie
                     * des logs d'erreur au démarrage de Nuxeo (FacesContext null -evidemment...-)
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
        mockFacesContext.relieveCurrent();
    }

    /**
     * Méthode permettant de récupérer tous les sous-types possiblement présents sous un PortalSite.
     */
    private Collection<Type> getAllowedTypesUnderPortalSite(Collection<Type> allowedSubTypesUnderPortalSite, Type type) {
        Collection<Type> allowedSubTypes = typeManager.getAllowedSubTypes(type.getId());
        if (allowedSubTypes != null && allowedSubTypes.size() > 0) {
            for (Type allowedType : allowedSubTypes) {
                if (!allowedSubTypesUnderPortalSite.contains(allowedType)) {
                    allowedSubTypesUnderPortalSite.add(allowedType);
                    getAllowedTypesUnderPortalSite(allowedSubTypesUnderPortalSite, allowedType);
                }
            }
        }
        return allowedSubTypesUnderPortalSite;
    }

}
