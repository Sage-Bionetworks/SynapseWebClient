package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface HomeSearchBoxView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * resets the view to default state
	 */
	public void clear();

	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void search(String value);
		
		String getSearchAllProjectsLink();
		
		String getSearchAllDataLink();
		
		String getSearchAllStudiesLink();
		
		String getSearchAllCodeLink();

		void setSearchAll(boolean searchAll);
		
	}


}
