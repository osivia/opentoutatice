package fr.toutatice.ecm.platform.web.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.forum.web.PostActionBean;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.core.service.TaskEventNotificationHelper;

import fr.toutatice.ecm.platform.web.annotations.Install;

@Name("postAction")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.TOUTATICE)
public class ToutaticePostActionBean extends PostActionBean {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void startModeration(DocumentModel post) throws ClientException {
		super.startModeration(post);
		
		DocumentModel thread = getParentThread();
		ArrayList<String> moderators = (ArrayList<String>) thread.getProperty("thread", "moderators");
        String[] listmoderators = new String[moderators.size()];
        int i = 0;
        for (String moderator : moderators) {
        	listmoderators[i++] = moderator;
        }
		
		// notification pour modération du nouveau commentaire/post par les modérateurs
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put("recipients", listmoderators);
        Task currentTask = getModerationTask(thread, post.getId());
        TaskEventNotificationHelper.notifyEvent(documentManager, thread, (NuxeoPrincipal) currentUser, currentTask, "ForumCommentPublication", properties, "", null);
	}
	
}
