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
package fr.toutatice.ecm.platform.service.editablewindows;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

import fr.toutatice.ecm.platform.service.editablewindows.types.EditableWindow;

@XObject("editablewindow")
public class EwDescriptor implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7956864563251815573L;

    protected EditableWindow instance;

    @XNode("@class")
    protected Class ew;

    @XNode("@code")
    protected String code;

    @XNode("@layout")
    protected String layout;

    @XNode("@title")
    protected String title;

    public void initFragment() throws Exception {
        instance = (EditableWindow) ew.newInstance();
    }


    /**
     * @return the instance
     */
    public EditableWindow getInstance() {
        return instance;
    }


    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }


    /**
     * @return the layout
     */
    public String getLayout() {
        return layout;
    }


    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }




}
