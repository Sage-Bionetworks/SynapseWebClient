package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

/**
 * Lightweight widget used to resolve markdown
 * 
 * @author Jay
 *
 */
public class MarkdownWidget extends LayoutContainer {
	
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJSNIUtils;
	private WidgetRegistrar widgetRegistrar;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public MarkdownWidget(SynapseClientAsync synapseClient, SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar, IconsImageBundle iconsImageBundle) {
		super();
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.widgetRegistrar = widgetRegistrar;
		this.iconsImageBundle = iconsImageBundle;
	}
	
	/**
	 * @param md
	 * @param attachmentBaseUrl if null, will use file handles
	 */
	public void setMarkdown(final String md, final String ownerId, final String ownerType, final boolean isPreview) {
		this.removeAll();
		synapseClient.markdown2Html(md, isPreview, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					HTMLPanel panel = new HTMLPanel(result);
					add(panel);
					layout();
					synapseJSNIUtils.highlightCodeBlocks();
					//asynchronously load the widgets
					loadWidgets(panel, ownerId, ownerType, widgetRegistrar, synapseClient, iconsImageBundle, isPreview);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				showErrorMessage(DisplayConstants.ERROR_LOADING_MARKDOWN_FAILED+caught.getMessage());
			}
		});
	}
	
	
	/**
	 * Shared method for loading the widgets into the html returned by the service (used to render the entity page, and to generate a preview of the description)
	 * @param panel
	 * @param bundle
	 * @param widgetRegistrar
	 * @param synapseClient
	 * @param nodeModelCreator
	 * @param view
	 * @throws JSONObjectAdapterException 
	 */
	public static void loadWidgets(final HTMLPanel panel, String ownerId, String ownerType, final WidgetRegistrar widgetRegistrar, SynapseClientAsync synapseClient, IconsImageBundle iconsImageBundle, Boolean isPreview) throws JSONObjectAdapterException {
		final String suffix = isPreview ? DisplayConstants.DIV_ID_PREVIEW_SUFFIX : "";
		//look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = DisplayConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
		Element el = panel.getElementById(currentWidgetDiv);
		while (el != null) {
				//based on the contents of the element, create the correct widget descriptor and renderer
				String innerText = el.getAttribute("widgetParams");
				if (innerText != null) {
					try {
						innerText = innerText.trim();
						String contentType = widgetRegistrar.getWidgetContentType(innerText);
						Map<String, String> widgetDescriptor = widgetRegistrar.getWidgetDescriptor(innerText);
						WidgetRendererPresenter presenter = widgetRegistrar.getWidgetRendererForWidgetDescriptor(ownerId, ownerType, contentType, widgetDescriptor);
						if (presenter == null)
							throw new IllegalArgumentException("unable to render widget from the specified markdown:" + innerText);
						panel.add(presenter.asWidget(), currentWidgetDiv);
					}catch(IllegalArgumentException e) {
						//try our best to load all of the widgets. if one fails to load, then fail quietly.
						panel.add(new HTMLPanel(DisplayUtils.getIconHtml(iconsImageBundle.error16()) + innerText), currentWidgetDiv);
					}
				}
			
			i++;
			currentWidgetDiv = DisplayConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
			el = panel.getElementById(currentWidgetDiv);
		}
	}
	
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
