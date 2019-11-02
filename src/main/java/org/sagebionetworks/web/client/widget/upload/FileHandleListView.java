package org.sagebionetworks.web.client.widget.upload;

import java.util.List;
import org.sagebionetworks.web.client.widget.CheckBoxState;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FileHandleListView extends IsWidget {

	public interface Presenter {
		List<String> getFileHandleIds();

		void deleteSelected();

		void selectNone();

		void selectAll();
	}

	void setPresenter(Presenter presenter);

	void setToolbarVisible(boolean visible);

	void setUploadWidget(Widget widget);

	void setUploadWidgetVisible(boolean visible);

	void addFileLink(Widget fileLinkWidget);

	void clearFileLinks();

	void setCanDelete(boolean canDelete);

	void setSelectionState(CheckBoxState selectionState);
}
