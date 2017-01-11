/**
 * 
 */
package org.opentoutatice.ecm.scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentoutatice.ecm.scanner.directive.Directive;



/**
 * @author david
 *
 */
public class ScannerImpl implements Scanner {
    
    /** Logger. */
    private static final Log log = LogFactory.getLog(ScannerImpl.class);
    
    /** Updater of scanned objects. */
    private AbstractScanUpdater scanUpdater;
    
    /**
     * 
     * @param scanUpdater
     */
    public ScannerImpl(AbstractScanUpdater scanUpdater) {
        this.scanUpdater = scanUpdater;
    }
    
//    /** Data output structure type. */
//    private Object dataStructure;

//    /**
//     * Constructor.
//     * 
//     * @throws IllegalAccessException 
//     * @throws InstantiationException 
//     */
//    public AbstractScanner(Class<?> dataStructureType) throws InstantiationException, IllegalAccessException {
//        super();
//        this.dataStructure = dataStructureType.newInstance();
//    }
//    
//    /**
//     * @return the dataStructure
//     */
//    public Object getDataStructure() {
//        return dataStructure;
//    }


    /**
    * {@inheritDoc}
    */
    @Override
    public Iterable<?> scan(Directive directive) throws Exception {
        
        // Execute directive
        return directive.execute();
        

//        IterableQueryResult rows = null;
//        try {
//            // Get objects
//            rows = (IterableQueryResult) directive.execute();
//
//            if (rows != null) {
//                Iterator<Map<String, Serializable>> iterator = rows.iterator();
//
//                while (iterator.hasNext()) {
//                    Map<String, Serializable> row = iterator.next();
//                    
//                    if(log.isDebugEnabled()){
//                        log.debug("Row result: " + row.toString());
//                    }
//                    
//                    ScannedObject scannedObject = transform(row);
//                    scannedObjects.add(scannedObject);
//
//                    // TODO: Build report
//
//                    // Update
//                    update(scannedObject);
//
//                    if (log.isDebugEnabled()) {
//                        log.debug("SCANNED: " + scannedObject.size() + "objects");
//                    }
//                }
//
//            }
//        } finally {
//            if (rows != null) {
//                rows.close();
//            }
//        }

    }
   
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractScanUpdater getUpdater() {
        return this.scanUpdater;
    }
    
}
