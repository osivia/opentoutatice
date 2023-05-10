package fr.toutatice.ecm.platform.automation.document;

import java.io.File;
import java.io.InputStream;

import java.util.Enumeration;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.platform.filemanager.utils.FileManagerUtils;

@Operation(id = ImportZipDocument.ID, category = Constants.CAT_SERVICES, label = "Create a Document hiearchy from a zipfile", description = "Create a Document hiearchy from a zipfile")
public class ImportZipDocument {

    public static final String ID = "FileManager.ImportZip";
    
    @Context
    protected AutomationService as;

    @Context
    protected OperationContext context;

    @Context
    protected FileManager fileManager;
    
    protected DocumentModel getCurrentDocument() throws Exception {
        String cdRef = (String) context.get("currentDocument");
        return as.getAdaptedValue(context, cdRef, DocumentModel.class);
    }
    
    @OperationMethod
    public void run(Blob blob) throws Exception {
        DocumentModel currentDocument = getCurrentDocument();
        
        int indexOfIgnoreCase = StringUtils.indexOfIgnoreCase(blob.getFilename(), ".zip");
        String zipRootFolderName = blob.getFilename().substring(0, indexOfIgnoreCase);
        
        DocumentModel zipRootFolder = fileManager.createFolder(context.getCoreSession(), zipRootFolderName, currentDocument.getPathAsString(), true);
        
        
        File tmp = null;
        try {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(blob.getStream());
            CharsetMatch charsetMatch = detector.detect();

        	tmp = File.createTempFile("importer", null);
        	blob.transferTo(tmp);

        	ZipFile zipFile = new ZipFile(tmp, charsetMatch.getName());
        	
            Enumeration<?> enu = zipFile.getEntries();
            while (enu.hasMoreElements()) {
                ZipArchiveEntry zipEntry = (ZipArchiveEntry) enu.nextElement();

                String name = zipEntry.getName();
                DocumentModel documentFolder = zipRootFolder;
                String entryFileName = name;
                if(name.contains("/") ) {
                    entryFileName = StringUtils.substringAfterLast(name, "/");
                    String entryDirName = StringUtils.substringBeforeLast(name, "/");
                    
                    if(StringUtils.isNotBlank(entryDirName)) {
                    	
                    	for(String dir : entryDirName.split("/")) {
                    		DocumentModel existingDocByTitle = FileManagerUtils.getExistingDocByTitle(context.getCoreSession(), documentFolder.getPathAsString(), dir);
                    		
                    		if(existingDocByTitle == null) {
                    			documentFolder = fileManager.createFolder(context.getCoreSession(), dir, documentFolder.getPathAsString(), true);
                    		}
                    		else {
                    			documentFolder = existingDocByTitle;
                    		}
                    	}
                    	
                    }
                }

                InputStream inputStream = zipFile.getInputStream(zipEntry);
                
                Blob input = new InputStreamBlob(inputStream);
                
                fileManager.createDocumentFromBlob(context.getCoreSession(), input, documentFolder.getPathAsString(), true, entryFileName);

            }
            zipFile.close();        	
        	
        }
        finally {
        	tmp.delete();
        }

    }
    
}
