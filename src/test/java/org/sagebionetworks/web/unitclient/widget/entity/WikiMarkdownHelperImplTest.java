package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownHelperImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;

public class WikiMarkdownHelperImplTest {
	WikiMarkdownHelperImpl helperImpl;
	AmazonS3Client mockS3Client;
	SynapseClientImpl synapseClient;
	
	FileHandle handle;
	
	@Before
	public void before() {
		mockS3Client = Mockito.mock(AmazonS3Client.class);
		synapseClient = new SynapseClientImpl();
		helperImpl = new WikiMarkdownHelperImpl(mockS3Client, synapseClient);
		
		handle = new org.sagebionetworks.repo.model.file.S3FileHandle();
	}
	
	@Test
	public void test() throws IOException {
		String markdown = "This is markdown.";
		String id = "syn123";
		
		// Zip up content
		File zippedUpMarkdown = helperImpl.zipUp(markdown, id);
		String unzippedMarkdown = FileUtils.readFileToString(zippedUpMarkdown, "UTF-8");
		// Compare original to the content unpacked from the zipped file
		assertEquals(markdown, unzippedMarkdown);
		
		S3FileHandle s3Handle = (S3FileHandle) handle;
		String result = helperImpl.getAndReadContent(s3Handle, id);
		// Make sure the s3 client calls on getObject to populate the file
		verify(mockS3Client).getObject(any(GetObjectRequest.class), any(File.class));
	}
}
