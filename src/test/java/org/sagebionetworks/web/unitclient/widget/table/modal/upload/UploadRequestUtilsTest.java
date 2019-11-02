package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadRequestUtils;

public class UploadRequestUtilsTest {

	CsvTableDescriptor sampleCsvTableDescriptor;
	UploadToTableRequest sampleUploadToTableRequest;
	UploadToTablePreviewRequest sampleUploadToTablePreviewRequest;

	@Before
	public void before() {
		sampleCsvTableDescriptor = new CsvTableDescriptor();
		sampleCsvTableDescriptor.setEscapeCharacter("escapse");
		sampleCsvTableDescriptor.setIsFirstLineHeader(Boolean.TRUE);
		sampleCsvTableDescriptor.setLineEnd("lineEnd");
		sampleCsvTableDescriptor.setQuoteCharacter("quoteChar");
		sampleCsvTableDescriptor.setSeparator("separator");

		sampleUploadToTableRequest = new UploadToTableRequest();
		sampleUploadToTableRequest.setCsvTableDescriptor(sampleCsvTableDescriptor);
		sampleUploadToTableRequest.setLinesToSkip(3L);
		sampleUploadToTableRequest.setTableId("syn123");
		sampleUploadToTableRequest.setUpdateEtag("etag");
		sampleUploadToTableRequest.setUploadFileHandleId("789");

		sampleUploadToTablePreviewRequest = new UploadToTablePreviewRequest();
		sampleUploadToTablePreviewRequest.setCsvTableDescriptor(sampleCsvTableDescriptor);
		sampleUploadToTablePreviewRequest.setDoFullFileScan(Boolean.TRUE);
		sampleUploadToTablePreviewRequest.setLinesToSkip(sampleUploadToTableRequest.getLinesToSkip());
		sampleUploadToTablePreviewRequest.setUploadFileHandleId(sampleUploadToTableRequest.getUploadFileHandleId());
	}

	@Test
	public void testCsvTableDescriptorNull() {
		CsvTableDescriptor in = null;
		CsvTableDescriptor clone = UploadRequestUtils.cloneCsvTableDescriptor(in);
		assertEquals(in, clone);
	}

	@Test
	public void testCloneCsvTableDescriptorEmpty() {
		CsvTableDescriptor in = new CsvTableDescriptor();
		CsvTableDescriptor clone = UploadRequestUtils.cloneCsvTableDescriptor(in);
		assertEquals(in, clone);
		assertFalse("The clone should be a new object", in == clone);
	}

	@Test
	public void testCloneCsvTableDescriptor() {
		CsvTableDescriptor in = sampleCsvTableDescriptor;
		CsvTableDescriptor clone = UploadRequestUtils.cloneCsvTableDescriptor(in);
		assertEquals(in, clone);
		assertFalse("The clone should be a new object", in == clone);
	}

	@Test
	public void testCloneUploadToTableRequestNull() {
		UploadToTableRequest in = null;
		UploadToTableRequest clone = UploadRequestUtils.cloneUploadToTableRequest(in);
		assertEquals(in, clone);
	}

	@Test
	public void testCloneUploadToTableRequestEmpty() {
		UploadToTableRequest in = new UploadToTableRequest();
		UploadToTableRequest clone = UploadRequestUtils.cloneUploadToTableRequest(in);
		assertEquals(in, clone);
		assertFalse("The clone should be a new object", in == clone);
	}

	@Test
	public void testCloneUploadToTableRequest() {
		UploadToTableRequest in = sampleUploadToTableRequest;
		UploadToTableRequest clone = UploadRequestUtils.cloneUploadToTableRequest(in);
		assertEquals(in, clone);
		assertFalse("The clone should be a new object", in == clone);
	}

	@Test
	public void testCreateFromPreviewNull() {
		UploadToTableRequest clone = UploadRequestUtils.createFromPreview(null);
		assertEquals(null, clone);
	}

	@Test
	public void testCreateFromPreview() {
		UploadToTableRequest clone = UploadRequestUtils.createFromPreview(sampleUploadToTablePreviewRequest);
		// The etag and tableId will not be copied.
		sampleUploadToTableRequest.setUpdateEtag(null);
		sampleUploadToTableRequest.setTableId(null);
		assertEquals(sampleUploadToTableRequest, clone);
		assertFalse("The clone should be a new object", sampleUploadToTableRequest == clone);
	}

}
