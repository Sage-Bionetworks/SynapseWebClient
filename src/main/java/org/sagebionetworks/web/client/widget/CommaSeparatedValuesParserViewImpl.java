package org.sagebionetworks.web.client.widget;


import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditorViewImpl;


public class CommaSeparatedValuesParserViewImpl implements CommaSeparatedValuesParserView{
	public interface Binder extends UiBinder<Widget, CommaSeparatedValuesParserViewImpl> {}

	private Widget widget;
	private Presenter presenter;

	@UiField
	Button cancelButton;

	@UiField
	Button addButton;

	@UiField
	TextArea commaSeparatedTextBox;


	@Inject
	public CommaSeparatedValuesParserViewImpl(CommaSeparatedValuesParserViewImpl.Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		cancelButton.addClickHandler(clickEvent -> presenter.onCancel());
		addButton.addClickHandler(clickEvent -> presenter.onAdd());
	}

	@Override
	public String getText(){
		return this.commaSeparatedTextBox.getValue();
	}

	@Override
	public void clearTextBox() {
		commaSeparatedTextBox.clear();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
