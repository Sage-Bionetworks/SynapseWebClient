package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadataViewImpl extends Composite implements EntityMetadataView {

	interface EntityMetadataViewImplUiBinder extends UiBinder<Widget, EntityMetadataViewImpl> {
	}

	private static EntityMetadataViewImplUiBinder uiBinder = GWT.create(EntityMetadataViewImplUiBinder.class);

	@UiField
	HTMLPanel detailedMetadata;
	@UiField
	HTMLPanel dataUseContainer;
	@UiField
	TextBox idField;
	@UiField
	Span doiPanel;
	@UiField
	Collapse annotationsContent;
	@UiField
	SimplePanel annotationsContainer;
	@UiField
	Span containerItemCountContainer;
	@UiField
	Span restrictionPanelV2;
	@UiField
	Div fileHistoryContainer;
	@UiField
	Span uploadDestinationPanel;
	@UiField
	Span uploadDestinationField;
	@UiField
	Text annotationsTitleText;

	@Inject
	public EntityMetadataViewImpl(final SynapseJSNIUtils jsniUtils) {
		initWidget(uiBinder.createAndBindUi(this));
		idField.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				idField.selectAll();
			}
		});
	}

	@Override
	public void setContainerItemCountWidget(IsWidget w) {
		containerItemCountContainer.clear();
		containerItemCountContainer.add(w);
	}

	@Override
	public void setDoiWidget(IsWidget doiWidget) {
		doiPanel.clear();
		doiPanel.add(doiWidget);
	}

	@Override
	public void setAnnotationsRendererWidget(IsWidget annotationsWidget) {
		annotationsContainer.setWidget(annotationsWidget);
	}

	@Override
	public void setUploadDestinationPanelVisible(boolean isVisible) {
		uploadDestinationPanel.setVisible(isVisible);
	}

	@Override
	public void setUploadDestinationText(String text) {
		uploadDestinationField.setText(text);
	}

	@Override
	public void setAnnotationsVisible(boolean visible) {
		if (visible) {
			annotationsContent.show();
		} else {
			annotationsContent.hide();
		}
	}

	@Override
	public void setVersionHistoryWidget(IsWidget fileHistoryWidget) {
		fileHistoryContainer.clear();
		fileHistoryContainer.add(fileHistoryWidget);
	}

	@Override
	public void clear() {
		dataUseContainer.setVisible(false);
		annotationsContent.hide();
		uploadDestinationField.setText("");
		uploadDestinationPanel.setVisible(false);
	}

	@Override
	public void setDetailedMetadataVisible(boolean visible) {
		detailedMetadata.setVisible(visible);
	}

	@Override
	public void setEntityId(String text) {
		idField.setText(text);
	}

	@Override
	public void setRestrictionPanelVisible(boolean visible) {
		dataUseContainer.setVisible(visible);
	}

	@Override
	public void setRestrictionWidgetV2(IsWidget restrictionWidget) {
		restrictionPanelV2.clear();
		restrictionPanelV2.add(restrictionWidget);
	}

	@Override
	public void setRestrictionWidgetV2Visible(boolean visible) {
		restrictionPanelV2.setVisible(visible);
	}

	@Override
	public void setAnnotationsTitleText(String text) {
		annotationsTitleText.setText(text);
	}
}
