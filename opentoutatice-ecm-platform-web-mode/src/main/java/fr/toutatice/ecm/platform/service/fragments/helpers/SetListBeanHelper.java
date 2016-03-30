package fr.toutatice.ecm.platform.service.fragments.helpers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;


/**
 * @author dorian
 */
@Name("setListBeanHelper")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class SetListBeanHelper implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = -4012062396374104983L;

    private static final String schemaName = "set_fragments";

    private static final String setXpath = "setfgt:setFragment";

    private static final String setName = "setName";

    private static final String setWebId = "setWebId";

    protected String pageWebId;

    protected String pageTitle;

    @In(create = true)
    protected NavigationContext navigationContext;

    public void addItem(int index) {
        if (getPageWebId() != null) {
            DocumentModel currentDocument = navigationContext.getCurrentDocument();
            final Map<String, Object> setProperties = currentDocument.getProperties(schemaName);
            final List<Map<String, Object>> listFgt = (List<Map<String, Object>>) setProperties.get(setXpath);
            final Map<String, Object> currentSetFgt = listFgt.get(index);
            final List<Map<String, Object>> listSetItems = (List<Map<String, Object>>) currentSetFgt.get("setItems");
            Map<String, Object> newSetItems = new HashMap<String, Object>(2);
            newSetItems.put(setName, getPageTitle());
            // on supprime le "/nuxeo/web/" devant le webId
            final String pageWebIdCleared = getPageWebId().substring(11);
            newSetItems.put(setWebId, pageWebIdCleared);
            listSetItems.add(newSetItems);
            currentDocument.setProperties(schemaName, setProperties);
        }
    }

    public void removeItem(int index, int setIndex) {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        final Map<String, Object> setProperties = currentDocument.getProperties(schemaName);
        final List<Map<String, Object>> listFgt = (List<Map<String, Object>>) setProperties.get(setXpath);
        final Map<String, Object> currentSetFgt = listFgt.get(index);
        final List<Map<String, Object>> listSetItems = (List<Map<String, Object>>) currentSetFgt.get("setItems");
        listSetItems.remove(setIndex);
        currentDocument.setProperties(schemaName, setProperties);
    }

    public void moveItem(int index, int previousPosition, int newPosition) {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        final Map<String, Object> setProperties = currentDocument.getProperties(schemaName);
        final List<Map<String, Object>> listFgt = (List<Map<String, Object>>) setProperties.get(setXpath);
        final Map<String, Object> currentSetFgt = listFgt.get(index);
        final List<Map<String, Object>> listSetItems = (List<Map<String, Object>>) currentSetFgt.get("setItems");

        listSetItems.add(newPosition, listSetItems.remove(previousPosition));

        currentDocument.setProperties(schemaName, setProperties);
    }

    /**
     * Getter for pageWebId.
     *
     * @return the pageWebId
     */
    public String getPageWebId() {
        return pageWebId;
    }


    /**
     * Setter for pageWebId.
     *
     * @param pageWebId the pageWebId to set
     */
    public void setPageWebId(String pageWebId) {
        this.pageWebId = pageWebId;
    }


    /**
     * Getter for pageTitle.
     *
     * @return the pageTitle
     */
    public String getPageTitle() {
        return pageTitle;
    }


    /**
     * Setter for pageTitle.
     *
     * @param pageTitle the pageTitle to set
     */
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

}
