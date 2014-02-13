package fr.toutatice.ecm.platform.automation;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

import fr.toutatice.ecm.platform.core.utils.helper.DirectoryMngtHelper;


@Operation(id = GetVocabularies.ID, 
		category = Constants.CAT_SERVICES,
        label = "Get Vocabularies", 
        since = "5.4",
        description = "Vocabularies are serialized using JSON and returned in a Blob.")

public class GetVocabularies
{
    public static final String ID = "Document.GetVocabularies";

    private static final Log log = LogFactory.getLog(GetVocabularies.class);

	@Param(name = "vocabularies", required = true)
	protected String vocabularies;
	
	@Param(name = "locale", required = false)
	protected String locale;
	
	ArrayList<String> listValVoc;
	HashMap<String, ArrayList<String>> listVoc;
	ArrayList<String> indexVoc;
	

    @OperationMethod
    public Blob run() throws Exception 
    {
    	DocumentModelList entries = null;
    	
    	if (log.isDebugEnabled()) {
    		log.debug("vocabularies:"+vocabularies);
    		log.debug("locale:"+locale);
    	}
    	
    	Locale localeChoisie = null;
    	if (StringUtils.isNotBlank(locale))
    	{
    		localeChoisie = new Locale(locale);
    	}
    	else
    	{
    		localeChoisie = Locale.FRENCH;
    	}
    	
    	// Alimentation des structures de données à partir des Directories Nuxeo
    	listValVoc = null;
    	listVoc = new HashMap<String, ArrayList<String>>();
    	indexVoc = new ArrayList<String>();
    	StringTokenizer parVocToken = new StringTokenizer(vocabularies, ";");
    	String voc;
        while (parVocToken.hasMoreTokens()) 
        {
        	voc = parVocToken.nextToken();
        	indexVoc.add(voc);
        	entries = ToutaticeDirectoryMngtHelper.instance().getEntries(voc);
        	
        	listValVoc = new ArrayList<String>();
        	for (DocumentModel entry : entries) 
            {
				String localizedEntryLabel = ToutaticeDirectoryMngtHelper.instance().getDirectoryEntryLocalizedLabel(voc, entry.getId(), localeChoisie);
        		listValVoc.add(URLEncoder.encode(entry.getTitle(),"UTF-8")+";"+URLEncoder.encode(localizedEntryLabel,"UTF-8")+";"+entry.getProperty("xvocabulary", "parent"));
            }
        	listVoc.put(voc, listValVoc);
        }
        
        // Parcours des structures de données et construction du flux JSON
        JSONArray rows = getChildren(null, 0);

        if (log.isDebugEnabled()) {
        	log.debug("JSON:"+rows);
        }

        return new StringBlob(rows.toString(), "application/json");
    }

    
    JSONArray getChildren(String key, int i  ) 
    {
    	JSONArray rows = new JSONArray();
    	String voc = "";
    	if (i < indexVoc.size()) 
        {
        	voc = (String) indexVoc.get(i); //continent
        	ArrayList<String> array = (ArrayList<String>) listVoc.get(voc);
        	i++; 
        	for (String valeur : array) 
        	{
        		StringTokenizer v = new StringTokenizer(valeur, ";");
     		   	String valkey = v.nextToken();
     		   	String vali18n = v.nextToken();
     		   	String valparent = v.nextToken();
     		   	if (valparent.equalsIgnoreCase(key) || (key == null))
     		   	{
	        		//remplir
	 		   		JSONObject obj = new JSONObject();
	                obj.element("key", valkey); //europe
	                obj.element("value", vali18n);
	                if(i != indexVoc.size())
	                {
	             	   obj.element("children", getChildren(valkey, i));
	                }
	                else
	                {
	             	   //obj.element("children", getChildren(cle, indexVoc, i, parVocToken.nextToken()));
	                }
	                rows.add(obj);
     		   	}
        	}
        }
    	return rows;
    }
    
}
