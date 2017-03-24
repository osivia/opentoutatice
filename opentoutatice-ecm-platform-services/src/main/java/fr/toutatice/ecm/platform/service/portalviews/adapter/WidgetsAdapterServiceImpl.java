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
 * lbillon
 * dchevrier
 */
package fr.toutatice.ecm.platform.service.portalviews.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.el.ELContext;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.forms.layout.api.FieldDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.service.LayoutStore;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.forms.layout.service.WebLayoutManager;
import org.nuxeo.ecm.platform.ui.web.rest.RestHelper;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;


/**
 * @author david chevrier
 *
 */
public class WidgetsAdapterServiceImpl extends DefaultComponent implements WidgetsAdapterService {

    private static final long serialVersionUID = 4780443408803007026L;

    private static final Log log = LogFactory.getLog(WidgetsAdapterService.class);

    protected static final String ADAPTER_EXT_PT = "adapter";
    private static final String FROM_URL_PARAM = "fromUrl";
    
    /** Widgets name mapping. */
    private Map<String, String> widgetsNamesMappings = new HashMap<String, String>(0);
    
    /** Widgets type mapping. */
    private Map<String, Map<String, PVPropertyDescriptor[]>> widgetsTypesMappings = new HashMap<String, Map<String, PVPropertyDescriptor[]>>(0);

    /** Metadata mapped to Nuxeo widgets. */
    private Map<String, List<String>> fieldsOfNxWidgets;
    
    /** Metadata mapped to portal view widgets. */
    private Map<String, List<String>> fieldsOfPvWidgets;

    private List<String> portalViewIds;

	private String currentPortalView;

    private String fromUrlParam;

    protected enum DefaultPortalViewId {
        toutatice_edit, toutatice_create, osivia_edit_document, osivia_create_document;
    }

