package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileTitleBarTest {
		
	FileTitleBar fileTitleBar;
	FileTitleBarView mockView;
	AuthenticationController mockAuthController;
	SynapseClientAsync mockSynapseClient;
	EntityBundle mockBundle;
	org.sagebionetworks.repo.model.Entity mockEntity;
	@Before
	public void setup(){	
		mockView = mock(FileTitleBarView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		fileTitleBar = new FileTitleBar(mockView, mockAuthController, mockSynapseClient);
		mockBundle = mock(EntityBundle.class);
		mockEntity = mock(org.sagebionetworks.repo.model.Entity.class);
		Mockito.when(mockEntity.getId()).thenReturn("syn123");
		Mockito.when(mockEntity.getName()).thenReturn("syn123");
		Mockito.when(mockBundle.getEntity()).thenReturn(mockEntity);
		verify(mockView).setPresenter(fileTitleBar);
	}
	
	@Test
	public void testAsWidget(){
		fileTitleBar.asWidget();
	}
	
	@Test
	public void testSetS3DescriptionForExternalS3() {
		List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
		uploadDestinations.add(new ExternalS3UploadDestination());
		AsyncMockStubber.callSuccessWith(uploadDestinations).when(mockSynapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		fileTitleBar.setEntityBundle(mockBundle);
		fileTitleBar.setS3Description();
		verify(mockView).setFileLocation(anyString());
	}

	@Test
	public void testSetS3DescriptionForSynapseStorage() {
		List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
		uploadDestinations.add(new S3UploadDestination());
		AsyncMockStubber.callSuccessWith(uploadDestinations).when(mockSynapseClient).getUploadDestinations(anyString(), any(AsyncCallback.class));
		fileTitleBar.setEntityBundle(mockBundle);
		fileTitleBar.setS3Description();
		verify(mockView).setFileLocation(Mockito.eq("| Synapse Storage"));
	}
}
