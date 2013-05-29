package org.sagebionetworks.web.client;

import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.gwt.client.schema.adapter.JSONArrayGwt;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.cookie.GWTCookieImpl;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.transform.JsoProviderImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.view.BCCOverviewView;
import org.sagebionetworks.web.client.view.BCCOverviewViewImpl;
import org.sagebionetworks.web.client.view.CellTableProvider;
import org.sagebionetworks.web.client.view.CellTableProviderImpl;
import org.sagebionetworks.web.client.view.ColumnsPopupView;
import org.sagebionetworks.web.client.view.ColumnsPopupViewImpl;
import org.sagebionetworks.web.client.view.ComingSoonView;
import org.sagebionetworks.web.client.view.ComingSoonViewImpl;
import org.sagebionetworks.web.client.view.DownView;
import org.sagebionetworks.web.client.view.DownViewImpl;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.view.EntityViewImpl;
import org.sagebionetworks.web.client.view.EvaluationView;
import org.sagebionetworks.web.client.view.EvaluationViewImpl;
import org.sagebionetworks.web.client.view.GovernanceView;
import org.sagebionetworks.web.client.view.GovernanceViewImpl;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.client.view.HomeViewImpl;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.view.LoginViewImpl;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.view.ProfileViewImpl;
import org.sagebionetworks.web.client.view.ProjectsHomeView;
import org.sagebionetworks.web.client.view.ProjectsHomeViewImpl;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.view.SearchViewImpl;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.client.view.SettingsViewImpl;
import org.sagebionetworks.web.client.view.SynapseWikiView;
import org.sagebionetworks.web.client.view.SynapseWikiViewImpl;
import org.sagebionetworks.web.client.view.WikiView;
import org.sagebionetworks.web.client.view.WikiViewImpl;
import org.sagebionetworks.web.client.view.table.ColumnFactory;
import org.sagebionetworks.web.client.view.table.ColumnFactoryImpl;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.view.users.PasswordResetViewImpl;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterAccountViewImpl;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbView;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbViewImpl;
import org.sagebionetworks.web.client.widget.entity.AttachmentsView;
import org.sagebionetworks.web.client.widget.entity.AttachmentsViewImpl;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetView;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyFormView;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyFormViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxView;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.entity.EvaluationListView;
import org.sagebionetworks.web.client.widget.entity.EvaluationListViewImpl;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelperImpl;
import org.sagebionetworks.web.client.widget.entity.MyEvaluationsListView;
import org.sagebionetworks.web.client.widget.entity.MyEvaluationsListViewImpl;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.PropertyWidget;
import org.sagebionetworks.web.client.widget.entity.PropertyWidgetView;
import org.sagebionetworks.web.client.widget.entity.PropertyWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidgetView;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiAttachmentsView;
import org.sagebionetworks.web.client.widget.entity.WikiAttachmentsViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.UploaderView;
import org.sagebionetworks.web.client.widget.entity.download.UploaderViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManagerView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManagerViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.OldImageConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.OldImageConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuView;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuViewImpl;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.OldImageWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.OldImageWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidgetViewImpl;
import org.sagebionetworks.web.client.widget.filter.QueryFilterView;
import org.sagebionetworks.web.client.widget.filter.QueryFilterViewImpl;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.FooterViewImpl;
import org.sagebionetworks.web.client.widget.header.HeaderView;
import org.sagebionetworks.web.client.widget.header.HeaderViewImpl;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloaderView;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloaderViewImpl;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;
import org.sagebionetworks.web.client.widget.login.LoginWidgetViewImpl;
import org.sagebionetworks.web.client.widget.modal.ModalWindowView;
import org.sagebionetworks.web.client.widget.modal.ModalWindowViewImpl;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.search.HomeSearchBoxView;
import org.sagebionetworks.web.client.widget.search.HomeSearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.search.SearchBoxView;
import org.sagebionetworks.web.client.widget.search.SearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButtonView;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButtonViewImpl;
import org.sagebionetworks.web.client.widget.statictable.StaticTableView;
import org.sagebionetworks.web.client.widget.statictable.StaticTableViewImpl;
import org.sagebionetworks.web.client.widget.table.QueryServiceTableView;
import org.sagebionetworks.web.client.widget.table.QueryServiceTableViewGxtImpl;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.client.widget.user.UserBadgeViewImpl;

import com.google.gwt.cell.client.widget.CustomWidgetImageBundle;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

public class PortalGinModule extends AbstractGinModule {

