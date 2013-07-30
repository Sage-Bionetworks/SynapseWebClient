package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;

import com.google.gwt.user.client.ui.IsWidget;

public interface MyEvaluationEntitiesListView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure(List<EntityHeader> evaluations);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
