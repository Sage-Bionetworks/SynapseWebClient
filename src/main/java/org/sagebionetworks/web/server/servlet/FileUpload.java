package org.sagebionetworks.web.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.common.io.Files;
import com.google.inject.Inject;

public class FileUpload extends HttpServlet {

	private static Logger logger = Logger.getLogger(FileUpload.class.getName());
	private static final long serialVersionUID = 1L;	

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
	
	private FileItemIterator fileItemIterator;
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	
	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private ServiceUrlProvider urlProvider;
		
	/**
	 * Essentially the constructor. Setup synapse client.
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider){
		this.urlProvider = provider;
	}
	
	/**
	 * Used for testing or if you want to specify what the FileItems are to upload outside of the requests
	 * @param fileItemIterator
	 */
	public void setFileItemIterator(FileItemIterator fileItemIterator) {
		this.fileItemIterator = fileItemIterator;
	}
	
	/**
	 * Unit test can override this.
	 * 
	 * @param provider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}
	
    public void doPost(final HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
    	ServletFileUpload upload = new ServletFileUpload();
        
        try{ 
            if(fileItemIterator == null) fileItemIterator = upload.getItemIterator(request);

            while (fileItemIterator.hasNext()) {
                FileItemStream item = fileItemIterator.next();

                String name = item.getFieldName();
                InputStream stream = item.openStream();


                // Process the input stream
                File tempDir = Files.createTempDir();
				File file = new File(tempDir.getAbsolutePath() + File.separator + item.getName());
				
				if(!file.createNewFile()) {
					throw new IOException("Unable to create server temporary file for upload: " + file.getAbsolutePath());
				}				
				
				ServiceUtils.writeToFile(file, stream, Long.MAX_VALUE); // TODO : check file size and restrict to a limit?      
				
				try{	                
	                TokenProvider tokenProvider = new TokenProvider() {					
						@Override
						public String getSessionToken() {
							return UserDataProvider.getThreadLocalUserToken(request);
						}
					};
	                
					// get Entity and store file in location
					String entityId = request.getParameter(DisplayUtils.ENTITY_PARAM_KEY);
					if(entityId != null) {					
						String makeAttachment = request.getParameter(DisplayUtils.MAKE_ATTACHMENT_PARAM_KEY);
						Synapse synapseClient = ServiceUtils.createSynapseClient(synapseProvider, urlProvider, tokenProvider.getSessionToken());						
						Entity locationable = synapseClient.getEntityById(entityId);
						if("true".equals(makeAttachment)) {
							// TODO : create Attachment entity, and set locationable
							// locationable = new attachment entity
						}
						if(!(locationable instanceof Locationable)) {
							throw new RuntimeException("Upload failed. Entity id: " + locationable.getId() + " is not Locationable.");
						}						
						Locationable uploaded = synapseClient.uploadLocationableToSynapse((Locationable)locationable, file);
						logger.info("Uploaded file " + item.getName() + " ("+ file.getName() +") to Synapse id: " + uploaded.getId());
						OutputStream os = response.getOutputStream();						
						PrintStream printStream = new PrintStream(os);
						printStream.print(DisplayUtils.UPLOAD_SUCCESS);
						printStream.close();
					} else {
						throw new IllegalArgumentException("entityId is a required parameter");
					}					
				}finally{
					// Unconditionally delete the temp file.
					file.delete();
				}
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

    }
}
