package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListRowBadgeViewImpl extends Composite implements EntityListRowBadgeView {
	
	private Presenter presenter;
	public interface Binder extends UiBinder<Widget, EntityListRowBadgeViewImpl> {	}
	
	@UiField
	TableRow row;
	@UiField
	Icon icon;
	@UiField
	Anchor entityLink;
	@UiField
	SimplePanel createdByField;
	@UiField
	Label createdOnField;
	
	@UiField
	Span synAlertContainer;
	@UiField
	Span fileDownloadButtonContainer;
	@UiField
	Label descriptionField;
	@UiField
	Label noteField;
	@UiField
	Label versionField;
	@UiField
	HTMLPanel loadingUI;
	@UiField
	CheckBox select;
	@UiField
	TableData selectTableData;
	@UiField
	TableData synAlertTableData;
	@UiField
	TableData iconTableData;
	@UiField
	TableData descriptionTableData;
	
	@Inject
	public EntityListRowBadgeViewImpl(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
		select.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSelectionChanged();
			}
		});
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		presenter.viewAttached();
	}
	
	@Override
	public void setEntityLink(String name, String url) {
		entityLink.setText(name);
		entityLink.setHref(url);
	}
	
	@Override
	public void setIcon(IconType iconType) {
		icon.setType(iconType);
	}
	

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}
		
	@Override
	public void setCreatedByWidget(Widget w) {
		createdByField.setWidget(w);
	}
	
	@Override
	public void setCreatedOn(String createdOnString) {
		createdOnField.setText(createdOnString);
	}
	
	
	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(this);
	}
	@Override
	public void setFileDownloadButton(Widget w) {
		fileDownloadButtonContainer.clear();
		fileDownloadButtonContainer.add(w);
	}
	@Override
	public void setDescriptionVisible(boolean visible) {
		descriptionTableData.setVisible(visible);
	}
	@Override
	public void setDescription(String description) {
		descriptionField.setText(description);
	}
	@Override
	public void setNote(String note) {
		noteField.setText(note);
	}
	
	@Override
	public void setVersion(String version) {
		versionField.setText(version);
	}
	@Override
	public void showLoading() {
		row.setVisible(false);
		loadingUI.setVisible(true);
	}
	@Override
	public void showRow() {
		loadingUI.setVisible(false);
		row.setVisible(true);
		iconTableData.setVisible(true);
	}
	@Override
	public void showSynAlert() {
		loadingUI.setVisible(false);
		row.setVisible(true);
		iconTableData.setVisible(false);
		synAlertTableData.setVisible(true);
	}
	@Override
	public void setIsSelectable(boolean isSelectable) {
		selectTableData.setVisible(isSelectable);
	}
	
	@Override
	public boolean isSelected() {
		return select.getValue();
	}
	
	@Override
	public void setSelected(boolean selected) {
		select.setValue(selected);
	}
	
	@Override
	public String getNote() {
		return noteField.getText();
	}
}