    @Override
    public int getApplicationStartedOrder() {
        // After WebLayoutManager
        return 2000;
    }

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        portalViewIds = new ArrayList<String>(0);
        fromUrlParam = StringUtils.EMPTY;
        fieldsOfNxWidgets = new HashMap<String, List<String>>(0);
        fieldsOfPvWidgets = new HashMap<String, List<String>>(0);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getWidgetsMappings() {
        return this.widgetsNamesMappings;
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentPortalView(){
        return this.currentPortalView;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isInPortalViewContext() {
        boolean is = false;

        RestHelper restHelper = null;
        try {
            restHelper = (RestHelper) SeamComponentCallHelper.getSeamComponentByName("restHelper");
        } catch (IllegalStateException ise) {
            // Seam context is not available (case of creation of document by automation)
            // Nothing to do
        }

        if (restHelper != null) {
            String viewId = StringUtils.EMPTY;
            DocumentView documentView = restHelper.getDocumentView();
            boolean fromUrlParamExists = false;

            if (documentView != null) {
                viewId = documentView.getViewId();
                fromUrlParam = documentView.getParameter(FROM_URL_PARAM);
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                viewId = context.getViewRoot().getViewId();
                fromUrlParamExists = isFromUrlParamExists(context);
            }
            currentPortalView = viewId;

            is = fromUrlParamExists || isPortalView(viewId);
        }

        return is;
    }

    /**
     * @return true if viewId is a PortaView.
     */
    private boolean isPortalView(String viewId) {
        boolean is = false;

        DefaultPortalViewId[] defaultPortalViewIds = DefaultPortalViewId.values();

        if (StringUtils.isNotBlank(viewId)) {
            for (DefaultPortalViewId defaultPortalViewId : defaultPortalViewIds) {
                is |= viewId.contains(defaultPortalViewId.name());
            }

            if (CollectionUtils.isNotEmpty(portalViewIds)) {
                for (String vId : portalViewIds) {
                    is |= viewId.contains(vId);
                }
            }
        }

        return is;
    }

    /**
     * @return true if "fromUrl" param is in request.
     */
    private boolean isFromUrlParamExists(FacesContext context) {
        boolean is = false;
        if (StringUtils.isNotBlank(fromUrlParam)) {

            Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
            Set<Entry<String, String>> params = requestParameterMap.entrySet();
            Iterator<Entry<String, String>> iterator = params.iterator();
            boolean fromUrlParamExists = false;

            while (iterator.hasNext() && !fromUrlParamExists) {
                Entry<String, String> param = iterator.next();
                if (param.getValue().contains(fromUrlParam)) {
                    fromUrlParamExists = true;
                    is = fromUrlParamExists;
                }
            }

        }
        return is;

    }

    /**
     * {@inheritDoc}
     */
    public List<String> getNxFields(String nxWidgetName) {
        List<String> fields = new ArrayList<String>(0);

        if (MapUtils.isNotEmpty(this.fieldsOfNxWidgets)) {
            fields = this.fieldsOfNxWidgets.get(nxWidgetName);
        }

        return fields;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<String> getPvFields(String pvWidgetName){
        List<String> fields = new ArrayList<String>(0);
        
        if (MapUtils.isNotEmpty(this.fieldsOfPvWidgets)) {
            fields = this.fieldsOfPvWidgets.get(pvWidgetName);
        }        
        
        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Widget getPortalViewWidget(Widget nxWidget) throws Exception {
        Widget pvWidget = nxWidget;

        String nxWidgetName = nxWidget.getName();
        String pvWidgetName = widgetsNamesMappings.get(nxWidgetName);
        
        WebLayoutManager layoutManager = Framework.getLocalService(WebLayoutManager.class);

        if (StringUtils.isNotBlank(pvWidgetName)) {

            FacesContext context = FacesContext.getCurrentInstance();
            ELContext elContext = (ELContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);

            if (elContext != null) {

                pvWidget = layoutManager.getWidget((FaceletContext) elContext, pvWidgetName, null, nxWidget.getMode(), "#{value}", null);
                pvWidget.setId(FaceletHandlerHelper.WIDGET_ID_PREFIX + pvWidgetName);

            }
        } else {
            WidgetDefinition widgetDefinition = nxWidget.getDefinition();
            if(widgetDefinition != null){
                String type = widgetDefinition.getType();
                
                Set<String> mappedTypes = widgetsTypesMappings.keySet();
                if(mappedTypes != null && mappedTypes.contains(type)){
                    
                    Map<String, PVPropertyDescriptor[]> mappedPvproperties = widgetsTypesMappings.get(type);
                    Set<String> mappedPvTypes = mappedPvproperties.keySet();
                    
                    if(mappedPvTypes != null && mappedPvTypes.size() == 1){
                        
                        if(canOverride(nxWidget)){
                        
                            String pvType = mappedPvTypes.iterator().next();
                            if(!StringUtils.equals(type, pvType)){
                                
                                widgetDefinition.setType(pvType);
                                
                                FacesContext context = FacesContext.getCurrentInstance();
                                ELContext elContext = (ELContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        
                                if (elContext != null) {
                                    pvWidget = layoutManager.createWidget((FaceletContext) elContext, widgetDefinition, nxWidget.getMode(), nxWidget.getValueName(), new Widget[0]);
                                }
                                
                                // Restore widget definition
                                widgetDefinition.setType(type);
                            }
                            
                            PVPropertyDescriptor[] pvPropertyDescriptors = mappedPvproperties.get(pvType);
                        
                            if(pvPropertyDescriptors != null && pvPropertyDescriptors.length > 0){
                                for(PVPropertyDescriptor pvProp : pvPropertyDescriptors){
                                    pvWidget.setProperty(pvProp.getName(), pvProp.getValue());
                                }
                            }
                        
                        }
                    
                    } else {
                        throw new Exception("Widget type can be mapped with only one PortalView widget type");
                    }
                    
                }
            }
        }

        return pvWidget;
    }
    
    /**
     * @param nxWidget
     * @return true if nxWidget can be override in PortalView context.
     */
    protected boolean canOverride(Widget nxWidget) {
        String override = (String) nxWidget.getProperty("pvOverride");
        if(StringUtils.isBlank(override)){
            return true;
        }
        return BooleanUtils.isTrue(BooleanUtils.toBoolean(override));
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (ADAPTER_EXT_PT.equals(extensionPoint)) {
            if(contribution instanceof WidgetsAdapterDescriptor){
                WidgetsAdapterDescriptor widgetsAdapterDesc = (WidgetsAdapterDescriptor) contribution;
                WidgetMappingDescriptor[] mappings = widgetsAdapterDesc.getWidgetsNamesMapping();
                fillMappings(mappings);
                
                WidgetMappingTypeDescriptor[] widgetsTypesMapping = widgetsAdapterDesc.getWidgetsTypesMapping();
                fillTypeMappings(widgetsTypesMapping);
            }
        }
    }
    
    protected void fillTypeMappings(WidgetMappingTypeDescriptor[] widgetsTypesMapping){
        if(widgetsTypesMapping.length > 0){
            for(WidgetMappingTypeDescriptor mappingDesc : widgetsTypesMapping){
                String type = mappingDesc.getType();
                String pvType = mappingDesc.getPvType();
                if(StringUtils.isEmpty(pvType)){
                    pvType = type;
                }
                PVPropertyDescriptor[] pvPropertiesDesc = mappingDesc.getPvProperties();
                
                Map<String, PVPropertyDescriptor[]> pvProperties = new HashMap<String, PVPropertyDescriptor[]>(0);
                pvProperties.put(pvType, pvPropertiesDesc);
                
                widgetsTypesMappings.put(type, pvProperties);
            }
        }
    }

    protected void fillMappings(WidgetMappingDescriptor[] mappings) {
        LayoutStore lStore = (LayoutStore) Framework.getService(LayoutStore.class);
        WebLayoutManager wlMng = (WebLayoutManager) Framework.getService(WebLayoutManager.class);

        for (WidgetMappingDescriptor mapping : mappings) {
            String nxWidget = mapping.getNxWidget();
            String pvWidget = mapping.getPvWidget();
            this.widgetsNamesMappings.put(nxWidget, pvWidget);
            
            WidgetDefinition pvWidgetDefinition = wlMng.getWidgetDefinition(pvWidget);

            WidgetDefinition nxWidgetDefinition = wlMng.getWidgetDefinition(nxWidget);
            // Search in layouts definitions if null
            if (nxWidgetDefinition == null) {
                List<String> categories = lStore.getCategories();

                for (String category : categories) {
                    List<String> layoutDefinitionNames = lStore.getLayoutDefinitionNames(category);

                    boolean defined = false;
                    Iterator<String> iterator = layoutDefinitionNames.iterator();

                    while (iterator.hasNext() && !defined) {
                        String layoutDefinitionName = iterator.next();
                        LayoutDefinition layoutDefinition = wlMng.getLayoutDefinition(layoutDefinitionName);

                        if (layoutDefinition != null) {
                            nxWidgetDefinition = layoutDefinition.getWidgetDefinition(nxWidget);
                            if (nxWidgetDefinition != null) {
                                defined = true;
                            }
                        }
                    }
                }
            }

            if (nxWidgetDefinition == null) {
                log.error(nxWidget + " widget mapped with " + pvWidget + " not found");
            } else {
                List<String> nxFields = storeWidgetFields(nxWidget, nxWidgetDefinition);
                fieldsOfNxWidgets.put(nxWidget, nxFields);
                
                List<String> pvFields = storeWidgetFields(pvWidget, pvWidgetDefinition);
                fieldsOfPvWidgets.put(pvWidget, pvFields);
            }
        }
    }

    /**
     * @param widgetName
     * @param widgetDefinition
     */
    private List<String> storeWidgetFields(String widgetName, WidgetDefinition widgetDefinition) {
        List<String> fields = new ArrayList<String>();
        
        FieldDefinition[] fieldDefinitions = widgetDefinition.getFieldDefinitions();
        if (fieldDefinitions != null) {

            for (FieldDefinition nxFieldDef : fieldDefinitions) {
                fields.add(nxFieldDef.getFieldName());
            }
        }
        
        return fields;
    }

    @Override
    public void addPortalViewId(String viewId) {
        if (StringUtils.isNotBlank(viewId) && !portalViewIds.contains(viewId)) {
            portalViewIds.add(viewId);
        }
    }

    @Override
    public void addPortalViewsIds(String... viewIds) {
        if (viewIds != null) {
            String[] ids = (String[]) viewIds;
            if (ArrayUtils.isNotEmpty(ids)) {
                for (String viewId : viewIds) {
                    addPortalViewId(viewId);
                }
            }
        }
    }

}
