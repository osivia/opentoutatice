package fr.toutatice.ecm.platform.service.publication;

import java.io.Serializable;

import org.nuxeo.ecm.core.api.DocumentModel;

public interface PublicationService  extends Serializable
{

    public abstract String getPermalink(DocumentModel documentmodel);
}
