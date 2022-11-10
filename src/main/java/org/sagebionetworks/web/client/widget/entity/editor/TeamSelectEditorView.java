package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;

public interface TeamSelectEditorView extends IsWidget {
  void setTeamSuggestBox(SynapseSuggestBox suggestBox);
}
