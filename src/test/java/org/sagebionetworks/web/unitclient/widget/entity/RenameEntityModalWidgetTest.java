package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.LABLE_SUFFIX;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.TITLE_PREFIX;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class RenameEntityModalWidgetTest {
	@Mock
	PromptForValuesModalView mockView;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	Callback mockCallback;
	String startName;
	String entityDispalyType;
	String parentId;
	RenameEntityModalWidgetImpl widget;
	TableEntity entity;
	@Captor
	ArgumentCaptor<CallbackP<String>> promptCallbackCaptor;

	@Before
	public void before() {
		entity = new TableEntity();
		startName = "Start Name";
		entity.setName(startName);
		entityDispalyType = "Table";
		widget = new RenameEntityModalWidgetImpl(mockView, mockJsClient);
	}

	@Test
	public void testOnRename() {
		widget.onRename(entity, mockCallback);

		verify(mockView).configureAndShow(eq(TITLE_PREFIX + entityDispalyType), eq(entityDispalyType + LABLE_SUFFIX), eq(startName), any(CallbackP.class));
	}

	@Test
	public void testNullName() {
		widget.onRename(entity, mockCallback);

		verify(mockView).configureAndShow(anyString(), anyString(), anyString(), promptCallbackCaptor.capture());
		promptCallbackCaptor.getValue().invoke(null);
		verify(mockView).showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockJsClient, never()).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testNameNotChanged() {
		widget.onRename(entity, mockCallback);
		verify(mockView).configureAndShow(anyString(), anyString(), anyString(), promptCallbackCaptor.capture());
		// Calling save with no real change just closes the dialog.
		promptCallbackCaptor.getValue().invoke(startName);
		verify(mockView, never()).setLoading(true);
		verify(mockView).hide();
		verify(mockJsClient, never()).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		// should only be called on success
		verify(mockCallback, never()).invoke();
	}

	@Test
	public void testRenameHappy() {
		String newName = "a new name";
		widget.onRename(entity, mockCallback);
		AsyncMockStubber.callSuccessWith(new TableEntity()).when(mockJsClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).configureAndShow(anyString(), anyString(), anyString(), promptCallbackCaptor.capture());
		promptCallbackCaptor.getValue().invoke(newName);

		verify(mockView).setLoading(true);
		verify(mockView).hide();
		verify(mockCallback).invoke();
	}

	@Test
	public void testRenameFailed() {
		Exception error = new Exception("an object already exists with that name");
		String newName = "a new name";
		widget.onRename(entity, mockCallback);
		AsyncMockStubber.callFailureWith(error).when(mockJsClient).updateEntity(any(Entity.class), anyString(), anyBoolean(), any(AsyncCallback.class));

		verify(mockView).configureAndShow(anyString(), anyString(), anyString(), promptCallbackCaptor.capture());
		promptCallbackCaptor.getValue().invoke(newName);

		verify(mockView).setLoading(true);
		verify(mockView).showError(error.getMessage());
		verify(mockView).setLoading(false);
		verify(mockView, never()).hide();
		verify(mockCallback, never()).invoke();
	}
}
