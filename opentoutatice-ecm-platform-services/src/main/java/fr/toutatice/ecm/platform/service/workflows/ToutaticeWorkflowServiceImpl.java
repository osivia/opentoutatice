/*
 * (C) Copyright 2015 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 * dchevrier
 */
package fr.toutatice.ecm.platform.service.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;


/**
 * @author david
 *
 */
public class ToutaticeWorkflowServiceImpl extends DefaultComponent implements ToutaticeWorkflowService {

    /** Workflows extension point contribution. */
    protected static final String WF_EXT_POINT = "workflows";

    /** Map of contributed workflows. */
    private Map<String, List<String>> wfContribs;

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        wfContribs = new HashMap<String, List<String>>(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (WF_EXT_POINT.equals(extensionPoint)) {

            WorkflowDescriptor wfDesc = (WorkflowDescriptor) contribution;
            String wfCategory = wfDesc.getWfCategory();

            if (StringUtils.isNotBlank(wfCategory)) {

                String wfName = wfDesc.getWfName();

                if (StringUtils.isNotBlank(wfName)) {

                    if (!wfContribs.containsKey(wfCategory)) {
                        List<String> wfNames = new ArrayList<String>(1);
                        wfNames.add(wfName);
                        wfContribs.put(wfCategory, wfNames);
                    } else {
                        List<String> wfNames = wfContribs.get(wfCategory);
                        wfNames.add(wfName);
                        wfContribs.put(wfCategory, wfNames);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (WF_EXT_POINT.equals(extensionPoint)) {

            WorkflowDescriptor wfDesc = (WorkflowDescriptor) contribution;
            String wfCategory = wfDesc.getWfCategory();

            if (StringUtils.isNotBlank(wfCategory)) {

                String wfName = wfDesc.getWfName();

                if (StringUtils.isNotBlank(wfName)) {

                    List<String> wfNames = wfContribs.get(wfCategory);
                    wfNames.remove(wfName);
                    wfContribs.put(wfCategory, wfNames);

                }

            }

        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {
        Map<String, Object> infos = new HashMap<String, Object>(0);

        if (MapUtils.isNotEmpty(wfContribs)) {
            infos.put(VALIDATION_WF_RUNNING_INFOS_KEY, isWorkflowOfCategoryRunning(VALIDATION_WF_CATEGORY, currentDocument));
        } else {
            infos.put(VALIDATION_WF_RUNNING_INFOS_KEY, false);
        }

        return infos;
    }

    /**
     * @param category category of workflows
     * @return workflows names of given category
     */
    public List<String> getWfnamesByCategory(String category) {

        List<String> wfNames = wfContribs.get(category);

        if (CollectionUtils.isEmpty(wfNames)) {
            wfNames = new ArrayList<String>(0);
        }

        return wfNames;
    }

    /**
     * @param category category of workflow(s)
     * @param document studied document
     * @return true if there is at least one workflow of given category running on given document
     */
    public boolean isWorkflowOfCategoryRunning(String category, DocumentModel document) {
        boolean running = false;

        List<String> wfNames = getWfnamesByCategory(category);
        Iterator<String> it = wfNames.iterator();

        while (it.hasNext() && !running) {

            String wfName = it.next();
            DocumentRoute workflow = ToutaticeWorkflowHelper.getWorkflowByName(wfName, document);

            running = workflow != null;
        }

        return running;

    }

}
