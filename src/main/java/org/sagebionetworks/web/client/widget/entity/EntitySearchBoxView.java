package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * 
 * @author dburdick
 *
 */
public interface EntitySearchBoxView extends IsWidget, SynapseWidgetView {
	
	public interface Presenter {				
		void search(String search);
		
		void entitySelected(String entityId, String name);		
	}

	void setPresenter(Presenter presenter);
	
	void build(int width);

	void setSearchResults(SearchResults results);

	void clearSelection();
}
