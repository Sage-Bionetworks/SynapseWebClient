package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorView;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

public class UserIdCellEditorImplTest {
	@Mock
	UserIdCellEditorView mockView;
	@Mock
	SynapseSuggestBox mockSynapseSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockUserGroupSuggestionProvider;
	@Mock
	SynapseSuggestion mockSynapseSuggestion;
	UserIdCellEditorImpl editor;

	public static final String SELECTED_USER_ID = "876";
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		editor = new UserIdCellEditorImpl(mockView, mockSynapseSuggestBox, mockUserGroupSuggestionProvider);
		when(mockSynapseSuggestion.getId()).thenReturn(SELECTED_USER_ID);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setSynapseSuggestBoxWidget(any(Widget.class));
		verify(mockSynapseSuggestBox).setSuggestionProvider(mockUserGroupSuggestionProvider);
		verify(mockSynapseSuggestBox).addItemSelectedHandler(any(CallbackP.class));
	}
	
	@Test
	public void testUserSelected(){
		editor.onUserSelected(mockSynapseSuggestion);
		verify(mockSynapseSuggestBox).clear();
		verify(mockSynapseSuggestBox).setText(SELECTED_USER_ID);
	}
	
	@Test
	public void testSetValueNull(){
		editor.setValue(null);
		verify(mockSynapseSuggestBox).setText(null);
	}
	
	@Test
	public void testSetValueReal(){
		String userId = "456765";
		editor.setValue(userId);
		verify(mockSynapseSuggestBox).clear();
		verify(mockSynapseSuggestBox).setText(userId);
	}
	
	@Test
	public void testGetValueEmpty(){
		// convert empty string to null
		when(mockSynapseSuggestBox.getText()).thenReturn("");
		assertNull(editor.getValue());
		when(mockSynapseSuggestBox.getText()).thenReturn("   ");
		assertNull(editor.getValue());
	}
	
	@Test
	public void testGetValueReal(){
		when(mockSynapseSuggestBox.getText()).thenReturn(SELECTED_USER_ID);
		assertEquals(SELECTED_USER_ID, editor.getValue());
	}
	
	@Test
	public void testAddKeyDownHandler(){
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
