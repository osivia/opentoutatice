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
package fr.toutatice.ecm.platform.web.document;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.webapp.edit.lock.LockActionsBean;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;

/**
 * Cette classe a été implémentée pour pallier un problème de gestion des verroux.
 * Voir le ticket Jira Nuxeo #4751
 * 
 * Si un document possède un verrou (un enregistrement dans la table 'locks' en base) et que les données
 * ont mal été importées (owner=null et created=null) alors:
 *  - renvoyer un objet 'lockDetails' non vide pour que les widgets de présentation du verrou/actions soient cohérents.
 *  - loger une erreur pour signaler le problème d'import de base de données
 * 
 * @author mberhaut1
 */
@Name("lockActions")
@Scope(ScopeType.EVENT)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeLockActionsBean extends LockActionsBean {

	private static final long serialVersionUID = 1L;

//	private static final Log log = LogFactory.getLog(ToutaticeLockActionsBean.class);
	
	public String getLockTime() throws NuxeoException {
		String lockTime = "Date de verouillage inconnue";
		
		Map<String, Serializable> details = getCurrentDocLockDetails();
		if (null != details && null != details.get(LOCK_CREATED)) {
			Calendar time = (Calendar) details.get(LOCK_CREATED);
			lockTime = DateFormat.getDateInstance().format(new Date(time.getTimeInMillis()));
		}
		
		return lockTime;
	}

}
