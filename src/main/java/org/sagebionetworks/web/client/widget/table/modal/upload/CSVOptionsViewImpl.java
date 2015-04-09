package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CSVOptionsViewImpl implements CSVOptionsView {

	public interface Binder extends UiBinder<Widget, CSVOptionsViewImpl> {}
	
	@UiField
	Radio commaRadio;
	@UiField
	Radio tabRadio;
	@UiField
	Radio otherRadio;
	@UiField
	TextBox otherTextBox;
	@UiField
	CheckBox firstLineHeader;
	@UiField
	Button refreshButton;
	
	Widget widget;
	
	@Inject
	public CSVOptionsViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSeparator(Delimiter delimiter) {
		if(Delimiter.CSV.equals(delimiter)){
			commaRadio.setValue(true);
		}else if(Delimiter.TSV.equals(delimiter)){
			tabRadio.setValue(true);
		}else{
			otherRadio.setValue(true);
		}
	}

	@Override
	public void setOtherSeparatorValue(String separator) {
		this.otherTextBox.setText(separator);
	}

	@Override
	public Delimiter getSeparator() {
		if(commaRadio.getValue()){
			return Delimiter.CSV;
		}else if(tabRadio.getValue()){
			return Delimiter.TSV;
		}else{
			return Delimiter.OTHER;
		}
	}

	@Override
	public String getOtherSeparatorValue() {
		return this.otherTextBox.getValue();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		commaRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				presenter.onSeparatorChanged();
			}
		});
		tabRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				presenter.onSeparatorChanged();
			}
		});
		otherRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				presenter.onSeparatorChanged();
			}
		});
		refreshButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRefreshPreview();
			}
		});
	}

	@Override
	public void setOtherSeparatorTextEnabled(boolean enabled) {
		this.otherTextBox.setEnabled(enabled);
	}

	@Override
	public void clearOtherSeparatorText() {
		this.otherTextBox.clear();
	}

	@Override
	public void setFirsLineIsHeader(boolean isFirstLineHeader) {
		this.firstLineHeader.setValue(isFirstLineHeader);
	}

	@Override
	public boolean getIsFristLineHeader() {
		return this.firstLineHeader.getValue();
	}

}
