package fr.toutatice.ecm.platform.web.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.actions.Action;
import org.nuxeo.ecm.platform.actions.ActionContext;
import org.nuxeo.ecm.webapp.action.WebActionsBean;
import org.nuxeo.ecm.webapp.helpers.EventNames;

import fr.toutatice.ecm.platform.core.constants.GlobalConst;
import fr.toutatice.ecm.platform.web.annotations.Install;

@Name("webActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.TOUTATICE)
public class ToutaticeWebActionsBean extends WebActionsBean {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(ToutaticeWebActionsBean.class);
	private static final String CST_CONDITIONAL_ACTION_PATTERN = "(.+@(VIRTUAL_ACTION_.+))";
	
	Pattern pattern = Pattern.compile(CST_CONDITIONAL_ACTION_PATTERN);
	
	private static enum ACTION_STATUS {
		UNDEFINED,
		ENABLED,
		DISABLED
	};
	
	// cache pour éviter les traitements identiques multiples et préserver les performances
	private Map<String, ACTION_STATUS> conditionalActionsStatusMap = null;
	
	/**
	 * Permet de bien gérer le rafraîchissement de l'IHM lors de la circulation entre les sous-onglets
	 */
	public void notifySubTabSelectionChanged() {
		Events.instance().raiseEvent(GlobalConst.CST_EVENT_SUB_TAB_SELECTION_CHANGED, "");
	}

	/**
	 * Cette méthode est surdéfinie afin de permettre la présentation conditionnelle de certaines actions de la catégorie "Document list Toolbar"
	 * (ex: les actions de la catégorie "CURRENT_SELECTION_SECTIONS_LIST")
	 * 
	 * Les actions de mise en ligne et hors ligne doivent apparaître seulement si le conteneur se trouve dans un espace de type "web container".
	 * Par défaut, l'implémentation Nuxeo ne permet pas d'afficher de façon conditionnelle une action sur le résultat d'un filtre ou évaluation
	 * d'une méthode de bean seam.
	 * 
	 * Mécanisme:
	 * 
	 * @see org.nuxeo.ecm.webapp.action.WebActionsBean#getUnfiltredActionsList(java.lang.String, org.nuxeo.ecm.platform.actions.ActionContext)
	 */
	@Override
	public List<Action> getUnfiltredActionsList(String category, ActionContext context) {
        List<Action> list = new ArrayList<Action>();
        
        List<Action> actions = actionManager.getActions(category, context, false);
        for (Action action : actions) {
        	if (action.getId().matches(CST_CONDITIONAL_ACTION_PATTERN)) {
        		if (isConditionalActionEnabled(context, action)) {
        			list.add(action);
        		}
        	} else {
        		list.add(action);
        	}
        }
        
        return list;
	}
	
	private boolean isConditionalActionEnabled(ActionContext context, Action action) {
		// extraire le nom de l'action virtuelle/conditionnelle associée à l'action courante
		String virtualActionName = getVirtualActionName(action);
		
		if (null == virtualActionName) {
			return false;
		}
		
		if (null == this.conditionalActionsStatusMap) {
			this.conditionalActionsStatusMap = new HashMap<String, ACTION_STATUS>();
		}
		
		if (null == this.conditionalActionsStatusMap.get(virtualActionName) || ACTION_STATUS.UNDEFINED == this.conditionalActionsStatusMap.get(virtualActionName)) {
			this.conditionalActionsStatusMap.put(virtualActionName, ACTION_STATUS.DISABLED);
			
			Action refAction = actionManager.getAction(virtualActionName);
			boolean isEnabled = actionManager.isEnabled(refAction.getId(), context);
			if (isEnabled) {
				this.conditionalActionsStatusMap.put(virtualActionName, ACTION_STATUS.ENABLED);
			}
		}
		
		return (ACTION_STATUS.ENABLED == this.conditionalActionsStatusMap.get(virtualActionName));
	}
	
	private String getVirtualActionName(Action action) {
		String name = null;
		
		Matcher matcher = pattern.matcher(action.getId());
		if (matcher.find() && (2 == matcher.groupCount())) {
			name = matcher.group(2);
		} else {
			log.debug("Failed to get the virtual action associated to the action id='"+ action.getId() +"' (pattern '...@VIRTUAL_ACTION_...' not fitted");
		}
		
		return name;
	}
	
	public String getLabeltoActionId(String actionId,DocumentModel doc){
		String res;
		
		if("SAVE_AND_MINOR_UPGRADE_CURRENT_DOCUMENT".equals(actionId)){
			res = getNextMinorversion(doc);
		}else if("SAVE_AND_MAJOR_UPGRADE_CURRENT_DOCUMENT".equals(actionId)){
			res = getNextMajorversion(doc);
		}else{			
			res="";
		}
		return	res;	
	}
	
	private String getNextMinorversion(DocumentModel doc){
		StringBuilder res= new StringBuilder();
		
		String versionLabel = doc.getVersionLabel();
		versionLabel=versionLabel.replaceAll("\\+", "");
		String [] tab = versionLabel.split("\\.");
		res.append(tab[0]);
		res.append(".");
		res.append(new Integer(tab[1]).intValue()+1);
		
		return res.toString();
	}
	
	private String getNextMajorversion(DocumentModel doc){
		StringBuilder res= new StringBuilder();
		
		String versionLabel = doc.getVersionLabel();
		
		String [] tab = versionLabel.split("\\.");
		res.append(new Integer(tab[0]).intValue()+1);
		res.append(".");
		res.append("0");
		
		return res.toString();
	}
	
    @Observer(value = { EventNames.DOCUMENT_SELECTION_CHANGED, 
    		EventNames.DOMAIN_SELECTION_CHANGED,
    		EventNames.CONTENT_ROOT_SELECTION_CHANGED,
    		EventNames.DOCUMENT_CHANGED,
    		EventNames.GO_HOME}, create = false)
	public void resetActionContext() {
    	if (null != this.conditionalActionsStatusMap) {
    		this.conditionalActionsStatusMap.clear();
    	}
        tabsActionsList = null;
        resetSubTabs();
        resetCurrentTab();
	}
	
}
