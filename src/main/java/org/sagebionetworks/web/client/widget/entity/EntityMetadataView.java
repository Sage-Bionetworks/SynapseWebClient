package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityMetadataView extends IsWidget {

	public void setDetailedMetadataVisible(boolean visible);
		
	void setAnnotationsVisible(boolean visible);
	
	public interface Presenter {
	}
	
	void setFileHistoryVisible(boolean visible);

	void setFileHistoryWidget(IsWidget fileHistoryWidget);

	public void setDoiWidget(IsWidget doiWidget);

	public void setAnnotationsRendererWidget(IsWidget annotationsWidget);

	void clear();

	public void setRestrictionPanelVisible(boolean visible);

	void setRestrictionWidgetV2(IsWidget restrictionWidget);

	void setEntityId(String text);

	void setUploadDestinationPanelVisible(boolean isVisible);

	void setUploadDestinationText(String text);
	void setRestrictionWidgetV2Visible(boolean visible);
	void setAnnotationsTitleText(String text);
}