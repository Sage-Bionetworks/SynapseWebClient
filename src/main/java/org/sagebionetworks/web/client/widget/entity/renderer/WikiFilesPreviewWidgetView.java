package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiFilesPreviewWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void configure(WikiPageKey wikiKey, List<FileHandle> list);

	public void showErrorMessage(String error);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
