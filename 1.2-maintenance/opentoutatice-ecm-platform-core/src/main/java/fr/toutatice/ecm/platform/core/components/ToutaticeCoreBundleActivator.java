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
package fr.toutatice.ecm.platform.core.components;

import java.util.List;

import org.nuxeo.ecm.automation.core.scripting.Functions;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.platform.ec.notification.NotificationEventListener;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationService;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationServiceHelper;
import org.nuxeo.runtime.api.Framework;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.toutatice.ecm.platform.core.freemarker.ToutaticeFunctions;
import fr.toutatice.ecm.platform.core.helper.ToutaticeEmailHelper;

/**
 * Cette classe à pour objectif d'installer le service custom permettant de filtrer les événements
 * pendant l'exécution des traitements en mode "silencieux". Permet de réaliser des opérations sur les
 * documents sans modifier les méta-données de ceux-ci pour autant (dublincore, ...).
 * 
 * La méthode start() sera appelé au démarrage lors du déploiement du bundle et installera le service
 * ToutaticeEventFilterService. Ainsi, lorsque le framework sera sollicité pour obtenir une 
 * instance du service EventService, il obtiendra un proxy sur ce dernier.
 *  
 * @author mberhaut1
 */
public class ToutaticeCoreBundleActivator implements BundleActivator {
	private static Object currentFnInstance = null;
//	private static Object currentMailHelper = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
		// sauvegarde de l'ancienne instance Fn
		currentFnInstance = Functions.getInstance();
		// instancier le nouvel Fn
		ToutaticeFunctions localFn = new ToutaticeFunctions();
		Functions.setInstance(localFn);
		// initialiser le mailHelper spécifique Toutatice (pour gestion du permalien portail) 
		installEmailHelper();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// restauration de l'ancienne instance Fn 
		Functions.setInstance(currentFnInstance);
	}

	private void installEmailHelper() throws Exception {
		ToutaticeEmailHelper toutaticeEmailHelper = new ToutaticeEmailHelper();
		
		// récupération du service de notification
		NotificationService notificationService = (NotificationService) NotificationServiceHelper.getNotificationService();
		//setter la nouvelle classe helper
		notificationService.setEmailHelper(toutaticeEmailHelper);
		
		//récupérer l' eventListener de notif
		EventService eventService = Framework.getService(EventService.class);
        List<PostCommitEventListener> listeners = eventService.getPostCommitEventListeners();
		//setter la nouvelle classe helper
        for (PostCommitEventListener postCommitEventListener : listeners) {
            if (postCommitEventListener.getClass().equals(NotificationEventListener.class)) {
                ((NotificationEventListener) postCommitEventListener).setEmailHelper(toutaticeEmailHelper);
            }
        }
		
	}
}
