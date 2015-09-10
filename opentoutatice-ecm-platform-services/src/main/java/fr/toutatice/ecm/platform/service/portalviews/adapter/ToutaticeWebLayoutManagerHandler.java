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
package fr.toutatice.ecm.platform.service.portalviews.adapter;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.platform.forms.layout.api.Layout;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRow;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.impl.LayoutImpl;
import org.nuxeo.ecm.platform.forms.layout.api.impl.LayoutRowImpl;
import org.nuxeo.ecm.platform.ui.web.rest.RestHelper;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.webengine.app.DefaultContext;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.components.ToutaticeAbstractServiceHandler;


/**
 * @author david chevrier
 *
 */
public class ToutaticeWebLayoutManagerHandler<T> extends ToutaticeAbstractServiceHandler<T> {

    @Override
    public T newProxy(T layoutService, Class<T> layoutServiceKlass) {
        setObject(layoutService);
        return layoutServiceKlass.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{layoutServiceKlass}, this));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("getLayout".equals(method.getName()) && (args != null && args.length == 7)) {

            WidgetsAdapterService widgetsAdapterService = Framework.getLocalService(WidgetsAdapterService.class);

            if (widgetsAdapterService.isInPortalViewContext()) {

                Layout layout = (Layout) method.invoke(object, args);

                LayoutRow[] rows = layout.getRows();
                List<LayoutRow> pvRows = new ArrayList<LayoutRow>(0);
                for (LayoutRow row : rows) {
                    Widget[] nxWidgets = row.getWidgets();
                    List<Widget> pvWidgets = new ArrayList<Widget>(0);
                    for (Widget nxWidget : nxWidgets) {
                        if(nxWidget != null){
                           pvWidgets.add(widgetsAdapterService.getPortalViewWidget(nxWidget));
                        }
                    }
                    pvRows.add(new LayoutRowImpl(row.getName(), row.isSelectedByDefault(), row.isAlwaysSelected(), pvWidgets, row.getProperties(), row
                            .getTagConfigId()));
                }
                /* To avoid unmodificable properties */
                Map<String, Serializable> properties = new HashMap<String, Serializable>(layout.getProperties());
                return new LayoutImpl(layout.getName(), layout.getMode(), layout.getTemplate(), pvRows, layout.getColumns(), properties,
                        layout.getTagConfigId());
            }
        }

        return method.invoke(object, args);
    }

}
