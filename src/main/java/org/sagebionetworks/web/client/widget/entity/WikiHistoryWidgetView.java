package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView.Presenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiHistoryWidgetView extends IsWidget, SynapseView{
	/**
	 * Set the presenter.
	 * @param presenter
	 * @param wikiPageViewPresenter TODO
	 */
	public void setPresenter(Presenter presenter, WikiPageWidgetView.Presenter  wikiPageViewPresenter);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		public void configure(final WikiPageKey key, final boolean canEdit, final WikiPageWidgetView.Presenter wikiPagePresenter);
		public void hideHistory();
	}
	
	public void configure(boolean canEdit, List<JSONEntity> historyAsList, WikiPageWidgetView.Presenter wikiPageWidgetPresenter);
	public void hideHistory();
}
