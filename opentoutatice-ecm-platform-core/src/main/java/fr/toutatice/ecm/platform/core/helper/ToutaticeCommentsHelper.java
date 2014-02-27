/**
 * 
 */
package fr.toutatice.ecm.platform.core.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;


/**
 * @author David Chevrier
 */
public class ToutaticeCommentsHelper {
    
    private ToutaticeCommentsHelper(){}

    public static Map<DocumentModel, List<DocumentModel>> getProxyComments(DocumentModel proxy) throws ClientException {
        Map<DocumentModel, List<DocumentModel>> comments = new HashMap<DocumentModel, List<DocumentModel>>();
        CommentableDocument commentableDoc = proxy.getAdapter(CommentableDocument.class);
        List<DocumentModel> rootComments = commentableDoc.getComments();
        for(DocumentModel rootComment : rootComments){
            List<DocumentModel> commentsThread = commentableDoc.getComments(rootComment);
            comments.put(rootComment, commentsThread);
        }
        return comments;
    }

    public static void setComments(CoreSession session, DocumentModel document, Map<DocumentModel, List<DocumentModel>> comments) throws ClientException {
        CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
        for(DocumentModel rootComment : comments.keySet()){
            commentableDoc.addComment(rootComment);
            for(DocumentModel comment : comments.get(rootComment)){
                commentableDoc.addComment(rootComment,  comment);
            }
        }
    };
    
    

}
