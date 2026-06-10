package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.ListBox;

/**
 * List editor view with zero business logic.
 *
 * @author John
 *
 */
public class ListCellEditorViewImpl implements ListCellEditorView {

  public interface Binder extends UiBinder<Widget, ListCellEditorViewImpl> {}

  @UiField
  ListBox listBox;

  Widget widget;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public ListCellEditorViewImpl() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setValue(Integer value) {
    listBox.setSelectedIndex(value);
  }

  @Override
  public Integer getValue() {
    return listBox.getSelectedIndex();
  }

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return listBox.addKeyDownHandler(handler);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    listBox.fireEvent(event);
  }

  @Override
  public int getTabIndex() {
    return listBox.getTabIndex();
  }

  @Override
  public void setAccessKey(char key) {
    listBox.setAccessKey(key);
  }

  @Override
  public void setFocus(boolean focused) {
    listBox.setFocus(focused);
  }

  @Override
  public void setTabIndex(int index) {
    listBox.setTabIndex(index);
  }

  @Override
  public void configure(List<String> items) {
    for (String item : items) {
      listBox.addItem(item);
    }
  }
}
