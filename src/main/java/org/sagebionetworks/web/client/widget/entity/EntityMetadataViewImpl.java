package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Collapse;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadataViewImpl extends Composite implements EntityMetadataView {
	
	interface EntityMetadataViewImplUiBinder extends UiBinder<Widget, EntityMetadataViewImpl> {
	}
	
	private static EntityMetadataViewImplUiBinder uiBinder = GWT
			.create(EntityMetadataViewImplUiBinder.class);

	@UiField
	HTMLPanel entityNamePanel;
	@UiField
	HTMLPanel detailedMetadata;
	@UiField
	HTMLPanel dataUseContainer;
	@UiField
	Image entityIcon;
	@UiField
	SpanElement entityName;
	@UiField
	TextBox idField;
	@UiField
	SimplePanel favoritePanel;
	@UiField
	SimplePanel doiPanel;
	@UiField
	Collapse annotationsContent;
	@UiField
	SimplePanel annotationsContainer;
	@UiField
	Collapse fileHistoryContent;
	@UiField
	SimplePanel fileHistoryContainer;
	
	private RestrictionWidget restrictionWidget;
	private Presenter presenter;
	
	@Inject
	public EntityMetadataViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		annotationsContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.ANNOTATIONS);
		fileHistoryContainer.getElement().setAttribute("highlight-box-title", "File History");
		idField.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				idField.selectAll();
			}
		});

	}

	@Override
	public void setFavoriteWidget(IsWidget favoriteWidget) {
		favoritePanel.setWidget(favoriteWidget);
	}

	@Override
	public void setDoiWidget(IsWidget doiWidget) {
		doiPanel.setWidget(doiWidget);
	}

	@Override
	public void setAnnotationsRendererWidget(IsWidget annotationsWidget) {
		annotationsContainer.setWidget(annotationsWidget);		
	}

	@Override
	public void setRestrictionWidget(RestrictionWidget restrictionWidget) {
		this.restrictionWidget = restrictionWidget;
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
	public void setFileHistoryWidget(IsWidget fileHistoryWidget) {
		fileHistoryContainer.setWidget(fileHistoryWidget);
	}
	
	@Override
	public void setFileHistoryVisible(boolean visible) {
		if (visible) {
			fileHistoryContent.show();
		} else {
			fileHistoryContent.hide();
		}
	}
	
	@Override
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		clear();
		Entity e = bundle.getEntity();
		restrictionWidget.configure(bundle, true, false, true, new Callback() {
			@Override
			public void invoke() {
				presenter.fireEntityUpdatedEvent();
			}
		});
//		AbstractImagePrototype synapseIconForEntity = AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(e, DisplayUtils.IconSize.PX24, icons));
//		synapseIconForEntity.applyTo(entityIcon);
		setEntityName(e.getName());
		setEntityId(e.getId());
		boolean isShowingOlderVersion = versionNumber == null;
		configureFileHistory(isShowingOlderVersion);
		configureAnnotations();
		configureRestrictionWidget();
	}
	
	private void configureFileHistory(boolean isShowingOlderVersion) {
		if (fileHistoryContent.isShown() && !isShowingOlderVersion) {
			fileHistoryContent.toggle();
		}
	}
	
	private void configureAnnotations() {
		if (annotationsContent.isShown()) {
			annotationsContent.toggle();
		}
	}
	
	private void configureRestrictionWidget() {
		dataUseContainer.clear();
		Widget dataUse = restrictionWidget.asWidget();
		if(dataUse != null) {
			dataUseContainer.setVisible(true);
			dataUseContainer.add(new InlineHTML("<span style=\"margin-right: 5px;\" class=\"boldText\">"+DisplayConstants.DATA_ACCESS_RESTRICTIONS_TEXT+"</span>"));
			dataUseContainer.add(dataUse);
		} else {
			dataUseContainer.setVisible(false);
		}
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}
	
	@Override
	public void setDetailedMetadataVisible(boolean visible) {
		detailedMetadata.setVisible(visible);
	}
	
	@Override
	public void setEntityNameVisible(boolean visible) {
		this.entityNamePanel.setVisible(visible);
	}

	public void setEntityName(String text) {
		entityName.setInnerText(text);
	}

	public void setEntityId(String text) {
		idField.setText(text);
	}

	@Override
	public void clear() {
		dataUseContainer.clear();
	}

}
