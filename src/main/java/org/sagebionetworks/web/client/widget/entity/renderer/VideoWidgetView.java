package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface VideoWidgetView extends IsWidget {
	void configure(String mp4SynapseId, String oggSynapseId, String webmSynapseId, String width, String height);

	void configure(String iframeTargetUrl);

	void showError(String error);
}
