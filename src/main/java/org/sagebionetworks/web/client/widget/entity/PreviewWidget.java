package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.util.ContentTypeUtils.isRecognizedCodeFileName;
import static org.sagebionetworks.web.client.ClientProperties.MB;
import static org.sagebionetworks.web.client.ContentTypeUtils.isCSV;
import static org.sagebionetworks.web.client.ContentTypeUtils.isHTML;
import static org.sagebionetworks.web.client.ContentTypeUtils.isPDF;
import static org.sagebionetworks.web.client.ContentTypeUtils.isRecognizedImageContentType;
import static org.sagebionetworks.web.client.ContentTypeUtils.isTAB;
import static org.sagebionetworks.web.client.ContentTypeUtils.isTextType;
import static org.sagebionetworks.web.client.ContentTypeUtils.isWebRecognizedCodeFileName;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.ArrayList;
import java.util.Map;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.file.CloudProviderFileHandleInterface;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.ContentTypeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.NbConvertPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.PDFPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidget implements PreviewWidgetView.Presenter, WidgetRendererPresenter {
	public static final String APPLICATION_ZIP = "application/zip";	
	public static final int MAX_LENGTH = 100000;
	public static final int VIDEO_WIDTH = 320;
	public static final int VIDEO_HEIGHT = 180;
	public enum PreviewFileType {
		PLAINTEXT, CODE, ZIP, CSV, IMAGE, NONE, TAB, HTML, PDF, IPYNB, VIDEO, MARKDOWN
	}
	public static final long MAX_HTML_FILE_SIZE = 5 * new Double(MB).longValue();
	
	PreviewWidgetView view;
	RequestBuilderWrapper requestBuilder;
	SynapseJSNIUtils synapseJSNIUtils;
	SynapseAlert synapseAlert;
	SynapseClientAsync synapseClient;
	AuthenticationController authController;
	EntityBundle bundle;
	SynapseJavascriptClient jsClient;
	PortalGinInjector ginInjector;
	
	@Inject
	public PreviewWidget(PreviewWidgetView view, 
			RequestBuilderWrapper requestBuilder,
			SynapseJSNIUtils synapseJSNIUtils,
			SynapseAlert synapseAlert,
			SynapseClientAsync synapseClient,
			AuthenticationController authController,
			SynapseJavascriptClient jsClient,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.requestBuilder = requestBuilder;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseAlert = synapseAlert;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.authController = authController;
		this.ginInjector = ginInjector;
		this.jsClient = jsClient;
	}
	
	public PreviewFileType getPreviewFileType(CloudProviderFileHandleInterface previewHandle, FileHandle originalFileHandle) {
		PreviewFileType previewFileType = PreviewFileType.NONE;
		if (previewHandle != null && originalFileHandle != null) {
			String contentType = previewHandle.getContentType();
			if (contentType != null) {
				if (isRecognizedImageContentType(contentType)) {
					previewFileType = PreviewFileType.IMAGE;
				} else if (isTextType(contentType)) {
					//some kind of text
					if (isCSV(contentType)) {
						if (APPLICATION_ZIP.equals(originalFileHandle.getContentType()))
							previewFileType = PreviewFileType.ZIP;
						else
							previewFileType = PreviewFileType.CSV;
					}
					else if (isCSV(originalFileHandle.getContentType())){
						previewFileType = PreviewFileType.CSV;
					}
					else if (isTAB(contentType) || isTAB(originalFileHandle.getContentType())) {
						previewFileType = PreviewFileType.TAB;
					}
					else {
						previewFileType = PreviewFileType.PLAINTEXT;
					}
				} else if (isPDF(contentType)) {
					previewFileType = PreviewFileType.PDF;
				}
			}
		}
		return previewFileType;
	}
	
	public PreviewFileType getOriginalFileType(FileHandle originalFileHandle) {
		if (originalFileHandle != null && originalFileHandle instanceof S3FileHandle) {
			String contentType = originalFileHandle.getContentType();
			String fileName = originalFileHandle.getFileName();
			if (VideoConfigEditor.isRecognizedVideoFileName(fileName)) {
				return PreviewFileType.VIDEO;
			} else if (fileName != null && fileName.toLowerCase().endsWith("ipynb")) {
				return PreviewFileType.IPYNB;
			} else if (contentType != null && isRecognizedImageContentType(contentType)) {
				return PreviewFileType.IMAGE;
			} else if (contentType != null && isHTML(contentType)) {
				return PreviewFileType.HTML;
			} else if (contentType != null && isPDF(contentType)) {
				return PreviewFileType.PDF;
			} else if (isRecognizedCodeFileName(fileName) || isWebRecognizedCodeFileName(fileName)) {
				return PreviewFileType.CODE;
			} else if (fileName != null && (fileName.toLowerCase().endsWith(".md") || fileName.toLowerCase().endsWith(".rmd"))) {
				return PreviewFileType.MARKDOWN;
			}
		} 
		return PreviewFileType.NONE;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor,
			Callback widgetRefreshRequired, 
			Long wikiVersionInView) {
		//get the entity id and version from the wiki widget parameters
		view.clear();
		String entityId = widgetDescriptor.get(WidgetConstants.WIDGET_ENTITY_ID_KEY);
		String version = widgetDescriptor.get(WidgetConstants.WIDGET_ENTITY_VERSION_KEY);
		configure(entityId, version);
	}
	
	public void configure(String entityId, String version) {
		if (version == null && entityId.contains(".")) {
			String[] tokens = entityId.split("\\.");
			entityId = tokens[0];
			version = tokens[1];
		}
		EntityBundleRequest bundleRequest = new EntityBundleRequest();
		bundleRequest.setIncludeEntity(true);
		bundleRequest.setIncludeFileHandles(true);
		AsyncCallback<EntityBundle> entityBundleCallback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.addSynapseAlertWidget(synapseAlert.asWidget());
				synapseAlert.handleException(caught);
			}
			@Override
			public void onSuccess(EntityBundle bundle) {
				configure(bundle);
			}
		};
		if (EntityPresenter.isValidEntityId(entityId)) {
			if (version == null) {
				jsClient.getEntityBundle(entityId, bundleRequest, entityBundleCallback);
			} else {
				jsClient.getEntityBundleForVersion(entityId, Long.parseLong(version), bundleRequest, entityBundleCallback);	
			}
		} else {
			view.addSynapseAlertWidget(synapseAlert.asWidget());
			synapseAlert.showError("Preview error: " + entityId + " does not appear to be a valid Synapse identifier.");
		}
	}
	
	public void configure(EntityBundle bundle) {
		this.bundle = bundle;
		view.clear();
		//if not logged in, don't even try to load the preview.  Just direct user to log in.
		if (!synapseAlert.isUserLoggedIn()) {
			view.addSynapseAlertWidget(synapseAlert.asWidget());
			synapseAlert.showLogin();
		} else if (bundle != null) {
			// SWC-2652: follow Link
			if (bundle.getEntity() instanceof Link) {
				// configure based on target
				Reference ref = ((Link)bundle.getEntity()).getLinksTo();
				String targetVersion = ref.getTargetVersionNumber() == null ? null : ref.getTargetVersionNumber() + "";
				configure(ref.getTargetId(), targetVersion);
				return;
			}
			if (!(bundle.getEntity() instanceof FileEntity)) {
				//not a file!
				view.addSynapseAlertWidget(synapseAlert.asWidget());
				synapseAlert.showError("Preview unavailable for \"" + bundle.getEntity().getName() + "\" ("+bundle.getEntity().getId()+")");
			} else {
				renderFilePreview(bundle);
			}
		}
	}
	
	private void renderFilePreview(EntityBundle bundle) {
		CloudProviderFileHandleInterface previewFileHandle = DisplayUtils.getPreviewFileHandle(bundle);
		FileHandle originalFileHandle = DisplayUtils.getFileHandle(bundle);
		PreviewFileType originalFileHandlePreviewType = getOriginalFileType(originalFileHandle);
		if (originalFileHandlePreviewType != PreviewFileType.NONE) {
			renderFilePreview(originalFileHandlePreviewType, originalFileHandle);	
		} else if (previewFileHandle != null) {
			PreviewFileType previewType = getPreviewFileType(previewFileHandle, originalFileHandle);
			renderFilePreview(previewType, previewFileHandle);
		} else {
			view.showNoPreviewAvailable(bundle.getEntity().getId(), ((FileEntity)bundle.getEntity()).getVersionNumber());
		}
	}
	
	private void renderFilePreview(PreviewFileType previewType, FileHandle fileHandleToShow) {
		final FileEntity fileEntity = (FileEntity)bundle.getEntity();
		String contentType = fileHandleToShow.getContentType();
		switch(previewType) {
			case IMAGE :
				//add a html panel that contains the image src from the attachments server (to pull asynchronously)
				//create img
				String fullFileUrl = DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(), ((Versionable)fileEntity).getVersionNumber(), false);
				view.setImagePreview(fullFileUrl);
				break;
			case PDF :
				// use pdf.js to view
				PDFPreviewWidget pdfPreviewWidget = ginInjector.getPDFPreviewWidget();
				pdfPreviewWidget.configure(bundle.getEntity().getId(), fileHandleToShow);
				view.setPreviewWidget(pdfPreviewWidget);
				break;
			case IPYNB :
				NbConvertPreviewWidget nbConvertPreviewWidget = ginInjector.getNbConvertPreviewWidget();
				nbConvertPreviewWidget.configure(bundle.getEntity().getId(), fileHandleToShow);
				view.setPreviewWidget(nbConvertPreviewWidget);
				break;
			case VIDEO :
				VideoWidget videoWidget = ginInjector.getVideoWidget();
				videoWidget.configure(bundle.getEntity().getId(), fileHandleToShow.getFileName(), VIDEO_WIDTH, VIDEO_HEIGHT);
				view.setPreviewWidget(videoWidget);
				break;
			case HTML :
				if (fileHandleToShow.getContentSize() != null && fileHandleToShow.getContentSize() < MAX_HTML_FILE_SIZE) {
					HtmlPreviewWidget htmlPreviewWidget = ginInjector.getHtmlPreviewWidget();
					htmlPreviewWidget.configure(bundle.getEntity().getId(), fileHandleToShow);
					view.setPreviewWidget(htmlPreviewWidget);
				} else {
					view.showNoPreviewAvailable(bundle.getEntity().getId(), ((FileEntity)bundle.getEntity()).getVersionNumber());
				}
				
				break;
			default :
				view.showLoading();
				boolean isGetPreviewFile = !fileHandleToShow.getId().equals(fileEntity.getDataFileHandleId());
				//must be a text type of some kind
				//try to load the text of the preview, if available
				jsClient.getFileEntityTemporaryUrlForVersion(fileEntity.getId(), ((Versionable)fileEntity).getVersionNumber(), isGetPreviewFile, new AsyncCallback<String>() {
					@Override
					public void onFailure(Throwable caught) {
						view.addSynapseAlertWidget(synapseAlert.asWidget());
						synapseAlert.handleException(caught);
					}
					@Override
					public void onSuccess(String url) {
						requestBuilder.configure(RequestBuilder.GET, url);
						// SWC-5014: Setting the request header "Content-Type" messes up the signed string verification in Google storage files (causes a 403). 
						// We must set another request header so RequestBuilder does not auto-set "Content-Type" to "text/plain; charset=utf-8"
						// (see http://www.gwtproject.org/javadoc/latest/com/google/gwt/http/client/RequestBuilder.html#send--)
						requestBuilder.setHeader("Sec-Fetch-Mode", "cors");
						try {
							requestBuilder.sendRequest(null, new RequestCallback() {
								public void onError(final Request request, final Throwable e) {
									view.addSynapseAlertWidget(synapseAlert.asWidget());
									synapseAlert.handleException(e);
								}
								public void onResponseReceived(final Request request, final Response response) {
									// if this response is not for the file entity associated to this preview widget, then ignore it!
									if (!fileEntity.equals(bundle.getEntity())) {
										return;
									}
									//add the response text
									int statusCode = response.getStatusCode();
									// if it's a 200 level response, it's OK
									if (statusCode > 199 && statusCode < 300) {
										String responseText = response.getText();
										if (responseText != null && responseText.length() > 0) {
											if (responseText.length() > MAX_LENGTH) {
												responseText = responseText.substring(0, MAX_LENGTH) + "...";
											}
											switch(previewType) {
												case CODE :
													String codePreview = SafeHtmlUtils.htmlEscapeAllowEntities(responseText);
													String extension = ContentTypeUtils.getExtension(fileHandleToShow.getFileName());
													view.setCodePreview(codePreview, getLanguage(extension));
													break;
												case MARKDOWN :
													MarkdownWidget markdownWidget = ginInjector.getMarkdownWidget();
													markdownWidget.configure(responseText);
													view.setPreviewWidget(markdownWidget);
													break;
												case CSV :
													parseCsv(responseText, ',');
													break;
												case TAB :
													parseCsv(responseText, '\t');
													break;
												case PLAINTEXT :
												case ZIP :
													view.setTextPreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));
													break;
												default :
											}
										}
									} else {
										String statusText = response.getStatusText();
										if (statusText == null || statusText.length() == 0) {
											statusText = "Unable to directly download a preview for " + fileEntity.getName();
										}
										synapseAlert.consoleError("Attempt to download \"" + fileHandleToShow.getFileName() + "\" (file handle ID = " + fileHandleToShow.getId() + ") failed with status code = " + response.getStatusCode());
										view.addSynapseAlertWidget(synapseAlert.asWidget());
										synapseAlert.showError(statusText);
									}
								}
							});
						} catch (final Exception e) {
							view.addSynapseAlertWidget(synapseAlert.asWidget());
							synapseAlert.handleException(e);
						}
					}
				});
		}
	}
	
	public void parseCsv(String csvPreviewText, char delimiter) {
		synapseClient.parseCsv(csvPreviewText, delimiter, new AsyncCallback<ArrayList<String[]>>() {
			@Override
			public void onFailure(Throwable caught) {
				view.addSynapseAlertWidget(synapseAlert.asWidget());
				synapseAlert.handleException(caught);
			}
			public void onSuccess(ArrayList<String[]> rows) {
				view.setTablePreview(rows);
			};
		});
	}
	public String getLanguage(String extension) {
		if (extension.equals("cwl")) {
			return "yaml";
		} else if (extension.equals("wdl")) {
			return "nohighlight";
		} else {
			return extension;
		}
	}

	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void imagePreviewLoadFailed(ErrorEvent e) {
		//show the load error
		view.addSynapseAlertWidget(synapseAlert);
		synapseAlert.showError("Unable to load image preview");
	}
	
	public void addStyleName(String style) {
		view.addStyleName(style);
	}
}
