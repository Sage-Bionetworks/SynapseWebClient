package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.table.ColumnType;

/**
 * View with zero business logic.
 *
 * @author jhill
 *
 */
public class JSONListCellEditorViewImpl implements JSONListCellEditorView {

  public interface Binder
    extends UiBinder<Widget, JSONListCellEditorViewImpl> {}

  @UiField
  FormGroup formGroup;

  @UiField
  FocusPanel rendererFocusPanel;

  @UiField
  Div rendererContainer;

  @UiField
  Icon editButton;

  @UiField
  HelpBlock helpBlock;

  EditJSONListModal editJSONListModal;

  Widget widget;
  Presenter presenter;
  CellFactory cellFactory;
  String rawValue = null;

  @Inject
  public JSONListCellEditorViewImpl(Binder binder, CellFactory cellFactory) {
    widget = binder.createAndBindUi(this);
    this.cellFactory = cellFactory;
    rendererFocusPanel.getElement().setAttribute("readonly", "true");
    // users want us to select all on focus see SWC-2213
    rendererFocusPanel.addClickHandler(clickEvent ->
      presenter.onEditButtonClick()
    );
    editButton.addClickHandler(clickEvent -> presenter.onEditButtonClick());
  }

  @Override
  public Widget asWidget() {
    return formGroup;
  }

  @Override
  public void setValue(String value) {
    rawValue = value;
    rendererContainer.clear();
    ColumnType columnType = presenter.getColumnModel().getColumnType();
    // entity id list and user id list renderers have links, do not use standard renderer for these.
    if (
      ColumnType.ENTITYID_LIST == columnType ||
      ColumnType.USERID_LIST == columnType
    ) {
      columnType = ColumnType.INTEGER_LIST;
    }
    Cell renderer = cellFactory.createRenderer(columnType);
    renderer.setValue(value);
    rendererContainer.add(renderer);
  }

  @Override
  public String getValue() {
    return rawValue;
  }

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return rendererFocusPanel.addKeyDownHandler(handler);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    rendererFocusPanel.fireEvent(event);
  }

  @Override
  public int getTabIndex() {
    return rendererFocusPanel.getTabIndex();
  }

  @Override
  public void setAccessKey(char key) {
    rendererFocusPanel.setAccessKey(key);
  }

  @Override
  public void setFocus(boolean focused) {
    rendererFocusPanel.setFocus(focused);
  }

  @Override
  public void setTabIndex(int index) {
    rendererFocusPanel.setTabIndex(index);
  }

  @Override
  public void setValidationState(ValidationState state) {
    this.formGroup.setValidationState(state);
  }

  @Override
  public void setHelpText(String help) {
    helpBlock.setVisible(help != null && !help.trim().isEmpty());
    this.helpBlock.setText(help);
  }

  @Override
  public void setPlaceholder(String placeholder) {}

  @Override
  public String getPlaceholder() {
    return "";
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }
}
