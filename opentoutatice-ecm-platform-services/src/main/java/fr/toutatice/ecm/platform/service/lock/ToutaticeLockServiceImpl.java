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

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.Lock;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

/**
 * Service used to get all lock informations about a document
 * @author lbillon
 *
 */
public class ToutaticeLockServiceImpl implements ToutaticeLockService {

	@Override
	public Map<String, Object> fetchInfos(CoreSession coreSession,
			DocumentModel currentDocument) throws ClientException {

		Map<String, Object> lockInfo = new HashMap<String, Object>();

		Lock lock = currentDocument.getLockInfo();

		NuxeoPrincipal principal = (NuxeoPrincipal) coreSession.getPrincipal();

		if (!currentDocument.isProxy() && !currentDocument.isFolder()) {
			if (lock != null) {
				lockInfo.put(LOCK_CREATION_DATE, lock.getCreated());
				lockInfo.put(LOCK_OWNER, lock.getOwner());

				if ((principal.isAdministrator()
						|| coreSession.hasPermission(principal,
								currentDocument.getRef(),
								SecurityConstants.EVERYTHING) || coreSession
							.hasPermission(principal, currentDocument.getRef(),
									SecurityConstants.WRITE))
						&& !currentDocument.isVersion()) {
					lockInfo.put(LOCK_STATUS, LockStatus.can_unlock);

				}
				else {
					lockInfo.put(LOCK_STATUS, LockStatus.locked);
				}
			} else if ((principal.isAdministrator()
					|| coreSession.hasPermission(principal,
							currentDocument.getRef(),
							SecurityConstants.EVERYTHING) || coreSession
						.hasPermission(principal, currentDocument.getRef(),
								SecurityConstants.WRITE))
					&& !currentDocument.isVersion()) {
				lockInfo.put(LOCK_STATUS, LockStatus.can_lock);
			}

		}
		return lockInfo;
	}

}
