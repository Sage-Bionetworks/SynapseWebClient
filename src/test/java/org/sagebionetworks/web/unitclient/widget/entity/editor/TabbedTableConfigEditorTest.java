package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigView;

public class TabbedTableConfigEditorTest {

	TabbedTableConfigEditor editor;
	TabbedTableConfigView mockView;

	@Before
	public void setup() {
		mockView = mock(TabbedTableConfigView.class);
		editor = new TabbedTableConfigEditor(mockView);
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testGetTextToInsert() {
		when(mockView.getTableContents()).thenReturn("a\tb\tc\td\nd\te\tf\tg");
		String md = editor.getTextToInsert();
		assertEquals("a|b|c|d\nd|e|f|g", md);
	}
}
