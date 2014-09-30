package org.sagebionetworks.web.unitclient.widget.table.modal;

import org.junit.Before;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.modal.UploadTableModalView;
import org.sagebionetworks.web.client.widget.table.modal.UploadTableModalWidgetImpl;

/**
 * 
 * @author jhill
 *
 */
public class UploadTableModalWidgetImplTest {
	
	UploadTableModalView mockView;
	UploadTableModalWidgetImpl widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(UploadTableModalView.class);
		widget = new UploadTableModalWidgetImpl(mockView);
	}

}
