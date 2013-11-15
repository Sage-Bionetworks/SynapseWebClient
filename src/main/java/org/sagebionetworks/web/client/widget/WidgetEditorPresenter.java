package org.sagebionetworks.web.client.widget;

import java.util.Map;

import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.Dialog;

/**
 * To support, add your editor to the PortalGinModule, and add it to the know widgets in the widget registrar.
 * @see org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl
 * @author jayhodgson
 *
 */
public interface WidgetEditorPresenter extends SynapseWidgetPresenter {
	/**
	 * This will be called to give you the parent entity ID, and the widget descriptor containing the params that you should edit.
	 * @param widgetDescriptor
	 * @param window TODO
	 * @param entityId
	 */
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Dialog window);
	/**
	 * You should update the parameters stored in your widget descriptor based on the values currently set in the view.
	 * User parameter validation should occur in this method (throwing an IllegalArgumentException if a problem is found).
	 */
	public void updateDescriptorFromView() throws IllegalArgumentException;
	
	/**
	 * Return the total height of your widget editor view.
	 * @return
	 */
	public int getDisplayHeight();

	/**
	 * Return the extra width necessary to display your widget editor fields (usually 0).
	 * @return
	 */
	public int getAdditionalWidth();
	
	/**
	 * used when you want the editor to simply return text that should be inserted into the description field (instead of updating the descriptor).  
	 * @see org.sagebionetworks.web.client.widget.entity.editor.OldImageConfigEditor
	 * @return
	 */
	public String getTextToInsert();
}