	@Override
	protected void configure() {
		// Event Bus
		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
		
		// JsoProvider
		bind(JsoProvider.class).to(JsoProviderImpl.class);
		bind(JsoProviderImpl.class).in(Singleton.class);
		
		// AuthenticationController
		bind(AuthenticationControllerImpl.class).in(Singleton.class);
		bind(AuthenticationController.class).to(AuthenticationControllerImpl.class);

		// GlobalApplicationState
		bind(GlobalApplicationStateImpl.class).in(Singleton.class);
		bind(GlobalApplicationState.class).to(GlobalApplicationStateImpl.class);
		
		// Header & Footer
		bind(HeaderViewImpl.class).in(Singleton.class);
		bind(HeaderView.class).to(HeaderViewImpl.class);
		bind(FooterViewImpl.class).in(Singleton.class);
		bind(FooterView.class).to(FooterViewImpl.class);

		// EntityType
		bind(EntityTypeProvider.class).in(Singleton.class);
		
		// JSONAdapters
		bind(JSONObjectAdapter.class).to(JSONObjectGwt.class);
		bind(JSONArrayAdapter.class).to(JSONArrayGwt.class);
		
		// JSONEntityFactory
		bind(JSONEntityFactoryImpl.class).in(Singleton.class);
		bind(JSONEntityFactory.class).to(JSONEntityFactoryImpl.class);
		
		// EntitySchemaCacheImpl
		bind(EntitySchemaCacheImpl.class).in(Singleton.class);
		bind(EntitySchemaCache.class).to(EntitySchemaCacheImpl.class);
		
		bind(PropertyWidget.class).in(Singleton.class);
		bind(PropertyWidgetViewImpl.class).in(Singleton.class);
		bind(PropertyWidgetView.class).to(PropertyWidgetViewImpl.class);
		
		// the logger
		bind(ClientLoggerImpl.class).in(Singleton.class);
		bind(ClientLogger.class).to(ClientLoggerImpl.class);

		// the Entity edit view
		bind(EntityPropertyFormViewImpl.class).in(Singleton.class);
		bind(EntityPropertyFormView.class).to(EntityPropertyFormViewImpl.class);
				
		// The URL cache
		bind(UrlCacheImpl.class).in(Singleton.class);
		bind(UrlCache.class).to(UrlCacheImpl.class);
		
		//GWT utility methods
		bind(GWTWrapperImpl.class).in(Singleton.class);
		bind(GWTWrapper.class).to(GWTWrapperImpl.class);
		
		//RequestBuilder
		bind(RequestBuilderWrapperImpl.class).in(Singleton.class);
		bind(RequestBuilderWrapper.class).to(RequestBuilderWrapperImpl.class);
		
		// Adapter factoyr
		bind(AdapterFactory.class).to(GwtAdapterFactory.class);
		
		bind(JiraURLHelper.class).to(JiraURLHelperImpl.class);
		
		/*
		 * Vanilla Implementation binding
		 */
		
		// Node Model Creator
		bind(NodeModelCreator.class).to(NodeModelCreatorImpl.class);
		
		// JSNI impls
		bind(SynapseJSNIUtils.class).to(SynapseJSNIUtilsImpl.class);
		
		/*
		 * Places
		 */
		
		// The home page
		bind(HomeViewImpl.class).in(Singleton.class);
		bind(HomeView.class).to(HomeViewImpl.class);

		// EntityView
		bind(EntityViewImpl.class).in(Singleton.class);
		bind(EntityView.class).to(EntityViewImpl.class);
		
		// ProjectsHomeView
		bind(ProjectsHomeViewImpl.class).in(Singleton.class);
		bind(ProjectsHomeView.class).to(ProjectsHomeViewImpl.class);		
				
		// QueryService View
		bind(QueryServiceTableView.class).to(QueryServiceTableViewGxtImpl.class);
		
		// LoginView
		bind(LoginViewImpl.class).in(Singleton.class);
		bind(LoginView.class).to(LoginViewImpl.class);
		
		// PasswordResetView
		bind(PasswordResetViewImpl.class).in(Singleton.class);
		bind(PasswordResetView.class).to(PasswordResetViewImpl.class);

		// RegisterAccountView
		bind(RegisterAccountViewImpl.class).in(Singleton.class);
		bind(RegisterAccountView.class).to(RegisterAccountViewImpl.class);

		// ProfileView
		bind(ProfileViewImpl.class).in(Singleton.class);
		bind(ProfileView.class).to(ProfileViewImpl.class);		
		
		// SettingsView
		bind(SettingsViewImpl.class).in(Singleton.class);
		bind(SettingsView.class).to(SettingsViewImpl.class);	
		
		// CominSoonView
		bind(ComingSoonViewImpl.class).in(Singleton.class);
		bind(ComingSoonView.class).to(ComingSoonViewImpl.class);									
		
		// BCCOverviewView
		bind(BCCOverviewViewImpl.class).in(Singleton.class);
		bind(BCCOverviewView.class).to(BCCOverviewViewImpl.class);	
		
		// CominSoonView
		bind(GovernanceViewImpl.class).in(Singleton.class);
		bind(GovernanceView.class).to(GovernanceViewImpl.class);									
		
		// SearchView
		bind(SearchViewImpl.class).in(Singleton.class);
		bind(SearchView.class).to(SearchViewImpl.class);

		// WikiView
		bind(WikiViewImpl.class).in(Singleton.class);
		bind(WikiView.class).to(WikiViewImpl.class);
		
		// Down
		bind(DownViewImpl.class).in(Singleton.class);
		bind(DownView.class).to(DownViewImpl.class);

		//Synapse Wiki Pages
		bind(SynapseWikiView.class).to(SynapseWikiViewImpl.class);

		//Evaluation
		bind(EvaluationView.class).to(EvaluationViewImpl.class);

		/*
		 * Widgets
		 */
		
		// LoginWidget
		bind(LoginWidgetViewImpl.class).in(Singleton.class);
		bind(LoginWidgetView.class).to(LoginWidgetViewImpl.class);
		
		// StaticTable
		bind(StaticTableView.class).to(StaticTableViewImpl.class);
		
		// LicenseBox
		bind(LicensedDownloaderView.class).to(LicensedDownloaderViewImpl.class);
		
		// Modal View
		bind(ModalWindowView.class).to(ModalWindowViewImpl.class);
		
		// Breadcrumb
		bind(BreadcrumbView.class).to(BreadcrumbViewImpl.class);
		
		// Bind the cookie provider
		bind(GWTCookieImpl.class).in(Singleton.class);
		bind(CookieProvider.class).to(GWTCookieImpl.class);

		// ColumnFactory
		bind(ColumnFactory.class).to(ColumnFactoryImpl.class);
		
		// The ImagePrototySingleton should be...well a singleton
		bind(ImagePrototypeSingleton.class).in(Singleton.class);
		
		// ClientBundle for Custom widgets
		bind(CustomWidgetImageBundle.class).in(Singleton.class);
		
		// The runtime provider
		bind(CellTableProvider.class).to(CellTableProviderImpl.class);
		
		// The column popup
		bind(ColumnsPopupViewImpl.class).in(Singleton.class);
		bind(ColumnsPopupView.class).to(ColumnsPopupViewImpl.class);
		
		// Query filter
		bind(QueryFilterViewImpl.class).in(Singleton.class);
		bind(QueryFilterView.class).to(QueryFilterViewImpl.class);
		
		// Access Menu Button
		bind(AccessMenuButtonViewImpl.class).in(Singleton.class);
		bind(AccessMenuButtonView.class).to(AccessMenuButtonViewImpl.class);

		// ACL Editor
		bind(AccessControlListEditorView.class).to(AccessControlListEditorViewImpl.class);
		
		// EntityPageTop
		bind(EntityPageTopViewImpl.class).in(Singleton.class);
		bind(EntityPageTopView.class).to(EntityPageTopViewImpl.class);
		
		// Preview
		bind(PreviewWidgetViewImpl.class).in(Singleton.class);
		bind(PreviewWidgetView.class).to(PreviewWidgetViewImpl.class);
		
		// ActionMenu
		bind(ActionMenuViewImpl.class).in(Singleton.class);
		bind(ActionMenuView.class).to(ActionMenuViewImpl.class);
		
		// FileBox
		bind(LocationableTitleBarViewImpl.class).in(Singleton.class);
		bind(LocationableTitleBarView.class).to(LocationableTitleBarViewImpl.class);
		
		// FileBox
		bind(FileTitleBarViewImpl.class).in(Singleton.class);
		bind(FileTitleBarView.class).to(FileTitleBarViewImpl.class);
				
		// Search Box
		bind(SearchBoxViewImpl.class).in(Singleton.class);
		bind(SearchBoxView.class).to(SearchBoxViewImpl.class);

		// Home Search Box
		bind(HomeSearchBoxViewImpl.class).in(Singleton.class);
		bind(HomeSearchBoxView.class).to(HomeSearchBoxViewImpl.class);

		// LocationableUploader
		bind(UploaderView.class).to(UploaderViewImpl.class);

		// EntityTreeBrowser
		bind(EntityTreeBrowserView.class).to(EntityTreeBrowserViewImpl.class);

		// MyEntitiesBrowser
		bind(MyEntitiesBrowserView.class).to(MyEntitiesBrowserViewImpl.class);

		// Attachments		
		bind(AttachmentsView.class).to(AttachmentsViewImpl.class);

		// Wiki Attachments		
		bind(WikiAttachmentsView.class).to(WikiAttachmentsViewImpl.class);
		
		//Evaluation selector
		bind(EvaluationListView.class).to(EvaluationListViewImpl.class);
		
		//My Evaluations
		bind(MyEvaluationsListView.class).to(MyEvaluationsListViewImpl.class);
		
		// SnapshotWidget		
		bind(SnapshotWidgetView.class).to(SnapshotWidgetViewImpl.class);

		// EntitySearchBox		
		bind(EntitySearchBoxView.class).to(EntitySearchBoxViewImpl.class);

		// EntityMetadata
		bind(EntityMetadataViewImpl.class).in(Singleton.class);
		bind(EntityMetadataView.class).to(EntityMetadataViewImpl.class);
		

		// API Table Column manager	
		bind(APITableColumnManagerView.class).to(APITableColumnManagerViewImpl.class);

		//Widget Registration
		bind(WidgetRegistrarImpl.class).in(Singleton.class);
		bind(WidgetRegistrar.class).to(WidgetRegistrarImpl.class);
		
		// UI Widget Descriptor editor
		bind(BaseEditWidgetDescriptorView.class).to(BaseEditWidgetDescriptorViewImpl.class);
		bind(YouTubeConfigView.class).to(YouTubeConfigViewImpl.class);
		bind(OldImageConfigView.class).to(OldImageConfigViewImpl.class);
		bind(ImageConfigView.class).to(ImageConfigViewImpl.class);
		bind(AttachmentConfigView.class).to(AttachmentConfigViewImpl.class);
		bind(ProvenanceConfigView.class).to(ProvenanceConfigViewImpl.class);
		bind(LinkConfigView.class).to(LinkConfigViewImpl.class);
		bind(TabbedTableConfigView.class).to(TabbedTableConfigViewImpl.class);
		bind(APITableConfigView.class).to(APITableConfigViewImpl.class);
		bind(EntityListConfigView.class).to(EntityListConfigViewImpl.class);
		bind(ShinySiteConfigView.class).to(ShinySiteConfigViewImpl.class);
		
		// UI Widget Renderers
		bind(YouTubeWidgetView.class).to(YouTubeWidgetViewImpl.class);
		bind(OldImageWidgetView.class).to(OldImageWidgetViewImpl.class);
		bind(EntityListWidgetView.class).to(EntityListWidgetViewImpl.class);
		bind(ShinySiteWidgetView.class).to(ShinySiteWidgetViewImpl.class);		
		bind(ImageWidgetView.class).to(ImageWidgetViewImpl.class);
		bind(AttachmentPreviewWidgetView.class).to(AttachmentPreviewWidgetViewImpl.class);
		bind(APITableWidgetView.class).to(APITableWidgetViewImpl.class);
		bind(TableOfContentsWidgetView.class).to(TableOfContentsWidgetViewImpl.class);
		bind(WikiFilesPreviewWidgetView.class).to(WikiFilesPreviewWidgetViewImpl.class);
		
		// ProvenanceWidget
		bind(ProvenanceWidgetView.class).to(ProvenanceWidgetViewImpl.class);
		
		// FilesBrowser
		bind(FilesBrowserView.class).to(FilesBrowserViewImpl.class);
		
		// PagesBrowser
		bind(PagesBrowserView.class).to(PagesBrowserViewImpl.class);

		// Entity Finder
		bind(EntityFinderView.class).to(EntityFinderViewImpl.class);		

		bind(FavoriteWidgetView.class).to(FavoriteWidgetViewImpl.class);
		
		bind(DoiWidgetView.class).to(DoiWidgetViewImpl.class);
		
		bind(WikiPageWidgetView.class).to(WikiPageWidgetViewImpl.class);
		bind(UserBadgeView.class).to(UserBadgeViewImpl.class);
	}

}
