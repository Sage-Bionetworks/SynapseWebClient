package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.ShowsErrors;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiPageDeleteConfirmationDialogView extends IsWidget, ShowsErrors {
	void setPresenter(Presenter presenter);

	void showModal(String wikiPageId, Map<String, V2WikiHeader> id2HeaderMap, Map<String, List<V2WikiHeader>> id2ChildrenMap);

	void showInfo(String message);

	public interface Presenter {
		void onDeleteWiki();
	}
}
