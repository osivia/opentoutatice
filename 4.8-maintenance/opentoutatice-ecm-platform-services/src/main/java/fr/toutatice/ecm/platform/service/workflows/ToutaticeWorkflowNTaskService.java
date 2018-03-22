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

import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;


/**
 * @author David Chevrier.
 *
 */
public class ToutaticeWorkflowNTaskService extends DefaultComponent {

    /** Service for workflows infos. */
    ToutaticeWorkflowService wfService;
    /** Service for tasks infos. */
    ToutaticeTaskService taskService;

    @Override
    public void activate(ComponentContext context) {
        wfService = new ToutaticeWorkflowServiceImpl();
        ((ToutaticeWorkflowServiceImpl) wfService).activate(context);

        taskService = new ToutaticeTaskServiceImpl();
        ((ToutaticeTaskServiceImpl) taskService).activate(context);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {

        ((ToutaticeWorkflowServiceImpl) wfService).registerContribution(contribution, extensionPoint, contributor);
        ((ToutaticeTaskServiceImpl) taskService).registerContribution(contribution, extensionPoint, contributor);

    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {

        ((ToutaticeWorkflowServiceImpl) wfService).unregisterContribution(contribution, extensionPoint, contributor);
        ((ToutaticeTaskServiceImpl) taskService).unregisterContribution(contribution, extensionPoint, contributor);


    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (ToutaticeWorkflowService.class.isAssignableFrom(adapter)) {
            return (T) wfService;
        } else if (ToutaticeTaskService.class.isAssignableFrom(adapter)) {
            return (T) taskService;
        }
        return null;
    }

    @Override
    public void deactivate(ComponentContext context) {
        wfService = null;
        taskService = null;
    }

}
