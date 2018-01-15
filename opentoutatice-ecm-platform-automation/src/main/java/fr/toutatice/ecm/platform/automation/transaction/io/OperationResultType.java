/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction.io;

import org.nuxeo.ecm.automation.jaxrs.io.documents.MultipartBlobs;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;


/**
 * @author david
 *
 */
public enum OperationResultType {

    document(DocumentModelImpl.class), documents(DocumentModelListImpl.class), blob(Blob.class), blobs(MultipartBlobs.class);

    private Class<?> type;

    private OperationResultType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getValue() {
        return this.type;
    }

    public static OperationResultType getType(Object input) {
        OperationResultType result = null;

        Class<? extends Object> inputType = input.getClass();

        for (OperationResultType type : OperationResultType.values()) {
            if (type.getValue().equals(inputType)) {
                result = type;
                break;
            }
        }

        return result;
    }

}
