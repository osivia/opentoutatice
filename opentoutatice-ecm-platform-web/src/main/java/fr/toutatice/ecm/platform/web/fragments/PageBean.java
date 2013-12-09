package fr.toutatice.ecm.platform.web.fragments;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

@Name("pageBean")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class PageBean {

    /**
     * identifiant url de provenance
     */
    @RequestParameter("fromUrl")
    private String requestedFromUrl;
    
    
    private String fromUrl;

    /**
     * @return the fromUrl
     */
    public String getFromUrl() {
        return fromUrl;
    }

    /**
     * @param fromUrl the fromUrl to set
     */
    public void setFromUrl(String fromUrl) {
        this.fromUrl = fromUrl;
    }

    @Create
    public void startUp() {
        // En mode édition, retourne l'uri passée en parmaètre
        if (requestedFromUrl != null) {
            fromUrl = requestedFromUrl;
        }
    }
}
