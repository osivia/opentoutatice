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
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.web.fragments;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.service.editablewindows.EditableWindowService;
import fr.toutatice.ecm.platform.service.editablewindows.EwDescriptor;
import fr.toutatice.ecm.platform.service.editablewindows.EwServiceException;
import fr.toutatice.ecm.platform.service.editablewindows.types.EditableWindow;


/**
 * Bean for managing fragments in the page
 * 
 * @author loic
 * 
 */
@Name("fragmentBean")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class FragmentActionBean extends GenericActionBean {


    private static final Log log = LogFactory.getLog(FragmentActionBean.class);

    /**
     * identifiant fragment passé en mode édition
     */
    @RequestParameter("refURI")
    private String requestedRefUri;

    private String uri;

    /**
     * @return the uri
     */
    public String getUri() {

        return uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * identifiant région passé en mode création (optionnel)
     */
    @RequestParameter("region")
    private String requestedRegion;

    private String region;

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region
     *            the region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * identifiant uri passé en mode création (optionnel)
     */
    @RequestParameter("belowURI")
    private String requestedBelowUri;

    private String belowUri;

    /**
     * @return the belowUri
     */
    public String getBelowUri() {
        return belowUri;
    }

    /**
     * @param belowUri
     *            the belowUri to set
     */
    public void setBelowUri(String belowUri) {
        this.belowUri = belowUri;
    }
    
    /** In création mode, user can go back and change fragment type */
    private boolean canCancel = false;
    

    /**
	 * @return the canCancel
	 */
	public boolean isCanCancel() {
		return canCancel;
	}
	

    /* ======================================= */


	/** Paramètres du widget selectOneDirectory, référence le vocabulaire list-views */
    private Map<String, String> listViewsParam = new HashMap<String, String>();

    /**
     * @return the listViewsParam
     */
    public Map<String, String> getListViewsParam() {

        if (listViewsParam.size() == 0) {
            listViewsParam.put("directoryName", "list-views");
        }

        return listViewsParam;
    }


    /* ======================================= */

    /** current fragment descriptor */
    private EwDescriptor descriptor;


    /**
     * @return the descriptor
     */
    public EwDescriptor getDescriptor() {
        if (descriptor == null) {
            initDescriptor();
        }
        return descriptor;
    }

    /**
     * Prepare and display the creation view
     * 
     * @param code the code of the nuxeo fragment object
     * @return osivia_create_fragment_2
     */
    public String dispatchCreation(String code, String code2) {

        try {
            Entry<EwDescriptor, EditableWindow> fragmentInfos = getFragmentService().findByCode(code);

            if (fragmentInfos != null) {

                EditableWindow fragment = fragmentInfos.getValue();

                descriptor = fragmentInfos.getKey();

                FacesContext context = FacesContext.getCurrentInstance();
                DocumentModel doc = context.getApplication().evaluateExpressionGet(context, "#{currentDocument}", DocumentModel.class);

                if (region != null || belowUri != null) {

                    uri = getFragmentService().prepareCreation(doc, fragment, code, region, belowUri, code2);
                } else {
                    addMessage("osivia.error.region_unbound");

                }
            } else {
                addMessage("osivia.error.fragment_not_found");
            }
        } catch (EwServiceException e) {
            addMessage(e.getMessage());
        }


        canCancel = true;
        return "osivia_create_fragment_2";

    }
    
    /**
     * Cancel the creation an back to the window selector
     * @return osivia_create_fragment
     */
    public String cancelCreation() {
    	
    	descriptor = null;
    	uri = null;
    	
    	return "osivia_create_fragment";
    }

    private EditableWindowService service;

    private EditableWindowService getFragmentService() {

        if (service == null) {
            try {
                service = Framework.getService(EditableWindowService.class);
            } catch (Exception e) {
                addMessage(e.getMessage());
            }
        }

        return service;
    }

    /**
     * Prepare and display the current fragment's informations
     */
    public void initDescriptor() {

        try {
            if (uri != null) {
                // Créer les schémas
                FacesContext context = FacesContext.getCurrentInstance();

                DocumentModel doc = context.getApplication().evaluateExpressionGet(context, "#{currentDocument}", DocumentModel.class);

                // Déterminer le type, cf navigation-rule

                Entry<EwDescriptor, EditableWindow> fragmentInfos = getFragmentService().getEwEntry(doc, uri);
                descriptor = fragmentInfos.getKey();

            } else {
                addMessage("osivia.error.fragment_not_found"); // this fragment is unknown

            }
        } catch (EwServiceException e) {
            addMessage(e.getMessage());
        }


    }


    @Create
    public void startUp() {
        if (requestedRefUri != null) {
            uri = requestedRefUri;
        }
        if (requestedBelowUri != null) {
            belowUri = requestedBelowUri;
        }
        if (requestedRegion != null) {
            region = requestedRegion;
        }

    }
}
