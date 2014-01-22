package fr.toutatice.ecm.platform.web.document;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.webapp.edit.lock.LockActionsBean;

import fr.toutatice.ecm.platform.web.annotations.Install;

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
@Install(precedence = Install.TOUTATICE)
public class ToutaticeLockActionsBean extends LockActionsBean {

	private static final long serialVersionUID = 1L;

//	private static final Log log = LogFactory.getLog(ToutaticeLockActionsBean.class);
	
	public String getLockTime() throws ClientException {
		String lockTime = "Date de verouillage inconnue";
		
		Map<String, Serializable> details = getCurrentDocLockDetails();
		if (null != details && null != details.get(LOCK_CREATED)) {
			Calendar time = (Calendar) details.get(LOCK_CREATED);
			lockTime = DateFormat.getDateInstance().format(new Date(time.getTimeInMillis()));
		}
		
		return lockTime;
	}

}
