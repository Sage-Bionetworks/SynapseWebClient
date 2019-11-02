package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetImpl.CANCEL;
import static org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetImpl.OK;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class AccessControlListModalWidgetImplTest {

	AccessControlListModalWidgetView mockView;
	AccessControlListEditor mockEditor;
	FileEntity fileEntity;
	Callback mockCallback;

	AccessControlListModalWidgetImpl modal;

	@Before
	public void before() {
		mockView = Mockito.mock(AccessControlListModalWidgetView.class);
		mockEditor = Mockito.mock(AccessControlListEditor.class);
		fileEntity = new FileEntity();
		mockCallback = Mockito.mock(Callback.class);
		modal = new AccessControlListModalWidgetImpl(mockView, mockEditor);
	}

	@Test
	public void testConfigureCanEdit() {
		boolean canChangePermission = true;
		modal.configure(fileEntity, canChangePermission);
		verify(mockView).setPrimaryButtonVisible(true);
		verify(mockView).setDefaultButtonText(CANCEL);
	}

	@Test
	public void testConfigurReadOnly() {
		boolean canChangePermission = false;
		modal.configure(fileEntity, canChangePermission);
		verify(mockView).setPrimaryButtonVisible(false);
		verify(mockView).setDefaultButtonText(OK);
	}

	@Test
	public void testOnChange() {
		boolean canChangePermission = true;
		modal.configure(fileEntity, canChangePermission);
		modal.hasChanges(true);
		verify(mockView).setLoading(false);
		verify(mockView).setPrimaryButtonEnabled(true);
	}

	@Test
	public void testOnChangeNoChange() {
		boolean canChangePermission = true;
		modal.configure(fileEntity, canChangePermission);
		modal.hasChanges(false);
		verify(mockView).setLoading(false);
		verify(mockView).setPrimaryButtonEnabled(false);
	}

	@Test
	public void testShowSharing() {
		boolean canChangePermission = true;
		modal.configure(fileEntity, canChangePermission);
		modal.showSharing(mockCallback);
		verify(mockView).setLoading(false);
		verify(mockEditor).refresh();
		verify(mockView).showDialog();
	}

	@Test
	public void testOnPrimary() {
		// Invoke the callback.
		AsyncMockStubber.callWithInvoke().when(mockEditor).pushChangesToSynapse(anyBoolean(), any(Callback.class));
		boolean canChangePermission = true;
		modal.configure(fileEntity, canChangePermission);
		modal.showSharing(mockCallback);
		modal.onPrimary();
		verify(mockView).hideDialog();
		verify(mockCallback).invoke();
	}
}
