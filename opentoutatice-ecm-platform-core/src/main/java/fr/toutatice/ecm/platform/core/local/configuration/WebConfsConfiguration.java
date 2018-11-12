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
 * lbillon
 * dchevrier
 */
package fr.toutatice.ecm.platform.core.local.configuration;

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * @author david chevrier.
 * 
 *         Configuration objects (WebConfiguration type) can be created in WebConfigurationFolder at:
 *         <ul>
 *         <li>Root level (repo)</li>
 *         <li>Domain level</li>
 *         </ul>
 *         Configuration Objects created at Root level can be allowed or not at Domain level according to the "Local configuration" tab of Nuxeo
 *         (this behavior is guided by WebConfigurationsLocalConfiguration facet - and SuperSPace facet of Nuxeo
 *         which allow "Local configuration" tab displaying).
 *         This class manages only configuration Objects created at Root level (global configurations) filtered or not at Domain level.
 *
 */
public interface WebConfsConfiguration {

    /**
     * @return Global configuration objects, i.e. accessible on all repo (direct children of Root document (repo))
     *         of a Domain adapted as WebConfsConfiguration.
     */
    List<DocumentModel> getAllConfsDocs();

    void setAllConfsDocs(List<DocumentModel> allConfsDocs);

    /**
     * @return Codes list of allowed web confs of a Domain adapted as WebConfsConfiguration.
     */
    List<String> getAllowedConfsDocs();

    boolean getDenyAllConfsDocs();

    /**
     * @param domain
     * @return Global configurations allowed on given Domain.
     * @throws ClientException
     */
    List<DocumentModel> getSelectedConfs(DocumentModel domain) throws ClientException;

    /**
     * @param domain
     * @return Codes list of allowed web confs of given Domain.
     * @throws ClientException
     */
    List<String> getAllowedWebConfs(DocumentModel domain) throws ClientException;

    /**
     * @param domain
     * @return Global web confs of given Domain.
     * @throws ClientException
     */
    List<DocumentModel> getAllGlobalWebConfs(DocumentModel domain);

}
