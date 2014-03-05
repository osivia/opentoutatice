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
package fr.toutatice.ecm.platform.automation.helper;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

public class ToutaticeXPathPropertyHelper {
	private static final Log log = LogFactory.getLog(ToutaticeXPathPropertyHelper.class);

	private static enum INDEX_TYPE {
		UNDEFINED,
		ONE,
		ALL;
	}
	
	private Object value = null;
	private int index;
	private INDEX_TYPE indexType = INDEX_TYPE.UNDEFINED;

	public ToutaticeXPathPropertyHelper(DocumentModel document, String xpath) throws PropertyException, ClientException {
		if (null == document || StringUtils.isBlank(xpath)) {
			log.debug("Both parameters 'document' and 'xpath' cannot be null");
			throw new ClientException("Both parameters 'document' and 'xpath' cannot be null");
		}
		
		// analyser la propriété
		if (xpath.matches(".+/.+$")) {
			// la propriété est une liste
			String[] tokens = xpath.split("/");
			xpath = tokens[0];

			if ("*".equals(tokens[1])) {
				// tous les éléments de la liste sont désignés
				this.indexType = INDEX_TYPE.ALL;
			} else {
				// un élément de la liste est désigné
				this.index = Integer.parseInt(tokens[1]);
				this.indexType = INDEX_TYPE.ONE;
			}
		}
		
		this.value = document.getPropertyValue(xpath);		
	}

	@SuppressWarnings("rawtypes")
	public Object getValue() {
		Object val = null;
		
		if (null != this.value) {
			val = this.value;
			
			if (this.value instanceof List && INDEX_TYPE.ONE.equals(this.indexType)) {
				val = ((List) this.value).get(index);
			}
		}
		
		return val;
	}
	
	public int getIndex() {
		return index;
	}

	public boolean isList() {
		return ((null != this.value) ? (this.value instanceof List) && allElements() : false);
	}
	
	public boolean allElements() {
		return !INDEX_TYPE.ONE.equals(this.indexType);
	}

}
