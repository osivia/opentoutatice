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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Role;
import org.jboss.seam.annotations.Scope;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.service.editablewindows.types.SliderListFragment;


/**
 * @author david
 *
 */
@Name("criteriaListHelper")
@Scope(ScopeType.SESSION)
@Role(name = "sliderListHelper", scope = ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.INHERIT_TOUTATICE)
public class SliderListBeanHelper extends CriteriaListBeanHelper {

    private static final long serialVersionUID = 5161205435542028242L;

    public static final String SLIDER = "slider";
    public static final int TIMER = 6;

    public enum SliderDocType {
        Picture("Picture", "osivia.fragment.slider.list.doctype.picture.title"), Article("Annonce", "osivia.fragment.slider.list.doctype.article.title"), others(
                "others", "osivia.fragment.slider.list.doctype.others.title");

        private String nxType;
        private String label;

        SliderDocType(String nxType, String label) {
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

    public SliderDocType[] getSliderDocTypes() {
        return SliderDocType.values();
    }
    
    public String getSliderKeyWordXPath(){
        return SliderListFragment.SLIDER_LIST_XPATH;
    }

}
