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
	
	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private ServiceUrlProvider urlProvider;
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	private TokenProvider tokenProvider = new TokenProvider() {
		@Override
		public String getSessionToken() {
			return UserDataProvider.getThreadLocalUserToken(FileAttachmentServlet.perThreadRequest.get());
		}
	};

	/**
	 * Unit test can override this.
	 *
	 * @param provider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	/**
	 * Essentially the constructor. Setup synapse client.
	 *
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider) {
		this.urlProvider = provider;
	}

	/**
	 * Unit test uses this to provide a mock token provider
	 *
	 * @param tokenProvider
	 */
	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}
		
	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		FileUpload.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}
	
	@Override
    public void doPost(final HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
    	ServletFileUpload upload = new ServletFileUpload();
        
        try{ 
			FileItemIterator fileItemIterator = upload.getItemIterator(request);

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
