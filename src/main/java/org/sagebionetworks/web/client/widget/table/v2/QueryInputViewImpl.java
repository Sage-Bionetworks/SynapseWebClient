package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Basic implementation of the QueryInputView.  This view has zero business logic.
 * 
 * @author John
 *
 */
public class QueryInputViewImpl implements QueryInputView{

	public interface Binder extends UiBinder<HTMLPanel, QueryInputViewImpl> {
	}
	
	@UiField
	FormGroup inputFormGroup;
	@UiField
	InputGroup queryInputGroup;
	@UiField
	TextBox queryInput;
	@UiField
	Button queryButton;
	@UiField
	Alert queryResultsMessage;
	@UiField
	Button resetButton;
	
	HTMLPanel panel;
	Presenter presenter;
	
	@Inject
	public QueryInputViewImpl(Binder binder){
		this.panel = binder.createAndBindUi(this);
		queryButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onExecuteQuery();
			}
		});
		resetButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onReset();
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void setInputQueryString(String startQuery) {
		this.queryInput.setText(startQuery);
	}

	@Override
	public void setQueryInputLoading(boolean loading) {
		this.queryInput.setEnabled(!loading);
		if (loading) {
			this.queryButton.state().loading();
		} else {
			this.queryButton.state().reset();
		}
	}

	@Override
	public Widget asWidget() {
		return panel;
	}

	@Override
	public String getInputQueryString() {
		return queryInput.getValue();
	}

	@Override
	public void showInputError(boolean visible) {
		if(visible){
			this.inputFormGroup.setValidationState(ValidationState.ERROR);
			this.queryResultsMessage.setVisible(true);
		}else{
			this.inputFormGroup.setValidationState(ValidationState.NONE);
			this.queryResultsMessage.setVisible(false);
		}
	}

	@Override
	public void setInputErrorMessage(String string) {
		this.queryResultsMessage.setText(string);
	}

}
