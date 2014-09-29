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
package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeUtilsConst;
import fr.toutatice.ecm.platform.core.utils.exception.ToutaticeException;

public class ToutaticeDirectoryMngtHelper {
	private static final Log log = LogFactory.getLog(ToutaticeDirectoryMngtHelper.class);
	
	private static ToutaticeDirectoryMngtHelper instance;
	private DirectoryService service;

	private ToutaticeDirectoryMngtHelper() {
		// singleton
	}

    public static ToutaticeDirectoryMngtHelper instance() throws ToutaticeException {
        if (null == instance) {
            instance = new ToutaticeDirectoryMngtHelper();
        }
        return instance;
    }

	/**
	 * Return the label of a directory entry
	 * 
	 * @param directory the directory name that contains the entry
	 * @param entryKey the key of the entry
	 * @return the entry label if found inside the directory. Otherwise will return an empty string
	 * @throws ToutaticeException if any error occurs while requesting the directory service
	 */
	public String getDirectoryEntryLabel(String directory, String entryKey) {
		String entryLabel = "";
		Session directorySession = null;
		
		if (StringUtils.isNotBlank(directory) && StringUtils.isNotBlank(entryKey)) {
			try {
				directorySession = getService().open(directory);
				if (null != directorySession) {
					DocumentModel entry = directorySession.getEntry(entryKey);
					String schemaName = getService().getDirectorySchema(directory.toString());
					entryLabel = (String) entry.getProperty(schemaName, "label");
				} else {
					log.error("Failed to obtain a session to the the directory '" + directory + "'");
				}
			} catch (Exception e) {
				log.warn("Failed to either get a session to the directory '" + directory + "' or failed to get the entry '" + entryKey + "', error: " + e.getMessage());
			} finally {
				if (null != directorySession) {
					try {
						directorySession.close();
					} catch (DirectoryException e) {
						log.error("Failed to close the session to the directory '" + directory + "', error: " + e.getMessage());
					}
				}
			}
		}

		return entryLabel;
	}
	
	public DocumentModelList getEntries(String directory) throws ToutaticeException {
		DocumentModelList entries = null;
		Session directorySession = null;
		
		try {
			directorySession = getService().open(directory);
			if (null != directorySession) {
				entries = directorySession.getEntries();
			} else {
				log.error("Failed to obtain a session to the the directory '" + directory + "'");
			}
		} catch (Exception e) {
			log.warn("Failed to either get a session to the directory '" + directory + "' or failed to get the entries, error: " + e.getMessage());
			throw new ToutaticeException(e);
		} finally {
			if (null != directorySession) {
				try {
					directorySession.close();
				} catch (DirectoryException e) {
					log.error("Failed to close the session to the directory '" + directory + "', error: " + e.getMessage());
				}
			}
		}
		
		return entries;
	}

	/**
	 * Return the label of a directory entry. The label is translated according to the locale passed-in parameter if not null.
	 * 
	 * @param directory the directory name that contains the entry
	 * @param entryKey the key of the entry
	 * @param the locale to apply to get the translated label
	 * @return the localized entry label if found inside the directory. Otherwise will return an empty string
	 */
	public String getDirectoryEntryLocalizedLabel(String directory, String entryKey, Locale locale) {
		String label = getDirectoryEntryLabel(directory, entryKey);
		return translate(label, locale);
	}

	/**
	 * Return all the labels associated to the keys & directories passed-in parameter
	 * 
	 * @param directories the list of directories associated to the entry keys
	 * @param rawKeys the raw keys (String type) with the usual separator character
	 * @param locale the local for translation
	 * @return The string representation of all labels concatenated (separated by the colon character).
	 */
	public String getDirectoryEntriesLocalizedLabel(String[] directories, String rawKeys, Locale locale) {
		String label = "";
		
		List<String> labelsList = getDirectoryEntriesLocalizedLabelList(directories, rawKeys, locale);
		for (String item : labelsList) {
			label += item + ",";
		}
		
		return label.replaceAll(",$", "");
	}
	
	public List<String> getDirectoryEntriesLocalizedLabelList(String[] directories, String rawKeys, Locale locale) {
		List<String> list = new ArrayList<String>();
		
		String[] keysList = rawKeys.split(ToutaticeUtilsConst.CST_DEFAULT_DIRECTORIES_SEPARATE_CHARACTER);
		if (keysList.length > directories.length) {
			log.warn("The list of directories ('" + directories + "') cannot be smaller than the keys list ('" + keysList + "')");
			return list;
		}

		int index = 0;
		for (String key : keysList) {
			String keyLabel = getDirectoryEntryLocalizedLabel(directories[index++], key, locale);
			if (StringUtils.isNotBlank(keyLabel)) {
				list.add(keyLabel);
			}
		}
				
		return list;
	}
	
	private static String translate(String label, Locale locale) {
		String localizedLabel = label;
		if (null != locale) {
			label = I18NUtils.getMessageString(ToutaticeUtilsConst.CST_DEFAULT_BUNDLE_NAME, label, null, locale);
		}
        return localizedLabel;
		
	}
	
	/**
	 * Initialize the service attribute
	 * 
	 * @throws ToutaticeException if failed to obtain an instance of the directory service
	 */
	private DirectoryService getService() throws ToutaticeException {
		try {
			if (null == this.service) {
				this.service = (DirectoryService) Framework.getService(DirectoryService.class);
			}
		} catch (Exception e) {
			log.error("Failed to get the directory service, exception message: " + e.getMessage());
			throw new ToutaticeException(e);
		}
		
		return this.service;
	}

}