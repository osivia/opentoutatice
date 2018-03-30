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
 * 
 */
package fr.toutatice.ecm.platform.service.url;

import java.util.Map;

import org.nuxeo.ecm.core.api.DocumentRef;

/**
 * @author David Chevrier
 * 
 */
public class WebIdRef implements DocumentRef {

	private static final long serialVersionUID = -3531204028673068100L;

	public final static int WEBID = 3;
	private final String explicitUrl;
	private final String value;
	private final String extensionUrl;
	private Map<String, String> parameters;

	public WebIdRef(String explicitUrl, String value, String extensionUrl) {
		this.explicitUrl = explicitUrl;
		this.value = value;
		this.extensionUrl = extensionUrl;
	}
	
	public WebIdRef(String explicitUrl, String value, String extensionUrl, Map<String, String> parameters) {
        this.explicitUrl = explicitUrl;
        this.value = value;
        this.extensionUrl = extensionUrl;
        this.parameters = parameters;
    }

	@Override
	public int type() {
		return WEBID;
	}

	@Override
	public Object reference() {
		return value;
	}
	
	public String getExplicitUrl(){
		return explicitUrl;
	}
	
	public String getExtensionUrl(){
		return this.extensionUrl;
	}
    
    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
	
}
