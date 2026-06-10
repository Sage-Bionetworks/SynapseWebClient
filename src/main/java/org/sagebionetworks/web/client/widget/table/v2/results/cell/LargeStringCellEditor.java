package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.constants.ValidationState;

/**
 * An editor for a large string.
 */
public class LargeStringCellEditor
  extends AbstractCellEditor
  implements CellEditor {

  LargeStringCellEditorView view;

  @Inject
  public LargeStringCellEditor(LargeStringCellEditorView view) {
    super(view);
    this.view = view;
  }

  @Override
  public boolean isValid() {
    view.setValidationState(ValidationState.NONE);
    view.setHelpText("");
    return true;
  }

  public void setVisibleLines(int lines) {
    view.setVisibleLines(lines);
  }
}
