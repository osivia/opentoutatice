package fr.toutatice.ecm.platform.automation.transaction.filter;

import java.util.HashSet;
import java.util.Set;

import org.nuxeo.ecm.automation.jaxrs.io.JsonAdapterWriter;
import org.nuxeo.ecm.automation.jaxrs.io.JsonLoginInfoWriter;
import org.nuxeo.ecm.automation.jaxrs.io.JsonRecordSetWriter;
import org.nuxeo.ecm.automation.jaxrs.io.JsonTreeWriter;
import org.nuxeo.ecm.automation.jaxrs.io.audit.LogEntryListWriter;
import org.nuxeo.ecm.automation.jaxrs.io.audit.LogEntryWriter;
import org.nuxeo.ecm.automation.jaxrs.io.documents.BlobsWriter;
import org.nuxeo.ecm.automation.jaxrs.io.documents.BusinessAdapterListWriter;
import org.nuxeo.ecm.automation.jaxrs.io.documents.BusinessAdapterReader;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JSONDocumentModelReader;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonDocumentListWriter;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonDocumentWriter;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonESDocumentListWriter;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonESDocumentWriter;
import org.nuxeo.ecm.automation.jaxrs.io.operations.JsonAutomationInfoWriter;
import org.nuxeo.ecm.automation.jaxrs.io.operations.JsonOperationWriter;
import org.nuxeo.ecm.automation.jaxrs.io.operations.MultiPartFormRequestReader;
import org.nuxeo.ecm.automation.jaxrs.io.operations.MultiPartRequestReader;
import org.nuxeo.ecm.automation.jaxrs.io.operations.UrlEncodedFormRequestReader;
import org.nuxeo.ecm.restapi.jaxrs.io.directory.DirectoryEntriesWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.directory.DirectoryEntryReader;
import org.nuxeo.ecm.restapi.jaxrs.io.directory.DirectoryEntryWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.documents.ACPWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.types.DocumentTypeWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.types.DocumentTypesWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.types.FacetWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.types.FacetsWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.types.SchemaWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.types.SchemasWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.usermanager.NuxeoGroupListWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.usermanager.NuxeoGroupReader;
import org.nuxeo.ecm.restapi.jaxrs.io.usermanager.NuxeoGroupWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.usermanager.NuxeoPrincipalListWriter;
import org.nuxeo.ecm.restapi.jaxrs.io.usermanager.NuxeoPrincipalReader;
import org.nuxeo.ecm.restapi.jaxrs.io.usermanager.NuxeoPrincipalWriter;
import org.nuxeo.ecm.webengine.app.WebEngineModule;

import fr.toutatice.ecm.platform.automation.transaction.io.TransactionalJsonRequestReader;

public class APIModuleBis extends WebEngineModule{

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> result = super.getClasses();
        // need to be stateless since it needs the request member to be
        // injected
        result.add(MultiPartRequestReader.class);
        result.add(MultiPartFormRequestReader.class);
        return result;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> result = new HashSet<Object>();

        result.add(new TransactionalJsonRequestReader());
        result.add(new JsonAutomationInfoWriter());
        result.add(new JsonDocumentWriter());
        result.add(new JsonDocumentListWriter());
        result.add(new JsonESDocumentWriter());
        result.add(new JsonESDocumentListWriter());
        result.add(new BlobsWriter());
        result.add(new JsonLoginInfoWriter());
        result.add(new JsonOperationWriter());
        result.add(new UrlEncodedFormRequestReader());
        result.add(new JsonTreeWriter());
        result.add(new JsonAdapterWriter());
        result.add(new JsonRecordSetWriter());
        result.add(new BusinessAdapterReader());
        result.add(new JSONDocumentModelReader());
        result.add(new NuxeoPrincipalWriter());
        result.add(new NuxeoPrincipalReader());
        result.add(new NuxeoGroupReader());
        result.add(new NuxeoGroupWriter());
        result.add(new NuxeoGroupListWriter());
        result.add(new NuxeoPrincipalListWriter());
        result.add(new ACPWriter());
        result.add(new DirectoryEntriesWriter());
        result.add(new DirectoryEntryReader());
        result.add(new DirectoryEntryWriter());
        result.add(new LogEntryListWriter());
        result.add(new LogEntryWriter());
        result.add(new BusinessAdapterListWriter());
        result.add(new SchemasWriter());
        result.add(new SchemaWriter());
        result.add(new DocumentTypeWriter());
        result.add(new DocumentTypesWriter());
        result.add(new FacetWriter());
        result.add(new FacetsWriter());

        return result;
}
    
}
