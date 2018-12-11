package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CytoscapeConfigViewImpl implements CytoscapeConfigView {
	public interface CytoscapeConfigViewImplUiBinder extends UiBinder<Widget, CytoscapeConfigViewImpl> {}
	private Presenter presenter;
	@UiField
	TextBox entity;
	@UiField
	TextBox styleEntity;
	@UiField
	Button button;
	@UiField
	Button styleButton;
	@UiField
	TextBox displayHeightField;
	
	EntityFinder entityFinder;
	
	Widget widget;
	
	@Inject
	public CytoscapeConfigViewImpl(CytoscapeConfigViewImplUiBinder binder, 
			EntityFinder entityFinder) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		button.addClickHandler(getClickHandler(entity));
		styleButton.addClickHandler(getClickHandler(styleEntity));
	}
	
	@Override
	public void initView() {
	}

	public ClickHandler getClickHandler(final TextBox textBox) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				entityFinder.configure(EntityFilter.ALL_BUT_LINK, false, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						textBox.setValue(selected.getTargetId());
						entityFinder.hide();
					}
				});
				entityFinder.show();
			}
		};
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if ("".equals(entity.getValue()))
			throw new IllegalArgumentException(DisplayConstants.ERROR_SELECT_CYTOSCAPE_FILE);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public String getEntity() {
		return entity.getValue();
	}
	
	@Override
	public void setEntity(String entityId) {
		entity.setValue(entityId);
	}

	@Override
	public String getStyleEntity() {
		return styleEntity.getValue();
	}

	@Override
	public void setStyleEntity(String entityId) {
		styleEntity.setValue(entityId);
	}
	
	@Override
	public void clear() {
		entity.setValue("");
		styleEntity.setValue("");
		displayHeightField.setValue("");
	}
	
	@Override
	public String getHeight() {
		return displayHeightField.getValue();
	}
	@Override
	public void setHeight(String height) {
		displayHeightField.setValue(height);
	}
}
