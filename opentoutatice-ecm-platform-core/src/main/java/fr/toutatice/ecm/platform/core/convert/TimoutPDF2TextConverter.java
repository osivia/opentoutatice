/**
 * 
 */
package fr.toutatice.ecm.platform.core.convert;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.extension.Converter;
import org.nuxeo.ecm.core.convert.extension.ConverterDescriptor;
import org.nuxeo.ecm.core.convert.plugins.text.extractors.PDF2TextConverter;

/**
 * pdf2text converter with timeout (prevent infinite loops during extraction)
 * 
 * @author Loïc Billon
 *
 */
public class TimoutPDF2TextConverter implements Converter {

	/** specific Logger */ 
    private static final Log sofficelog = LogFactory.getLog("soffice");

	
	/* (non-Javadoc)
	 * @see org.nuxeo.ecm.core.convert.plugins.text.extractors.PDF2TextConverter#convert(org.nuxeo.ecm.core.api.blobholder.BlobHolder, java.util.Map)
	 */
	@Override
	public BlobHolder convert(BlobHolder blobHolder, Map<String, Serializable> parameters) throws ConversionException {
		
		Date begin = new Date(); 
		
		if(sofficelog.isDebugEnabled()) {
			
			sofficelog.debug("[pdf2text] Start extraction of : "+blobHolder.getFilePath());
		}
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<BlobHolder> future = executor.submit(new ConverterTask(blobHolder, parameters));
		BlobHolder ret = null;
		
		try {
			ret = future.get(30, TimeUnit.SECONDS);
		}
		catch(InterruptedException | ExecutionException | TimeoutException e) {
					
			if(sofficelog.isDebugEnabled()) {
				Date end = new Date(); 
				sofficelog.debug("[pdf2text] Error during extraction of : "+blobHolder.getFilePath()+ ",  "+ (end.getTime() - begin.getTime()) +"ms elapsed."+" "+e.getMessage());

			}
			
			future.cancel(true);
			throw new ConversionException("", e);
		}
		
		if(sofficelog.isDebugEnabled()) {
			Date end = new Date(); 
			sofficelog.debug("[pdf2text] End extraction of : "+blobHolder.getFilePath()+ ", "+(end.getTime() - begin.getTime())+"ms elapsed.");
			
		}
		
		return ret;
	}

	/**
	 * Timed thread wrapping the default PDF2TextConverter
	 *
	 */
	class ConverterTask extends PDF2TextConverter implements Callable<BlobHolder> {

		private BlobHolder blobHolder;
		private Map<String, Serializable> parameters;
		
		public ConverterTask(BlobHolder blobHolder, Map<String, Serializable> parameters) {
			this.blobHolder = blobHolder;
			this.parameters = parameters;
		}
		
		/* (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public BlobHolder call() throws Exception {
			
			return super.convert(blobHolder, parameters);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.nuxeo.ecm.core.convert.extension.Converter#init(org.nuxeo.ecm.core.convert.extension.ConverterDescriptor)
	 */
	@Override
	public void init(ConverterDescriptor descriptor) {

	}
	
}
