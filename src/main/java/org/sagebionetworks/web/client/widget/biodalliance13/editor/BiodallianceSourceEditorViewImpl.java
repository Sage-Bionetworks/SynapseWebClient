package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BiodallianceSourceEditorViewImpl implements IsWidget, BiodallianceSourceEditorView {
	public interface BiodallianceSourceViewImplUiBinder extends UiBinder<Widget, BiodallianceSourceEditorViewImpl> {}
	Widget widget;
	@UiField
	TextBox sourceNameTextbox;
	@UiField
	TextBox entityPickerTextbox;
	@UiField
	Button entityPickerButton;
	@UiField
	TextBox indexEntityPickerTextbox;
	@UiField
	Button indexEntityPickerButton;
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
	@UiField
	Div entityFinderContainer;
	@UiField
	Div indexEntityFinderContainer;

	Presenter presenter;
	@Inject
	public BiodallianceSourceEditorViewImpl(BiodallianceSourceViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		ClickHandler entityPickerClickHandler = getEntityPickerClickHandler();
		ClickHandler indexEntityPickerClickHandler = getIndexEntityPickerClickHandler();
		entityPickerTextbox.addClickHandler(entityPickerClickHandler);
		entityPickerButton.addClickHandler(entityPickerClickHandler);
		indexEntityPickerTextbox.addClickHandler(indexEntityPickerClickHandler);
		indexEntityPickerButton.addClickHandler(indexEntityPickerClickHandler);
		moveUpButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.moveUpClicked();
			}
		});
		moveDownButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.moveDownClicked();
			}
		});
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteClicked();
			}
		});
	}

	public ClickHandler getEntityPickerClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				entityPickerTextbox.selectAll();
				presenter.entityPickerClicked();
			}
		};
	}
	@Override
	public void setEntityFinder(Widget widget) {
		entityFinderContainer.clear();
		entityFinderContainer.add(widget);
	}
	
	@Override
	public void setIndexEntityFinder(Widget widget) {
		indexEntityFinderContainer.clear();
		indexEntityFinderContainer.add(widget);
	}
	
	public ClickHandler getIndexEntityPickerClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				indexEntityPickerTextbox.selectAll();
				presenter.indexEntityPickerClicked();
			}
		};
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
	
	@Override
	public void setMoveDownEnabled(boolean enabled) {
		moveDownButton.setEnabled(enabled);
	}
	@Override
	public void setMoveUpEnabled(boolean enabled) {
		moveUpButton.setEnabled(enabled);
	}
}
