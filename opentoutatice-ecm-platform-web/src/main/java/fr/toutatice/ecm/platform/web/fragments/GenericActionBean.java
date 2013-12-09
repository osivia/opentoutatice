package fr.toutatice.ecm.platform.web.fragments;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.In;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;


public class GenericActionBean {


    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    public void addMessage(String key) {

        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, resourcesAccessor.getMessages().get(key), "");
        context.addMessage("", message);
    }
}
