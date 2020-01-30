package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRenderer;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class UserIdCellEditorImplTest {
	@Mock
	UserIdCellEditorView mockView;
	@Mock
	SynapseSuggestBox mockSynapseSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockUserGroupSuggestionProvider;
	@Mock
	UserGroupSuggestion mockSynapseSuggestion;
	@Mock
	UserIdCellRenderer mockUserIdCellRenderer;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	ClickHandler onUserBadgeClick;

	UserIdCellEditor editor;

	public static final String SELECTED_USER_ID = "876";

	@Before
	public void before() {
		editor = new UserIdCellEditor(mockView, mockSynapseSuggestBox, mockUserGroupSuggestionProvider, mockUserIdCellRenderer);
		when(mockSynapseSuggestion.getId()).thenReturn(SELECTED_USER_ID);
		verify(mockView).setUserIdCellRendererClickHandler(clickHandlerCaptor.capture());
		onUserBadgeClick = clickHandlerCaptor.getValue();
	}

	@Test
	public void testConstruction() {
		verify(mockView).setSynapseSuggestBoxWidget(any(Widget.class));
		verify(mockView).setUserIdCellRenderer(any(Widget.class));
		verify(mockSynapseSuggestBox).setSuggestionProvider(mockUserGroupSuggestionProvider);
		verify(mockSynapseSuggestBox).addItemSelectedHandler(any(CallbackP.class));
	}

	@Test
	public void testUserSelected() {
		editor.onUserSelected(mockSynapseSuggestion);
		verify(mockSynapseSuggestBox).clear();
		verify(mockSynapseSuggestBox).setText(SELECTED_USER_ID);
	}

	@Test
	public void testSetValueNull() {
		editor.setValue(null);
		verify(mockSynapseSuggestBox).setText(null);
		verify(mockUserIdCellRenderer).setValue(null, onUserBadgeClick);
		verify(mockView).showEditor(true);
	}

	@Test
	public void testSetValueReal() {
		String userId = "456765";
		editor.setValue(userId);
		verify(mockSynapseSuggestBox).clear();
		verify(mockSynapseSuggestBox).setText(userId);
		verify(mockUserIdCellRenderer).setValue(userId, onUserBadgeClick);
		verify(mockView).showEditor(false);
	}

	@Test
	public void testGetValueEmpty() {
		// convert empty string to null
		when(mockSynapseSuggestBox.getText()).thenReturn("");
		assertNull(editor.getValue());
		when(mockSynapseSuggestBox.getText()).thenReturn("   ");
		assertNull(editor.getValue());
	}

	@Test
	public void testGetValueReal() {
		when(mockSynapseSuggestBox.getText()).thenReturn(SELECTED_USER_ID);
		assertEquals(SELECTED_USER_ID, editor.getValue());
	}

	@Test
	public void testUserBadgeClick() {
		// simulate user clicking on the badge or anywhere in the "textbox" (parent div with cursor: text)
		onUserBadgeClick.onClick(null);
		verify(mockView).showEditor(true);
		verify(mockSynapseSuggestBox).setFocus(true);
		verify(mockSynapseSuggestBox).selectAll();
	}

	@Test
	public void testAddKeyDownHandler() {
		KeyDownHandler mockKeyDownHandler = Mockito.mock(KeyDownHandler.class);
		editor.addKeyDownHandler(mockKeyDownHandler);
		verify(mockSynapseSuggestBox).addKeyDownHandler(mockKeyDownHandler);
	}

	@Test
	public void testFireEvent() {
		GwtEvent event = Mockito.mock(GwtEvent.class);
		editor.fireEvent(event);
		verify(mockSynapseSuggestBox).fireEvent(event);
	}

	@Test
	public void testGetTabIndex() {
		int tabIndex = 4;
		when(mockSynapseSuggestBox.getTabIndex()).thenReturn(tabIndex);
		assertEquals(tabIndex, editor.getTabIndex());
	}

	@Test
	public void testSetAccessKey() {
		char key = 'a';
		editor.setAccessKey(key);
		verify(mockSynapseSuggestBox).setAccessKey(key);
	}

	@Test
	public void testSetTabIndex() {
		int tabIndex = 5;
		editor.setTabIndex(tabIndex);
		verify(mockSynapseSuggestBox).setTabIndex(tabIndex);
	}
}
