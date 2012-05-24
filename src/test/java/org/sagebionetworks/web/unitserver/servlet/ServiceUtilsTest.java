package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.ServiceUtils;
import org.sagebionetworks.web.server.servlet.TokenProvider;

import com.sun.istack.logging.Logger;

/**
 * This is a unit test of the UserAccountServiceImpl service. It depends on a
 * local stub implementation of the platform API to be deployed locally.
 * 
 * @author dburdick
 * 
 */
public class ServiceUtilsTest {

	public static Logger logger = Logger.getLogger(ServiceUtilsTest.class);

	@Test
	public void testCreateSynapseClient() {
		final String SESSION_TOKEN = "token";

		TokenProvider mockTokenProvider = mock(TokenProvider.class);
		Mockito.when(mockTokenProvider.getSessionToken()).thenReturn(
				SESSION_TOKEN);
		ServiceUrlProvider serviceUrlProvider = new ServiceUrlProvider();

		Synapse client = ServiceUtils.createSynapseClient(mockTokenProvider,
				serviceUrlProvider);

		Mockito.verify(mockTokenProvider).getSessionToken();
		assertEquals(SESSION_TOKEN, client.getCurrentSessionToken());
		assertEquals(serviceUrlProvider.getRepositoryServiceUrl(),
				client.getRepoEndpoint());
		assertEquals(serviceUrlProvider.getPublicAuthBaseUrl(),
				client.getAuthEndpoint());
	}

	@Test
	public void testWriteToFile() throws Exception {
		File temp = null;
		try {
			temp = File.createTempFile("temporaryFile", ".tmp");
			String fileContentsString = "File Contents";
			InputStream stream = new ByteArrayInputStream(
					fileContentsString.getBytes("UTF-8"));
			long maxAttachmentSizeBytes = 1024 * 100; // 100k

			ServiceUtils.writeToFile(temp, stream, maxAttachmentSizeBytes);

			validateFileContents(temp, fileContentsString);
		} finally {
			if (temp != null) {
				temp.delete();
			}
		}
				
	}
	
	@Test(expected=RuntimeException.class)
	public void testWriteToFileTooBig() throws Exception {
		File temp = null;
		// test too large of file
		try {
			temp = File.createTempFile("temporaryFile", ".tmp");
			String fileContentsString = "File Contents";
			InputStream stream = new ByteArrayInputStream(new byte[] {9, 9, 9});
			long maxAttachmentSizeBytes = 2; 

			ServiceUtils.writeToFile(temp, stream, maxAttachmentSizeBytes);
			assertTrue(false); // assure error if we get here
		} finally {
			if (temp != null) {
				temp.delete();
			}
		}
	}

	@Test
	public void testWriteToTempFile() throws Exception {
		File temp = null;
		try {
			String fileContentsString = "File Contents";
			InputStream stream = new ByteArrayInputStream(
					fileContentsString.getBytes("UTF-8"));
			long maxAttachmentSizeBytes = 1024 * 100; // 100k

			temp = ServiceUtils.writeToTempFile(stream, maxAttachmentSizeBytes);
			validateFileContents(temp, fileContentsString);
		} finally {
			if (temp != null) {
				temp.delete();
			}
		}

	}

	/*
	 * Private Methods
	 */
	private void validateFileContents(File temp, String fileContentsString)
			throws FileNotFoundException, IOException {
		String readFromFile = "";
		FileInputStream fis = new FileInputStream(temp);
		DataInputStream in = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			readFromFile += strLine;
		}
		in.close();

		assertEquals(fileContentsString, readFromFile);
	}

}
