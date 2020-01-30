package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;

/**
 * Utility methods for UploadToTableRequests.
 * 
 * @author jhill
 *
 */
public class UploadRequestUtils {

	/**
	 * Create a deep clone of an UploadToTableRequest
	 * 
	 * @param in
	 * @return
	 */
	public static UploadToTableRequest cloneUploadToTableRequest(UploadToTableRequest in) {
		if (in == null) {
			return null;
		}
		UploadToTableRequest clone = new UploadToTableRequest();
		clone.setCsvTableDescriptor(cloneCsvTableDescriptor(in.getCsvTableDescriptor()));
		clone.setLinesToSkip(in.getLinesToSkip());
		clone.setTableId(in.getTableId());
		clone.setUpdateEtag(in.getUpdateEtag());
		clone.setUploadFileHandleId(in.getUploadFileHandleId());
		return clone;
	}

	/**
	 * Create a deep clone of a CsvTableDescriptor
	 * 
	 * @param in
	 * @return
	 */
	public static CsvTableDescriptor cloneCsvTableDescriptor(CsvTableDescriptor in) {
		if (in == null) {
			return null;
		}
		CsvTableDescriptor clone = new CsvTableDescriptor();
		clone.setEscapeCharacter(in.getEscapeCharacter());
		clone.setIsFirstLineHeader(in.getIsFirstLineHeader());
		clone.setLineEnd(in.getLineEnd());
		clone.setQuoteCharacter(in.getQuoteCharacter());
		clone.setSeparator(in.getSeparator());
		return clone;
	}

	/**
	 * The UploadToTableRequest and UploadToTablePreviewRequest are similar. This method will create a
	 * UploadToTableRequest from an UploadToTablePreviewRequest.
	 * 
	 * @param preview
	 * @return
	 */
	public static UploadToTableRequest createFromPreview(UploadToTablePreviewRequest preview) {
		if (preview == null) {
			return null;
		}
		UploadToTableRequest out = new UploadToTableRequest();
		out.setCsvTableDescriptor(cloneCsvTableDescriptor(preview.getCsvTableDescriptor()));
		out.setLinesToSkip(preview.getLinesToSkip());
		out.setUploadFileHandleId(preview.getUploadFileHandleId());
		return out;
	}
}
