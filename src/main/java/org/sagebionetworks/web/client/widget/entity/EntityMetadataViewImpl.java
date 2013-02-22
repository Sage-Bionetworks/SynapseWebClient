package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.AnimationProtector;
import org.sagebionetworks.web.client.utils.AnimationProtectorViewImpl;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.GridFineSelectionModel;
import org.sagebionetworks.web.client.widget.IconMenu;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
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
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
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
	private static final int NAME_TIME_STUB_LENGTH = 7;
	
	private static String favoriteStarOffHtml;
	private static String favoriteStarHtml;
	

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
	HTMLPanel readOnly;
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
	HTMLPanel addedBy;
	@UiField
	HTMLPanel modifiedBy;
	@UiField
	SpanElement label;
	@UiField
	Anchor favoriteAnchor;

	@UiField
	LayoutContainer previousVersions;

	@UiField
	InlineLabel allVersions;

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

	// Widget variables
	private PagingToolBar vToolbar;
	private Grid<BaseModelData> vGrid;
	private AnimationProtector versionAnimation;
	boolean isFavorite = false;

	@Inject
	public EntityMetadataViewImpl(IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.icons = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;

		initWidget(uiBinder.createAndBindUi(this));

		versionAnimation = new AnimationProtector(new AnimationProtectorViewImpl(allVersions, previousVersions));
		FxConfig hideConfig = new FxConfig(400);
		hideConfig.setEffectCompleteListener(new Listener<FxEvent>() {
			@Override
			public void handleEvent(FxEvent be) {
				// This call to layout is necessary to force the scroll bar to appear on page-load
				previousVersions.layout(true);
				allVersions.setText(DisplayConstants.SHOW_VERSIONS);
				vGrid.getSelectionModel().select(currentModel, false);
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
				vGrid.getSelectionModel().select(currentModel, false);
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
		sm.setSelectionMode(SelectionMode.SINGLE);

		vGrid.setSelectionModel(sm);
		vGrid.getView().setForceFit(true);
		vGrid.getView().setEmptyText("Sorry, no versions were found.");
		vGrid.setLayoutData(new FitLayout());
		vGrid.setStateful(false);
		vGrid.setLoadMask(true);
		vGrid.setAutoWidth(true);
		vGrid.setBorders(false);
		vGrid.setStripeRows(true);

		ContentPanel cp = new ContentPanel();
		cp.setLayout(new FitLayout());
		cp.setBodyBorder(true);
		cp.setButtonAlign(HorizontalAlignment.CENTER);
		cp.setHeaderVisible(false);
		cp.setHeight(155);
		cp.setBottomComponent(vToolbar);
		cp.add(vGrid);

		setPreviousVersions(cp);
		
		favoriteStarHtml = AbstractImagePrototype.create(iconsImageBundle.star16()).getHTML();
		favoriteStarOffHtml = AbstractImagePrototype.create(iconsImageBundle.starEmpty16()).getHTML();
		
		favoriteAnchor = new Anchor();
		favoriteAnchor.setHTML(favoriteStarOffHtml);
		favoriteAnchor.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				setFavoriteIcon();
				presenter.setIsFavorite(isFavorite);
			}
		});
		
		previousVersions.setLayout(new FlowLayout(10));
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, boolean readOnly) {
		clear();

		Entity e = bundle.getEntity();

		AbstractImagePrototype synapseIconForEntity = AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(e, DisplayUtils.IconSize.PX24, icons));
		synapseIconForEntity.applyTo(entityIcon);
		
		setEntityName(e.getName());
		setEntityId(e.getId());
		boolean isLocationable = (e instanceof Locationable);
		
		this.readOnly.setVisible(readOnly);
		
		sharingContainer.clear();
		sharingContainer.add(createShareSettingsWidget(bundle.getPermissions().getCanPublicRead()));

		setCreatedBy(e.getCreatedBy(), synapseJSNIUtils.convertDateToSmallString(e.getCreatedOn()));
		setModified(e.getModifiedBy(), synapseJSNIUtils.convertDateToSmallString(e.getModifiedOn()));
			
		dataUseContainer.clear();
		Widget dataUse = createRestrictionWidget();
		if(dataUse != null) {
			dataUseContainer.setVisible(true);
			dataUseContainer.add(dataUse);
		} else {
			dataUseContainer.setVisible(false);
		}
		
		setVersionsVisible(false);
		if (e instanceof Versionable) {
			setVersionsVisible(true);
			Versionable vb = (Versionable) e;
			setVersionInfo(vb);
			setEntityVersions(vb);
			versionAnimation.hide();
		}
		
		isFavorite = presenter.isFavorite();
		setFavoriteIcon();
	}

	private void setFavoriteIcon() {
		if(isFavorite)
			favoriteAnchor.setHTML(favoriteStarHtml);
		else 
			favoriteAnchor.setHTML(favoriteStarOffHtml);
	}

	private void clear() {
		dataUseContainer.clear();
		//reset versions ui
		setVersionsVisible(false);
		previousVersions.setVisible(false);
		allVersions.setText(DisplayConstants.SHOW_VERSIONS);
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

	public void setCreatedBy(String who, String when) {
		String text =  DisplayConstants.CREATED + " by " + who + "<br/>" + when; 
		DisplayUtils.addTooltip(synapseJSNIUtils, addedBy, text, TOOLTIP_POSITION.BOTTOM);		
		addedBy.clear();
		addedBy.add(new HTML(DisplayConstants.CREATED + " By"));
	}

	public void setModified(String who, String when) {
		String text =  DisplayConstants.MODIFIED + " by " + who + "<br/>" + when;
		DisplayUtils.addTooltip(synapseJSNIUtils, modifiedBy, text, TOOLTIP_POSITION.BOTTOM);
		modifiedBy.clear();
		modifiedBy.add(new HTML(DisplayConstants.MODIFIED + " By"));		
	}

	public void setVersionInfo(Versionable vb) {
		StringBuilder sb = new StringBuilder();
		sb.append(vb.getVersionLabel());

		if (vb.getVersionComment() != null) {
			DisplayUtils.addTooltip(synapseJSNIUtils, versions, vb.getVersionComment(), TOOLTIP_POSITION.BOTTOM);
		} else {
			DisplayUtils.addTooltip(synapseJSNIUtils, versions, DisplayConstants.NO_VERSION_COMMENT, TOOLTIP_POSITION.BOTTOM);
		}
		
		label.setInnerText(sb.toString());
	}

	public void setPreviousVersions(ContentPanel versions) {
		previousVersions.add(versions);
		previousVersions.layout(true);
	}

	public void setVersionsVisible(boolean visible) {
		if(visible) versions.addStyleName("metadata-tag"); 
		else versions.removeStyleName("metadata-tag");
		versions.setVisible(visible);
	}

	public Style getStyle() {
		return style;
	}

	public void setEntityVersions(final Versionable entity) {
		// create bottom paging toolbar

		RpcProxy<PagingLoadResult<BaseModelData>> proxy = new RpcProxy<PagingLoadResult<BaseModelData>>() {

			@Override
			protected void load(
					Object loadConfig,
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
											version.getModifiedBy());

									if (entity.getVersionNumber().equals(version.getVersionNumber()))
										currentModel = model;
									dataList.add(model);
								}
								PagingLoadResult<BaseModelData> loadResultData = new BasePagingLoadResult<BaseModelData>(
										dataList);
								loadResultData.setTotalLength((int) result
										.getTotalNumberOfResults());
								vToolbar.setVisible(loadResultData
										.getTotalLength() > VERSION_LIMIT);

								loadResultData.setOffset(offset);
								callback.onSuccess(loadResultData);
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

				} else if (model.get(property) != null) {
					return model.get(property).toString();

				} else {
					return null;
				}
			}

			private Object setupIconMenu(final ModelData model, boolean currentVersion) {
				IconMenu menu = new IconMenu();
				final String versionLabel = (String) model.get(VERSION_KEY_LABEL);
				if (!currentVersion) {
					menu.addIcon(icons.arrowTurnLeftGrey16(),
							"Promote Version to Top", new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									MessageBox.confirm("Promote "+ versionLabel,
											DisplayConstants.PROMPT_SURE_PROMOTE,
											new Listener<MessageBoxEvent>() {
												@Override
												public void handleEvent(MessageBoxEvent be) {
													Button btn = be.getButtonClicked();
													if (Dialog.YES.equals(btn.getItemId())) {
														presenter.promoteVersion((String) model.get(VERSION_KEY_ID),
																				(Long) model.get(VERSION_KEY_NUMBER));
													}
												}
											});
								}
							});
				} else {
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
				return menu.asWidget();
			}
		};
		return cellRenderer;
	}

	private ColumnModel setupColumnModel(Versionable vb) {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		String[] keys =  {VERSION_KEY_LABEL, VERSION_KEY_COMMENT, VERSION_KEY_MOD_ON, VERSION_KEY_MOD_BY , VERSION_KEY_NUMBER};
		String[] names = {"Version"        , "Comment"          , "Modified On"     , "Modified By"      , ""                };
		int[] widths =	 {70               , 230                , 70                , 100                , 50                };
		int MOD_ON_INDEX = -1;

		if (keys.length != names.length || names.length != widths.length)
			throw new IllegalArgumentException("All configuration arrays must be the same length.");

		GridCellRenderer<BaseModelData> cellRenderer = configureVersionsGridCellRenderer(vb);

		for (int i = 0; i < keys.length; i++) {
			if (VERSION_KEY_MOD_ON.equals(keys[i]))
				MOD_ON_INDEX = i;

			ColumnConfig colConfig = new ColumnConfig(keys[i], names[i], widths[i]);
			colConfig.setRenderer(cellRenderer);
			colConfig.setSortable(false);
			colConfig.setResizable(false);
			colConfig.setMenuDisabled(true);
			columns.add(colConfig);
		}

		columns.get(MOD_ON_INDEX).setDateTimeFormat(DateTimeFormat.getShortDateFormat());
		columns.get(MOD_ON_INDEX).setRenderer(null);
		return new ColumnModel(columns);
	}

	private Widget createShareSettingsWidget(boolean isPublic) {
		final SimplePanel lc = new SimplePanel();
		String styleName = isPublic ? "public-acl-image" : "private-acl-image";
		String description = isPublic ? DisplayConstants.PUBLIC_ACL_ENTITY_PAGE : DisplayConstants.PRIVATE_ACL_ENTITY_PAGE;
		String tooltip = isPublic ? DisplayConstants.PUBLIC_ACL_DESCRIPTION : DisplayConstants.PRIVATE_ACL_DESCRIPTION;

		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<span style=\"margin-right: 5px;\">Sharing:</span><div class=\"" + styleName+ "\" style=\"display:inline-block; position:absolute\"></div>");
		shb.appendHtmlConstant("<span style=\"margin-left: 20px;\">"+description+"</span>");

		//form the html
		HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
		htmlPanel.addStyleName("inline-block");
		DisplayUtils.addTooltip(synapseJSNIUtils, htmlPanel, tooltip, TOOLTIP_POSITION.BOTTOM);
		lc.add(htmlPanel);

		return lc;
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	private String stubUseTime(String userTime) {
		String stub = userTime.substring(0,NAME_TIME_STUB_LENGTH-1);
		if(userTime.length() > NAME_TIME_STUB_LENGTH) stub += "...";
		return stub;
	}

}
