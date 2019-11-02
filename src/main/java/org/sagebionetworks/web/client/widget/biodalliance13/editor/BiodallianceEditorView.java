package org.sagebionetworks.web.client.widget.biodalliance13.editor;


import org.sagebionetworks.web.client.widget.SelectableListView;
import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface BiodallianceEditorView extends IsWidget, WidgetEditorView, SelectableListView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	void addTrack(Widget w);

	void clearTracks();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void addTrackClicked();
	}

	String getChr();

	void setChr(String chr);

	String getViewStart();

	void setViewStart(String viewStart);

	String getViewEnd();

	void setViewEnd(String viewEnd);

	boolean isMouse();

	void setMouse();

	boolean isHuman();

	void setHuman();

	void setTrackHeaderColumnsVisible(boolean visible);
}
