package org.sagebionetworks.web.client.widget.biodalliance13.editor;


import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface BiodallianceSourceEditorView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void entitySelected(Reference ref);
		void indexEntitySelected(Reference ref);
		void entityPickerClicked();
		void indexEntityPickerClicked();
	}
	
	void setEntityFinderText(String text);
	void setIndexEntityFinderText(String text);
	
	String getColor();
	void setColor(String color);

	String getHeight();
	void setHeight(String height);
	
	String getSourceName();
	void setSourceName(String sourceName);
	
	void setEntityFinder(Widget widget);
	void setIndexEntityFinder(Widget widget);
}
