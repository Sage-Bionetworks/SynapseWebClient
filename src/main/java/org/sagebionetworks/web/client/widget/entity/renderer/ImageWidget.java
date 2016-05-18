package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidget implements ImageWidgetView.Presenter, WidgetRendererPresenter {
	
	private ImageWidgetView view;
	private Map<String,String> descriptor;
	AuthenticationController authenticationController;
	public static final String MAX_WIDTH_NONE = "max-width-none";
	@Inject
	public ImageWidget(ImageWidgetView view, 
			AuthenticationController authenticationController) {
		this.view = view;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		String synapseId = descriptor.get(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY);
		view.configure(wikiKey,
				descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY),
				descriptor.get(WidgetConstants.IMAGE_WIDGET_SCALE_KEY),
				descriptor.get(WidgetConstants.IMAGE_WIDGET_ALIGNMENT_KEY),
				synapseId, authenticationController.isLoggedIn(), wikiVersionInView);
		
		String responsiveValue = descriptor.get(WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY);
		if (responsiveValue != null && !Boolean.parseBoolean(responsiveValue)) {
			view.addStyleName(MAX_WIDTH_NONE);
		}
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

		/*
	 * Private Methods
	 */
}
