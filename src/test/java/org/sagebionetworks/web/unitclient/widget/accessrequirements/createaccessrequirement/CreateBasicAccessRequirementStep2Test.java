package org.sagebionetworks.web.unitclient.widget.accessrequirements.createaccessrequirement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateBasicAccessRequirementStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateBasicAccessRequirementStep2View;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class CreateBasicAccessRequirementStep2Test {

	CreateBasicAccessRequirementStep2 widget;
	@Mock
	ModalPresenter mockModalPresenter;

	@Mock
	CreateBasicAccessRequirementStep2View mockView;
	@Mock
	WikiMarkdownEditor mockWikiMarkdownEditor;
	@Mock
	WikiPageWidget mockWikiPageRenderer;
	@Mock
	TermsOfUseAccessRequirement mockTermsOfUseAccessRequirement;
	@Mock
	ACTAccessRequirement mockACTAccessRequirement;
	@Captor
	ArgumentCaptor<WikiPageKey> wikiPageKeyCaptor;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	PopupUtilsView mockPopupUtils;

	public static final Long AR_ID = 8765L;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new CreateBasicAccessRequirementStep2(mockView, mockWikiMarkdownEditor, mockWikiPageRenderer, mockSynapseClient, mockSynAlert, mockPopupUtils);
		widget.setModalPresenter(mockModalPresenter);
		when(mockTermsOfUseAccessRequirement.getId()).thenReturn(AR_ID);
		AsyncMockStubber.callSuccessWith(mockTermsOfUseAccessRequirement).when(mockSynapseClient).createOrUpdateAccessRequirement(any(AccessRequirement.class), any(AsyncCallback.class));
	}

	@Test
	public void testConstruction() {
		verify(mockView).setWikiPageRenderer(any(IsWidget.class));
		verify(mockView).setPresenter(widget);
		verify(mockWikiPageRenderer).setModifiedCreatedByHistoryVisible(false);
		verify(mockWikiMarkdownEditor).setDeleteButtonVisible(false);
	}

	@Test
	public void testConfigureWithWiki() {
		widget.configure(mockTermsOfUseAccessRequirement);
		verify(mockView).setOldTermsVisible(false);
		verify(mockView).setOldTerms("");
		verify(mockWikiPageRenderer).configure(wikiPageKeyCaptor.capture(), eq(false), eq((WikiPageWidget.Callback) null));
		WikiPageKey key = wikiPageKeyCaptor.getValue();
		assertEquals(AR_ID.toString(), key.getOwnerObjectId());
		assertEquals(ObjectType.ACCESS_REQUIREMENT.toString(), key.getOwnerObjectType());

		// on edit of wiki
		widget.onEditWiki();
		verify(mockWikiMarkdownEditor).configure(eq(key), any(CallbackP.class));

		// on finish
		widget.onPrimary();
		verify(mockModalPresenter).onFinished();
	}

	@Test
	public void testConfigureWithOldTermsOfUse() {
		String tou = "these are the old conditions";
		when(mockTermsOfUseAccessRequirement.getTermsOfUse()).thenReturn(tou);
		widget.configure(mockTermsOfUseAccessRequirement);
		verify(mockView).setOldTermsVisible(true);
		verify(mockView).setOldTerms(tou);
		verify(mockWikiPageRenderer).configure(wikiPageKeyCaptor.capture(), eq(false), eq((WikiPageWidget.Callback) null));
		WikiPageKey key = wikiPageKeyCaptor.getValue();
		assertEquals(AR_ID.toString(), key.getOwnerObjectId());
		assertEquals(ObjectType.ACCESS_REQUIREMENT.toString(), key.getOwnerObjectType());

		// try clearing the old terms
		widget.onClearOldInstructionsAfterConfirm();
		verify(mockTermsOfUseAccessRequirement).setTermsOfUse(null);
		verify(mockSynapseClient).createOrUpdateAccessRequirement(eq(mockTermsOfUseAccessRequirement), any(AsyncCallback.class));

		// on edit of wiki
		widget.onEditWiki();
		verify(mockWikiMarkdownEditor).configure(eq(key), any(CallbackP.class));

		// on finish
		widget.onPrimary();
		verify(mockModalPresenter).onFinished();
	}

	@Test
	public void testConfigureWithOldTermsOfUseACTAccessRequirement() {
		String tou = "these are the old conditions";
		when(mockACTAccessRequirement.getActContactInfo()).thenReturn(tou);
		widget.configure(mockACTAccessRequirement);
		verify(mockView).setOldTermsVisible(true);
		verify(mockView).setOldTerms(tou);

		// try clearing the old terms
		widget.onClearOldInstructionsAfterConfirm();
		verify(mockACTAccessRequirement).setActContactInfo(null);
		verify(mockSynapseClient).createOrUpdateAccessRequirement(eq(mockACTAccessRequirement), any(AsyncCallback.class));
	}

	@Test
	public void testClearOldInstructionsFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).createOrUpdateAccessRequirement(any(AccessRequirement.class), any(AsyncCallback.class));
		widget.configure(mockACTAccessRequirement);

		// try clearing the old terms
		widget.onClearOldInstructionsAfterConfirm();
		verify(mockSynAlert).clear();
		verify(mockACTAccessRequirement).setActContactInfo(null);
		verify(mockSynapseClient).createOrUpdateAccessRequirement(eq(mockACTAccessRequirement), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnClearOldInstructions() {
		// verify action is confirmed
		widget.onClearOldInstructions();
		verify(mockPopupUtils).showConfirmDialog(anyString(), anyString(), any(Callback.class));
	}
}
