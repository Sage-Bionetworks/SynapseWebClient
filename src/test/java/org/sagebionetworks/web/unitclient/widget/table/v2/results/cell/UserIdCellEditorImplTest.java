package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseUserGroupSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorView;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

public class UserIdCellEditorImplTest {
	@Mock
	UserIdCellEditorView mockView;
	@Mock
	SynapseUserGroupSuggestBox mockSynapseUserGroupSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockUserGroupSuggestionProvider;
	@Mock
	UserGroupSuggestion mockSynapseSuggestion;
	UserIdCellEditorImpl editor;

	public static final String SELECTED_USER_ID = "876";
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		editor = new UserIdCellEditorImpl(mockView, mockSynapseUserGroupSuggestBox, mockUserGroupSuggestionProvider);
		when(mockSynapseSuggestion.getId()).thenReturn(SELECTED_USER_ID);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setSynapseSuggestBoxWidget(any(Widget.class));
		verify(mockSynapseUserGroupSuggestBox).setSuggestionProvider(mockUserGroupSuggestionProvider);
		verify(mockSynapseUserGroupSuggestBox).addItemSelectedHandler(any(CallbackP.class));
	}
	
	@Test
	public void testUserSelected(){
		editor.onUserSelected(mockSynapseSuggestion);
		verify(mockSynapseUserGroupSuggestBox).clear();
		verify(mockSynapseUserGroupSuggestBox).setText(SELECTED_USER_ID);
	}
	
	@Test
	public void testSetValueNull(){
		editor.setValue(null);
		verify(mockSynapseUserGroupSuggestBox).setText(null);
	}
	
	@Test
	public void testSetValueReal(){
		String userId = "456765";
		editor.setValue(userId);
		verify(mockSynapseUserGroupSuggestBox).clear();
		verify(mockSynapseUserGroupSuggestBox).setText(userId);
	}
	
	@Test
	public void testGetValueEmpty(){
		// convert empty string to null
		when(mockSynapseUserGroupSuggestBox.getText()).thenReturn("");
		assertNull(editor.getValue());
		when(mockSynapseUserGroupSuggestBox.getText()).thenReturn("   ");
		assertNull(editor.getValue());
	}
	
	@Test
	public void testGetValueReal(){
		when(mockSynapseUserGroupSuggestBox.getText()).thenReturn(SELECTED_USER_ID);
		assertEquals(SELECTED_USER_ID, editor.getValue());
	}
	
	@Test
	public void testAddKeyDownHandler(){
		KeyDownHandler mockKeyDownHandler = Mockito.mock(KeyDownHandler.class);
		editor.addKeyDownHandler(mockKeyDownHandler);
		verify(mockSynapseUserGroupSuggestBox).addKeyDownHandler(mockKeyDownHandler);
	}


	@Test
	public void testFireEvent() {
		GwtEvent event = Mockito.mock(GwtEvent.class);
		editor.fireEvent(event);
		verify(mockSynapseUserGroupSuggestBox).fireEvent(event);
	}
	
	@Test
	public void testGetTabIndex() {
		int tabIndex = 4;
		when(mockSynapseUserGroupSuggestBox.getTabIndex()).thenReturn(tabIndex);
		assertEquals(tabIndex, editor.getTabIndex());
	}
	
	@Test
	public void testSetAccessKey() {
		char key = 'a';
		editor.setAccessKey(key);
		verify(mockSynapseUserGroupSuggestBox).setAccessKey(key);
	}
	
	@Test
	public void testSetTabIndex() {
		int tabIndex = 5;
		editor.setTabIndex(tabIndex);
		verify(mockSynapseUserGroupSuggestBox).setTabIndex(tabIndex);
	}
}
