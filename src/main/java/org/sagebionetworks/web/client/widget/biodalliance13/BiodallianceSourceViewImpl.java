package org.sagebionetworks.web.client.widget.biodalliance13;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BiodallianceSourceViewImpl implements IsWidget, BiodallianceSourceView {
	public interface BiodallianceSourceViewImplUiBinder extends UiBinder<Widget, BiodallianceSourceViewImpl> {}
	Widget widget;
	EntityFinder entityFinder;
	@UiField
	TextBox sourceNameTextbox;
	@UiField
	TextBox entityPickerTextbox;
	@UiField
	TextBox indexEntityPickerTextbox;
	@UiField
	Input colorPicker;
	@UiField
	TextBox heightField;
	@UiField
	Button moveUpButton;
	@UiField
	Button moveDownButton;
	@UiField
	Button deleteButton;
	Presenter presenter;
	@Inject
	public BiodallianceSourceViewImpl(BiodallianceSourceViewImplUiBinder binder, EntityFinder entityFinder) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		entityPickerTextbox.addClickHandler(getEntityPickerClickHandler());
		indexEntityPickerTextbox.addClickHandler(getIndexEntityPickerClickHandler());
	}

	public ClickHandler getEntityPickerClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				entityFinder.configure(true, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {
							presenter.entitySelected(selected);
						} else {
							DisplayUtils.showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
				entityFinder.show();
			}
		};
	}
	
	public ClickHandler getIndexEntityPickerClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				entityFinder.configure(true, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {
							presenter.indexEntitySelected(selected);
						} else {
							DisplayUtils.showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
				entityFinder.show();
			}
		};
	}

	@Override
	public void hideEntityFinder() {
		entityFinder.hide();
	}
	
	public void setEntityFinderText(String text) {
		this.entityPickerTextbox.setValue(text);
	}
	
	@Override
	public void setIndexEntityFinderText(String text) {
		this.indexEntityPickerTextbox.setValue(text);
	}

	public String getColor() {
		return colorPicker.getValue();
	}

	public void setColor(String color) {
		colorPicker.setValue(color);
	}

	public String getHeight() {
		return heightField.getValue();
	}

	public void setHeight(String height) {
		heightField.setValue(height);
	}
	@Override
	public String getSourceName() {
		return sourceNameTextbox.getValue();
	}
	@Override
	public void setSourceName(String sourceName) {
		sourceNameTextbox.setValue(sourceName);
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
	public void initView() {
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void clear() {
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
