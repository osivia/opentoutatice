package fr.toutatice.ecm.platform.automation.transaction.filter;

import java.util.HashSet;
import java.util.Set;

import org.nuxeo.ecm.automation.jaxrs.io.operations.MultiPartFormRequestReader;
import org.nuxeo.ecm.automation.jaxrs.io.operations.MultiPartRequestReader;
import org.nuxeo.ecm.webengine.app.WebEngineModule;

public class APIModuleBis extends WebEngineModule {

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

        // result.add(new JsonAutomationInfoWriter());
        // result.add(new JsonDocumentWriter());
        // result.add(new JsonDocumentListWriter());
        // result.add(new JsonESDocumentWriter());
        // result.add(new JsonESDocumentListWriter());
        // result.add(new BlobsWriter());
        // result.add(new JsonLoginInfoWriter());
        // result.add(new JsonOperationWriter());
        // result.add(new UrlEncodedFormRequestReader());
        // result.add(new JsonTreeWriter());
        // result.add(new JsonAdapterWriter());
        // result.add(new JsonRecordSetWriter());
        // result.add(new BusinessAdapterReader());
        // result.add(new DocumentModelJsonReader());
        // result.add(new NuxeoPrincipalWriter());
        // result.add(new NuxeoPrincipalReader());
        // result.add(new NuxeoGroupReader());
        // result.add(new NuxeoGroupWriter());
        // result.add(new NuxeoGroupListWriter());
        // result.add(new NuxeoPrincipalListWriter());
        // result.add(new ACPWriter());
        // result.add(new DirectoryEntriesWriter());
        // result.add(new DirectoryEntryReader());
        // result.add(new DirectoryEntryWriter());
        // result.add(new LogEntryListWriter());
        // result.add(new LogEntryWriter());
        // result.add(new BusinessAdapterListWriter());
        // result.add(new SchemasWriter());
        // result.add(new SchemaWriter());
        // result.add(new DocumentTypeWriter());
        // result.add(new DocumentTypesWriter());
        // result.add(new FacetWriter());
        // result.add(new FacetsWriter());

        return result;
    }

}
