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
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.client.view.HomeViewImpl;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.view.LoginViewImpl;
import org.sagebionetworks.web.client.view.LookupView;
import org.sagebionetworks.web.client.view.LookupViewImpl;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.view.ProfileViewImpl;
import org.sagebionetworks.web.client.view.ProjectView;
import org.sagebionetworks.web.client.view.ProjectViewImpl;
import org.sagebionetworks.web.client.view.ProjectsHomeView;
import org.sagebionetworks.web.client.view.ProjectsHomeViewImpl;
import org.sagebionetworks.web.client.view.PublicProfileView;
import org.sagebionetworks.web.client.view.PublicProfileViewImpl;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.view.SearchViewImpl;
import org.sagebionetworks.web.client.view.table.ColumnFactory;
import org.sagebionetworks.web.client.view.table.ColumnFactoryImpl;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.view.users.PasswordResetViewImpl;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterAccountViewImpl;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbView;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.AnnotationEditorView;
import org.sagebionetworks.web.client.widget.editpanels.AnnotationEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.NodeEditorView;
import org.sagebionetworks.web.client.widget.editpanels.NodeEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.ColumnDefinitionEditorView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.ColumnDefinitionEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.ColumnMappingEditorView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.ColumnMappingEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.OntologySearchPanelView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.OntologySearchPanelViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.PhenotypeEditorView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.PhenotypeEditorViewImpl;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.PhenotypeMatrixView;
import org.sagebionetworks.web.client.widget.editpanels.phenotype.PhenotypeMatrixViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserViewImpl;

import org.sagebionetworks.web.client.widget.entity.EntityPropertyGrid;
import org.sagebionetworks.web.client.widget.entity.PropertyWidget;
import org.sagebionetworks.web.client.widget.entity.PropertyWidgetView;
import org.sagebionetworks.web.client.widget.entity.children.EntityChildBrowserView;
import org.sagebionetworks.web.client.widget.entity.children.EntityChildBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.EntityEditorDialogImpl;
import org.sagebionetworks.web.client.widget.entity.download.LocationableDownloaderView;
import org.sagebionetworks.web.client.widget.entity.download.LocationableDownloaderViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.LocationableUploaderView;
import org.sagebionetworks.web.client.widget.entity.download.LocationableUploaderViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuView;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuViewImpl;
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
import org.sagebionetworks.web.server.servlet.BCCSignupImpl;

import com.google.gwt.cell.client.widget.CustomWidgetImageBundle;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

public class PortalGinModule extends AbstractGinModule {

