package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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

	public static final String REST_DOC_URL = "http://rest.synapse.org/org/sagebionetworks/repo/web/controller/TableExamples.html";

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
	@UiField
	Button editResultsButton;
	@UiField
	Button showQueryButton;
	@UiField
	AnchorListItem addToDownloadListLink;
	@UiField
	AnchorListItem programmaticOptionsLink;
	@UiField
	AnchorListItem exportTableLink;
	@UiField
	Divider downloadOptionsDivider;

	HTMLPanel panel;
	Presenter presenter;
	
	@Inject
	public QueryInputViewImpl(Binder binder){
		this.panel = binder.createAndBindUi(this);
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		queryButton.addClickHandler(event -> {
			presenter.onExecuteQuery();
		});
		resetButton.addClickHandler(event -> {
			presenter.onReset();
		});
		// Enter key should execute the query.
		queryInput.addKeyDownHandler(event -> {
			if(KeyCodes.KEY_ENTER == event.getNativeKeyCode()){
				presenter.onExecuteQuery();
			}
		});
		editResultsButton.addClickHandler(event -> {
			presenter.onEditResults();
		});
		exportTableLink.addClickHandler(event -> {
			presenter.onExportTable();
		});
		showQueryButton.addClickHandler(event -> {
			presenter.onShowQuery();
		});
		programmaticOptionsLink.addClickHandler(event -> {
			presenter.onDownloadFilesProgrammatically();
		});
		addToDownloadListLink.addClickHandler(event -> {
			presenter.onAddToDownloadList();
		});
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

	@Override
	public void setEditEnabled(boolean enabled) {
		this.editResultsButton.setEnabled(enabled);
	}
	
	@Override
	public void setEditVisible(boolean visibile) {
		this.editResultsButton.setVisible(visibile);
	}

	@Override
	public void setDownloadEnabled(boolean enabled) {
		exportTableLink.setEnabled(enabled);
	}


	@Override
	public void setQueryInputVisible(boolean visible) {
		queryInputGroup.setVisible(visible);
	}
	
	@Override
	public void setShowQueryVisible(boolean visible) {
		showQueryButton.setVisible(visible);
	}
	@Override
	public void setDownloadFilesVisible(boolean visible) {
		addToDownloadListLink.setVisible(visible);
		programmaticOptionsLink.setVisible(visible);
		downloadOptionsDivider.setVisible(visible);
	}
}
