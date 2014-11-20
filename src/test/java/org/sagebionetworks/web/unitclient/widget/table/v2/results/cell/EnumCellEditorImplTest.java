package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumCellEditorView;

public class EnumCellEditorImplTest {
	
	EnumCellEditorView mockView;
	EnumCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(EnumCellEditorView.class);
		editor = new EnumCellEditorImpl(mockView);
	}

	@Test
	public void testConfigure(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		for(String value: values){
			verify(mockView).addOption(value);
		}
	}
}
