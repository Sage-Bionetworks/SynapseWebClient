package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalView;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidgetImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class EditProjectMetadataModalWidgetTest {

	@Mock
	EditProjectMetadataModalView mockView;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	Callback mockCallback;
	String startName;
	String parentId;
	EditProjectMetadataModalWidgetImpl widget;
	Project entity;
	boolean canChangeSettings;
	String newEntityName;
	String newAlias;

	@Before
	public void before() {
		entity = new Project();
		startName = "Start Name";
		canChangeSettings = true;
		entity.setName(startName);
		entity.setAlias(null);
		widget = new EditProjectMetadataModalWidgetImpl(mockView, mockSynapseJavascriptClient);
		newEntityName = "Modified Entity Name";
		when(mockView.getEntityName()).thenReturn(newEntityName);
		newAlias = "modified";
		when(mockView.getAlias()).thenReturn(newAlias);
		AsyncMockStubber.callSuccessWith(new Project()).when(mockSynapseJavascriptClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() {
		widget.configure(entity, canChangeSettings, mockCallback);
		verify(mockView).clear();
		verify(mockView).show();
		verify(mockView).configure(startName, null);
	}

	@Test
	public void testNullName() {
		widget.configure(entity, canChangeSettings, mockCallback);
		when(mockView.getEntityName()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseJavascriptClient, never()).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testNullAlias() {
		// ok to be null or empty
		widget.configure(entity, canChangeSettings, mockCallback);
		when(mockView.getAlias()).thenReturn(null);
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
	}

	@Test
	public void testEmptyAlias() {
		// ok to be null or empty
		widget.configure(entity, canChangeSettings, mockCallback);
		when(mockView.getAlias()).thenReturn("");
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
	}

	@Test
	public void testNoChanges() {
		widget.configure(entity, canChangeSettings, mockCallback);
		when(mockView.getEntityName()).thenReturn(startName);
		when(mockView.getAlias()).thenReturn(null);
		// Calling save with no real change just closes the dialog.
		widget.onPrimary();
		verify(mockView, never()).setLoading(true);
		verify(mockView).hide();
		verify(mockSynapseJavascriptClient, never()).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testHappyCase() {
		widget.configure(entity, canChangeSettings, mockCallback);
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
	}

	@Test
	public void testUpdateFailed() {
		Exception error = new Exception("an error");
		widget.configure(entity, canChangeSettings, mockCallback);
		AsyncMockStubber.callFailureWith(error).when(mockSynapseJavascriptClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		// save button
		widget.onPrimary();
		verify(mockView).setLoading(true);
		verify(mockView).showError(error.getMessage());
		verify(mockView).setLoading(false);
		verify(mockView, never()).hide();
		verify(mockCallback, never()).invoke();
	}

}
