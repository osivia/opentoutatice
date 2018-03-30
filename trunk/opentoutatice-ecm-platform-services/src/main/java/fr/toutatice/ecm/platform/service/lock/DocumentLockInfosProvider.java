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
package fr.toutatice.ecm.platform.service.lock;

import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;

/**
 * Service used to get all lock informations about a document
 * @author lbillon
 *
 */
public interface DocumentLockInfosProvider extends DocumentInformationsProvider {

	/** The time when document has been locked */
	public final static String LOCK_CREATION_DATE = "lockCreationDate";

	/** Owner of the lock */
	public final static String LOCK_OWNER = "lockOwner";
	
	/** Lock status */
	public final static String LOCK_STATUS = "lockStatus";
	
    public enum LockStatus {
        /** Default state : can lock */
        can_lock,
        /** Can uunlock */
        can_unlock,
        /** a lock is set and is not removable by this user*/
        locked,
        /** No lock avaliable (proxies, versions, ...) */
        no_lock;    	
    }
	
}