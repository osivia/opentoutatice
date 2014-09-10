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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

@Name("pageBean")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class PageBean {

    /**
     * identifiant url de provenance
     */
    @RequestParameter("fromUrl")
    private String requestedFromUrl;
    
    
    private String fromUrl;

    /**
     * @return the fromUrl
     */
    public String getFromUrl() {
        return fromUrl;
    }

    /**
     * @param fromUrl the fromUrl to set
     */
    public void setFromUrl(String fromUrl) {
        this.fromUrl = fromUrl;
    }

    @Create
    public void startUp() {
        // En mode édition, retourne l'uri passée en parmaètre
        if (requestedFromUrl != null) {
            fromUrl = requestedFromUrl;
        }
    }
}
