/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.automation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

@Operation(id = TextExtraction.ID, category = Constants.CAT_CONVERSION, label = "Text extraction operation", description = "Operation to extract plain text from fields. For HTLM text, format_in must contain html for escaping. ")

public class TextExtraction {
    
    public static final String ID = "TextExtraction.Operation";

    public static final Log log = LogFactory.getLog(TextExtraction.class); 

    @Context
    protected CoreSession session;
    
    @Param(name="xpath_in", required=true)
    protected String xpath_in;
    
    @Param(name="format_in", required=false)
    protected String format_in;

    @Param(name="xpath_out", required=true)
    protected String xpath_out;
    
    @Param(name="nb_chars_out", required=true)
    protected int nb_chars_out;
    
    @Param(name="override", required=false)
    protected boolean override=true;
    

    @OperationMethod
    public DocumentModel run(DocumentModel doc) throws PropertyException, NuxeoException {
        
        String input = (String) doc.getPropertyValue(xpath_in);
        if (input == null) {
            return doc;
        }
        
        String output = (String) doc.getPropertyValue(xpath_out);
        boolean hasContent = output != null && output.length()>0;
        
        
        String extraction = input;
        
        if( "html".equals(format_in))
            extraction = Jsoup.parse(extraction).text();
            //extraction = StringEscapeUtils.unescapeHtml(extraction).replaceAll("\\<.*?\\>", "");
  
        
        if( extraction.length() > nb_chars_out) {
            // Recherche dernier espace
            while( extraction.length() > nb_chars_out - 3 && extraction.lastIndexOf(' ') != -1) {
                extraction = extraction.substring(0,  extraction.lastIndexOf(' '));
            }
            
            if( extraction.length() <= nb_chars_out - 3)    {
                extraction += "...";
            }
            
        }
     
        if (override || !hasContent) {
            doc.setPropertyValue(xpath_out, extraction);
        }
        
        
        return doc;
    }

}

