package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.extjs.gxt.ui.client.Style.Direction;
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
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
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
	HTMLPanel panel;
	@UiField
	HTMLPanel versions;
	@UiField
	HTMLPanel readOnly;

	@UiField
	Image entityIcon;
	@UiField
	SpanElement entityName;
	@UiField
	SpanElement entityId;
	@UiField
	SpanElement createName;
	@UiField
	SpanElement createDate;
	@UiField
	SpanElement modifyName;
	@UiField
	SpanElement modifyDate;
	@UiField
	SpanElement label;
	@UiField
	SpanElement comment;

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

	@Inject
	public EntityMetadataViewImpl(IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.icons = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;

		initWidget(uiBinder.createAndBindUi(this));

		final FxConfig config = new FxConfig(400);
		config.setEffectCompleteListener(new Listener<FxEvent>() {
			@Override
			public void handleEvent(FxEvent be) {
				// This call to layout is necessary to force the scroll bar to appear on page-load
				previousVersions.layout(true);
				allVersions.getElement().setPropertyBoolean("animating", false);
			}
		});

		allVersions.setText(DisplayConstants.SHOW_VERSIONS);
		allVersions.getElement().setPropertyBoolean("animating", false);
		allVersions.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!allVersions.getElement().getPropertyBoolean("animating")) {
					allVersions.getElement().setPropertyBoolean("animating", true);
					if (previousVersions.el().isVisible()) {
						allVersions.setText(DisplayConstants.SHOW_VERSIONS);
						previousVersions.el().slideOut(Direction.UP, config);
					} else {
						previousVersions.setVisible(true);
						allVersions.setText(DisplayConstants.HIDE_VERSIONS);
						previousVersions.el().slideIn(Direction.DOWN, config);
					}
				}
			}
		});
		previousVersions.setLayout(new FlowLayout(10));
	}

	@Override
	public void setEntityBundle(EntityBundle bundle) {
		Entity e = bundle.getEntity();
		setEntityName(e.getName());
		setEntityId(e.getId());

		setCreateName(e.getCreatedBy());
		setCreateDate(String.valueOf(e.getCreatedOn()));

		setModifyName(e.getModifiedBy());
		setModifyDate(String.valueOf(e.getModifiedOn()));

		setVersionsVisible(false);
		if (e instanceof Versionable) {
			setVersionsVisible(true);
			Versionable vb = (Versionable) e;
			setVersionInfo(vb);
			clearPreviousVersions();
			setEntityVersions(vb);
		}
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly.setVisible(readOnly);
	}

	public void setEntityName(String text) {
		entityName.setInnerText(text);
	}

	public void setEntityId(String text) {
		entityId.setInnerText(text);
	}

	public void setCreateName(String text) {
		createName.setInnerText(text);
	}

	public void setCreateDate(String text) {
		createDate.setInnerText(text);
	}

	public void setModifyName(String text) {
		modifyName.setInnerText(text);
	}

	public void setModifyDate(String text) {
		modifyDate.setInnerText(text);
	}

	public void setVersionInfo(Versionable vb) {
		StringBuilder sb = new StringBuilder();
		sb.append(vb.getVersionLabel());

		if (vb.getVersionComment() != null) {
			sb.append(" - ");

			comment.setTitle(vb.getVersionComment());
			comment.setInnerText(DisplayUtils.stubStr(vb.getVersionComment(), 60));
		}
		label.setInnerText(sb.toString());
	}

	public void setPreviousVersions(ContentPanel versions) {
		previousVersions.add(versions);
		previousVersions.layout(true);
	}

	public void clearPreviousVersions() {
		previousVersions.removeAll();
	}

	public void setVersionsVisible(boolean visible) {
		versions.setVisible(visible);
	}

	public Style getStyle() {
		return style;
	}

	public void setEntityVersions(final Versionable entity) {
		// create bottom paging toolbar
		final PagingToolBar toolBar = new PagingToolBar(VERSION_LIMIT);
		toolBar.setSpacing(2);
		toolBar.insert(new SeparatorToolItem(), toolBar.getItemCount() - 2);

		ContentPanel cp = new ContentPanel();
		cp.setLayout(new FitLayout());
		cp.setBodyBorder(true);
		cp.setButtonAlign(HorizontalAlignment.CENTER);
		cp.setHeaderVisible(false);
		cp.setHeight(155);
		cp.setBottomComponent(toolBar);

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
									dataList.add(model);
								}
								PagingLoadResult<BaseModelData> loadResultData = new BasePagingLoadResult<BaseModelData>(
										dataList);
								loadResultData.setTotalLength((int) result
										.getTotalNumberOfResults());
								toolBar.setVisible(loadResultData
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
		toolBar.bind(loader);

		// add initial data to the store
		ListStore<BaseModelData> store = new ListStore<BaseModelData>(loader);

		Grid<BaseModelData> grid = new Grid<BaseModelData>(store,
				setupColumnModel(entity));
		grid.getView().setForceFit(true);
		grid.getView().setEmptyText("Sorry, no versions were found.");
		grid.setLayoutData(new FitLayout());
		grid.setStateful(false);
		grid.setLoadMask(true);
		grid.setAutoWidth(true);
		grid.setBorders(false);
		grid.setStripeRows(true);
		grid.addListener(Events.Attach, new Listener<GridEvent<ModelData>>() {
			public void handleEvent(GridEvent<ModelData> be) {
				BasePagingLoadConfig config = new BasePagingLoadConfig();
				config.setLimit(VERSION_LIMIT);
				config.setOffset(0);
				loader.load(config);
			}
		});

		cp.add(grid);
		setPreviousVersions(cp);
	}

	private GridCellRenderer<BaseModelData> configureVersionsGridCellRenderer(final Versionable vb) {
		GridCellRenderer<BaseModelData> cellRenderer = new GridCellRenderer<BaseModelData>() {
			@Override
			public Object render(BaseModelData model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {

				if         (property.equals(VERSION_KEY_NUMBER)) {
					if (vb.getVersionNumber().equals(model.get(property))) {
						InlineLabel label = new InlineLabel("viewing");
						label.getElement().setAttribute("style", "font-weight:bold;");
						return label;
					} else {
						Hyperlink link = new Hyperlink();
						if (previousVersionsHasNotPaged && rowIndex == 0) {
							// This is so the user can easily get back to the non-readonly page
							link.setTargetHistoryToken(DisplayUtils
								.getSynapseHistoryTokenNoHash(vb.getId()));
						} else {
							link.setTargetHistoryToken(DisplayUtils
									.getSynapseHistoryTokenNoHash(vb.getId(),
											(Long) model.get(property)));
						}
						link.setText("view");
						link.setStyleName("link");
						return link;
					}
				} else if (property.equals(VERSION_KEY_COMMENT)) {
					String comment;
					if (null != model.get(property))
						comment = model.get(property).toString();
					else
						return null;
					// By default, overflow on a gridcell, results in eliding of the text.
					// This label and setTitle makes it to so that hovering will show the full comment.
					InlineLabel label = new InlineLabel(comment);
					label.setTitle(comment);
					return label;
				} else if (model.get(property) != null) {
					return model.get(property).toString();
				} else {
					return null;
				}
			}
		};
		return cellRenderer;
	}

	private ColumnModel setupColumnModel(Versionable vb) {
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		String[] keys =  {VERSION_KEY_LABEL, VERSION_KEY_COMMENT, VERSION_KEY_MOD_ON, VERSION_KEY_MOD_BY, VERSION_KEY_NUMBER};
		String[] names = {"Version"        , "Comment"          , "Modified On"     , "Modified By"     , ""                };
		int[] widths =	 {100              , 230                , 100               , 100               , 70                };
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

}
