package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiAttachmentsView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void configure(WikiPageKey wikiKey);

		void deleteAttachment(String fileName);

		void setSelectedFilename(String fileName);
	}

	void addFileHandles(List<FileHandle> list);

	void showNoAttachmentRow();

	void reset();

	void setSelectedFilename(String fileName);

}
