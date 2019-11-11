/**
 * 
 */
package fr.toutatice.ecm.plarform.web.filemanager;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.component.file.InputFileInfo;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.service.quota.QuotaService;


/**
 * @author david, Loïc Billon
 * 
 *
 */
@Name("ottcFileValidator")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class FileValidatorBean implements Serializable {
    
    private static final long serialVersionUID = 6142695676085523525L;
    
    /** Media primary mime-types. */
    public static final String[] MEDIA_PRIMARY_MIME_TYPES = {"image", "audio", "video"};
        
    @In(create = true, required = false)
    protected CoreSession documentManager;
    
    @In
    protected DocumentModel currentDocument;
    
    
    
    /**
     * Checks if File has no media type file content.
     * 
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validateMimeType(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if(value != null){
            InputFileInfo file = (InputFileInfo) value;
            try {
                
                MimeType mimeType = new MimeType((String) file.getMimeType());
                if(ArrayUtils.contains(MEDIA_PRIMARY_MIME_TYPES, mimeType.getPrimaryType())){
                    String msg = ComponentUtils.translate(context, "ottc.validator.file.bad.mime.type");
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                    throw new ValidatorException(message);
                }
                
            } catch (MimeTypeParseException e) {
                String msg = ComponentUtils.translate(context, "ottc.validator.file.unknown.mime.type");
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                throw new ValidatorException(message);
            }
            
        }
    }
    
	/** File size units. */
	private static final String[] UNITS = { "o", "Ko", "Mo", "Go", "To" };
	/** Unit factor. */
	private static final double UNIT_FACTOR = 1024;
    
	/**
	 * Return the computed free size on the workspace (in case of workspace),
	 * else return the default limit if specified
	 * else return blank (no limit) 
	 * 
	 * @return allowed space
	 */
    public String getFreeSpace() {
    	
    	String propertyWidgetMaxSize = Framework.getProperty("ottc.widget.filemaxSize");
    	String propertyWidgetMaxSizeInt = Framework.getProperty("ottc.widget.filemaxSize.long");
    	long propertyWidgetInLong = Long.parseLong(propertyWidgetMaxSizeInt);
    	
    	String freeSpaceStr = "";
    	
    	if(StringUtils.isNotBlank(propertyWidgetMaxSize)) {
    		freeSpaceStr = propertyWidgetMaxSize;
    	}
    	
        QuotaService qs = Framework.getService(QuotaService.class);
        if(qs != null) {
        	Long freeSpace = qs.getFreeSpace(documentManager, currentDocument);
        	
        	if(freeSpace != null) {
        		
        		// if free space is less than a MB.
        		if(freeSpace < 1000000) {
        			return "1Ko"; // no space left
        		}
        		// size beyond the default widget config
        		else if(freeSpace < propertyWidgetInLong) {
					// Factor
					int factor = Double.valueOf(Math.log10(freeSpace) / Math.log10(UNIT_FACTOR)).intValue();
					// Factorized size
					double factorizedSize = freeSpace / Math.pow(UNIT_FACTOR, factor);
					
					String unit = I18NUtils.getMessageString("messages", UNITS[factor], null,Locale.getDefault());
					
					// Number format
					NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
					numberFormat.setMaximumFractionDigits(0);
		
					freeSpaceStr = numberFormat.format(factorizedSize) + unit;
        		}
        		// else display default widget settings
        	}
        }
        
        
    	return freeSpaceStr;
    }

}
