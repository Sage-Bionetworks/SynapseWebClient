package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProvenanceConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setEntityId(String entityId);
	public String getEntityId();
	public void setDepth(Long depth);
	public Long getDepth();
	public void setIsExpanded(boolean b);
	public boolean isExpanded();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
