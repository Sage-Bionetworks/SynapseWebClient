package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.shared.WikiPageKey;

public interface BaseEditWidgetDescriptorView extends SynapseView {

	public void setPresenter(Presenter presenter);

	/**
	 * Show the popup
	 */
	public void show();

	/**
	 * Hide the popup
	 */
	public void hide();

	public void setWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> widgetDescriptor);

	/**
	 * Call to tell the widget descriptor view to update the widgetDescriptor based on user input
	 * (called on save)
	 */
	public void updateDescriptorFromView();

	/**
	 * Will return a non-null value when this widget should simply insert a value into the description
	 * field (without updating the widget descriptor).
	 * 
	 * @return
	 */
	public String getTextToInsert();

	public List<String> getNewFileHandleIds();

	public List<String> getDeletedFileHandleIds();

	void clearErrors();

	public interface Presenter {

		/**
		 * Pop up an editor for a new widget (of the given widget type)
		 * 
		 * @param entityId
		 * @param widgetType
		 */
		public void editNew(WikiPageKey wikiKey, String widgetType);

		public void editExisting(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> descriptor);

		/**
		 * 
		 * @param handler
		 */
		public void addWidgetDescriptorUpdatedHandler(WidgetDescriptorUpdatedHandler handler);

		/**
		 * The user selected apply
		 */
		public void apply();
	}

}
