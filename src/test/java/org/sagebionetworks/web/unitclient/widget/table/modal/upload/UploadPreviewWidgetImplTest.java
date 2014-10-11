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
	
	@Test
	public void testPreProcessColumns(){
		// input
		ColumnModel one = new ColumnModel();
		one.setMaximumSize(100L);
		one.setColumnType(ColumnType.STRING);
		ColumnModel two = new ColumnModel();
		two.setMaximumSize(null);
		two.setColumnType(ColumnType.STRING);
		List<ColumnModel> list = Arrays.asList(one, two);
		UploadToTablePreviewResult preview = new UploadToTablePreviewResult();
		preview.setSuggestedColumns(list);
		widget.configure(new UploadToTablePreviewRequest(), preview);
		
		// the call under test
		List<ColumnModel> results = widget.getCurrentModel();
		// expected
		ColumnModel oneExpected = new ColumnModel();
		oneExpected.setColumnType(ColumnType.STRING);
		// size should be increased by the buffer
		oneExpected.setMaximumSize((long)(100+(100*UploadPreviewWidgetImpl.COLUMN_SIZE_BUFFER)));
		ColumnModel twoExpected = new ColumnModel();
		twoExpected.setColumnType(ColumnType.STRING);
		twoExpected.setMaximumSize(null);
		List<ColumnModel> expected = Arrays.asList(oneExpected, twoExpected);
		assertEquals(expected, results);
	}

}
