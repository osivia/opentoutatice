
package fr.toutatice.ecm.platform.services.permalink;

import java.util.Map;

import org.nuxeo.ecm.core.api.DocumentModel;

public interface Permalink
{

    public abstract String getPermalink(DocumentModel documentmodel, String host, Map<String,String> params);
}
