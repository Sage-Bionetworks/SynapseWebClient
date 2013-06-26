package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.AnimationProtector;
import org.sagebionetworks.web.client.utils.AnimationProtectorViewImpl;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.GridFineSelectionModel;
import org.sagebionetworks.web.client.widget.IconMenu;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadataViewImpl extends Composite implements EntityMetadataView {

	private static final String VERSION_KEY_ID = "id";
	private static final String VERSION_KEY_NUMBER = "number";
	private static final String VERSION_KEY_LABEL = "label";
	private static final String VERSION_KEY_COMMENT = "comment";
	private static final String VERSION_KEY_MOD_ON = "modifiedOn";
	private static final String VERSION_KEY_MOD_BY = "modifiedBy";

	private static final int VERSION_LIMIT = 100;
	private FavoriteWidget favoriteWidget;
	private DoiWidget doiWidget;
	private PortalGinInjector ginInjector;
	//if true, will expand file history after it has completed loading
	private boolean isShowingOlderVersion;
	
	private boolean canEdit;
	
	interface EntityMetadataViewImplUiBinder extends UiBinder<Widget, EntityMetadataViewImpl> {
	}
	
	private static EntityMetadataViewImplUiBinder uiBinder = GWT
			.create(EntityMetadataViewImplUiBinder.class);

	interface Style extends CssResource {
		String limitedHeight();
		String currentVersion();
	}

	@UiField
	Style style;

	@UiField
	HTMLPanel versions;
	@UiField
	HTMLPanel entityNamePanel;
	@UiField
	HTMLPanel detailedMetadata;
	
	@UiField
	HTMLPanel dataUseContainer;
	@UiField
	HTMLPanel sharingContainer;

	@UiField
	Image entityIcon;
	@UiField
	SpanElement entityName;
	@UiField
	SpanElement entityId;
	@UiField
	SimplePanel favoritePanel;

	@UiField
	SimplePanel doiPanel;

	@UiField
	LayoutContainer previousVersions;

	@UiField
	InlineLabel allVersions;

	@UiField
	Hyperlink currentVersionLink;
	
	private Presenter presenter;

	@UiField(provided = true)
	final IconsImageBundle icons;

	private SynapseJSNIUtils synapseJSNIUtils;

	/**
	 * This variable should ONLY be set by the load call in the VersionsRpcProxy
	 * The previousVersions GridCellRenderer uses it to determine how to set the top
	 * row link.
	 */
	private boolean previousVersionsHasNotPaged = true;

	/**
	 * This variable should ONLY be set by the load call in the VersionsRpcProxy
	 * It is used to set the selection (i.e. highlighting) for the currently being
	 * viewed model in the grid.
	 */
	private BaseModelData currentModel;
	private Element scrollToElement;
	
	// Widget variables
	private PagingToolBar vToolbar;
	private Grid<BaseModelData> vGrid;
	private AnimationProtector versionAnimation;
	private static DateTimeFormat shortDateFormat = DateTimeFormat.getShortDateFormat();
	private static final int GRID_MARGIN = 0;
	
	@Inject
	public EntityMetadataViewImpl(IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils, FavoriteWidget favoriteWidget, DoiWidget doiWidget, PortalGinInjector ginInjector) {
		this.icons = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.favoriteWidget = favoriteWidget;
		this.doiWidget = doiWidget;
		this.ginInjector = ginInjector;
		initWidget(uiBinder.createAndBindUi(this));

		versionAnimation = new AnimationProtector(new AnimationProtectorViewImpl(allVersions, previousVersions));
		FxConfig hideConfig = new FxConfig(400);
		hideConfig.setEffectCompleteListener(new Listener<FxEvent>() {
			@Override
			public void handleEvent(FxEvent be) {
				// This call to layout is necessary to force the scroll bar to appear on page-load
				previousVersions.layout(true);
				allVersions.setText(DisplayConstants.SHOW_VERSIONS);
			}
		});
		versionAnimation.setHideConfig(hideConfig);
		FxConfig showConfig = new FxConfig(400);
		showConfig.setEffectCompleteListener(new Listener<FxEvent>() {
			@Override
			public void handleEvent(FxEvent be) {
				// This call to layout is necessary to force the scroll bar to appear on page-load
				previousVersions.layout(true);
				allVersions.setText(DisplayConstants.HIDE_VERSIONS);
				if (scrollToElement != null)
					scrollToElement.scrollIntoView();
			}
		});
		versionAnimation.setShowConfig(showConfig);

		allVersions.setText(DisplayConstants.SHOW_VERSIONS);

		vToolbar = new PagingToolBar(VERSION_LIMIT);
		vToolbar.setSpacing(2);
		vToolbar.insert(new SeparatorToolItem(), vToolbar.getItemCount() - 2);

		vGrid = new Grid<BaseModelData>(new ListStore<BaseModelData>(),
										new ColumnModel(new ArrayList<ColumnConfig>()));

		GridFineSelectionModel<BaseModelData> sm = new GridFineSelectionModel<BaseModelData>();
		sm.setLocked(false);
		sm.setUserLocked(true);
		sm.setFiresEvents(false);
		
		vGrid.getView().setForceFit(true);
		vGrid.getView().setEmptyText("Sorry, no versions were found.");
		vGrid.setLayoutData(new FitLayout());
		vGrid.setStateful(false);
		vGrid.setLoadMask(true);
		vGrid.setAutoWidth(true);
		vGrid.setBorders(false);
		vGrid.setTrackMouseOver(false);
		vGrid.setHideHeaders(true);
		vGrid.setStripeRows(false);
		//and disable row selection
		vGrid.disableEvents(true);
		vGrid.disableTextSelection(true);
		
		ContentPanel cp = new ContentPanel();
		cp.setLayout(new FitLayout());
		cp.setBodyBorder(true);
		cp.setButtonAlign(HorizontalAlignment.CENTER);
		cp.setHeaderVisible(false);
		cp.setHeight(155);
		cp.setBottomComponent(vToolbar);
		cp.add(vGrid);

		setPreviousVersions(cp);
		
		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoriteWidget.asWidget());
		
		doiPanel.addStyleName("inline-block");
		doiPanel.setWidget(doiWidget.asWidget());
		
		previousVersions.setLayout(new FlowLayout(GRID_MARGIN));
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, boolean canEdit, boolean isShowingOlderVersion) {
		clearmeta();
		this.isShowingOlderVersion = isShowingOlderVersion;
		this.canEdit = canEdit;
		Entity e = bundle.getEntity();

		AbstractImagePrototype synapseIconForEntity = AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(e, DisplayUtils.IconSize.PX24, icons));
		synapseIconForEntity.applyTo(entityIcon);
		
		setEntityName(e.getName());
		setEntityId(e.getId());
		
		currentVersionLink.setTargetHistoryToken(DisplayUtils.getSynapseHistoryTokenNoHash(e.getId()));
		currentVersionLink.setVisible(isShowingOlderVersion);
		
		sharingContainer.clear();
		sharingContainer.add(DisplayUtils.getShareSettingsDisplay("<span style=\"margin-right: 5px;\" class=\"boldText\">Sharing:</span>", bundle.getPermissions().getCanPublicRead(), synapseJSNIUtils));
			
		dataUseContainer.clear();
		if(bundle.getPermissions().getCanPublicRead()) {
			Widget dataUse = createRestrictionWidget();
			if(dataUse != null) {
				dataUseContainer.setVisible(true);
				dataUseContainer.add(dataUse);
			} else {
				dataUseContainer.setVisible(false);
			}		
		} else {
			dataUseContainer.setVisible(false);
		}
		
		setVersionsVisible(false);
		Long versionNumber = null;
		if (e instanceof Versionable) {
			setVersionsVisible(true);
			Versionable vb = (Versionable) e;
			versionAnimation.hide();
			versionNumber = vb.getVersionNumber();
			setEntityVersions(vb);
		}
		favoriteWidget.configure(bundle.getEntity().getId());
		
		//doi widget
		doiWidget.configure(bundle.getEntity().getId(), bundle.getPermissions().getCanEdit(), versionNumber);
	}

	private void clearmeta() {
		dataUseContainer.clear();
		//reset versions ui
		setVersionsVisible(false);
		previousVersions.setVisible(false);
		allVersions.setText(DisplayConstants.SHOW_VERSIONS);
		doiWidget.clear();
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}
	
	@Override
	public void setDetailedMetadataVisible(boolean visible) {
		detailedMetadata.setVisible(visible);
	}
	
	@Override
	public void setEntityNameVisible(boolean visible) {
		this.entityNamePanel.setVisible(visible);
	}
	
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	public void setEntityName(String text) {
		entityName.setInnerText(text);
	}

	public void setEntityId(String text) {
		entityId.setInnerText(text);
	}


	public void setPreviousVersions(ContentPanel versions) {
		previousVersions.add(versions);
		previousVersions.layout(true);
	}

	public void setVersionsVisible(boolean visible) {
		versions.setVisible(visible);
	}

	public Style getStyle() {
		return style;
	}

	public void setEntityVersions(final Versionable entity) {
		// create bottom paging toolbar
		currentModel = null;
		scrollToElement = null;
		RpcProxy<PagingLoadResult<BaseModelData>> proxy = new RpcProxy<PagingLoadResult<BaseModelData>>() {

			@Override
			protected void load(
					final Object loadConfig,
					final AsyncCallback<PagingLoadResult<BaseModelData>> callback) {
				final int offset = ((PagingLoadConfig) loadConfig).getOffset();
				int limit = ((PagingLoadConfig) loadConfig).getLimit();
				previousVersionsHasNotPaged = (offset == 0);
				presenter.loadVersions(entity.getId(), offset, limit,
						new AsyncCallback<PaginatedResults<VersionInfo>>() {
							@Override
							public void onSuccess(
									PaginatedResults<VersionInfo> result) {
								List<BaseModelData> dataList = new ArrayList<BaseModelData>();
								for (VersionInfo version : result.getResults()) {
									BaseModelData model = new BaseModelData();
									model.set(
											EntityMetadataViewImpl.VERSION_KEY_ID,
											version.getId());
									model.set(
											EntityMetadataViewImpl.VERSION_KEY_NUMBER,
											version.getVersionNumber());
									model.set(
											EntityMetadataViewImpl.VERSION_KEY_LABEL,
											version.getVersionLabel());
									model.set(
											EntityMetadataViewImpl.VERSION_KEY_COMMENT,
											version.getVersionComment());
									model.set(
											EntityMetadataViewImpl.VERSION_KEY_MOD_ON,
											version.getModifiedOn());
									model.set(
											EntityMetadataViewImpl.VERSION_KEY_MOD_BY,
											version.getModifiedByPrincipalId());

									if (entity.getVersionNumber().equals(version.getVersionNumber())) {
										currentModel = model;
									}
										
									dataList.add(model);
								}
								int totalResultCount = (int)result.getTotalNumberOfResults();
								if (currentModel == null) {
									//we have not found the current model.  keep paging until it's found
									//we should keep paging until it is found, or another page would be over the record count
									int nextPageOffset = offset+VERSION_LIMIT;
									boolean continueFetching = !(nextPageOffset > totalResultCount);
									if (continueFetching) {
										((PagingLoadConfig)loadConfig).setOffset(offset + VERSION_LIMIT);
										load(loadConfig, callback);
									}
								}
								else {
									PagingLoadResult<BaseModelData> loadResultData = new BasePagingLoadResult<BaseModelData>(dataList);
									
									loadResultData.setTotalLength(totalResultCount);
									vToolbar.setVisible(loadResultData.getTotalLength() > VERSION_LIMIT);
	
									loadResultData.setOffset(offset);
									callback.onSuccess(loadResultData);
									scrollToElement = vGrid.getView().getRow(currentModel);
									if (isShowingOlderVersion)
										setFileHistoryVisible(true);
								}
							}

							@Override
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}
						});
			}

		};
		final BasePagingLoader<PagingLoadResult<ModelData>> loader = new BasePagingLoader<PagingLoadResult<ModelData>>(
				proxy);
		loader.setRemoteSort(false);
		loader.setReuseLoadConfig(true);
		vToolbar.bind(loader);

		// add initial data to the store
		ListStore<BaseModelData> store = new ListStore<BaseModelData>(loader);
		vGrid.reconfigure(store, setupColumnModel(entity));
		loader.load();
		vGrid.addListener(Events.Attach, new Listener<GridEvent<ModelData>>() {
			public void handleEvent(GridEvent<ModelData> be) {
				BasePagingLoadConfig config = new BasePagingLoadConfig();
				config.setLimit(VERSION_LIMIT);
				config.setOffset(0);
				loader.load(config);
			}
		});
		
	}

	private GridCellRenderer<BaseModelData> configureVersionsGridCellRenderer(final Versionable vb) {
		GridCellRenderer<BaseModelData> cellRenderer = new GridCellRenderer<BaseModelData>() {
			@Override
			public Object render(BaseModelData model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
				boolean currentVersion = vb.getVersionNumber().equals(model.get(VERSION_KEY_NUMBER));
				boolean topVersion = previousVersionsHasNotPaged && rowIndex == 0;

				config.css = currentVersion ? "highlighted-version" :  "not-highlighted-version";
				
				if (property.equals(VERSION_KEY_LABEL)) {
					if (currentVersion) {
						InlineLabel label = new InlineLabel("Version "
								+ model.get(VERSION_KEY_LABEL));
						label.addStyleName(style.currentVersion());
						return label;
					} else {
						Hyperlink link = new Hyperlink();
						if (topVersion) {
							// This is so the user can easily get back to the non-readonly page
							link.setTargetHistoryToken(DisplayUtils
								.getSynapseHistoryTokenNoHash(vb.getId()));
						} else {
							link.setTargetHistoryToken(DisplayUtils
									.getSynapseHistoryTokenNoHash(vb.getId(),
											(Long) model.get(VERSION_KEY_NUMBER)));
						}
						link.setText("Version " + model.get(VERSION_KEY_LABEL));
						link.setStyleName("link");
						return link;
					}
				} else if (property.equals(VERSION_KEY_COMMENT)) {
					String comment;
					if (null != model.get(VERSION_KEY_COMMENT))
						comment = model.get(VERSION_KEY_COMMENT).toString();
					else
						return null;
					// By default, overflow on a gridcell, results in eliding of the text.
					// This label and setTitle makes it to so that hovering will show the full comment.
					InlineLabel label = new InlineLabel(comment);
					label.setTitle(comment);
					return label;
				} else if (property.equals(VERSION_KEY_NUMBER)) {
					return setupIconMenu(model, topVersion);
				} else if (property.equals(VERSION_KEY_MOD_BY)) {
					UserBadge badge = ginInjector.getUserBadgeWidget();
					badge.configure((String)model.get(VERSION_KEY_MOD_BY));
					return badge.asWidget();
				} else if (property.equals(VERSION_KEY_MOD_ON)) {
					return EntityMetadataViewImpl.shortDateFormat.format((Date)model.get(property));
				} else if (model.get(property) != null) {
					return model.get(property).toString();

				} else {
					return null;
				}
			}

			private Object setupIconMenu(final ModelData model, boolean currentVersion) {
				IconMenu menu = new IconMenu();
				if (canEdit) {
					final String versionLabel = (String) model.get(VERSION_KEY_LABEL);
					menu.addIcon(icons.deleteButtonGrey16(), "Delete Version",
							new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									MessageBox.confirm(DisplayConstants.LABEL_DELETE + " " + versionLabel,
											DisplayConstants.PROMPT_SURE_DELETE + " version?",
											new Listener<MessageBoxEvent>() {
										@Override
										public void handleEvent(MessageBoxEvent be) {
											Button btn = be.getButtonClicked();
											if(Dialog.YES.equals(btn.getItemId())) {
												presenter.deleteVersion(
														(String) model.get(VERSION_KEY_ID),
														(Long) model.get(VERSION_KEY_NUMBER));
											}
										}
									});
								}
							});
					if (currentVersion) {
						menu.addIcon(icons.editGrey16(), "Edit Version Info",
								new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										NameAndDescriptionEditorDialog.showNameAndDescriptionDialog(
												(String) model.get(VERSION_KEY_LABEL),
												(String) model.get(VERSION_KEY_COMMENT),
												"Version",
												"Comment",
												new NameAndDescriptionEditorDialog.Callback() {
													@Override
													public void onSave(String version,
															String comment) {
														presenter.editCurrentVersionInfo(
																(String) model.get(VERSION_KEY_ID), version, comment);
													}
												});
									}
								});
					}
				}	
				return menu.asWidget();
			}
		};
		return cellRenderer;
	}

	private ColumnModel setupColumnModel(Versionable vb) {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		String[] keys =  {VERSION_KEY_LABEL, VERSION_KEY_MOD_BY, VERSION_KEY_MOD_ON , VERSION_KEY_COMMENT,  VERSION_KEY_NUMBER};
		String[] names = {"Version"        , "Modified By"      , "Modified On"     , "Comment"          ,   ""                };
		int[] widths =	 {100               , 220               , 70                , 200                ,   50                };
		
		if (keys.length != names.length || names.length != widths.length)
			throw new IllegalArgumentException("All configuration arrays must be the same length.");

		GridCellRenderer<BaseModelData> cellRenderer = configureVersionsGridCellRenderer(vb);
		for (int i = 0; i < keys.length; i++) {
			ColumnConfig colConfig = new ColumnConfig(keys[i], names[i], widths[i]);
			colConfig.setRenderer(cellRenderer);
			colConfig.setSortable(false);
			colConfig.setResizable(false);
			colConfig.setMenuDisabled(true);
			columns.add(colConfig);
		}

		return new ColumnModel(columns);
	}

	
	private Widget createRestrictionWidget() {
		if (!presenter.includeRestrictionWidget()) return null;
		boolean isAnonymous = presenter.isAnonymous();
		boolean hasAdministrativeAccess = false;
		boolean hasFulfilledAccessRequirements = false;
		String jiraFlagLink = null;
		if (!isAnonymous) {
			hasAdministrativeAccess = presenter.hasAdministrativeAccess();
			jiraFlagLink = presenter.getJiraFlagUrl();
		}
		RESTRICTION_LEVEL restrictionLevel = presenter.getRestrictionLevel();
		APPROVAL_TYPE approvalType = presenter.getApprovalType();
		String accessRequirementText = null;
		Callback touAcceptanceCallback = null;
		Callback requestACTCallback = null;
		Callback imposeRestrictionsCallback = presenter.getImposeRestrictionsCallback();
		Callback loginCallback = presenter.getLoginCallback();
		if (approvalType!=APPROVAL_TYPE.NONE) {
			accessRequirementText = presenter.accessRequirementText();
			if (approvalType==APPROVAL_TYPE.USER_AGREEMENT) {
				touAcceptanceCallback = presenter.accessRequirementCallback();
			} else { // APPROVAL_TYPE.ACT_APPROVAL
				// get the Jira link for ACT approval
				if (!isAnonymous) {
					requestACTCallback = new Callback() {
						@Override
						public void invoke() {
							Window.open(presenter.getJiraRequestAccessUrl(), "_blank", "");

						}
					};
				}
			}
			if (!isAnonymous) hasFulfilledAccessRequirements = presenter.hasFulfilledAccessRequirements();
		}
		return EntityViewUtils.createRestrictionsWidget(
				jiraFlagLink,
				isAnonymous,
				hasAdministrativeAccess,
				accessRequirementText,
				touAcceptanceCallback,
				requestACTCallback,
				imposeRestrictionsCallback,
				loginCallback,
				restrictionLevel,
				approvalType,
				hasFulfilledAccessRequirements,
				icons,
				synapseJSNIUtils);
	}

	@Override
	public void setFileHistoryVisible(boolean v) {
		boolean isV = versionAnimation.isVisible();
		//Only do the action if it's not already in that state.
		//That is, if v=true and isV=true, then do nothing.  if v=false and isV=false, do nothing.  Only do something if they differ (v=T and isV=F, or v=F and isV=T).
		if (isV ^ v) {
			if (v) {
				versionAnimation.show();
			} else
				versionAnimation.hide();
		}
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
}

	@Override
	public void clear() {
		clearmeta();
	}

}
