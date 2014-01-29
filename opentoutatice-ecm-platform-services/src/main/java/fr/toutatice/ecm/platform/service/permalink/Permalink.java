
package fr.toutatice.ecm.platform.service.permalink;

import java.util.Map;

import org.nuxeo.ecm.core.api.DocumentModel;

public interface Permalink
{

    public abstract String getPermalink(DocumentModel documentmodel, String host, Map<String,String> params);
}
