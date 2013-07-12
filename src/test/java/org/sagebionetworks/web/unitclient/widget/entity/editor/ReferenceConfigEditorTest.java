package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigView;

public class ReferenceConfigEditorTest {
	ReferenceConfigEditor editor;
	ReferenceConfigView mockView;
	
	@Before
	public void setup(){
		mockView = mock(ReferenceConfigView.class);
		editor = new ReferenceConfigEditor(mockView);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testGetTextToInsert() {
		
	}
}
