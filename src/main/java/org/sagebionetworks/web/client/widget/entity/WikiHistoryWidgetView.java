package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView.Presenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiHistoryWidgetView extends IsWidget, SynapseView{
	/**
	 * Set the presenter.
	 * @param presenter
	 * @param wikiPageViewPresenter TODO
	 */
	public void setPresenter(Presenter presenter, Presenter wikiPageViewPresenter);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		public void configure(final WikiPageKey key, final boolean canEdit, final WikiPageWidgetView.Presenter wikiPagePresenter);
		
	}
	
	public void configure(boolean canEdit, List<V2WikiHistorySnapshot> historyAsList, WikiPageWidgetView.Presenter wikiPageWidgetPresenter);
	
}
