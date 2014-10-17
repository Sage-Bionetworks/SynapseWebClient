package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidgetImpl;



public class UploadPreviewWidgetImplTest {
	
	UploadPreviewView mockView;
	UploadPreviewWidgetImpl widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(UploadPreviewView.class);
		widget = new UploadPreviewWidgetImpl(mockView);
	}
	


}
