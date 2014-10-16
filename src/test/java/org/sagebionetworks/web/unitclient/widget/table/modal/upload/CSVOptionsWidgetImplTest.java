package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import org.junit.Before;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsView;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidgetImpl;

public class CSVOptionsWidgetImplTest {
	
	CSVOptionsView mockView;
	CSVOptionsWidgetImpl widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(CSVOptionsView.class);
		widget = new CSVOptionsWidgetImpl(mockView);
	}

}
