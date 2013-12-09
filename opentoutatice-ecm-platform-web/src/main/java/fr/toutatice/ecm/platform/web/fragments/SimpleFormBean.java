package fr.toutatice.ecm.platform.web.fragments;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * SimpleForm is used for configuration icon selector to hold an UIComponent
 * the clientId of this component is used by the js scripts
 * 
 */
@Name("simpleForm")
@Scope(ScopeType.SESSION)
@Install(precedence = Install.DEPLOYMENT)
public class SimpleFormBean {

    private UIComponent simpleComponent;

    /**
     * @return the simpleComponent
     */
    public UIComponent getSimpleComponent() {
        return simpleComponent;
    }

    /**
     * @param simpleComponent the simpleComponent to set
     */
    public void setSimpleComponent(UIComponent simpleComponent) {
        this.simpleComponent = simpleComponent;
    }


    /**
     * @return the UIComponent client ID
     */
    public String getSimpleComponentClientId() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return simpleComponent.getClientId(fc);
    }
}