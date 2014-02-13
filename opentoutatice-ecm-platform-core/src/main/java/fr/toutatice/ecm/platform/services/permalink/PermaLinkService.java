package fr.toutatice.ecm.platform.services.permalink;

import java.io.Serializable;

import org.nuxeo.ecm.core.api.DocumentModel;

public interface PermaLinkService  extends Serializable
{
    public abstract String getPermalink(DocumentModel documentmodel);
}
