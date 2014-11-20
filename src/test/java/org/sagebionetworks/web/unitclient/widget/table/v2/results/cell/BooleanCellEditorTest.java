package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumCellEditorView;

public class BooleanCellEditorTest {

	EnumCellEditorView mockView;
	BooleanCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(EnumCellEditorView.class);
		editor = new BooleanCellEditorImpl(mockView);
	}

	@Test
	public void testConfigure(){
		verify(mockView).addOption("true");
		verify(mockView).addOption("false");
	}
}
