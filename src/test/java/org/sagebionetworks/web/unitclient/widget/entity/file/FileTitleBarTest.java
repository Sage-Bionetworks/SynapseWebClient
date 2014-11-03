package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileTitleBarTest {
		
	FileTitleBar fileTitleBar;
	FileTitleBarView mockView;
	AuthenticationController mockAuthController;
	EntityTypeProvider mockEntityTypeProvider;
	SynapseClientAsync mockSynapseClient;
	EntityEditor mockEntityEditor;
	@Before
	public void setup(){	
		mockView = mock(FileTitleBarView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockEntityEditor = mock(EntityEditor.class);
		fileTitleBar = new FileTitleBar(mockView, mockAuthController, mockEntityTypeProvider, mockSynapseClient, mockEntityEditor);
		
		verify(mockView).setPresenter(fileTitleBar);
	}
	
	@Test
	public void testAsWidget(){
		fileTitleBar.asWidget();
	}
	
	@Test
	public void testIsDataNotInFile() {
		FileEntity fileEntity = new FileEntity();
		fileEntity.setDataFileHandleId(null);
		Assert.assertFalse(FileTitleBar.isDataPossiblyWithin(fileEntity));
	}
	
	@Test
	public void testIsDataInFile() {
		FileEntity fileEntity = new FileEntity();
		fileEntity.setDataFileHandleId("123");
		Assert.assertTrue(FileTitleBar.isDataPossiblyWithin(fileEntity));
	}

	@Test
	public void testGetSftpDomain() {
		assertEquals("tcgaftps.nci.nih.gov", FileTitleBar.getSftpDomain("sftp%3A%2F%2Ftcgaftps.nci.nih.gov%3A22%2Ftcgapancantestdir%2Fsynapse%2Fjays-project-settings-test%2F58965f00-6714-4f8e-9f15-6da302259e13%2Ftesting-only.txt"));
		assertEquals("tcgaftps.nci.nih.gov", FileTitleBar.getSftpDomain("sftp%3A%2F%2Ftcgaftps.nci.nih.gov%2Ftcgapancantestdir%2Fsynapse%2Fjays-project-settings-test%2F58965f00-6714-4f8e-9f15-6da302259e13%2Ftesting-only.txt"));
		assertEquals("tcgaftps.nci.nih.gov", FileTitleBar.getSftpDomain("sftp%3A%2F%2Ftcgaftps.nci.nih.gov"));
		
		assertNull(FileTitleBar.getSftpDomain(null));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidSftpUrl() {
		FileTitleBar.getSftpDomain("http://notsftp.com/bar");
	}
	
	@Test
	public void testGetEncodedSftpUrl() {
		String sftpProxy = "https://sftp.synapse.org/sftp";
		String encodedSftpUrl = "sftp%3A%2F%2Ftcgaftps.nci.nih.gov%3A22%2Ftcgapancantestdir%2Fsynapse%2Fjays-project-settings-test%2F58965f00-6714-4f8e-9f15-6da302259e13%2Ftesting-only.txt";
		String proxiedSftpLink = "https://sftp.synapse.org/sftp?url=" + encodedSftpUrl;
		assertEquals(encodedSftpUrl, FileTitleBar.getEncodedSftpUrl(sftpProxy, proxiedSftpLink));
		assertNull(FileTitleBar.getEncodedSftpUrl(null, proxiedSftpLink));
		assertNull(FileTitleBar.getEncodedSftpUrl(sftpProxy, null));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testInvalidEncodedSftpUrl() {
		String sftpProxy = "https://sftp.synapse.org/sftp";
		String encodedSftpUrl = "invalidUrl";
		String proxiedSftpLink = "https://sftp.synapse.org/sftp?url=" + encodedSftpUrl;
		FileTitleBar.getEncodedSftpUrl(sftpProxy, proxiedSftpLink);
	}
	
}
