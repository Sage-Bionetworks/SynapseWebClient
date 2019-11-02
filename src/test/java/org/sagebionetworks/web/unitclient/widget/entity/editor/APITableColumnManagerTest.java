package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManager;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManagerView;
import org.sagebionetworks.web.shared.WidgetConstants;
import com.google.gwt.user.client.ui.Widget;

public class APITableColumnManagerTest {
	APITableColumnManager manager;
	@Mock
	APITableColumnManagerView mockView;
	String defaultInputColumnName;
	String defaultRendererName;
	Set<String> defaultInputColumnNamesSet;
	@Mock
	PortalGinInjector mockGinInjector;
	List<APITableColumnConfig> configs;
	@Mock
	APITableColumnConfig mockConfig1;
	@Mock
	APITableColumnConfig mockConfig2;
	@Mock
	APITableColumnConfigView mockColumnConfigEditor;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		manager = new APITableColumnManager(mockView, mockGinInjector);
		configs = new ArrayList<APITableColumnConfig>();
		defaultInputColumnName = "myTestColumn";
		defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE;
		defaultInputColumnNamesSet = new HashSet<String>();
		defaultInputColumnNamesSet.add(defaultInputColumnName);
		when(mockGinInjector.getAPITableColumnConfigView()).thenReturn(mockColumnConfigEditor);
		when(mockColumnConfigEditor.getConfig()).thenReturn(mockConfig1);
	}

	@Test
	public void testAsWidget() {
		verify(mockView).setPresenter(manager);
		manager.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() {
		configs.add(mockConfig1);
		configs.add(mockConfig2);
		manager.configure(configs);

		verify(mockView).clearColumns();
		verify(mockView, times(2)).addColumn(any(Widget.class));
		verify(mockView).setButtonToolbarVisible(true);
		verify(mockView).setHeaderColumnsVisible(true);
		verify(mockView).setNoColumnsUIVisible(false);
	}

	@Test
	public void testConfigureNoColumnConfigs() {
		manager.configure(configs);

		verify(mockView).clearColumns();
		verify(mockView, never()).addColumn(any(Widget.class));
		verify(mockView).setButtonToolbarVisible(false);
		verify(mockView).setHeaderColumnsVisible(false);
		verify(mockView).setNoColumnsUIVisible(true);
	}

	@Test
	public void testAddDelete() {
		manager.configure(configs);
		// before assert it's empty
		assertTrue(manager.getColumnConfigs().isEmpty()); // sanity test
		manager.addColumnConfig();
		assertTrue(manager.getColumnConfigs().size() == 1); // sanity test
		verify(mockColumnConfigEditor).setSelectionChangedCallback(any(Callback.class));
		verify(mockColumnConfigEditor).configure(any(APITableColumnConfig.class));

		verify(mockView).addColumn(any(Widget.class));
		verify(mockView).setButtonToolbarVisible(true);
		verify(mockView).setHeaderColumnsVisible(true);
		verify(mockView).setNoColumnsUIVisible(false);

		// try delete with nothing selected
		when(mockColumnConfigEditor.isSelected()).thenReturn(false);
		manager.deleteSelected();
		assertTrue(manager.getColumnConfigs().size() == 1);

		// now select it and delete
		when(mockColumnConfigEditor.isSelected()).thenReturn(true);
		manager.deleteSelected();
		assertTrue(manager.getColumnConfigs().size() == 0);
	}

	private APITableColumnConfigView setupColumnEditor(boolean isSelected) {
		APITableColumnConfigView mockSourceEditor = mock(APITableColumnConfigView.class);
		when(mockSourceEditor.isSelected()).thenReturn(isSelected);
		return mockSourceEditor;
	}

	@Test
	public void testMoveAndDelete() {
		manager.configure(configs);
		// set up 3 columns, where the second reports that it's selected
		APITableColumnConfigView s1 = setupColumnEditor(false);
		APITableColumnConfigView s2 = setupColumnEditor(true);
		APITableColumnConfigView s3 = setupColumnEditor(false);

		when(mockGinInjector.getAPITableColumnConfigView()).thenReturn(s1, s2, s3);
		// add the 3 columns
		manager.addColumnConfig();
		manager.addColumnConfig();
		manager.addColumnConfig();

		// check source order with move up, delete, and down
		List<APITableColumnConfigView> sourceEditors = manager.getColumnEditors();
		assertEquals(Arrays.asList(s1, s2, s3), sourceEditors);

		// move up clicked. move s2 to index 0
		manager.onMoveUp();
		assertEquals(Arrays.asList(s2, s1, s3), sourceEditors);

		manager.deleteSelected();
		assertEquals(Arrays.asList(s1, s3), sourceEditors);

		when(s1.isSelected()).thenReturn(true);
		manager.onMoveDown();
		assertEquals(Arrays.asList(s3, s1), sourceEditors);
	}

	@Test
	public void testSelectionToolbarState() {
		manager.configure(configs);
		// set up 2 columns, nothing selected
		APITableColumnConfigView s1 = setupColumnEditor(false);
		APITableColumnConfigView s2 = setupColumnEditor(false);

		when(mockGinInjector.getAPITableColumnConfigView()).thenReturn(s1, s2);
		// add the columns
		manager.addColumnConfig();
		manager.addColumnConfig();

		reset(mockView);
		manager.checkSelectionState();

		verify(mockView).setCanDelete(false);
		verify(mockView).setCanMoveUp(false);
		verify(mockView).setCanMoveDown(false);

		// 1 is selected, should now be able to move down or delete
		reset(mockView);
		when(s1.isSelected()).thenReturn(true);
		manager.checkSelectionState();
		verify(mockView).setCanDelete(true);
		verify(mockView).setCanMoveUp(false);
		verify(mockView).setCanMoveDown(true);

		// both are selected, should be able to delete only
		reset(mockView);
		when(s2.isSelected()).thenReturn(true);
		manager.checkSelectionState();
		verify(mockView).setCanDelete(true);
		verify(mockView).setCanMoveUp(false);
		verify(mockView).setCanMoveDown(false);

		// column 2 is selected, should be able to delete or move up
		reset(mockView);
		when(s1.isSelected()).thenReturn(false);
		manager.checkSelectionState();
		verify(mockView).setCanDelete(true);
		verify(mockView).setCanMoveUp(true);
		verify(mockView).setCanMoveDown(false);
	}

}
