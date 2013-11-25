package org.sagebionetworks.web.client.widget.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;


import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
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
	GWTWrapper gwt;
	PortalGinInjector ginInjector;
	private ResourceLoader resourceLoader;
	private String md;
	private WikiPageKey wikiKey;
	private boolean isWiki;
	private boolean isPreview;
	
	@Inject
	public MarkdownWidget(SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar,
			IconsImageBundle iconsImageBundle,
			CookieProvider cookies,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			NodeModelCreator nodeModelCreator,
			ResourceLoader resourceLoader, 
			GWTWrapper gwt,
			PortalGinInjector ginInjector) {
		super();
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.widgetRegistrar = widgetRegistrar;
		this.iconsImageBundle = iconsImageBundle;
		this.cookies = cookies;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
		this.resourceLoader = resourceLoader;
		this.gwt = gwt;
		this.ginInjector = ginInjector;
	}
	
	public void loadMarkdownFromWikiPage(final WikiPageKey wikiKey, final boolean isPreview) {
		//get the wiki page
		synapseClient.getV2WikiPage(wikiKey, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					V2WikiPage page = nodeModelCreator.createJSONEntity(result, V2WikiPage.class);
					wikiKey.setWikiPageId(page.getId());
					try {
						synapseClient.getAndReadS3Object(page.getMarkdownFileHandleId(), page.getId() + "_markdown", new AsyncCallback<String>() {
							@Override
							public void onSuccess(String result) {
								setMarkdown(result, wikiKey, true, isPreview);
							}
							@Override
							public void onFailure(Throwable caught) {
								MarkdownWidget.this.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
							}	
						});
						
					} catch (Exception e) {
						onFailure(e);
					}
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

	public void refresh() {
		setMarkdown(md, wikiKey, isWiki, isPreview);
	}
	
	/**
	 * @param md
	 * @param attachmentBaseUrl if null, will use file handles
	 */
	public void setMarkdown(final String md, final WikiPageKey wikiKey, final boolean isWiki, final boolean isPreview) {
		final SynapseView view = this;
		this.md = md;
		this.wikiKey = wikiKey;
		this.isWiki = isWiki;
		this.isPreview= isPreview;
		synapseClient.markdown2Html(md, isPreview, DisplayUtils.isInTestWebsite(cookies), gwt.getHostPrefix(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					removeAll();
					String content = "";
					
					if(result == null || result.isEmpty()) {
						content += SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%;\">" + DisplayConstants.LABEL_NO_MARKDOWN + "</div>").asString();
					}
					
					if (result != null) {
						content += result;
					}
					ResizeLayoutPanel wikiSubpagesPanel = new ResizeLayoutPanel();
					add(wikiSubpagesPanel);

					HTMLPanel panel = new HTMLPanel(content);
					add(panel);
					layout();
					synapseJSNIUtils.highlightCodeBlocks();
					DisplayUtils.loadTableSorters(panel, synapseJSNIUtils);
					MarkdownWidget.loadMath(panel, synapseJSNIUtils, isPreview, resourceLoader);
					Callback widgetRefreshRequired = new Callback() {
						@Override
						public void invoke() {
							refresh();
						}
					};
					//asynchronously load the widgets
					Set<String> contentTypes = loadWidgets(panel, wikiKey, isWiki, widgetRegistrar, synapseClient, iconsImageBundle, isPreview, widgetRefreshRequired);

					//also add the wiki subpages widget, unless explicitly instructed not to in the markdown
					if (!contentTypes.contains(WidgetConstants.NO_AUTO_WIKI_SUBPAGES)) {
						WikiSubpagesWidget widget = ginInjector.getWikiSubpagesRenderer();
						//subpages widget is special in that it applies styles to the markdown html panel (if there are subpages)
						widget.configure(wikiKey, new HashMap<String, String>(), widgetRefreshRequired, wikiSubpagesPanel, panel);
						wikiSubpagesPanel.add(widget.asWidget());
					}
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
	public static Set<String> loadWidgets(final HTMLPanel panel, WikiPageKey wikiKey, boolean isWiki, final WidgetRegistrar widgetRegistrar, SynapseClientAsync synapseClient, IconsImageBundle iconsImageBundle, Boolean isPreview, Callback widgetRefreshRequired) throws JSONObjectAdapterException {
		Set<String> contentTypes = new HashSet<String>();
		final String suffix = SharedMarkdownUtils.getPreviewSuffix(isPreview);
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
						WidgetRendererPresenter presenter = widgetRegistrar.getWidgetRendererForWidgetDescriptor(wikiKey, contentType, widgetDescriptor, isWiki, widgetRefreshRequired);
						if (presenter == null)
							throw new IllegalArgumentException("Unable to render widget from the specified markdown.");
						panel.add(presenter.asWidget(), currentWidgetDiv);
						contentTypes.add(contentType);
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
		return contentTypes;
	}
	
	
	/**
	 * Shared method for loading the math elements returned by the Synapse Markdown parser
	 * @throws JSONObjectAdapterException 
	 */
	public static void loadMath(final HTMLPanel panel, final SynapseJSNIUtils synapseJSNIUtils, Boolean isPreview, final ResourceLoader resourceLoader) throws JSONObjectAdapterException {
		final String suffix = SharedMarkdownUtils.getPreviewSuffix(isPreview);
		//look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = WebConstants.DIV_ID_MATHJAX_PREFIX + i + suffix;
		Element el = panel.getElementById(currentWidgetDiv);
		while (el != null) {
			final Element loadElement = el;
			final AsyncCallback<Void> mathjaxLoadedCallback = new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					synapseJSNIUtils.processWithMathJax(loadElement);
				}
				@Override
				public void onFailure(Throwable caught) {
				}
			};
			
			AsyncCallback<Void> mathjaxInitializedCallback = new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					resourceLoader.requires(ClientProperties.MATHJAX_LOADER_JS, mathjaxLoadedCallback);
				}
				@Override
				public void onFailure(Throwable caught) {
				}
			};
			if (resourceLoader.isLoaded(ClientProperties.MATHJAX_JS))
				//already loaded
				synapseJSNIUtils.processWithMathJax(loadElement);
			else
				resourceLoader.requires(ClientProperties.MATHJAX_JS, mathjaxInitializedCallback);
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
