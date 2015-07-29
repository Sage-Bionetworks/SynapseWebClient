package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityBundle;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityMetadataView extends IsWidget {

	public void setPresenter(Presenter p);
	
	public void setDetailedMetadataVisible(boolean visible);
	
	public void setEntityNameVisible(boolean visible);
	
	void setAnnotationsVisible(boolean visible);
	
	public interface Presenter {
		
		void fireEntityUpdatedEvent();
		
	}
	
	void setFileHistoryVisible(boolean visible);

	void setFileHistoryWidget(IsWidget fileHistoryWidget);

	public void setFavoriteWidget(IsWidget favoriteWidget);

	public void setDoiWidget(IsWidget doiWidget);

	public void setAnnotationsRendererWidget(IsWidget annotationsWidget);

	void setRestrictionWidget(RestrictionWidget restrictionWidget);

	void clear();

	void setEntityBundle(EntityBundle bundle, Long versionNumber);

}
