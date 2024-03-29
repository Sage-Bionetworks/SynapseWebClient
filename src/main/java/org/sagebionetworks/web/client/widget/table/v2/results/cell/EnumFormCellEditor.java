package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.view.DivView;

public class EnumFormCellEditor implements CellEditor {

  public static final String NOTHING_SELECTED = "nothing selected";
  ListIndexSelectionCellEditorView currentView;
  PortalGinInjector ginInjector;
  ArrayList<String> items;
  DivView view;
  public static final int MAX_RADIO_BUTTONS = 10;

  @Inject
  public EnumFormCellEditor(DivView view, PortalGinInjector ginInjector) {
    this.view = view;
    this.ginInjector = ginInjector;
  }

  @Override
  public boolean isValid() {
    // The widget will not allow invalid values.
    return true;
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void setValue(String value) {
    value = StringUtils.emptyAsNull(value);
    if (value == null) {
      return;
    }
    /*
     * Find the index matching the value. Note: Linear search for less than 100 items is reasonable.
     */
    for (int i = 0; i < items.size(); i++) {
      if (value.equals(items.get(i))) {
        currentView.setValue(i);
        return;
      }
    }

    // If here we did not match a value
    throw new IllegalArgumentException("Unknown value: " + value);
  }

  @Override
  public String getValue() {
    Integer index = currentView.getValue();

    if (index == null) {
      return null;
    } else {
      return this.items.get(index);
    }
  }

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return null;
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    // Cannot handle this.
  }

  @Override
  public int getTabIndex() {
    return currentView.getTabIndex();
  }

  @Override
  public void setAccessKey(char key) {
    currentView.setAccessKey(key);
  }

  @Override
  public void setFocus(boolean focused) {
    currentView.setFocus(focused);
  }

  @Override
  public void setTabIndex(int index) {
    currentView.setTabIndex(index);
  }

  public void configure(List<String> validValues) {
    view.clear();
    this.items = new ArrayList<String>(validValues.size());
    for (String value : validValues) {
      this.items.add(value);
    }
    if (validValues.size() > MAX_RADIO_BUTTONS) {
      currentView = ginInjector.createListCellEditorView();
    } else {
      currentView = ginInjector.createRadioCellEditorView();
    }
    currentView.configure(this.items);
    view.add(currentView);
  }
}
