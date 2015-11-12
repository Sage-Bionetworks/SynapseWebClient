package org.sagebionetworks.web.unitclient.widget.verification;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionModalViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidgetView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class VerificationSubmissionWidgetTest {
	@Mock
	VerificationSubmissionModalViewImpl mockView; 
	@Mock
	UserProfileClientAsync mockUserProfileClient;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	FileHandleList mockFileHandleList;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	PromptModalView mockPromptModalView;
	@Mock
	CookieProvider mockCookieProvider;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	VerificationSubmission mockSubmission;
	@Mock
	UserProfile mockProfile;
	
	PromptModalView.Presenter reasonPromptCallback;
	
	VerificationSubmissionWidget widget;
	String fileUrl = "https://s3/file.txt";
	String submissionId = "5432";
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.getVerificationSubmissionModalViewImpl()).thenReturn(mockView);
		widget = new VerificationSubmissionWidget(mockGinInjector, mockUserProfileClient, mockMarkdownWidget, mockSynapseClient, mockSynapseAlert, mockFileHandleList, mockSynapseJSNIUtils, mockPromptModalView, mockCookieProvider, mockGlobalApplicationState);
		
		ArgumentCaptor<PromptModalView.Presenter> captor = ArgumentCaptor.forClass(PromptModalView.Presenter.class);
		verify(mockPromptModalView).setPresenter(captor.capture());
		reasonPromptCallback = captor.getValue();
		
		AsyncMockStubber.callSuccessWith(fileUrl).when(mockUserProfileClient).getFileURL(any(FileHandleAssociation.class), any(AsyncCallback.class));
		when(mockSubmission.getId()).thenReturn(submissionId);
	}

	@Test
	public void testConfigureEditAsModal() {
		boolean isACTMember = false;
		boolean isModal = true;
		widget.configure(mockSubmission, isACTMember, isModal);
		
		assertFalse(widget.isNewSubmission());
		verify(mockGinInjector).getVerificationSubmissionModalViewImpl();
		verify(mockView).setFileHandleList(any(Widget.class));
		verify(mockView).setWikiPage(any(Widget.class));
		verify(mockView).setPromptModal(any(Widget.class));
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockView).setPresenter(widget);
		
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigureNewAsModal() {
		boolean isACTMember = false;
		boolean isModal = true;
		String orcId = "http://orcid.org/123";
		widget.configure(mockProfile, orcId, isACTMember, isModal);
		
		assertTrue(widget.isNewSubmission());
		verify(mockGinInjector).getVerificationSubmissionModalViewImpl();
		verify(mockView).setFileHandleList(any(Widget.class));
		verify(mockView).setWikiPage(any(Widget.class));
		verify(mockView).setPromptModal(any(Widget.class));
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockView).setPresenter(widget);
	}

	
	@Test
	public void testConfigureAsTableRow() {
		//TODO: for ACT place
	}

	@Test
	public void testGetVerificationSubmissionHandleUrlOpen() {
		boolean isACTMember = false;
		boolean isModal = true;
		widget.configure(mockSubmission, isACTMember, isModal);
		
		String fileHandleId = "8888";
		widget.getVerificationSubmissionHandleUrlAndOpen(fileHandleId);
		ArgumentCaptor<FileHandleAssociation> captor = ArgumentCaptor.forClass(FileHandleAssociation.class);
		verify(mockUserProfileClient).getFileURL(captor.capture(), any(AsyncCallback.class));
		verify(mockView).openWindow(fileUrl);
		
		FileHandleAssociation fha = captor.getValue();
		assertEquals(submissionId, fha.getAssociateObjectId());
		assertEquals(fileHandleId, fha.getFileHandleId());
		assertEquals(FileHandleAssociateType.VerificationSubmission, fha.getAssociateObjectType());
	}

}
