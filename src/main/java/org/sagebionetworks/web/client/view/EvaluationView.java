package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationView extends IsWidget, SynapseView {
	public void showPage(WikiPageKey wikiKey,  boolean canEdit);
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);	
	
	public interface Presenter extends SynapsePresenter {
		public void configure(String evaluationId);
	}

}
