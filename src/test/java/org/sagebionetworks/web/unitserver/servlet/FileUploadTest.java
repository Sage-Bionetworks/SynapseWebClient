package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemStream;
import org.hamcrest.BaseMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.server.servlet.FileUpload;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.unitserver.TestUtils;

import com.sun.istack.logging.Logger;

/**
 * This is a unit test of the UserAccountServiceImpl service.
 * It depends on a local stub implementation of the platform API
 * to be deployed locally.
 * 
 * @author dburdick
 *
 */
public class FileUploadTest {
	
	public static Logger logger = Logger.getLogger(FileUploadTest.class);
	final static String UPLOAD_CONTENTS = "upload content";
	
	HttpServletRequest mockRequest;
	HttpServletResponse mockResponse;
	ServiceUrlProvider mockUrlProvider;
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	PresignedUrl mockUrl;
	Synapse mockSynapse;
	ServletOutputStream responseOutputStream;
	FileUpload servlet;
	boolean hasNext = true;
	Data entity = new Data();

	@Before
	public void setup() throws Exception {
		servlet = new FileUpload();

		// Mock synapse and provider so we don't need to worry about
		// unintentionally testing those classes
		mockSynapse = mock(Synapse.class);
		mockSynapseProvider = mock(SynapseProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);

		mockUrlProvider = mock(ServiceUrlProvider.class);
		mockTokenProvider = mock(TokenProvider.class);
		when(mockTokenProvider.getSessionToken()).thenReturn("sessionToken");

		servlet.setServiceUrlProvider(mockUrlProvider);
		servlet.setSynapseProvider(mockSynapseProvider);
		servlet.setTokenProvider(mockTokenProvider);

		// Setup output stream and response
		responseOutputStream = mock(ServletOutputStream.class);
		mockResponse = mock(HttpServletResponse.class);
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);

		// Setup request
		mockRequest = mock(HttpServletRequest.class);
		String entityId = "syn1234";
		ServletInputStream inputStream = mock(ServletInputStream.class);		
		when(mockRequest.getContentType()).thenReturn("multipart/form-data; boundary=AaB03x");
		when(mockRequest.getInputStream()).thenReturn(inputStream);
		when(mockRequest.getContentLength()).thenReturn(1);
		when(mockRequest.getCharacterEncoding()).thenReturn("UTF-8");
		when(mockRequest.getParameter(DisplayUtils.ENTITY_PARAM_KEY)).thenReturn(entityId);
		hasNext = true;
		
		// setup response
		responseOutputStream = mock(ServletOutputStream.class);
		when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);
		
		// setup Synapse client		
		assertTrue(entity instanceof Locationable);		
		entity.setId(entityId);
		when(mockSynapse.getEntityById(entityId)).thenReturn(entity);
		
		// setup uploaded file
//		mockFileItemIterator = mock(FileItemIterator.class);
		// answer true once
//		when(mockFileItemIterator.hasNext()).thenAnswer(new Answer<Boolean>() {
//			@Override
//			public Boolean answer(InvocationOnMock invocation) throws Throwable {
//				boolean ret = hasNext;
//				hasNext = false;
//				return ret;
//			}
//		});
		
		FileItemStream mockStream = mock(FileItemStream.class);
		when(mockStream.openStream()).thenReturn(new ByteArrayInputStream(new byte[1024]));
		//when(mockFileItemIterator.next()).thenReturn(mockStream);		
		when(mockSynapse.uploadLocationableToSynapse(eq(entity), any(File.class))).thenReturn(entity);

		// setup fileupload
		servlet.setServiceUrlProvider(new ServiceUrlProvider());
		servlet.setSynapseProvider(mockSynapseProvider);
		
	}
	
	@Ignore
	@Test(expected=RuntimeException.class)
	public void testDoPostNoEntityIdParam() throws Exception {		
		// set parameter to null
		when(mockRequest.getParameter(DisplayUtils.ENTITY_PARAM_KEY)).thenReturn(null);
				
		servlet.doPost(mockRequest, mockResponse);
	}
	
	@Ignore
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testDoPost() throws Exception {				
		servlet.doPost(mockRequest, mockResponse);
		
		// verify success response
		BaseMatcher matcher = TestUtils.createByteArrayPrefixMatcher(DisplayUtils.UPLOAD_SUCCESS.getBytes());
		verify(responseOutputStream).write((byte[]) argThat(matcher), eq(0), eq(DisplayUtils.UPLOAD_SUCCESS.getBytes().length));		
	}
	
}
