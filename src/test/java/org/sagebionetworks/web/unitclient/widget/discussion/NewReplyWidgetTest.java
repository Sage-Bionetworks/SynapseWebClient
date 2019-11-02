package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.discussion.NewReplyWidget.DEFAULT_MARKDOWN;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.NewReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.NewReplyWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class NewReplyWidgetTest {
	private NewReplyWidget newReplyWidget;
	@Mock
	private NewReplyWidgetView mockView;
	@Mock
	private DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private MarkdownEditorWidget mockMarkdownEditor;
	@Mock
	private AuthenticationController mockAuthController;
	@Mock
	private GlobalApplicationState mockGlobalApplicationState;
	@Mock
	private PlaceChanger mockPlaceChanger;
	@Mock
	private DiscussionReplyBundle mockDiscussionReplyBundle;
	@Mock
	private SessionStorage mockStorage;
	@Mock
	PopupUtilsView mockPopupUtilsView;
	@Mock
	Callback mockCallback;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		newReplyWidget = new NewReplyWidget(mockView, mockDiscussionForumClient, mockSynAlert, mockMarkdownEditor, mockAuthController, mockGlobalApplicationState, mockStorage, mockPopupUtilsView);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockMarkdownEditor.getMarkdown()).thenReturn(DEFAULT_MARKDOWN);
		newReplyWidget.configure("1", mockCallback);
	}

	@Test
	public void testConfigure() {
		verify(mockView).resetButton();
		verify(mockView).setReplyTextBoxVisible(true);
		verify(mockView).setNewReplyContainerVisible(false);
	}

	@Test
	public void testOnClickNewReply() {
		newReplyWidget.onClickNewReply();
		verify(mockMarkdownEditor).configure(anyString());
		verify(mockView).setReplyTextBoxVisible(false);
		verify(mockView).setNewReplyContainerVisible(true);
		verify(mockMarkdownEditor).setMarkdownFocus();
		verify(mockView).scrollIntoView();
		verify(mockGlobalApplicationState).setIsEditing(true);
	}

	@Test
	public void testOnClickNewReplyAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		newReplyWidget.onClickNewReply();
		verify(mockMarkdownEditor, never()).configure(anyString());
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockView).showErrorMessage(anyString());
		verify(mockView, never()).setReplyTextBoxVisible(false);
		verify(mockView, never()).setNewReplyContainerVisible(true);
		verify(mockMarkdownEditor, never()).setMarkdownFocus();
	}

	@Test
	public void testOnClickCancel() {
		newReplyWidget.onCancel();

		// since no changes were made, verify confirmation dialog was not shown
		verify(mockPopupUtilsView, never()).showConfirmDialog(eq(DisplayConstants.UNSAVED_CHANGES), eq(DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE), callbackCaptor.capture());
		verify(mockView, times(2)).resetButton();
		verify(mockView, times(2)).setReplyTextBoxVisible(true);
		verify(mockView, times(2)).setNewReplyContainerVisible(false);
		verify(mockGlobalApplicationState, times(2)).setIsEditing(false);
	}

	@Test
	public void testOnClickCancelWithUnsavedChanges() {
		when(mockMarkdownEditor.getMarkdown()).thenReturn("unsaved changes");
		newReplyWidget.onCancel();

		verify(mockPopupUtilsView).showConfirmDialog(eq(DisplayConstants.UNSAVED_CHANGES), eq(DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE), callbackCaptor.capture());

		// simulate user confirmed
		callbackCaptor.getValue().invoke();

		// verify cancel
		verify(mockView, times(2)).resetButton();
		verify(mockView, times(2)).setReplyTextBoxVisible(true);
		verify(mockView, times(2)).setNewReplyContainerVisible(false);
		verify(mockGlobalApplicationState, times(2)).setIsEditing(false);
	}

	@Test
	public void testOnClickSaveInvalidArgument() {
		when(mockMarkdownEditor.getMarkdown()).thenReturn("");
		newReplyWidget.onSave();
		verify(mockSynAlert).clear();
		verify(mockMarkdownEditor).getMarkdown();
		verify(mockSynAlert).showError(anyString());
		verifyZeroInteractions(mockDiscussionForumClient);
	}

	@Test
	public void testOnClickSaveSuccess() {
		when(mockMarkdownEditor.getMarkdown()).thenReturn("message");
		AsyncMockStubber.callSuccessWith(mockDiscussionReplyBundle).when(mockDiscussionForumClient).createReply(any(CreateDiscussionReply.class), any(AsyncCallback.class));
		newReplyWidget.onSave();
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockView).showSaving();
		verify(mockView).showSuccess(anyString(), anyString());
		verify(mockDiscussionForumClient).createReply(any(CreateDiscussionReply.class), any(AsyncCallback.class));
		verify(mockView, times(2)).resetButton();
		verify(mockView, times(2)).setReplyTextBoxVisible(true);
		verify(mockView, times(2)).setNewReplyContainerVisible(false);
		verify(mockCallback).invoke();
		// called once during configure (reset), once on reset after successful save
		verify(mockGlobalApplicationState, times(2)).setIsEditing(false);
	}

	@Test
	public void testOnSaveFailure() {
		when(mockMarkdownEditor.getMarkdown()).thenReturn("message");
		Exception exception = new Exception();
		AsyncMockStubber.callFailureWith(exception).when(mockDiscussionForumClient).createReply(any(CreateDiscussionReply.class), any(AsyncCallback.class));
		newReplyWidget.onSave();
		verify(mockSynAlert).clear();
		verify(mockView).showSaving();
		verify(mockDiscussionForumClient).createReply(any(CreateDiscussionReply.class), any(AsyncCallback.class));
		verifyZeroInteractions(mockCallback);
		verify(mockSynAlert).handleException(exception);
		verify(mockView, times(2)).resetButton();
		// only called once on the initial reset (not after save)
		verify(mockGlobalApplicationState).setIsEditing(false);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setMarkdownEditor(any(Widget.class));
		verify(mockView).setPresenter(newReplyWidget);
	}

	@Test
	public void testCacheReplyWhenLoggedOut() {
		when(mockMarkdownEditor.getMarkdown()).thenReturn("message");
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		newReplyWidget.onSave();
		verify(mockStorage).setItem(anyString(), eq("message"));
	}

	@Test
	public void testLoadCachedReplyClickYes() {
		when(mockStorage.getItem(anyString())).thenReturn("message");
		newReplyWidget.onClickNewReply();
		verify(mockView).showConfirmDialog(anyString(), anyString(), callbackCaptor.capture(), any(Callback.class));
		callbackCaptor.getValue().invoke();

		verify(mockMarkdownEditor).configure("message");
		verify(mockMarkdownEditor).showExternalImageButton();
		verify(mockMarkdownEditor).hideUploadRelatedCommands();
		verify(mockStorage).removeItem(anyString());
	}

	@Test
	public void testLoadCachedReplyClickNo() {
		when(mockStorage.getItem(anyString())).thenReturn("message");
		newReplyWidget.onClickNewReply();
		verify(mockView).showConfirmDialog(anyString(), anyString(), any(Callback.class), callbackCaptor.capture());
		callbackCaptor.getValue().invoke();

		verify(mockMarkdownEditor).configure(DEFAULT_MARKDOWN);
		verify(mockStorage).removeItem(anyString());
	}

	@Test
	public void testNoCacheToLoad() {
		when(mockStorage.getItem(anyString())).thenReturn(null);
		newReplyWidget.onClickNewReply();
		verify(mockView, times(0)).showConfirmDialog(anyString(), anyString(), any(Callback.class), any(Callback.class));

		verify(mockMarkdownEditor).configure(DEFAULT_MARKDOWN);
		verify(mockStorage, times(0)).removeItem(anyString());
	}

}
