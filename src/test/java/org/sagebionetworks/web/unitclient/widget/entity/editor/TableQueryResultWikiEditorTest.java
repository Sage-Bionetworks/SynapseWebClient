package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.TableQueryResultWikiEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TableQueryResultWikiView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class TableQueryResultWikiEditorTest {

	TableQueryResultWikiEditor editor;
	TableQueryResultWikiView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);

	@Before
	public void setup() {
		mockView = mock(TableQueryResultWikiView.class);
		editor = new TableQueryResultWikiEditor(mockView);
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() {
		String query = "SELECT * FROM syn54321 ORDER BY \"age\" ASC";
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, query);
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setSql(query);
		// query visible param results in showing the query by default
		verify(mockView).setQueryVisible(true);
	}

	@Test
	public void testConfigureQueryVisible() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.QUERY_VISIBLE, Boolean.TRUE.toString());
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setQueryVisible(true);
	}

	@Test
	public void testConfigureQueryNotVisible() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.QUERY_VISIBLE, Boolean.FALSE.toString());
		editor.configure(wikiKey, descriptor, null);
		verify(mockView).setQueryVisible(false);
	}

	@Test
	public void testUpdateDescriptorFromView() {
		String query = "SELECT * FROM syn54321 ORDER BY \"age\" ASC";
		when(mockView.getSql()).thenReturn(query);
		when(mockView.isQueryVisible()).thenReturn(true);
		editor.configure(wikiKey, new HashMap<String, String>(), null);
		editor.updateDescriptorFromView();
		verify(mockView).getSql();
		verify(mockView).isQueryVisible();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidQueryFromView() {
		String query = "";
		when(mockView.getSql()).thenReturn(query);
		editor.configure(wikiKey, new HashMap<String, String>(), null);
		editor.updateDescriptorFromView();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullQueryFromView() {
		String query = null;
		when(mockView.getSql()).thenReturn(query);
		editor.configure(wikiKey, new HashMap<String, String>(), null);
		editor.updateDescriptorFromView();
	}
}
