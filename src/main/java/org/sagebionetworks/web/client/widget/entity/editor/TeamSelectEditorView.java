package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;

import com.google.gwt.user.client.ui.IsWidget;

public interface TeamSelectEditorView extends IsWidget {

	void setTeamSuggestBox(SynapseSuggestBox suggestBox);
}
