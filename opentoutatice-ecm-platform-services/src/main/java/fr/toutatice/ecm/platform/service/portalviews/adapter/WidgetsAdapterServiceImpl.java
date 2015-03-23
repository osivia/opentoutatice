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
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
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

    protected static final String ADAPTER_EXT_PT = "adapter";
    private static final String FROM_URL_PARAM = "fromUrl";

    private Map<String, String> widgetsMappings = new HashMap<String, String>(0);

    private List<String> portalViewIds;

    private String fromUrlParam;

    protected enum DefaultPortalViewId {
        toutatice_edit, toutatice_create;
    }

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        portalViewIds = new ArrayList<String>(0);
        fromUrlParam = StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInPortalViewContext() {
        boolean is = false;
        RestHelper restHelper = (RestHelper) SeamComponentCallHelper.getSeamComponentByName("restHelper");
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
        is = fromUrlParamExists || isPortalView(viewId);

        return is;
    }

    /**
     * @return true if viewId is a PortaView.
     */
    private boolean isPortalView(String viewId) {
        boolean is = false;

        if (StringUtils.isNotBlank(viewId)) {
            is = (viewId.contains(DefaultPortalViewId.toutatice_edit.name())) || (viewId.contains(DefaultPortalViewId.toutatice_create.name()));
        }
        if (CollectionUtils.isNotEmpty(portalViewIds)) {
            for (String vId : portalViewIds) {
                is |= viewId.contains(vId);
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
    @Override
    public Widget getPortalViewWidget(Widget nxWidget) {
        Widget pvWidget = nxWidget;

        String pvWidgetName = widgetsMappings.get(nxWidget.getName());

        if (StringUtils.isNotBlank(pvWidgetName)) {
            WebLayoutManager layoutManager = Framework.getLocalService(WebLayoutManager.class);
            // layoutManager.getWidgetTypeHandler("", "")

            FacesContext context = FacesContext.getCurrentInstance();
            ELContext elContext = (ELContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);

            if (elContext != null) {

                pvWidget = layoutManager.getWidget((FaceletContext) elContext, pvWidgetName, null, nxWidget.getMode(), "#{value}", null);
                pvWidget.setId(FaceletHandlerHelper.WIDGET_ID_PREFIX + pvWidgetName);

            }
        }

        return pvWidget;
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (ADAPTER_EXT_PT.equals(extensionPoint)) {
            WidgetsAdapterDescriptor widgetsAdapterDesc = (WidgetsAdapterDescriptor) contribution;
            WidgetMappingDescriptor[] mappings = widgetsAdapterDesc.getWidgetsMapping();
            fillMappings(mappings);
        }
    }

    protected void fillMappings(WidgetMappingDescriptor[] mappings) {
        for (WidgetMappingDescriptor mapping : mappings) {
            String nxWidget = mapping.getNxWidget();
            String pvWidget = mapping.getPvWidget();
            this.widgetsMappings.put(nxWidget, pvWidget);
        }
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
