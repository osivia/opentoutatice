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
 * lbillon
 * dchevrier
 * mberhaut1
 */
package fr.toutatice.ecm.platform.service.fragments.helpers;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.service.editablewindows.types.CriteriaListFragment;

/**
 * Criteria bean for CriteriaListFragment.
 * 
 * @author david chevrier
 */
@Name("criteriaListHelper")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class CriteriaListBeanHelper implements Serializable {

    private static final long serialVersionUID = 9151077743052798077L;

    public static final int NB_REQUEST_RESULTS = 10;
    public static final int NB_REQUEST_RESULTS_PER_PAGE = 10;

    public static final String COMPLEX_PROPERTY_SEPARATOR = "/";
    
    @In(create = true)
    protected NavigationContext navigationContext;

    protected String newKeyWord;

    public String getNewKeyWord() {
        return newKeyWord;
    }

    public void setNewKeyWord(String newKeyWord) {
        this.newKeyWord = newKeyWord;
    }

    public enum DocType {
        Article("Annonce", "osivia.fragment.criteria.list.doctype.article.title"), File("File", "osivia.fragment.criteria.list.doctype.file.title"), Link(
                "ContextualLink", "osivia.fragment.criteria.list.doctype.link.title");

        private String nxType;
        private String label;

        DocType(String nxType, String label) {
            this.nxType = nxType;
            this.label = label;
        }

        public String type() {
            return this.nxType;
        }

        public String label() {
            return this.label;
        }
    }

    public enum Order {
        publicationDate("dc:issued desc", "osivia.fragment.criteria.list.order.publication.title"), alphabetic("dc:title asc",
                "osivia.fragment.criteria.list.order.alphabetic.title");

        private String nxProperty;
        private String label;

        Order(String nxProperty, String label) {
            this.nxProperty = nxProperty;
            this.label = label;
        }

        public String value() {
            return this.nxProperty;
        }

        public String label() {
            return this.label;
        }
    }

    public enum SearchArea {
        currentPage("ecm:parentId", "osivia.fragment.criteria.list.place.current.title"), fromCurrentPage("ecm:ancestorId",
                "osivia.fragment.criteria.list.place.childs.title"), space("ttc:spaceID", "osivia.fragment.criteria.list.place.space.title");

        private String nxProperty;
        private String label;

        SearchArea(String nxProperty, String label) {
            this.nxProperty = nxProperty;
            this.label = label;
        }

        public String value() {
            return this.nxProperty;
        }

        public String label() {
            return this.label;
        }
    }

    public enum DisplayStyle implements ListDisplayStyle {
        normal("normal", "osivia.fragment.criteria.list.style.normal.title"), minimal("mini", "osivia.fragment.criteria.list.style.minimal.title"), editorial(
                "editorial", "osivia.fragment.criteria.list.style.editorial.title");

        private String portletProperty;
        private String label;

        private DisplayStyle(String portletProperty, String label) {
            this.portletProperty = portletProperty;
            this.label = label;
        }

        public String value() {
            return this.portletProperty;
        }

        public String label() {
            return this.label;
        }
    }

    public DocType[] getDocTypes() {
        return DocType.values();
    }

    public ListDisplayStyle[] getStyles() {
        return DisplayStyle.values();
    }

    public Order[] getOrders() {
        return Order.values();
    }

    public SearchArea[] getSearchAreas() {
        return SearchArea.values();
    }
    
    public String getKeyWordXPath(){
        return CriteriaListFragment.CRITERIA_LIST_XPATH;
    }

    public String getKeyWordsProperty(String xpath, int index) {
        StringBuffer keyWordsProperty = new StringBuffer().append(xpath).append(COMPLEX_PROPERTY_SEPARATOR)
                .append(String.valueOf(index)).append(COMPLEX_PROPERTY_SEPARATOR).append("requestCriteria").append(COMPLEX_PROPERTY_SEPARATOR)
                .append("keyWords");
        return keyWordsProperty.toString();
    }

    public void addKeyWord(String xpath, int index) {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        
        String keyWord = StringUtils.trim(newKeyWord);
        newKeyWord = StringUtils.EMPTY;

        String keyWordsProperty = getKeyWordsProperty(xpath, index);
        String[] keyWordsValue = (String[]) currentDocument.getPropertyValue(keyWordsProperty);
        if (keyWordsValue == null) {
            keyWordsValue = new String[0];
        }
        keyWordsValue = (String[]) ArrayUtils.add(keyWordsValue, keyWord);

        currentDocument.setPropertyValue(keyWordsProperty, keyWordsValue);
    }

    public void removeKeyWord(String xpath, int index) {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        
        String keyWordsProperty = getKeyWordsProperty(xpath, index);
        String[] keyWordsValue = (String[]) currentDocument.getPropertyValue(keyWordsProperty);

        FacesContext context = FacesContext.getCurrentInstance();
        String keyWordToDel = context.getExternalContext().getRequestParameterMap().get("keyWordToDel");
        keyWordsValue = (String[]) ArrayUtils.removeElement(keyWordsValue, keyWordToDel);

        currentDocument.setPropertyValue(keyWordsProperty, keyWordsValue);
    }

}
