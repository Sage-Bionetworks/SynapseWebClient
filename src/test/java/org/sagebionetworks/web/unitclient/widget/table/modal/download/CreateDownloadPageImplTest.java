package org.sagebionetworks.web.unitclient.widget.table.modal.download;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.web.client.widget.table.modal.download.CreateDownloadPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.CreateDownloadPageView;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadFilePage;
import org.sagebionetworks.web.client.widget.table.modal.download.FileType;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

public class CreateDownloadPageImplTest {

	CreateDownloadPageView mockView;
	JobTrackingWidgetStub jobTrackingWidgetStub;
	DownloadFilePage mockNextPage;
	ModalPresenter mockModalPresenter;
	CreateDownloadPageImpl page;
	String sql;
	
	@Before
	public void before(){
		mockView = Mockito.mock(CreateDownloadPageView.class);
		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockNextPage = Mockito.mock(DownloadFilePage.class);
		mockModalPresenter = Mockito.mock(ModalPresenter.class);
		page = new CreateDownloadPageImpl(mockView, jobTrackingWidgetStub, mockNextPage);
		sql = "select * from syn123";
		page.configure(sql);
	}
	
	@Test
	public void testSetModalPresenter(){
		// This is the main entry to the page
		page.setModalPresenter(mockModalPresenter);
		verify(mockModalPresenter).setPrimaryButtonText(CreateDownloadPageImpl.NEXT);
		verify(mockView).setFileType(FileType.CSV);
		verify(mockView).setIncludeHeaders(true);
		verify(mockView).setIncludeRowMetadata(true);
		verify(mockView).setTrackerVisible(false);
	}
	
	@Test
	public void testgetDownloadFromTableRequest(){
		page.setModalPresenter(mockModalPresenter);
		DownloadFromTableRequest expected = new DownloadFromTableRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		descriptor.setSeparator("\t");
		expected.setCsvTableDescriptor(descriptor);
		expected.setIncludeRowIdAndRowVersion(false);
		expected.setSql(sql);
		expected.setWriteHeader(true);
		when(mockView.getFileType()).thenReturn(FileType.TSV);
		when(mockView.getIncludeHeaders()).thenReturn(true);
		when(mockView.getIncludeRowMetadata()).thenReturn(false);
		
		DownloadFromTableRequest request = page.getDownloadFromTableRequest();
		assertEquals(expected, request);
	}
	
	@Test
	public void testOnPrimarySuccess(){
		page.setModalPresenter(mockModalPresenter);
		when(mockView.getFileType()).thenReturn(FileType.TSV);
		when(mockView.getIncludeHeaders()).thenReturn(true);
		when(mockView.getIncludeRowMetadata()).thenReturn(false);
	
		String fileHandle = "45678";
		DownloadFromTableResult results = new DownloadFromTableResult();
		results.setResultsFileHandleId(fileHandle);
	
		jobTrackingWidgetStub.setResponse(results);
		page.onPrimary();
		verify(mockModalPresenter).setLoading(true);
		verify(mockView).setTrackerVisible(true);
		verify(mockNextPage).configure(fileHandle);
		verify(mockModalPresenter).setNextActivePage(mockNextPage);
	}
	
	@Test
	public void testOnPrimaryCancel(){
		page.setModalPresenter(mockModalPresenter);
		when(mockView.getFileType()).thenReturn(FileType.TSV);
		when(mockView.getIncludeHeaders()).thenReturn(true);
		when(mockView.getIncludeRowMetadata()).thenReturn(false);
	
		String fileHandle = "45678";
		DownloadFromTableResult results = new DownloadFromTableResult();
		results.setResultsFileHandleId(fileHandle);
	
		jobTrackingWidgetStub.setOnCancel(true);
		page.onPrimary();
		verify(mockModalPresenter).setLoading(true);
		verify(mockView).setTrackerVisible(true);
		verify(mockNextPage, never()).configure(fileHandle);
		verify(mockModalPresenter).onCancel();
	}
	
	@Test
	public void testOnPrimaryFailure(){
		page.setModalPresenter(mockModalPresenter);
		when(mockView.getFileType()).thenReturn(FileType.TSV);
		when(mockView.getIncludeHeaders()).thenReturn(true);
		when(mockView.getIncludeRowMetadata()).thenReturn(false);
	
		String fileHandle = "45678";
		DownloadFromTableResult results = new DownloadFromTableResult();
		results.setResultsFileHandleId(fileHandle);
		String error = "failure";
		jobTrackingWidgetStub.setError(new Throwable(error));
		page.onPrimary();
		verify(mockModalPresenter).setLoading(true);
		verify(mockView).setTrackerVisible(true);
		verify(mockNextPage, never()).configure(fileHandle);
		verify(mockModalPresenter).setErrorMessage(error);
	}
}
