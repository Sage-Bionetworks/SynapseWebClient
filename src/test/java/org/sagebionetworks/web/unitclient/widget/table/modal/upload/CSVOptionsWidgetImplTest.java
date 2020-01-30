package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsView;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.Delimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.EscapeCharacter;

@RunWith(MockitoJUnitRunner.class)
public class CSVOptionsWidgetImplTest {
	@Mock
	Callback mockHandler;
	@Mock
	CSVOptionsView mockView;
	@Mock
	CSVOptionsWidget widget;

	String fileHandleId;

	@Before
	public void before() {
		fileHandleId = "123456";
		widget = new CSVOptionsWidget(mockView);
	}

	@Test
	public void testOptionsRoundTripOther() {
		String separatorValue = "p";
		String escapeCharacter = "`";
		UploadToTablePreviewRequest inRequest = new UploadToTablePreviewRequest();
		CsvTableDescriptor csvTableDescriptor = new CsvTableDescriptor();
		csvTableDescriptor.setSeparator(separatorValue);
		csvTableDescriptor.setEscapeCharacter(escapeCharacter);
		csvTableDescriptor.setIsFirstLineHeader(true);
		inRequest.setCsvTableDescriptor(csvTableDescriptor);
		inRequest.setUploadFileHandleId(fileHandleId);
		inRequest.setDoFullFileScan(true);

		widget.configure(inRequest, mockHandler);

		// set up expected view responses
		verify(mockView).setSeparator(Delimiter.OTHER);
		verify(mockView).setOtherSeparatorValue(separatorValue);
		verify(mockView).setEscapeCharacter(EscapeCharacter.OTHER);
		verify(mockView).setOtherEscapeCharacterValue(escapeCharacter);
		when(mockView.getEscapeCharacter()).thenReturn(EscapeCharacter.OTHER);
		when(mockView.getSeparator()).thenReturn(Delimiter.OTHER);
		when(mockView.getOtherSeparatorValue()).thenReturn(separatorValue);
		when(mockView.getOtherEscapeCharacterValue()).thenReturn(escapeCharacter);
		when(mockView.getIsFristLineHeader()).thenReturn(true);

		UploadToTablePreviewRequest outRequest = widget.getCurrentOptions();
		assertNotNull(outRequest);
		assertEquals(inRequest, outRequest);
	}

	@Test
	public void testOptionsRoundTripCommaBackslash() {
		String separator = ",";
		String escapeCharacter = "\\";
		UploadToTablePreviewRequest inRequest = new UploadToTablePreviewRequest();
		CsvTableDescriptor csvTableDescriptor = new CsvTableDescriptor();
		csvTableDescriptor.setIsFirstLineHeader(false);
		csvTableDescriptor.setSeparator(separator);
		csvTableDescriptor.setEscapeCharacter(escapeCharacter);
		inRequest.setCsvTableDescriptor(csvTableDescriptor);
		inRequest.setUploadFileHandleId(fileHandleId);

		widget.configure(inRequest, mockHandler);

		verify(mockView).setSeparator(Delimiter.CSV);
		verify(mockView).setEscapeCharacter(EscapeCharacter.BACKSLASH);
		when(mockView.getEscapeCharacter()).thenReturn(EscapeCharacter.BACKSLASH);
		when(mockView.getSeparator()).thenReturn(Delimiter.CSV);
		when(mockView.getIsFristLineHeader()).thenReturn(false);

		UploadToTablePreviewRequest outRequest = widget.getCurrentOptions();
		assertNotNull(outRequest);
		assertEquals(inRequest, outRequest);
	}
}
