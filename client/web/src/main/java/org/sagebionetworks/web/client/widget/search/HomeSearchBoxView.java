package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

public interface HomeSearchBoxView extends IsWidget, SynapseWidgetView {

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

		PlaceChanger getPlaceChanger();

		void search(String value);
		
		void searchAllProjects();
		
		void searchAllData();
		
		void searchAllStudies();
		
		void searchAllCode();
		
	}


}
