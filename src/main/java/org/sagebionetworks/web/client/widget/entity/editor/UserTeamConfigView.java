package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserTeamConfigView extends IsWidget, WidgetEditorView {

	public void setId(String principalId);

	public String getId();

	public String isIndividual();

	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
