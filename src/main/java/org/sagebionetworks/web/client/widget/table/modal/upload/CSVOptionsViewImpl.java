package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CSVOptionsViewImpl implements CSVOptionsView {

	public interface Binder extends UiBinder<Widget, CSVOptionsViewImpl> {
	}

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
	@UiField
	Radio escapeCharacterBackslashRadio;
	@UiField
	Radio escapeCharacterOtherRadio;
	@UiField
	TextBox escapeCharacterOtherTextBox;

	Widget widget;

	@Inject
	public CSVOptionsViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSeparator(Delimiter delimiter) {
		if (Delimiter.CSV.equals(delimiter)) {
			commaRadio.setValue(true);
		} else if (Delimiter.TSV.equals(delimiter)) {
			tabRadio.setValue(true);
		} else {
			otherRadio.setValue(true);
		}
	}

	@Override
	public void setEscapeCharacter(EscapeCharacter character) {
		if (EscapeCharacter.BACKSLASH.equals(character)) {
			escapeCharacterBackslashRadio.setValue(true);
		} else {
			escapeCharacterOtherRadio.setValue(true);
		}
	}

	@Override
	public void setOtherSeparatorValue(String separator) {
		this.otherTextBox.setText(separator);
	}

	@Override
	public void setOtherEscapeCharacterValue(String character) {
		this.escapeCharacterOtherTextBox.setText(character);
	}

	@Override
	public Delimiter getSeparator() {
		if (commaRadio.getValue()) {
			return Delimiter.CSV;
		} else if (tabRadio.getValue()) {
			return Delimiter.TSV;
		} else {
			return Delimiter.OTHER;
		}
	}

	@Override
	public EscapeCharacter getEscapeCharacter() {
		if (escapeCharacterBackslashRadio.getValue()) {
			return EscapeCharacter.BACKSLASH;
		} else {
			return EscapeCharacter.OTHER;
		}
	}

	@Override
	public String getOtherSeparatorValue() {
		return this.otherTextBox.getValue();
	}

	@Override
	public String getOtherEscapeCharacterValue() {
		return this.escapeCharacterOtherTextBox.getValue();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		ValueChangeHandler<Boolean> separatorChangedHandler = event -> {
			presenter.onSeparatorChanged();
		};
		commaRadio.addValueChangeHandler(separatorChangedHandler);
		tabRadio.addValueChangeHandler(separatorChangedHandler);
		otherRadio.addValueChangeHandler(separatorChangedHandler);
		refreshButton.addClickHandler(event -> {
			presenter.onRefreshPreview();
		});

		ValueChangeHandler<Boolean> escapeCharacterChangedHandler = event -> {
			presenter.onEscapeCharacterChanged();
		};
		escapeCharacterBackslashRadio.addValueChangeHandler(escapeCharacterChangedHandler);
		escapeCharacterOtherRadio.addValueChangeHandler(escapeCharacterChangedHandler);
	}

	@Override
	public void setOtherSeparatorTextEnabled(boolean enabled) {
		this.otherTextBox.setEnabled(enabled);
	}

	@Override
	public void setOtherEscapeCharacterTextEnabled(boolean enabled) {
		this.escapeCharacterOtherTextBox.setEnabled(enabled);
	}

	@Override
	public void clearOtherSeparatorText() {
		this.otherTextBox.clear();
	}

	@Override
	public void clearOtherEscapeCharacterText() {
		this.escapeCharacterOtherTextBox.clear();
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
