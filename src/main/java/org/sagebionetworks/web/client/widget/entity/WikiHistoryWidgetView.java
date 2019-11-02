package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiHistoryWidgetView extends IsWidget, SynapseView {
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
		public void configure(final WikiPageKey key, final boolean canEdit, ActionHandler actionHandler);

		public void configureNextPage(Long offset, Long limit);

		public String getNameForUserId(String userId);
	}

	public void configure(boolean canEdit, ActionHandler actionHandler);

	public void updateHistoryList(List<V2WikiHistorySnapshot> historyResults);

	public void buildHistoryWidget();

	public void hideHistoryWidget();

	public void showHistoryWidget();

	void hideLoadMoreButton();

	void setSynAlert(IsWidget w);
}
