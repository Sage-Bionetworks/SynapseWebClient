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
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.view.EntityViewImpl;
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
import org.sagebionetworks.web.client.view.WikiView;
import org.sagebionetworks.web.client.view.WikiViewImpl;
import org.sagebionetworks.web.client.view.table.ColumnFactory;
import org.sagebionetworks.web.client.view.table.ColumnFactoryImpl;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.view.users.PasswordResetViewImpl;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterAccountViewImpl;
import org.sagebionetworks.web.client.widget.WidgetFactory;
import org.sagebionetworks.web.client.widget.WidgetFactoryImpl;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbView;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbViewImpl;
import org.sagebionetworks.web.client.widget.entity.AttachmentsView;
import org.sagebionetworks.web.client.widget.entity.AttachmentsViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxView;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelperImpl;
import org.sagebionetworks.web.client.widget.entity.PropertyWidget;
import org.sagebionetworks.web.client.widget.entity.PropertyWidgetView;
import org.sagebionetworks.web.client.widget.entity.PropertyWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidgetView;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorViewImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialogImpl;
import org.sagebionetworks.web.client.widget.entity.download.LocationableDownloaderView;
import org.sagebionetworks.web.client.widget.entity.download.LocationableDownloaderViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.LocationableUploaderView;
import org.sagebionetworks.web.client.widget.entity.download.LocationableUploaderViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuView;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuViewImpl;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetViewImpl;
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
		// the edit dialog
		bind(EntityEditorDialogImpl.class).in(Singleton.class);
		bind(EntityEditorDialog.class).to(EntityEditorDialogImpl.class);
		
		// The URL cache
		bind(UrlCacheImpl.class).in(Singleton.class);
		bind(UrlCache.class).to(UrlCacheImpl.class);
		
		//GWT utility methods
		bind(GWTWrapperImpl.class).in(Singleton.class);
		bind(GWTWrapper.class).to(GWTWrapperImpl.class);
				
	
		// Adapter factoyr
		bind(AdapterFactory.class).to(GwtAdapterFactory.class);
		
		bind(JiraURLHelper.class).to(JiraURLHelperImpl.class);
		
		bind(WidgetFactory.class).to(WidgetFactoryImpl.class);
		
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
		
		// ActionMenu
		bind(ActionMenuViewImpl.class).in(Singleton.class);
		bind(ActionMenuView.class).to(ActionMenuViewImpl.class);
		
		// FileBox
		bind(LocationableTitleBarViewImpl.class).in(Singleton.class);
		bind(LocationableTitleBarView.class).to(LocationableTitleBarViewImpl.class);
		
		// EntityChildBrowser (not singleton as you may want multiple)
		bind(LocationableDownloaderView.class).to(LocationableDownloaderViewImpl.class);

		// Search Box
		bind(SearchBoxViewImpl.class).in(Singleton.class);
		bind(SearchBoxView.class).to(SearchBoxViewImpl.class);

		// Home Search Box
		bind(HomeSearchBoxViewImpl.class).in(Singleton.class);
		bind(HomeSearchBoxView.class).to(HomeSearchBoxViewImpl.class);

		// LocationableUploader
		bind(LocationableUploaderView.class).to(LocationableUploaderViewImpl.class);

		// EntityTreeBrowser
		bind(EntityTreeBrowserView.class).to(EntityTreeBrowserViewImpl.class);

		// MyEntitiesBrowser
		bind(MyEntitiesBrowserView.class).to(MyEntitiesBrowserViewImpl.class);

		// Attachments		
		bind(AttachmentsView.class).to(AttachmentsViewImpl.class);

		// SnapshotWidget		
		bind(SnapshotWidgetView.class).to(SnapshotWidgetViewImpl.class);

		// EntitySearchBox		
		bind(EntitySearchBoxView.class).to(EntitySearchBoxViewImpl.class);

		// EntityMetadata
		bind(EntityMetadataViewImpl.class).in(Singleton.class);
		bind(EntityMetadataView.class).to(EntityMetadataViewImpl.class);
		

		//Widget Registration
		bind(WidgetRegistrarImpl.class).in(Singleton.class);
		bind(WidgetRegistrar.class).to(WidgetRegistrarImpl.class);
		
		// UI Widget Descriptor editor
		bind(BaseEditWidgetDescriptorView.class).to(BaseEditWidgetDescriptorViewImpl.class);
		bind(YouTubeConfigView.class).to(YouTubeConfigViewImpl.class);
		bind(ImageConfigView.class).to(ImageConfigViewImpl.class);
		bind(ProvenanceConfigView.class).to(ProvenanceConfigViewImpl.class);
		bind(LinkConfigView.class).to(LinkConfigViewImpl.class);
		
		// UI Widget Renderers
		bind(YouTubeWidgetView.class).to(YouTubeWidgetViewImpl.class);
		bind(ImageWidgetView.class).to(ImageWidgetViewImpl.class);
		// ProvenanceWidget
		bind(ProvenanceWidgetView.class).to(ProvenanceWidgetViewImpl.class);
		
		// FilesBrowser
		bind(FilesBrowserView.class).to(FilesBrowserViewImpl.class);
	}

}
