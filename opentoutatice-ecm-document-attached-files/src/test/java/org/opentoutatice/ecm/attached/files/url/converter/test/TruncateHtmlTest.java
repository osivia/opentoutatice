package org.opentoutatice.ecm.attached.files.url.converter.test;

import org.jsoup.Jsoup;
import org.junit.Test;

import fr.toutatice.ecm.platform.core.freemarker.ToutaticeFunctions;

//@RunWith(FeaturesRunner.class)
//@Features(CoreFeature.class)
public class TruncateHtmlTest {

    @Test
    public void test_truncate_html() {

        String html = "<blockquote>"
                + "<p style='text-align: center;'>WSACLtestcontributorEtudesdulittoralahah</p>"
                + "</blockquote>"
                + "<p style='text-align: right;'>test WSACLtestcontributorEtudesdulittoralahah</p>"
                + "<ol>"
                + "<li>WSACLtestcontributorEtudesdulittoralahah</li>"
                + "</ol>"
                + "<ul>"
                + "<li>WSACLtestcontributorEtudesdulittoralahah</li>"
                + "</ul>"
                + "<p><span style='text-decoration: underline;'><em><strong>WSACLtestcontributorEtudesdulittoralahah</strong></em></span></p>"
                + "<p style='text-align: justify;'><sub><span style='text-decoration: line-through;'>WSACLtestcontributorEtudesdulittoralahah</span></sub></p>"
                + "<p><span style='color: #ff0000;'>WSACLtestcontributorEtudesdulittoralahah</span></p>"
                + "<p style='padding-left: 60.0px;'><span style='background-color: #ffff00;'>Philat&eacute;lie</span></p>"
                + "<h3>Les amateurs de timbres.</h3>"
                + "<h3 class='h4 media-heading'><a class='no-ajax-link'>WSACL</a></h3>"
                + " <p>Biblioth&egrave;que</p>"
                + "<div class='media'>"
                + "<div class='media-left media-middle'><img class='media-object' src='http://vm-integ-foad/toutatice-portail-cms-nuxeo/binary?type=FILE&amp;path=%2Fdefault-domain%2Fworkspaces%2Fetudes-du-littoral&amp;portalName=foad&amp;liveState=true&amp;fieldName=ttc:vignette&amp;t=1485975602350' alt='' /></div>"
                + "<div class='media-body media-middle'>"
                + "<h3 class='h4 media-heading'><a class='no-ajax-link'>Etudes du littoral</a></h3>"
                + "<p>Etudes du littoral</p>"
                + "</div>"
                + "</div>"
                + "<div class='media'>"
                + "<div class='media-left media-middle'><img class='media-object' src='http://vm-integ-foad/toutatice-portail-cms-nuxeo/binary?type=FILE&amp;path=%2Fdefault-domain%2Fworkspaces%2Fbibliotheque&amp;portalName=foad&amp;liveState=true&amp;fieldName=ttc:vignette&amp;t=1485975603100' alt='' /></div>"
                + "<div class='media-body media-middle'>"
                + "<h3 class='h4 media-heading'><a class='no-ajax-link'>Biblioth&egrave;que</a></h3>"
                + "<p>Biblioth&egrave;que</p>"
                + "</div>"
                + "</div>"
                + "<div class='media-left media-middle'><img class='media-object' src='http://vm-integ-foad/toutatice-portail-cms-nuxeo/binary?type=FILE&amp;path=%2Fdefault-domain%2Fworkspaces%2Fphilatelie&amp;portalName=foad&amp;liveState=true&amp;fieldName=ttc:vignette&amp;t=1484314241130' alt='' /></div>"
                + "<h3 class='h4 media-heading'><a class='no-ajax-link'>Philat&eacute;lie</a></h3>"
                + "<p>Les amateurs de timbres.</p>"
                + "<p>&nbsp;</p>"
                + "<p>workspace.gif','common:icon-expanded':null,'common:size':null,'file:content':null,'file:filename':null,'webc:welcomeText':null,'webc:useCaptcha':null,'webc:welcomeMedia':null,'webc:theme':'sites','webc:isWebContainer':null,'webc:baseline':null,'webc:template':null,'webc:logo':null,'webc:themePerspective':null,'webc:themePage':'workspace','webc:name':'WS cr&eacute;a ACL 2','webc:moderationType':'aposteriori','webc:url':'ws-crea-acl-2','webc:email':null,'ttcn:picture':null,'publish:sections':[]}]}, index {[nuxeo][doc][b9f96ffd-fedd-44ef-b120-54fb68040082], source[{'ecm:repository':'default','ecm:uuid':'b9f96ffd-fedd-44ef-b120-54fb68040082','ecm:name':'test-caches','ecm:title':'Test Caches','ecm:path':'/default-domain/workspaces/test-caches','ecm:primaryType':'Workspace','ecm:parentId':'65112d8b-a36b-4b91-a46c-95a360595c0b','ecm:currentLifeCycleState':'project','ecm:versionLabel':'0.0','ecm:isCheckedIn':false,'ecm:isProxy':false,'ecm:isVersion':false,'ecm:isLatestVersion':false,'ecm:isLatestMajorVersion':false,'ecm:mixinType':['TTCPortalConfigurable','SuperSpace','Versionable','Folderish','Space','Orderable'],'ecm:tag':[],'ecm:changeToken':'1485939002402','ecm:acl':['test-caches_owner','test-caches_admin','test-caches_writer','test-caches_contributor','test-caches_contributor','test-caches_reader'],'dc:creator':'admin','dc:source':null,'dc:nature':null,'dc:contributors':['admin'],'dc:created':'2016-10-05T13:28:24.10Z','dc:description':null,'dc:rights':null,'dc:subjects':[],'dc:publisher':null,'dc:valid':null,'dc:format':null,'dc:issued':null,'dc:modified':'2017-02-01T08:50:02.40Z','dc:expired':null,'dc:coverage':null,'dc:language':null,'dc:title':'Test Caches','dc:lastContributor':'admin','uid:uid':null,'uid:minor_version':0,'uid:major_version':0,'ttcs:headImage':null,'ttcs:visibility':'INVITATION','ttcs:spaceCommentable':true,'ttcs:catSpaceKey':null,'ttcs:spaceMembers':[{'login':'admin','newsPeriod':'weekly','nextNewsDate':'2017-02-01T08:52:02.33Z','joinedDate':'2016-10-05T13:28:25.00Z','lastNewsDate':'2017-02-01T08:50:01.11Z'}],'ttcs:lstPublicAuth':[],'ttc:queryPart':null,'ttc:selectors':null,'ttc:childrenPageTemplate':null,'ttc:contextualizeExternalContents':false,'ttc:hiddenInNavigation':null,'ttc:isOnline':false,'ttc:showInMenu':true,'ttc:spaceID':'0fb2b8a3-23ec-4b77-a80e-f20076683ae9','ttc:tabOrder':0,'ttc:contextualizeInternalContents':false,'ttc:pageScope':null,'ttc:extensionUrl':null,'ttc:isPreloadedOnLogin':false,'ttc:domainID':'default-domain-55','ttc:webid':'test-caches','ttc:keywords':[],'ttc:editorialTitle':null,'ttc:vignette':null,'ttc:abstract':null,'ttc:pageTemplate':null,'ttc:images':[],'ttc:theme':null,'ttc:explicitUrl':null,'ttc:lstTargetedPublic':[],'ttc:useES':false,'files:files':[],'common:icon':'/icons/workspace.gif','common:icon-expanded':null,'common:size':null,'file:content':null,'file:filename':null,'webc:welcomeText':null,'webc:useCaptcha':null,'webc:welcomeMedia':null,'webc:theme':'sites','webc:isWebContainer':null,'webc:baseline':null,'webc:template':null,'webc:logo':null,'webc:themePerspective':null,'webc:themePage':'workspace','webc:name':'Test Caches','webc:moderationType':'aposteriori','webc:url':'test-caches','webc:email':null,'ttcn:picture':null,'publish:sections':[]}]}, index {[nuxeo][doc][3ad0a13f-2c25-49aa-9fb2-95b8a722b93b], source[{'ecm:repository':'default','ecm:uuid':'3ad0a13f-2c25-49aa-9fb2-95b8a722b93b','ecm:name':'occupation-des-salles','ecm:title':'Occupation des salles','ecm:path':'/default-domain/workspaces/occupation-des-salles','ecm:primaryType':'Workspace','ecm:parentId':'65112d8b-a36b-4b91-a46c-95a360595c0b','ecm:currentLifeCycleState':'project','ecm:versionLabel':'0.0','ecm:isCheckedIn':false,'ecm:isProxy':false,'ecm:isVersion':false,'ecm:isLatestVersion':false,'ecm:isLatestMajorVersion':false,'ecm:mixinType':['TTCPortalConfigurable','SuperSpace','Versionable','Folderish','Space','Orderable'],'ecm:tag':[],'ecm:changeToken':'1485939000106','ecm:acl':['occupation-des-salles_owner','occupation-des-salles_admin','occupation-des-salles_writer','occupation-des-salles_contributor','occupation-des-salles_contributor','occupation-des-salles_reader'],'dc:creator':'admin','dc:source':null,'dc:nature':null,'dc:contributors':['admin'],'dc:created':'2016-09-30T08:25:11.64Z','dc:description':'Planning','dc:rights':null,'dc:subjects':[],'dc:publisher':null,'dc:valid':null,'dc:format':null,'dc:issued':null,'dc:modified':'2017-02-01T08:50:00.10Z','dc:expired':null,'dc:coverage':null,'dc:language':null,'dc:title':'Occupation des salles','dc:lastContributor':'admin','uid:uid':null,'uid:minor_version':0,'uid:major_version':0,'ttcs:headImage':null,'ttcs:visibility':'INVITATION','ttcs:spaceCommentable':true,'ttcs:catSpaceKey':null,'ttcs:spaceMembers':[{'login':'admin','newsPeriod':'weekly','nextNewsDate':'2017-02-01T08:52:00.04Z','joinedDate':'2016-09-30T08:25:12.20Z','lastNewsDate':'2017-02-01T08:50:00.02Z'}],'ttcs:lstPublicAuth':[],'ttc:queryPart':null,'ttc:selectors':null,'ttc:childrenPageTemplate':null,'ttc:contextualizeExternalContents':false,'ttc:hiddenInNavigation':null,'ttc:isOnline':false,'ttc:showInMenu':true,'ttc:spaceID':'0fb2b8a3-23ec-4b77-a80e-f20076683ae9','ttc:tabOrder':0,'ttc:contextualizeInternalContents':false,'ttc:pageScope':null,'ttc:extensionUrl':null,'ttc:isPreloadedOnLogin':false,'ttc:domainID':'default-domain-55','ttc:webid':'occupation-des-salles','ttc:keywords':[],'ttc:editorialTitle':null,'ttc:vignette':null,'ttc:abstract':null,'ttc:pageTemplate':null,'ttc:images':[],'ttc:theme':null,'ttc:explicitUrl':null,'ttc:lstTargetedPublic':[],'ttc:useES':false,'files:files':[],'common:icon':'/icons/workspace.gif','common:icon-expanded':null,'common:size':null,'file:content':null,'file:filename':null,'webc:welcomeText':null,'webc:useCaptcha':null,'webc:welcomeMedia':null,'webc:theme':'sites','webc:isWebContainer':null,'webc:baseline':null,'webc:template':null,'webc:logo':null,'webc:themePerspective':null,'webc:themePage':'workspace','webc:name':'Occupation des salles','webc:moderationType':'aposteriori','webc:url':'occupation-des-salles','webc:email':null,'ttcn:picture':null,'publish:sections':[]}]}]'{}<br />2017-02-01 09:50:02,833 DEBUG [Nuxeo-Event-PostCommit-45][org.nuxeo.elasticsearch.core.ElasticSearchAdminImpl] Refreshing index associated with repo: default{}<br />2017-02-01 09:50:02,913 DEBUG [Nuxeo-Event-PostCommit-45][org.nuxeo.elasticsearch.core.ElasticSearchAdminImpl] Refreshing index done{}</p>";

        String supContentToKeep = "Le piège s’est mis en place dès le premier tour de la présidentielle. Dans ce fief de droite, Emmanuel Macron a fait jeu égal avec François Fillon, député sortant de la circonscription). Persuadée que M. Macron pourrait choisir de l’épargner, NKM tente alors d’obtenir le retrait du candidat macroniste. En vain. Après une période d’hésitation parmi les principaux cadres d’En marche !, le président de la République lui fait savoir que la décision est irrévocable."
+ "Procès d’intention";
        String supContent = "Pari perdu pour Nathalie Kosciusko-Morizet dans la 2e circonscription de la capitale. La présidente du groupe LR au Conseil de Paris s’est inclinée « malgré un net sursaut des électeurs en [sa]faveur par rapport au premier tour », a-t-elle souligné. La défaite de « NKM » face à Gilles Le Gendre, candidat La République en marche (LRM) qui l’a emporté par 54,53 %, est autant le résultat d’un engrenage fatal que d’erreurs stratégiques de sa part.";

        String notSupContent = "Pari perdu pour Nathalie Kosciusko-Morizet dans la 2e circonscription de la capitale. La présidente ";
        String limitContent = "ALe piège s’est mis en place dès le premier tour de la présidentielle. Dans ce fief de droite, Emmanuel Macron a fait jeu égal avec François Fillon, député sortant de la circonscription). Persuadée que M. Macron pourrait choisir de l’épargner, NKM tente alors d’obtenir le retrait du candidat macroni";

        // String html = "<p>" + limitContent + "<span>&nbsp;...</span></p>";

        int bLength = Jsoup.parse(limitContent).text().length();

        String truncatedHtml = new ToutaticeFunctions().truncateTextFromHTML(limitContent, 300);

        int postLength = Jsoup.parse(truncatedHtml).text().length();

        System.out.println("Before: " + bLength + " | " + "After: " + postLength);
        System.out.println(truncatedHtml);
    }
    

}