	@Override
	protected void configure() {
		
		// AuthenticationController
		bind(AuthenticationControllerImpl.class).in(Singleton.class);
		bind(AuthenticationController.class).to(AuthenticationControllerImpl.class);

		// GlobalApplicationState
		bind(GlobalApplicationStateImpl.class).in(Singleton.class);
		bind(GlobalApplicationState.class).to(GlobalApplicationStateImpl.class);
		
		// Header & Footer
		bind(HeaderView.class).to(HeaderViewImpl.class);
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
		bind(EntityPropertyGrid.class).in(Singleton.class);
		bind(PropertyWidgetView.class).to(EntityPropertyGrid.class);
		
		// the logger
		bind(ClientLoggerImpl.class).in(Singleton.class);
		bind(ClientLogger.class).to(ClientLoggerImpl.class);
		// the edit dialog
		bind(EntityEditorDialogImpl.class).in(Singleton.class);
		bind(EntityEditorDialog.class).to(EntityEditorDialogImpl.class);
		
		// The URL cache
		bind(UrlCacheImpl.class).in(Singleton.class);
		bind(UrlCache.class).to(UrlCacheImpl.class);
		
	
		// Adapter factoyr
		bind(AdapterFactory.class).to(GwtAdapterFactory.class);
		
		/*
		 * Vanilla Implementation binding
		 */
		
		// Node Model Creator
		bind(NodeModelCreator.class).to(NodeModelCreatorImpl.class);
		
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
		
		// ProjectView
		bind(ProjectViewImpl.class).in(Singleton.class);
		bind(ProjectView.class).to(ProjectViewImpl.class);
		
		// QueryService View
		//bind(QueryServiceTableView.class).to(QueryServiceTableViewImpl.class);
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
		
		// CominSoonView
		bind(ComingSoonViewImpl.class).in(Singleton.class);
		bind(ComingSoonView.class).to(ComingSoonViewImpl.class);									
		
		// BCCOverviewView
		bind(BCCOverviewViewImpl.class).in(Singleton.class);
		bind(BCCOverviewView.class).to(BCCOverviewViewImpl.class);									
				// LookupView
		bind(LookupViewImpl.class).in(Singleton.class);
		bind(LookupView.class).to(LookupViewImpl.class);					
		
		// PublicProfileView
		bind(PublicProfileViewImpl.class).in(Singleton.class);
		bind(PublicProfileView.class).to(PublicProfileViewImpl.class);
		
		// SearchView
		bind(SearchViewImpl.class).in(Singleton.class);
		bind(SearchView.class).to(SearchViewImpl.class);
		
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

		// NodeEditor
		bind(NodeEditorViewImpl.class).in(Singleton.class);
		bind(NodeEditorView.class).to(NodeEditorViewImpl.class);

		// AnnotationEditor
		bind(AnnotationEditorViewImpl.class).in(Singleton.class);
		bind(AnnotationEditorView.class).to(AnnotationEditorViewImpl.class);

		// ACL Editor
		bind(AccessControlListEditorViewImpl.class).in(Singleton.class);
		bind(AccessControlListEditorView.class).to(AccessControlListEditorViewImpl.class);
		
		// PhenotypeEditor
		bind(PhenotypeEditorViewImpl.class).in(Singleton.class);
		bind(PhenotypeEditorView.class).to(PhenotypeEditorViewImpl.class);
		
		// Column Definition Editor
		bind(ColumnDefinitionEditorViewImpl.class).in(Singleton.class);
		bind(ColumnDefinitionEditorView.class).to(ColumnDefinitionEditorViewImpl.class);		

		// Column Mapping Editor
		bind(ColumnMappingEditorViewImpl.class).in(Singleton.class);
		bind(ColumnMappingEditorView.class).to(ColumnMappingEditorViewImpl.class);		

		// Ontology Search Panel
		bind(OntologySearchPanelViewImpl.class).in(Singleton.class);
		bind(OntologySearchPanelView.class).to(OntologySearchPanelViewImpl.class);		

		// PhenotypeMatrix
		bind(PhenotypeMatrixViewImpl.class).in(Singleton.class);
		bind(PhenotypeMatrixView.class).to(PhenotypeMatrixViewImpl.class);
		
		// EntityPageTop
		bind(EntityPageTopViewImpl.class).in(Singleton.class);
		bind(EntityPageTopView.class).to(EntityPageTopViewImpl.class);
		
		// ActionMenu
		bind(ActionMenuViewImpl.class).in(Singleton.class);
		bind(ActionMenuView.class).to(ActionMenuViewImpl.class);
		
		// EntityChildBrowser
		bind(EntityChildBrowserViewImpl.class).in(Singleton.class);
		bind(EntityChildBrowserView.class).to(EntityChildBrowserViewImpl.class);
		
		// EntityChildBrowser (not singleton as you may want multiple)
		bind(LocationableDownloaderView.class).to(LocationableDownloaderViewImpl.class);

		// Search Box
		bind(SearchBoxViewImpl.class).in(Singleton.class);
		bind(SearchBoxView.class).to(SearchBoxViewImpl.class);

		// Home Search Box
		bind(HomeSearchBoxViewImpl.class).in(Singleton.class);
		bind(HomeSearchBoxView.class).to(HomeSearchBoxViewImpl.class);

		// LocationableUploader
		bind(LocationableUploaderViewImpl.class).in(Singleton.class);
		bind(LocationableUploaderView.class).to(LocationableUploaderViewImpl.class);

		// EntityTreeBrowser
		bind(EntityTreeBrowserViewImpl.class).in(Singleton.class);
		bind(EntityTreeBrowserView.class).to(EntityTreeBrowserViewImpl.class);

		// MyEntitiesBrowser
		bind(MyEntitiesBrowserViewImpl.class).in(Singleton.class);
		bind(MyEntitiesBrowserView.class).to(MyEntitiesBrowserViewImpl.class);

	}

}
