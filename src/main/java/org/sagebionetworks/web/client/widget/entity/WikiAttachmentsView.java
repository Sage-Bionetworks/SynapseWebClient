package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiAttachmentsView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void configure(WikiPageKey wikiKey, WikiPage wikiPage, Callback callback);
		/**
		 * Delete attachment
		 */
		void deleteAttachment(String fileName);
		void attachmentClicked(final String fileName);
		void setAttachmentColumnWidth(int width);
	}

	public void configure(WikiPageKey wikiKey, List<FileHandle> list);
	public void setAttachmentColumnWidth(int width);
	public void attachmentDeleted(String fileHandleId);
}
