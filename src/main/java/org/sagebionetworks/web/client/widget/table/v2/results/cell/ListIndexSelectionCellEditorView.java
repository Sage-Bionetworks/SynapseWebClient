package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;

public interface ListIndexSelectionCellEditorView
  extends IsWidget, TakesValue<Integer>, HasKeyDownHandlers, Focusable {
  public void configure(List<String> values);
}
