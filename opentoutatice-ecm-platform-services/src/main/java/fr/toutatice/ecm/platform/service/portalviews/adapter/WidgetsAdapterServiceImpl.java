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

import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.forms.layout.service.WebLayoutManager;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;


/**
 * @author david chevrier
 *
 */
public class WidgetsAdapterServiceImpl extends DefaultComponent implements WidgetsAdapterService {

    private static final long serialVersionUID = 4780443408803007026L;

    protected static final String ADAPTER_EXT_PT = "adapter";

    private Map<String, String> widgetsMappings = new HashMap<String, String>(0);

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

                pvWidget = layoutManager.getWidget((FaceletContext) elContext, pvWidgetName, null, "edit", "#{value}", null);
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

}
