package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TablesTabViewImpl implements TablesTabView {
	
	public static final String TABLES_API_DOCS_URL = "http://rest.synapse.org/#org.sagebionetworks.repo.web.controller.TableController";
	public static final String TABLES_LEARN_MORE_URL = "#!Wiki:syn2305384/ENTITY/61139";

	@UiField
	Button tableLearnMoreButton;
	@UiField
	Button tableAPIDocsButton;

	@UiField
	SimplePanel tableTitlebarContainer;
	
	//tables
	@UiField
	SimplePanel tableBreadcrumbContainer;
	@UiField
	SimplePanel tableMetadataContainer;
	@UiField
	SimplePanel tableActionMenuContainer;
	@UiField
	SimplePanel tableWidgetContainer;
	@UiField
	SimplePanel tableModifiedAndCreatedContainer;
	@UiField
	SimplePanel tableListWidgetContainer;
	@UiField
	SimplePanel synapseAlertContainer;
	
	public interface TabsViewImplUiBinder extends UiBinder<Widget, TablesTabViewImpl> {}
	
	Widget widget;
	UserBadge createdByBadge, modifiedByBadge;
	@Inject
	public TablesTabViewImpl(
			UserBadge createdByBadge,
			UserBadge modifiedByBadge
			) {
		//empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
		
		this.createdByBadge = createdByBadge;
		this.modifiedByBadge = modifiedByBadge;
		
		initClickHandlers();
	}
	
	private void initClickHandlers() {		
		tableLearnMoreButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(TABLES_LEARN_MORE_URL, "", "");
			}
		});

		tableAPIDocsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(TABLES_API_DOCS_URL, "", "");
			}
		});
	}
	
	@Override
	public void setBreadcrumb(Widget w) {
		tableBreadcrumbContainer.add(w);
	}
	
	@Override
	public void setTableList(Widget w) {
		tableListWidgetContainer.add(w);
	}
	@Override
	public void setTitlebar(Widget w) {
		tableTitlebarContainer.add(w);
	}
	
	@Override
	public void configureModifiedAndCreatedWidget(Entity entity)  {
		createdByBadge.asWidget().removeFromParent();
		modifiedByBadge.asWidget().removeFromParent();
		
		FlowPanel attributionPanel = new FlowPanel();
		createdByBadge.configure(entity.getCreatedBy());
		modifiedByBadge.configure(entity.getModifiedBy());
		
		InlineHTML inlineHtml = new InlineHTML(DisplayConstants.CREATED_BY);
		attributionPanel.add(inlineHtml);
		Widget createdByBadgeWidget = createdByBadge.asWidget();
		createdByBadgeWidget.addStyleName("movedown-4 margin-left-5");
		attributionPanel.add(createdByBadgeWidget);
		
		inlineHtml = new InlineHTML(" on " + DisplayUtils.convertDataToPrettyString(entity.getCreatedOn()) + "<br>" + DisplayConstants.MODIFIED_BY);
		
		attributionPanel.add(inlineHtml);
		Widget modifiedByBadgeWidget = modifiedByBadge.asWidget();
		modifiedByBadgeWidget.addStyleName("movedown-4 margin-left-5");
		attributionPanel.add(modifiedByBadgeWidget);
		inlineHtml = new InlineHTML(" on " + DisplayUtils.convertDataToPrettyString(entity.getModifiedOn()));
		
		attributionPanel.add(inlineHtml);
		tableModifiedAndCreatedContainer.add(attributionPanel);
	}
	@Override
	public void clearModifiedAndCreatedWidget() {
		tableModifiedAndCreatedContainer.clear();
	}
	@Override
	public void setEntityMetadata(Widget w) {
		tableMetadataContainer.add(w);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setTableEntityWidget(Widget w) {
		tableWidgetContainer.add(w);
	}
	@Override
	public void clearTableEntityWidget() {
		tableWidgetContainer.clear();
	}
	
	@Override
	public void setActionMenu(Widget w) {
		tableActionMenuContainer.add(w);
	}
	@Override
	public void clearActionMenuContainer() {
		tableActionMenuContainer.clear();
	}
	@Override
	public void setSynapseAlert(Widget w) {
		synapseAlertContainer.setWidget(w);
	}
	
	@Override
	public void setBreadcrumbVisible(boolean visible) {
		tableBreadcrumbContainer.setVisible(visible);
	}
	
	@Override
	public void setEntityMetadataVisible(boolean visible) {
		tableMetadataContainer.setVisible(visible);
	}
	@Override
	public void setTableListVisible(boolean visible) {
		tableListWidgetContainer.setVisible(visible);
	}
	@Override
	public void setTitlebarVisible(boolean visible) {
		tableTitlebarContainer.setVisible(visible);
	}
}

