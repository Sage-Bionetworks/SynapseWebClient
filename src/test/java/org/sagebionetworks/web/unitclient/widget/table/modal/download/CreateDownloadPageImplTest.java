package org.sagebionetworks.web.unitclient.widget.table.modal.download;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
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
	String tableId;
	List<FacetColumnRequest> selectedFacets;
	@Mock
	FacetColumnRequest mockFacetColumnRequest;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		mockView = Mockito.mock(CreateDownloadPageView.class);
		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockNextPage = Mockito.mock(DownloadFilePage.class);
		mockModalPresenter = Mockito.mock(ModalPresenter.class);
		selectedFacets = new ArrayList<FacetColumnRequest>();
		page = new CreateDownloadPageImpl(mockView, jobTrackingWidgetStub, mockNextPage);
		tableId = "syn123";
		sql = "select * from " + tableId;
		page.configure(sql, tableId, selectedFacets);
	}

	@Test
	public void testSetModalPresenter() {
		// This is the main entry to the page
		page.setModalPresenter(mockModalPresenter);
		verify(mockModalPresenter).setPrimaryButtonText(CreateDownloadPageImpl.NEXT);
		verify(mockView).setFileType(FileType.CSV);
		verify(mockView).setIncludeHeaders(true);
		verify(mockView).setIncludeRowMetadata(true);
		verify(mockView).setTrackerVisible(false);
	}

	@Test
	public void testgetDownloadFromTableRequest() {
		selectedFacets.add(mockFacetColumnRequest);
		page.setModalPresenter(mockModalPresenter);
		DownloadFromTableRequest expected = new DownloadFromTableRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		descriptor.setSeparator("\t");
		expected.setCsvTableDescriptor(descriptor);
		expected.setIncludeRowIdAndRowVersion(false);
		expected.setSql(sql);
		expected.setWriteHeader(true);
		expected.setSelectedFacets(selectedFacets);
		when(mockView.getFileType()).thenReturn(FileType.TSV);
		when(mockView.getIncludeHeaders()).thenReturn(true);
		when(mockView.getIncludeRowMetadata()).thenReturn(false);

		DownloadFromTableRequest request = page.getDownloadFromTableRequest();
		assertEquals(expected, request);
	}

	@Test
	public void testOnPrimarySuccess() {
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
	public void testOnPrimaryCancel() {
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
	public void testOnPrimaryFailure() {
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
