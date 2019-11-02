package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigView;

public class LinkConfigEditorTest {

	LinkConfigEditor editor;
	LinkConfigView mockView;

	@Before
	public void setup() {
		mockView = mock(LinkConfigView.class);
		editor = new LinkConfigEditor(mockView);
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testGetTextToInsert() {
		when(mockView.getLinkUrl()).thenReturn("http://synapse.sagebase.org/");
		when(mockView.getName()).thenReturn("Synapse");
		String md = editor.getTextToInsert();
		assertEquals("[Synapse](http://synapse.sagebase.org/)", md);
	}
}
