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
	
	CallbackP<FileUpload> callback;
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
		presenter.setTeam(mockTeam);
		presenter.setRefreshCallback(mockRefreshCallback);
		ArgumentCaptor<CallbackP> captor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockUploader).configure(anyString(), captor.capture());
		callback = captor.getValue();
		
		when(mockFileUpload.getFileMeta()).thenReturn(mockFileMeta);
		when(mockFileMeta.getFileName()).thenReturn("new filename");
		when(mockTeam.getName()).thenReturn(oldName);
		when(mockTeam.getDescription()).thenReturn(oldDesc);
		when(mockTeam.getCanPublicJoin()).thenReturn(oldPublicJoin);
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
		verify(mockView).setTeam(mockTeam);
	}
	
	@Test
	public void testConfirmNoChanges() {
		presenter.onConfirm(oldName, oldDesc, oldPublicJoin);
		verify(mockSynAlert).showError("No changes were provided");
	}
	
	@Test
	public void testConfirmNoName() {
		presenter.onConfirm("", newDesc, newPublicJoin);
		verify(mockSynAlert).showError("You must provide a name.");
	}
	
	@Test
	public void testConfirmSuccessfulChanges() {
		when(mockFileUpload.getFileHandleId()).thenReturn("newIcon");
		callback.invoke(mockFileUpload);
		verify(mockView).setLoading(false);
		verify(mockView).setImageURL(anyString());
		verify(mockFileUpload).getFileHandleId();
		
		presenter.onConfirm(newName, newDesc, newPublicJoin);
		verify(mockTeam).setName(newName);
		verify(mockTeam).setDescription(newDesc);
		verify(mockTeam).setCanPublicJoin(newPublicJoin);
		verify(mockTeam).setIcon(newIcon);
		
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockSynapseClient).updateTeam(eq(mockTeam), captor.capture());
		captor.getValue().onSuccess(mockTeam);
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockRefreshCallback).invoke();
		verify(mockView).setVisible(false);
	}
	
	@Test
	public void testConfirmFailedChanges() {
		when(mockFileUpload.getFileHandleId()).thenReturn("newIcon");
		callback.invoke(mockFileUpload);
		verify(mockView).setLoading(false);
		verify(mockView).setImageURL(anyString());
		verify(mockFileUpload).getFileHandleId();
		
		presenter.onConfirm(newName, newDesc, newPublicJoin);
		verify(mockTeam).setName(newName);
		verify(mockTeam).setDescription(newDesc);
		verify(mockTeam).setCanPublicJoin(newPublicJoin);
		verify(mockTeam).setIcon(newIcon);
		
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		verify(mockSynapseClient).updateTeam(eq(mockTeam), captor.capture());
		captor.getValue().onFailure(caught);
		verify(mockUploader).reset();
		verify(mockView, Mockito.atLeast(1)).setLoading(false);
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testSetVisibleTrueIconExists() {
		boolean visible = true;
		presenter.setVisible(visible);
		verify(mockView).setImageURL(anyString());
		verify(mockView).setVisible(visible);
	}
	
	@Test
	public void testSetVisibleTrueIconDoesNotExist() {
		boolean visible = true;
		when(mockTeam.getIcon()).thenReturn(null);
		presenter.setVisible(visible);
		verify(mockView).setDefaultIconVisible();
		verify(mockView).setVisible(visible);
	}
	
	@Test
	public void testSetVisibleFalse() {
		boolean visible = false;
		presenter.setVisible(visible);
		verify(mockView).setVisible(visible);
		verify(mockView, never()).setDefaultIconVisible();
		verify(mockView, never()).setImageURL(anyString());
	}
	
}