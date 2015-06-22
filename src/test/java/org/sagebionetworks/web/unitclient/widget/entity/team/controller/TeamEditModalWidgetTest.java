package org.sagebionetworks.web.unitclient.widget.entity.team.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidgetView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.FileValidator;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeamEditModalWidgetTest {

	TeamEditModalWidget presenter;
	SynapseAlert mockSynAlert;
	TeamEditModalWidgetView mockView;
	SynapseJSNIUtils mockJSNIUtils;
	SynapseClientAsync mockSynapseClient;
	FileHandleUploadWidget mockUploader;
	AuthenticationController mockAuthenticationController;
	Team mockTeam;
	Callback mockRefreshCallback;
	FileUpload mockFileUpload;
	FileMetadata mockFileMeta;
	
	Callback startedUploadingCallback;
	CallbackP<FileUpload> finishedUploadingCallback;
	String oldName = "oldName";
	String newName = "newName";
	String oldDesc = "oldDesc";
	String newDesc = "newDesc";
	boolean oldPublicJoin = false;
	boolean newPublicJoin = true;
	String oldIcon = "oldIcon";
	String newIcon = "newIcon";
	Exception caught = new Exception("this is an exception");
	
	@Before
	public void setup() {
		mockSynAlert = mock(SynapseAlert.class);
		mockView = mock(TeamEditModalWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockUploader = mock(FileHandleUploadWidget.class);
		mockJSNIUtils = mock(SynapseJSNIUtils.class);
		mockTeam = mock(Team.class);
		mockRefreshCallback = mock(Callback.class);
		mockFileUpload = mock(FileUpload.class);
		mockFileMeta = mock(FileMetadata.class);
		presenter = new TeamEditModalWidget(mockSynAlert, mockView, mockSynapseClient,
				mockUploader, mockJSNIUtils, mockAuthenticationController);
		presenter.configure(mockTeam);
		presenter.setRefreshCallback(mockRefreshCallback);
		
		ArgumentCaptor<Callback> startedUploadingCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockUploader).setUploadingCallback(startedUploadingCaptor.capture());
		// can invoke to check when loading finishes
		startedUploadingCallback = startedUploadingCaptor.getValue();
		
		ArgumentCaptor<CallbackP> finishedUploadingCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockUploader).configure(anyString(), finishedUploadingCaptor.capture());
		// can invoke to check when loading finishes
		finishedUploadingCallback = finishedUploadingCaptor.getValue();
		
		AsyncMockStubber.callSuccessWith(mockTeam).when(mockSynapseClient)
				.updateTeam(eq(mockTeam), any(AsyncCallback.class));
		when(mockFileUpload.getFileMeta()).thenReturn(mockFileMeta);
		when(mockFileMeta.getFileName()).thenReturn("new filename");
		when(mockView.getName()).thenReturn(newName);
		when(mockView.getDescription()).thenReturn(newDesc);
		when(mockView.getPublicJoin()).thenReturn(newPublicJoin);
		when(mockTeam.getIcon()).thenReturn(oldIcon);
	}
	
	@Test
	public void testConstruction() {
		verify(mockJSNIUtils).getBaseFileHandleUrl();
		verify(mockUploader).configure(anyString(), any(CallbackP.class));
		verify(mockUploader).setValidation(any(FileValidator.class));
		verify(mockUploader).setUploadingCallback(any(Callback.class));
		verify(mockView).setUploadWidget(mockUploader.asWidget());
		verify(mockView).setAlertWidget(mockSynAlert.asWidget());
		verify(mockView).setPresenter(presenter);
	}
	
	@Test
	public void testConfirmNoName() {
		when(mockView.getName()).thenReturn("");
		presenter.onConfirm();
		verify(mockSynAlert).showError("You must provide a name.");
	}
	
	@Test
	public void testImageLoading() {
	}
	
	@Test
	public void testConfirmSuccessfulChanges() {
		when(mockFileUpload.getFileHandleId()).thenReturn("newIcon");
		finishedUploadingCallback.invoke(mockFileUpload);
		verify(mockView).hideLoading();
		verify(mockView).setImageURL(anyString());
		verify(mockFileUpload).getFileHandleId();
		
		presenter.onConfirm();
		verify(mockView).getName();
		verify(mockView).getDescription();
		verify(mockView).getPublicJoin();
		verify(mockTeam).setName(newName);
		verify(mockTeam).setDescription(newDesc);
		verify(mockTeam).setCanPublicJoin(newPublicJoin);
		verify(mockTeam).setIcon(newIcon);
		
		verify(mockSynapseClient).updateTeam(eq(mockTeam), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockRefreshCallback).invoke();
		verify(mockView).hide();
	}
	
	@Test
	public void testConfirmNoIconUploaded() {
		when(mockFileUpload.getFileHandleId()).thenReturn(null);
		finishedUploadingCallback.invoke(mockFileUpload);
		verify(mockView).hideLoading();
		verify(mockView).setImageURL(anyString());
		verify(mockFileUpload).getFileHandleId();
		
		presenter.onConfirm();
		verify(mockView).getName();
		verify(mockView).getDescription();
		verify(mockView).getPublicJoin();
		verify(mockTeam).setName(newName);
		verify(mockTeam).setDescription(newDesc);
		verify(mockTeam).setCanPublicJoin(newPublicJoin);
		verify(mockTeam, never()).setIcon(newIcon);
		
		verify(mockSynapseClient).updateTeam(eq(mockTeam), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockRefreshCallback).invoke();
		verify(mockView).hide();
	}	
	
	@Test
	public void testConfirmFailedChanges() {
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient)
				.updateTeam(eq(mockTeam), any(AsyncCallback.class));
		when(mockFileUpload.getFileHandleId()).thenReturn(newIcon);
		finishedUploadingCallback.invoke(mockFileUpload);
		verify(mockView).hideLoading();
		verify(mockView).setImageURL(anyString());
		verify(mockFileUpload).getFileHandleId();
		
		presenter.onConfirm();
		verify(mockTeam).setName(newName);
		verify(mockTeam).setDescription(newDesc);
		verify(mockTeam).setCanPublicJoin(newPublicJoin);
		verify(mockTeam).setIcon(newIcon);
		
		verify(mockSynapseClient).updateTeam(eq(mockTeam), any(AsyncCallback.class));
		verify(mockUploader).reset();
		verify(mockView, Mockito.atLeast(1)).hideLoading();
		verify(mockSynAlert).handleException(caught);
		verify(mockView, never()).hide();
	}
	
	@Test
	public void testShowIconExists() {
		presenter.show();
		verify(mockUploader).reset();
		verify(mockSynAlert).clear();
		verify(mockView).hideLoading();
		verify(mockView).configure(mockTeam);
		verify(mockView).setImageURL(anyString());
		verify(mockView).show();
	}
	
	@Test
	public void testShowIconDoesNotExist() {
		when(mockTeam.getIcon()).thenReturn(null);
		presenter.show();
		verify(mockUploader).reset();
		verify(mockSynAlert).clear();
		verify(mockView).hideLoading();
		verify(mockView).configure(mockTeam);
		verify(mockView).setDefaultIconVisible();
		verify(mockView).show();
	}
	
	@Test
	public void testHide() {
		presenter.hide();
		verify(mockView).hide();
		verify(mockView, never()).setDefaultIconVisible();
		verify(mockView, never()).setImageURL(anyString());
	}
	
}