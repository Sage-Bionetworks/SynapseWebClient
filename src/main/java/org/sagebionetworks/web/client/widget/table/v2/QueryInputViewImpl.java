package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.FullWidthAlert;

/**
 * Basic implementation of the QueryInputView. This view has zero business logic.
 *
 * @author John
 *
 */
public class QueryInputViewImpl implements QueryInputView {

  public static final String REST_DOC_URL =
    "http://rest.synapse.org/org/sagebionetworks/repo/web/controller/TableExamples.html";

  public interface Binder extends UiBinder<HTMLPanel, QueryInputViewImpl> {}

  @UiField
  FormGroup inputFormGroup;

  @UiField
  InputGroup queryInputGroup;

  @UiField
  TextBox queryInput;

  @UiField
  Button queryButton;

  @UiField
  FullWidthAlert queryResultsMessage;

  HTMLPanel panel;
  Presenter presenter;
  String originalButtonText;

  @Inject
  public QueryInputViewImpl(Binder binder) {
    this.panel = binder.createAndBindUi(this);
    originalButtonText = queryButton.getText();
  }

  @Override
  public void setPresenter(final Presenter presenter) {
    this.presenter = presenter;
    queryButton.addClickHandler(event -> {
      presenter.onExecuteQuery();
    });
    queryResultsMessage.addPrimaryCTAClickHandler(event -> {
      presenter.onReset();
    });
    // Enter key should execute the query.
    queryInput.addKeyDownHandler(event -> {
      if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
        presenter.onExecuteQuery();
      }
    });
  }

  @Override
  public void setInputQueryString(String startQuery) {
    this.queryInput.setText(startQuery);
  }

  @Override
  public void setQueryInputLoading(boolean loading) {
    this.queryInput.setEnabled(!loading);
    DisplayUtils.showLoading(queryButton, loading, originalButtonText);
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
    if (visible) {
      this.inputFormGroup.setValidationState(ValidationState.ERROR);
      this.queryResultsMessage.setVisible(true);
    } else {
      this.inputFormGroup.setValidationState(ValidationState.NONE);
      this.queryResultsMessage.setVisible(false);
    }
  }

  @Override
  public void setInputErrorMessage(String string) {
    this.queryResultsMessage.setMessage(string);
  }

  @Override
  public void setQueryInputVisible(boolean visible) {
    queryInputGroup.setVisible(visible);
  }
}
