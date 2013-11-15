package org.sagebionetworks.web.client.widget;

import java.util.Map;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

public interface WidgetRendererPresenter extends SynapseWidgetPresenter {
	/**
	 * This will be called to give you the parent entity ID, and the widget descriptor containing the params that should guide your display
	 * @param wikiKey
	 * @param widgetDescriptor
	 */
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired);
}
