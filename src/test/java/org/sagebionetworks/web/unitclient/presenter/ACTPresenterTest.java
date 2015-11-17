package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ACTPlace;
import org.sagebionetworks.web.client.presenter.ACTPresenter;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ACTView;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionModalViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionRowViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ACTPresenterTest {
	@Mock
	ACTView mockView; 
	@Mock
	VerificationSubmissionRowViewImpl mockRowView;
	@Mock
	UserProfileClientAsync mockUserProfileClient;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ACTPlace mockACTPlace;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseSuggestBox mockPeopleSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockUserGroupSuggestionProvider;
	@Mock
	VerificationPagedResults mockVerificationPagedResults;
	@Mock
	VerificationSubmission mockVerificationSubmission;
	@Mock
	VerificationSubmissionWidget mockVerificationSubmissionWidget;
	ACTPresenter widget;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.getVerificationSubmissionRowViewImpl()).thenReturn(mockRowView);
		
		widget = new ACTPresenter(mockView, mockUserProfileClient, mockSynapseAlert, mockPeopleSuggestBox, mockUserGroupSuggestionProvider, mockGinInjector, mockGlobalApplicationState);
		AsyncMockStubber.callSuccessWith(mockVerificationPagedResults).when(mockUserProfileClient).listVerificationSubmissions(any(VerificationStateEnum.class), anyLong(), anyLong(), anyLong(), any(AsyncCallback.class));
		when(mockVerificationPagedResults.getResults()).thenReturn(Collections.singletonList(mockVerificationSubmission));
		when(mockGinInjector.getVerificationSubmissionWidget()).thenReturn(mockVerificationSubmissionWidget);
	}

	@Test
	public void testConstruction(){
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockView).setStates(anyList());
		verify(mockView).setUserPickerWidget(any(Widget.class));
	}
	@Test
	public void testLoadData() {
		widget.loadData();
		verify(mockView).clearRows();
		verify(mockSynapseAlert).clear();
		verify(mockGlobalApplicationState).pushCurrentPlace(any(Place.class));
		verify(mockGinInjector).getVerificationSubmissionWidget();
		boolean isACTMember = true;
		boolean isModal = false;
		verify(mockVerificationSubmissionWidget).configure(mockVerificationSubmission, isACTMember, isModal);
		verify(mockView).addRow(any(Widget.class));
		verify(mockVerificationSubmissionWidget).show();
		verify(mockView).updatePagination(anyList());
	}
}
