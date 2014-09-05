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
package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.event.EventService;

import fr.toutatice.ecm.platform.core.components.ToutaticeServiceProvider;

public abstract class ToutaticeSilentProcessRunnerHelper extends UnrestrictedSessionRunner {
	
	private static final Log log = LogFactory.getLog(ToutaticeSilentProcessRunnerHelper.class);
	
	private static final List<Class<?>> DEFAULT_FILTERED_SERVICES_LIST = new ArrayList<Class<?>>() {
		private static final long serialVersionUID = 1L;

		{
			add(EventService.class);
		}
	};
		
	public ToutaticeSilentProcessRunnerHelper(CoreSession session) {
		super(session);
	}

    /**
     * Calls the {@link #run()} method with a silent mode. The dublincore events are disabled either for the current user session (usual run method is called)
     * or the for the system/administrator user session (unrestricted method is called).
     * 
     * @param runInUnrestrictedMode Indique si les traitements doivent être réalisés en mode unrestricted (avec l'utilisateur system/administrateur) 
     * @throws ClientException
     */
    public void silentRun(boolean runInUnrestrictedMode) throws ClientException {
    	silentRun(runInUnrestrictedMode, DEFAULT_FILTERED_SERVICES_LIST);
    }
    
    /**
     * Calls the {@link #run()} method with a silent mode. The dublincore events are disabled either for the current user session (usual run method is called)
     * or the for the system/administrator user session (unrestricted method is called).
     * 
     * @param runInUnrestrictedMode Indique si les traitements doivent être réalisés en mode unrestricted (avec l'utilisateur system/administrateur)
     * @param filteredServices the class name of services to filter (provided these one have a handler contributed onto the proxy factory service)
     * @throws ClientException
     */
    public void silentRun(boolean runInUnrestrictedMode, List<Class<?>> filteredServices) throws ClientException {
    	String userName = session.getPrincipal().getName();
		
    	log.debug("Démarrage de l'exécution d'un processus en mode silencieux");
    	
    	if (runInUnrestrictedMode && !isUnrestricted(session)) {
    		/* prendre en compte le passage en utilisateur "system" quand un utilisateur lambda demande l'exécution en mode unrestricted
    		 * d'un traitement.
    		 * 
    		 * Quand un utilisateur est de type "administrateur" (c'est à dire appartenant au groupe des administrateurs déclaré par le 
    		 * fichier "usermanager-config.xml" via le tag "<administratorsGroup>...</administratorsGroup>") le  mode unrestricted est 
    		 * déjà actif. Donc l'usager n'est pas reloggé en "system". 
    		 */
    		userName = SecurityConstants.SYSTEM_USERNAME;
    	}
    	
		try {
			// installer le service de filtrage pour l'utilisateur
			if (null != filteredServices) {
				for (Class<?> service : filteredServices) {
					ToutaticeServiceProvider.instance().register(service, userName);
				}
			} else {
				ToutaticeServiceProvider.instance().registerAll(userName);
			}
			
			// Exécuter le coprs du traitement
			if (runInUnrestrictedMode) {
				runUnrestricted();
			} else {
				run();
			}
		} finally {
			// désinstaller le service de filtrage pour l'utilisateur
			if (null != filteredServices) {
				for (Class<?> service : filteredServices) {
					ToutaticeServiceProvider.instance().unregister(service, userName);
				}
			} else {
				ToutaticeServiceProvider.instance().unregisterAll(userName);
			}
			
	    	log.debug("Fin de l'exécution d'un processus en mode silencieux");
		}
    }
    
}
