package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;

public class TeamSelectEditorViewImpl implements TeamSelectEditorView {

  public interface TeamSelectEditorViewImplUiBinder
    extends UiBinder<Widget, TeamSelectEditorViewImpl> {}

  @UiField
  Div teamSelectContainer;

  private Widget widget;

  @Inject
  public TeamSelectEditorViewImpl(TeamSelectEditorViewImplUiBinder binder) {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTeamSuggestBox(SynapseSuggestBox suggestBox) {
    teamSelectContainer.clear();
    teamSelectContainer.add(suggestBox);
  }
}
