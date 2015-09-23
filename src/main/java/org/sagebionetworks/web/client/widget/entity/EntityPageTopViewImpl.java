package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTopViewImpl extends Composite implements EntityPageTopView {

	public interface Binder extends UiBinder<Widget, EntityPageTopViewImpl> {
	}
	
	@UiField
	Row projectMetaContainer;
	
	@UiField
	Div tabsUI;
	
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private ActionMenuWidget actionMenu;
	
	//project level info
	@UiField
	SimplePanel projectMetadataContainer;
	@UiField
	SimplePanel projectDescriptionContainer;
	@UiField
	SimplePanel projectActionMenuContainer;
	
	
	private Long versionNumber;
	private SynapseJSNIUtils synapseJSNIUtils;
	private EntityMetadata entityMetadata;
	private EntityActionController controller;

	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SynapseJSNIUtils synapseJSNIUtils,
			PortalGinInjector ginInjector, 
			CookieProvider cookies) {
		this.entityMetadata = entityMetadata;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	

	
	@Override
	public void setEntityBundle(EntityBundle bundle, UserProfile userProfile,
			String entityTypeDisplay, Long versionNumber, Synapse.EntityArea area, String areaToken, EntityHeader projectHeader, String wikiPageId) {
		this.versionNumber = versionNumber;
		this.currentArea = area;
		DisplayUtils.hide(adminListItem);
		clearContent();
		hideTabContent();
		
		synapseJSNIUtils.setPageTitle(bundle.getEntity().getName() + " - " + bundle.getEntity().getId());
		synapseJSNIUtils.setPageDescription(bundle.getEntity().getDescription());

	}
	
	@Override
	public void setProjectMetadata(Widget w) {
		projectMetadataContainer.setWidget(w);
	}
	
	private void clearContent() {
		projectMetadataContainer.clear();
		projectDescriptionContainer.clear();
		projectActionMenuContainer.clear();
		
		fileBreadcrumbContainer.clear();
		fileTitlebarContainer.setVisible(false);
		folderTitlebarContainer.setVisible(false);
		tableTitlebarContainer.setVisible(false);
		fileMetadataContainer.clear();
		fileActionMenuContainer.clear();
		fileDescriptionContainer.clear();
		fileBrowserContainer.clear();
		filesWikiPageContainer.clear();
		filePreviewContainer.clear();
		fileProvenanceContainer.clear();
		fileProgrammaticClientsContainer.clear();
		fileModifiedAndCreatedContainer.clear();
		wikiPageContainer.clear();
		
		tableBreadcrumbContainer.clear();
		tableMetadataContainer.clear();
		tableActionMenuContainer.clear();
		tableWidgetContainer.clear();
		tableModifiedAndCreatedContainer.clear();
		tableListWidgetContainer.setVisible(false);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		};
		projectFilesBrowser.setEntityUpdatedHandler(handler);
		folderFilesBrowser.setEntityUpdatedHandler(handler);
		entityMetadata.setEntityUpdatedHandler(handler);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		fileTitleBar.clearState();
	}

	@Override
	public void configureProjectActionMenu(EntityBundle bundle, String wikiPageId) {
		projectActionMenuContainer.clear();
		ActionMenuWidget actionMenu = createEntityActionMenu(bundle, wikiPageId);
		projectActionMenuContainer.add(actionMenu.asWidget());
	}

	// Render the Project entity
	private void renderProjectEntity(final EntityBundle bundle,
			String entityTypeDisplay,
			Synapse.EntityArea area, String wikiPageId) {		
		// tab container
		setTabSelected(area, false);

		// Project header: Metadata & Description
		entityMetadata.setEntityBundle(bundle, versionNumber); 		
		projectMetadataContainer.add(entityMetadata.asWidget());
		projectDescriptionContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, true));

		// Wiki Tab: Wiki
		addWikiPageWidget(wikiPageContainer, bundle, wikiPageId, area);
		
		// File Tab: Files, Annotations & old
		fileBrowserContainer.add(configureFilesBrowser(projectFilesBrowser, bundle.getEntity(), bundle.getPermissions().getCanCertifiedUserAddChild(), bundle.getPermissions().getIsCertifiedUser()));
		// Created By/Modified By
		fileModifiedAndCreatedContainer.add(createModifiedAndCreatedWidget(bundle.getEntity(), true));

		// Tables Tab
		tableListWidget.configure(bundle);
		tableListWidgetContainer.setVisible(true);
		
		// Admin Tab: evaluations
		evaluationList.configure(bundle.getEntity().getId(), new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isVisible) {
				if (isVisible)
					DisplayUtils.show(adminListItem);
			}
		});
		
		// ActionMenu
		projectActionMenuContainer.clear();
		ActionMenuWidget actionMenu = createEntityActionMenu(bundle, wikiPageId);
		projectActionMenuContainer.add(actionMenu.asWidget());
	}

	private void addWikiPageWidget(SimplePanel container, EntityBundle bundle, String wikiPageId, final Synapse.EntityArea area) {
		wikiPageWidget.clear();
		if (DisplayUtils.isWikiSupportedType(bundle.getEntity())) {
			Widget wikiW = wikiPageWidget.asWidget();
			final SimplePanel wrapper = new SimplePanel(wikiW);
			wrapper.addStyleName("panel panel-default panel-body margin-bottom-0-imp");
			if(!isProject) 
				wrapper.addStyleName("margin-top-15");
			container.add(wrapper);
			boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
			wikiPageWidget.configure(new WikiPageKey(bundle.getEntity().getId(), ObjectType.ENTITY.toString(), wikiPageId, versionNumber), canEdit, new WikiPageWidget.Callback() {
				@Override
				public void pageUpdated() {
					presenter.fireEntityUpdatedEvent();
				}
				@Override
				public void noWikiFound() {
					if(isProject) {
						//if wiki area not specified and no wiki found, show Files tab instead for projects 
						// Note: The fix for SWC-1785 was to set this check to area == null.  Prior to this change it was area != WIKI.
						if(area == null) {							
							setTabSelected(Synapse.EntityArea.FILES, false);
						}
					} else {
						// hide description area, and add description command in the Tools menu
						wrapper.setVisible(false);
					}
				}
			}, true);
		}
	}
	
	private Widget createProvenanceWidget(EntityBundle bundle, boolean fullWidth) {
		// Create the property body
	    // the headers for properties.
		ProvenanceWidget provenanceWidget = ginInjector.getProvenanceRenderer();						
		
		Map<String,String> configMap = new HashMap<String,String>();
		Long version = bundle.getEntity() instanceof Versionable ? ((Versionable)bundle.getEntity()).getVersionNumber() : null; 
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(bundle.getEntity().getId(), version));
		configMap.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, Integer.toString(1));		
		configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, Integer.toString(WIDGET_HEIGHT_PX-84));
	    provenanceWidget.configure(null, configMap, null, null);
	    final Widget provViewWidget = provenanceWidget.asWidget(); 
	    FlowPanel lc = new FlowPanel();
	    lc.addStyleName("highlight-box");
	    lc.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, DisplayConstants.PROVENANCE);
	    lc.add(provViewWidget);
	    SimplePanel wrapper = new SimplePanel(lc);
	    String width = fullWidth ? "col-md-12" : "col-md-6";
	    wrapper.addStyleName(width);
		return wrapper;
	}
	
	private void createProgrammaticClientsWidget(EntityBundle bundle, Long versionNumber) {
		fileProgrammaticClientsContainer.clear();
		String id = bundle.getEntity().getId();
		rLoadWidget.configure(id, versionNumber);
		pythonLoadWidget.configure(id);
		javaLoadWidget.configure(id);
		commandLineLoadWidget.configure(id);
		fileProgrammaticClientsContainer.add(rLoadWidget.asWidget());
		fileProgrammaticClientsContainer.add(pythonLoadWidget.asWidget());
		fileProgrammaticClientsContainer.add(javaLoadWidget.asWidget());
		fileProgrammaticClientsContainer.add(commandLineLoadWidget.asWidget());		
	}

	private Widget configureFilesBrowser(FilesBrowser filesBrowser, Entity entity, boolean canCertifiedUserAddChild, boolean isCertifiedUser) {
		filesBrowser.configure(entity.getId(), canCertifiedUserAddChild, isCertifiedUser);
		return filesBrowser.asWidget();
	}
	
	private Widget createDescriptionWidget(final EntityBundle bundle, String entityTypeDisplay, boolean showWhenEmpty) {
		final FlowPanel lc = new FlowPanel();
		String description = bundle.getEntity().getDescription();
	
		if(!showWhenEmpty) {
			if(description == null || "".equals(description))
				return lc;
		}
		
		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div style=\"clear: left;\"></div>")));
	
		// Add the description body
	    if(description != null && !("".equals(description))) {
    		Label plainDescriptionText = new Label();
    		plainDescriptionText.addStyleName("wiki-description");
    		plainDescriptionText.setText(description);
    		lc.add(plainDescriptionText);
	    }
	    
   		return lc;
	}
	
	/**
	 * Create a new action menu for an entity.
	 * @param bundle
	 * @return
	 */
	private ActionMenuWidget createEntityActionMenu(EntityBundle bundle, String wikiPageId) {
		actionMenu = ginInjector.createActionMenuWidget();
		// Create a menu
		// Create a controller.
		controller = ginInjector.createEntityActionController();
		actionMenu.addControllerWidget(controller.asWidget());
		controller.configure(actionMenu, bundle, wikiPageId, new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		});
		annotationsShown = false;
		actionMenu.addActionListener(Action.TOGGLE_ANNOTATIONS, new ActionListener() {
			@Override
			public void onAction(Action action) {
				annotationsShown = !annotationsShown;
				controller.onAnnotationsToggled(annotationsShown);
				entityMetadata.setAnnotationsVisible(annotationsShown);
			}
		});
		fileHistoryShown = false;
		actionMenu.addActionListener(Action.TOGGLE_FILE_HISTORY, new ActionListener() {
			@Override
			public void onAction(Action action) {
				fileHistoryShown = !fileHistoryShown;
				controller.onFileHistoryToggled(fileHistoryShown);
				entityMetadata.setFileHistoryVisible(fileHistoryShown);
			}
		});
		return actionMenu;
	}

	@Override
	public void setFileHistoryVisible(boolean isVisible) {
		fileHistoryShown = isVisible;
		if (controller != null) {
			controller.onFileHistoryToggled(isVisible);
		}
	}
	
	@Override
	public void setTabs(Widget w) {
		tabsUI.clear();
		tabsUI.add(w);
	}
}
