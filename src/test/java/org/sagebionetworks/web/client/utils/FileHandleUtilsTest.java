package org.sagebionetworks.web.client.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.sagebionetworks.repo.model.file.CloudProviderFileHandleInterface;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;

public class FileHandleUtilsTest {

	@Test
	public void isPreview() {
		CloudProviderFileHandleInterface fileHandle = new S3FileHandle();
		fileHandle.setIsPreview(true);
		assertTrue(FileHandleUtils.isPreviewFileHandle(fileHandle));
	}

	@Test
	public void isNotPreview() {
		CloudProviderFileHandleInterface fileHandle = new S3FileHandle();
		fileHandle.setIsPreview(false);
		assertFalse(FileHandleUtils.isPreviewFileHandle(fileHandle));
	}

	@Test
	public void isPreview_nonCloudProviderFileHandle() {
		// Any class that does not extend CloudProviderFileHandle is not a preview
		FileHandle fileHandle = new ExternalFileHandle();
		assertFalse(FileHandleUtils.isPreviewFileHandle(fileHandle));
	}
}
