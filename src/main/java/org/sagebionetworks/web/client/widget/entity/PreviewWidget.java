package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidget implements PreviewWidgetView.Presenter{
	public static final String APPLICATION_ZIP = "application/zip";
	
	PreviewWidgetView view;
	RequestBuilderWrapper requestBuilder;
	SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public PreviewWidget(PreviewWidgetView view, RequestBuilderWrapper requestBuilder,SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.requestBuilder = requestBuilder;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	public Widget asWidget(EntityBundle bundle) {
		view.clear();
		PreviewFileHandle handle = FileTitleBar.getPreviewFileHandle(bundle);
		FileHandle originalFileHandle = FileTitleBar.getFileHandle(bundle);
		if (handle != null) {
			final String contentType = handle.getContentType();
			if (contentType != null) {
				FileEntity fileEntity = (FileEntity)bundle.getEntity();
				if (DisplayUtils.isRecognizedImageContentType(contentType)) {
					//add a html panel that contains the image src from the attachments server (to pull asynchronously)
					//create img
					view.setImagePreview(DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(), ((Versionable)fileEntity).getVersionNumber(), false), 
										DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(),  ((Versionable)fileEntity).getVersionNumber(), true));
				}
				else {
					final String originalFileName = originalFileHandle.getFileName();
					final String originalContentType = originalFileHandle.getContentType();
					final boolean isCode = ContentTypeUtils.isRecognizedCodeFileName(originalFileName);
					final boolean isTextType = DisplayUtils.isTextType(contentType);
					if (isTextType) {
						final boolean isCSV = DisplayUtils.isCSV(contentType);
						//try to load the text of the preview, if available
						//must have file handle servlet proxy the request to the endpoint (because of cross-domain access restrictions)
						requestBuilder.configure(RequestBuilder.GET,DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(),  ((Versionable)fileEntity).getVersionNumber(), true, true));
						
						try {
							requestBuilder.sendRequest(null, new RequestCallback() {
								public void onError(final Request request, final Throwable e) {
									view.showErrorMessage(e.getMessage());
								}
								public void onResponseReceived(final Request request, final Response response) {
									//add the response text
									int statusCode = response.getStatusCode();
									if (statusCode == Response.SC_OK) {
										String responseText = response.getText();
										if (responseText != null && responseText.length() > 0) {
											if (isCode) {
												view.setCodePreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));
											} else if (isCSV){
												if(APPLICATION_ZIP.equals(originalContentType)) {
													//show a tree instead
													FolderTreeModel model = getTreeModel(responseText);
												    TreeStore<ModelData> store = new TreeStore<ModelData>();
												    store.add(model.getChildren(), true);
												    view.setTreePreview(store);
												}
												else {
													view.setTablePreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));	
												}
											} else if (isTextType){
												view.setBlockQuotePreview(SafeHtmlUtils.htmlEscapeAllowEntities(responseText));
											}
										}
									}
								}
							});
						} catch (final Exception e) {
							view.showErrorMessage(e.getMessage());
						}
					}
				}
			}
		}
		return view.asWidget();
	}
	
	public FolderTreeModel getTreeModel(String zipContents) {
		FolderTreeModel root = new FolderTreeModel("root");
		if (zipContents != null && zipContents.length() > 0) {
			String[] allPaths = zipContents.split("\n");
			//process each path
			for (int i = 0; i < allPaths.length; i++) {
				String path = allPaths[i];
				String[] parts = path.split("/");
				FolderTreeModel currentParent = root;
				for (int j = 0; j < parts.length; j++) {
					if (j == parts.length - 1 && !path.endsWith("/")) {
						//leaf, add it to the current parent
						currentParent.add(new FileTreeModel(parts[j]));
					}
					else {
						FolderTreeModel folder = getChildFolderWithName(currentParent, parts[j]);
						if (folder == null) {
							folder = new FolderTreeModel(parts[j]);
							currentParent.add(folder);
						}
						currentParent = folder;
					}
				}
			}
		}
		return root;
	}
	
	public FolderTreeModel getChildFolderWithName(FolderTreeModel folder, String name) {
		FolderTreeModel returnChild = null;
		for (ModelData child : folder.getChildren()) {
			if (child instanceof FolderTreeModel && name.equals(((FolderTreeModel)child).getName())) {
				returnChild = (FolderTreeModel)child;
				break;
			}
		}
		return returnChild;
	}
	
	/**
	 * Set of simple model objects for use when displaying hierarchical data
	 */
	
	public class FolderTreeModel extends FileTreeModel {
		public FolderTreeModel() {
		}
		public FolderTreeModel(String name) {
			super(name);
		}
	}
	
	public class FileTreeModel extends BaseTreeModel {
		public FileTreeModel() {}

		public FileTreeModel(String name) {
			set("name", name);
		}

		public String getName() {
			return (String) get("name");
		}

		public String toString() {
			return getName();
		}
	}
}
