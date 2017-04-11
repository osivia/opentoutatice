package fr.toutatice.ecm.platform.core.convert;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.cache.SimpleCachableBlobHolder;
import org.nuxeo.ecm.core.convert.extension.ConverterDescriptor;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.convert.plugins.CommandLineBasedConverter;


public class LibreOfficeCommandLineConverter extends CommandLineBasedConverter {

    private static final String POOL_SIZE_PARAMETER = "EnvironmentPoolSize";

    private static int poolSize;

    private static AtomicInteger instanceCounter;

    @Override
    protected Map<String, Blob> getCmdBlobParameters(BlobHolder blobHolder, Map<String, Serializable> parameters) throws ConversionException {
        Map<String, Blob> cmdBlobParams = new HashMap<String, Blob>();
        try {
            cmdBlobParams.put("inFilePath", blobHolder.getBlob());
        } catch (ClientException e) {
            throw new ConversionException("Unable to get Blob for holder", e);
        }
        return cmdBlobParams;
    }

    @Override
    protected Map<String, String> getCmdStringParameters(BlobHolder blobHolder, Map<String, Serializable> parameters) throws ConversionException {
        Map<String, String> cmdStringParams = new HashMap<String, String>();

        // tmp working directory
        String baseDir = getTmpDirectory(parameters);
        Path tmpPath = new Path(baseDir).append("soffice_" + System.currentTimeMillis());
        File outDir = new File(tmpPath.toString());
        if (!outDir.mkdir()) {
            throw new ConversionException("Unable to create tmp dir for transformer output");
        }
        cmdStringParams.put("outDirPath", outDir.getAbsolutePath());

        // tmp soffice user directory to manage multiple instances
        if(instanceCounter.get() < poolSize){
            int envIndex = instanceCounter.incrementAndGet();
            Path envPath = new Path(baseDir).append("sofficeUserEnv_" + String.valueOf(envIndex));
            File envDir = new File(envPath.toString());
            if (!envDir.isDirectory() && !envDir.mkdir()) {
                throw new ConversionException("Unable to create tmp soffice user directory for transformer output");
            }
            cmdStringParams.put("envDirPath", envDir.getAbsolutePath());
        }else{
            throw new ConversionException("LibreOffice user environment pool is full");
        }
        
        return cmdStringParams;
    }

    @Override
    protected BlobHolder buildResult(List<String> cmdOutput, CmdParameters cmdParams) throws ConversionException {
        String outputPath = cmdParams.getParameters().get("outDirPath");
        File outputDir = new File(outputPath);
        File[] files = outputDir.listFiles();
        List<Blob> blobs = new ArrayList<Blob>();

        for (File file : files) {
            Blob blob = new FileBlob(file);
            blob.setFilename(file.getName());

            if (file.getName().equalsIgnoreCase("index.html")) {
                blobs.add(0, blob);
            } else {
                blobs.add(blob);
            }
        }

        instanceCounter.decrementAndGet();

        return new SimpleCachableBlobHolder(blobs);
    }

    @Override
    public void init(ConverterDescriptor descriptor) {
        initParameters = descriptor.getParameters();
        if (initParameters == null) {
            initParameters = new HashMap<>();
        } else {
            poolSize = Integer.valueOf(initParameters.get(POOL_SIZE_PARAMETER));
        }
        instanceCounter = new AtomicInteger();
        getCommandLineService();
    }

}
