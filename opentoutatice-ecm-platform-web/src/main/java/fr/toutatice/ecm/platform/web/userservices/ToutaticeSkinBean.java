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
 * mberhaut1
 * dchevrier
 * lbillon
 */
package fr.toutatice.ecm.platform.web.userservices;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.richfaces.skin.Skin;
import org.richfaces.skin.SkinFactory;

/**
 * @author David Chevrier
 * 
 * To override default RichFaces skin.
 *
 */
@Name("skinBean")
@Scope(ScopeType.SESSION)
public class ToutaticeSkinBean {
	
	public static String NO_SKIN = "plain";
	public static String DEFAULT_SKIN = "DEFAULT";
	
	private String skin;
	
	@RequestParameter("fromUrl")
	private String requestedFromUrl;
	
	@Create
	public void startUp() {
		skin = getConfiguredSkin();
	}

	public String getSkin() {
		String skinName = skin;
		if(StringUtils.isNotBlank(requestedFromUrl)){
			skinName = NO_SKIN;
		}
		return skinName;
	}

	public void setSkin(String skin) {
		this.skin = skin;
	}
	
	private static String getConfiguredSkin(){
		String skinName = StringUtils.EMPTY;
		FacesContext context = FacesContext.getCurrentInstance();
        Skin skin = SkinFactory.getInstance(context).getSkin(context);
        if(skin != null){
        	skinName = skin.getName();
        }
        return skinName;
	}

}
