package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManager;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManagerView;

public class APITableColumnManagerTest {
		
	APITableColumnManager manager;
	APITableColumnManagerView mockView;
	String defaultInputColumnName;
	String defaultRendererName;
	Set<String> defaultInputColumnNamesSet;
	
	@Before
	public void setup(){
		mockView = mock(APITableColumnManagerView.class);
		manager = new APITableColumnManager(mockView);
		List<APITableColumnConfig> configs = new ArrayList<APITableColumnConfig>();
		manager.configure(configs);
		defaultInputColumnName = "myTestColumn";
		defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE;
		defaultInputColumnNamesSet = new HashSet<String>();
		defaultInputColumnNamesSet.add(defaultInputColumnName);
		reset(mockView);
	}
	
	@Test
	public void testAsWidget() {
		manager.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testAddDelete() {
		//before assert it's empty
		assertTrue(manager.getColumnConfigs().isEmpty());  //sanity test
		//if we only have a single input column, the display column name should be called that by default
		manager.addColumnConfig(defaultRendererName, defaultInputColumnName, null, null);
		assertTrue(manager.getColumnConfigs().size() == 1);  //sanity test
		APITableColumnConfig newConfig = manager.getColumnConfigs().get(0);
		verify(mockView).configure(anyList());
		assertEquals(defaultInputColumnNamesSet, newConfig.getInputColumnNames());
		assertEquals(defaultRendererName, newConfig.getRendererFriendlyName());
		assertEquals(defaultInputColumnName, newConfig.getDisplayColumnName());
		assertEquals(COLUMN_SORT_TYPE.NONE, newConfig.getSort());
		
		//now try deleting the thing
		manager.deleteColumnConfig(newConfig);
		assertTrue(manager.getColumnConfigs().isEmpty());  //should be empty again
		verify(mockView, Mockito.times(2)).configure(anyList());
	}
	
	@Test
	public void testAddColumnConfigWithDisplayName() {
		String myDisplayColumnName = "My Test";
		assertTrue(manager.getColumnConfigs().isEmpty());  //sanity test
		//if we only have a single input column, the display column name should be called that by default
		manager.addColumnConfig(defaultRendererName, defaultInputColumnName, myDisplayColumnName, COLUMN_SORT_TYPE.ASC);
		assertTrue(manager.getColumnConfigs().size() == 1);  //sanity test
		APITableColumnConfig newConfig = manager.getColumnConfigs().get(0);
		assertEquals(myDisplayColumnName, newConfig.getDisplayColumnName());
		assertEquals(COLUMN_SORT_TYPE.ASC, newConfig.getSort());
	}
	
	@Test
	public void testSortTypeDesc() {
		String myDisplayColumnName = "My Test";
		manager.addColumnConfig(defaultRendererName, defaultInputColumnName, myDisplayColumnName, COLUMN_SORT_TYPE.DESC);
		APITableColumnConfig newConfig = manager.getColumnConfigs().get(0);
		assertEquals(COLUMN_SORT_TYPE.DESC, newConfig.getSort());
	}

}
