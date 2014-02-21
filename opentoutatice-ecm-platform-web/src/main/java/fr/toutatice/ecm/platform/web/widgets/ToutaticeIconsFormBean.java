package fr.toutatice.ecm.platform.web.widgets;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;

/**
 * SimpleForm is used for configuration icon selector to hold an UIComponent
 * the clientId of this component is used by the js scripts
 * 
 */
@Name("iconsForm")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeIconsFormBean {

    private UIComponent hiddenFieldComponent;

    /**
     * @return the simpleComponent
     */
    public UIComponent getHiddenFieldComponent() {
        return hiddenFieldComponent;
    }

    /**
     * @param simpleComponent the simpleComponent to set
     */
    public void setHiddenFieldComponent(UIComponent hiddenFieldComponent) {
        this.hiddenFieldComponent = hiddenFieldComponent;
    }


    /**
     * @return the UIComponent client ID
     */
    public String getHiddenFieldComponentClientId() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return hiddenFieldComponent.getClientId(fc);
    }
}