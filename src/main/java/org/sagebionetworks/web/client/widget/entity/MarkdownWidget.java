package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;

import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
public class MarkdownWidget extends LayoutContainer implements SynapseView {
	
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJSNIUtils;
	private WidgetRegistrar widgetRegistrar;
	private IconsImageBundle iconsImageBundle;
	private CookieProvider cookies;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	NodeModelCreator nodeModelCreator;
	
	@Inject
	public MarkdownWidget(SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar,
			IconsImageBundle iconsImageBundle,
			CookieProvider cookies,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			NodeModelCreator nodeModelCreator) {
		super();
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.widgetRegistrar = widgetRegistrar;
		this.iconsImageBundle = iconsImageBundle;
		this.cookies = cookies;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
	}
	
	public void loadMarkdownFromWikiPage(final WikiPageKey wikiKey, final boolean isPreview) {
		//get the wiki page
		synapseClient.getWikiPage(wikiKey, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					WikiPage page = nodeModelCreator.createJSONEntity(result, WikiPage.class);
					wikiKey.setWikiPageId(page.getId());
					setMarkdown(page.getMarkdown(), wikiKey, true, isPreview);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), MarkdownWidget.this))
					MarkdownWidget.this.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
			}
		});				
	}
	
	
	/**
	 * @param md
	 * @param attachmentBaseUrl if null, will use file handles
	 */
	public void setMarkdown(final String md, final WikiPageKey wikiKey, final boolean isWiki, final boolean isPreview) {
		final SynapseView view = this;
		synapseClient.markdown2Html(md, isPreview, DisplayUtils.isInTestWebsite(cookies), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					removeAll();
					String content = "";
					
					if(result == null || SharedMarkdownUtils.getDefaultWikiMarkdown().equals(result)) {
						content += SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%;margin-bottom:30px\">" + DisplayConstants.LABEL_NO_MARKDOWN + "</div>").asString();
					}
					
					if (result != null) {
						content += result;
					}
					HTMLPanel panel = new HTMLPanel(content); 
					add(panel);
					layout();
					synapseJSNIUtils.highlightCodeBlocks();
					DisplayUtils.loadTableSorters(panel, synapseJSNIUtils);
					MarkdownWidget.loadMath(panel, synapseJSNIUtils, isPreview);
					//asynchronously load the widgets
					loadWidgets(panel, wikiKey, isWiki, widgetRegistrar, synapseClient, iconsImageBundle, isPreview);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				removeAll();
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
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
	public static void loadWidgets(final HTMLPanel panel, WikiPageKey wikiKey, boolean isWiki, final WidgetRegistrar widgetRegistrar, SynapseClientAsync synapseClient, IconsImageBundle iconsImageBundle, Boolean isPreview) throws JSONObjectAdapterException {
		final String suffix = isPreview ? WebConstants.DIV_ID_PREVIEW_SUFFIX : "";
		//look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = WebConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
		Element el = panel.getElementById(currentWidgetDiv);
		while (el != null) {
				//based on the contents of the element, create the correct widget descriptor and renderer
				String innerText = el.getAttribute("widgetParams");
				if (innerText != null) {
					try {
						innerText = innerText.trim();
						String contentType = widgetRegistrar.getWidgetContentType(innerText);
						Map<String, String> widgetDescriptor = widgetRegistrar.getWidgetDescriptor(innerText);
						WidgetRendererPresenter presenter = widgetRegistrar.getWidgetRendererForWidgetDescriptor(wikiKey, contentType, widgetDescriptor, isWiki);
						if (presenter == null)
							throw new IllegalArgumentException("Unable to render widget from the specified markdown.");
						panel.add(presenter.asWidget(), currentWidgetDiv);
					}catch(Throwable e) {
						//try our best to load all of the widgets. if one fails to load, then fail quietly.
						String message = innerText;
						if (e.getMessage() != null)
							message += "<br>" + e.getMessage();
						panel.add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(message)), currentWidgetDiv);
					}
				}
			
			i++;
			currentWidgetDiv = WebConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
			el = panel.getElementById(currentWidgetDiv);
		}
	}
	
	
	/**
	 * Shared method for loading the math elements returned by the Synapse Markdown parser
	 * @throws JSONObjectAdapterException 
	 */
	public static void loadMath(final HTMLPanel panel, final SynapseJSNIUtils synapseJSNIUtils, Boolean isPreview) throws JSONObjectAdapterException {
		final String suffix = isPreview ? WebConstants.DIV_ID_PREVIEW_SUFFIX : "";
		//look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = WebConstants.DIV_ID_MATHJAX_PREFIX + i + suffix;
		Element el = panel.getElementById(currentWidgetDiv);
		while (el != null) {
			synapseJSNIUtils.processWithMathJax(el);
			i++;
			currentWidgetDiv = WebConstants.DIV_ID_MATHJAX_PREFIX + i + suffix;
			el = panel.getElementById(currentWidgetDiv);
		}
	}
	
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}
}
