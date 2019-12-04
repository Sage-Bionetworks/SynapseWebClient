package org.sagebionetworks.web.unitclient.widget.entity.act;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.act.ACTRevokeUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.act.RevokeUserAccessModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ACTRevokeUserAccessModalTest {

	ACTRevokeUserAccessModal dialog;

	@Mock
	RevokeUserAccessModalView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseSuggestBox mockPeopleSuggestWidget;
	@Mock
	UserGroupSuggestionProvider mockProvider;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	UserGroupSuggestion mockUser;
	@Mock
	ACTAccessRequirement mockACTAccessRequirement;
	String selectedUserId = "34543";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		dialog = new ACTRevokeUserAccessModal(mockView, mockSynAlert, mockPeopleSuggestWidget, mockProvider, mockSynapseClient);
		dialog.configure(mockACTAccessRequirement);
		when(mockUser.getId()).thenReturn(selectedUserId);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteAccessApprovals(anyString(), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() {
		verify(mockView).setPresenter(dialog);
		verify(mockView).setUserPickerWidget(any(Widget.class));
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockSynAlert).clear();
		verify(mockPeopleSuggestWidget).setTypeFilter(TypeFilter.USERS_ONLY);
		verify(mockView).show();
	}

	@Test
	public void testOnRevokeNoUserSelected() {
		dialog.onRevoke();
		verify(mockSynAlert).showError(ACTRevokeUserAccessModal.NO_USER_SELECTED);
	}

	@Test
	public void testOnRevokeUserSelected() {
		dialog.onUserSelected(mockUser);
		dialog.onRevoke();
		verify(mockView).setRevokeProcessing(true);
		verify(mockSynapseClient).deleteAccessApprovals(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).setRevokeProcessing(false);
		verify(mockView).hide();
		verify(mockView).showInfo(ACTRevokeUserAccessModal.REVOKED_USER);
	}

	@Test
	public void testOnRevokeUserSelectedFailure() {
		Exception ex = new Exception("");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).deleteAccessApprovals(anyString(), anyString(), any(AsyncCallback.class));
		dialog.onUserSelected(mockUser);
		dialog.onRevoke();
		verify(mockView).setRevokeProcessing(true);
		verify(mockSynapseClient).deleteAccessApprovals(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).setRevokeProcessing(false);
		verify(mockSynAlert).handleException(ex);
	}
}
