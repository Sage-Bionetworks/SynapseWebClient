package org.sagebionetworks.web.client.widget;

import java.util.Map;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.IsWidget;

public interface WidgetRendererPresenter extends SynapseWidgetPresenter, IsWidget {
	/**
	 * This will be called to give you the parent entity ID, and the widget descriptor containing the
	 * params that should guide your display
	 * 
	 * @param wikiKey
	 * @param widgetDescriptor
	 * @param wikiVersionInView TODO
	 */
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView);
}
