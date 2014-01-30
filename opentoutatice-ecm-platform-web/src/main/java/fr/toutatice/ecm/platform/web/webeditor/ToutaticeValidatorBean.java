package fr.toutatice.ecm.platform.web.webeditor;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;


@Name("widgetValidator")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeValidatorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(ToutaticeValidatorBean.class);

	private static final String CST_PATTERN_URL = "^(((http|ftp)[s]{0,1}://)|(mailto:[a-zA-Z0-9])).*";
	private static final String CST_PATTERN_EMAIL = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+([\\.-]+[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String CST_PATTERN_TEL = "^[0-9]{2}([- ]?[0-9]{2}){4}$";

	public void validateUrl(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		String url = (String) value;
		try {
			Pattern p = Pattern.compile(CST_PATTERN_URL);
			Matcher m = p.matcher(url);
			if (!m.matches()) {
				String msg = ComponentUtils.translate(context, "label.toutatice.validator.bad.url.format");
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
				throw new ValidatorException(message);
			}
		} catch (PatternSyntaxException pse) {
			log.error(pse.getMessage());
		}
	}

	public void validateEmail(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {

		String emails = (String) value;

		String[] tabEmails = emails.split(",");
		try {
			if (tabEmails != null && tabEmails.length > 0) {
				for (String email : tabEmails) {

					Pattern p = Pattern.compile(CST_PATTERN_EMAIL);
					Matcher m = p.matcher(email);
					if (!m.matches()) {
						String msg = ComponentUtils.translate(context, "label.toutatice.validator.bad.email.format");
						FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
						throw new ValidatorException(message);
					}

				}
			}
		} catch (PatternSyntaxException pse) {
			log.error(pse.getMessage());
		}
	}

	public void validateTel(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		String url = (String) value;
		try {
			Pattern p = Pattern.compile(CST_PATTERN_TEL);
			Matcher m = p.matcher(url);
			if (!m.matches()) {
				String msg = ComponentUtils.translate(context, "label.toutatice.validator.bad.tel.format");
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
				throw new ValidatorException(message);
			}
		} catch (PatternSyntaxException pse) {
			log.error(pse.getMessage());
		}
	}

}
