package org.sagebionetworks.web.client;


import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.gwt.client.schema.adapter.JSONArrayGwt;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.ClientCacheImpl;
import org.sagebionetworks.web.client.cache.StorageImpl;
import org.sagebionetworks.web.client.cache.StorageWrapper;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.cookie.GWTCookieImpl;
import org.sagebionetworks.web.client.factory.EditorFactory;
import org.sagebionetworks.web.client.factory.EditorFactoryImpl;
import org.sagebionetworks.web.client.factory.RendererFactory;
import org.sagebionetworks.web.client.factory.RendererFactoryImpl;
import org.sagebionetworks.web.client.factory.TableColumnRendererFactory;
import org.sagebionetworks.web.client.factory.TableColumnRendererFactoryImpl;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.ResourceLoaderImpl;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.transform.JsoProviderImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.view.AccountView;
import org.sagebionetworks.web.client.view.AccountViewImpl;
import org.sagebionetworks.web.client.view.CellTableProvider;
import org.sagebionetworks.web.client.view.CellTableProviderImpl;
import org.sagebionetworks.web.client.view.CertificateView;
import org.sagebionetworks.web.client.view.CertificateViewImpl;
import org.sagebionetworks.web.client.view.ChallengeOverviewView;
import org.sagebionetworks.web.client.view.ChallengeOverviewViewImpl;
import org.sagebionetworks.web.client.view.ChangeUsernameView;
import org.sagebionetworks.web.client.view.ChangeUsernameViewImpl;
import org.sagebionetworks.web.client.view.ColumnsPopupView;
import org.sagebionetworks.web.client.view.ColumnsPopupViewImpl;
import org.sagebionetworks.web.client.view.ComingSoonView;
import org.sagebionetworks.web.client.view.ComingSoonViewImpl;
import org.sagebionetworks.web.client.view.DownView;
import org.sagebionetworks.web.client.view.DownViewImpl;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.view.EntityViewImpl;
import org.sagebionetworks.web.client.view.HelpView;
import org.sagebionetworks.web.client.view.HelpViewImpl;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.client.view.HomeViewImpl;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.view.LoginViewImpl;
import org.sagebionetworks.web.client.view.NewAccountView;
import org.sagebionetworks.web.client.view.NewAccountViewImpl;
import org.sagebionetworks.web.client.view.PeopleSearchView;
import org.sagebionetworks.web.client.view.PeopleSearchViewImpl;
import org.sagebionetworks.web.client.view.ProfileFormView;
import org.sagebionetworks.web.client.view.ProfileFormViewImpl;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.view.ProfileViewImpl;
import org.sagebionetworks.web.client.view.ProjectsHomeView;
import org.sagebionetworks.web.client.view.ProjectsHomeViewImpl;
import org.sagebionetworks.web.client.view.QuizView;
import org.sagebionetworks.web.client.view.QuizViewImpl;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.view.SearchViewImpl;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.client.view.SettingsViewImpl;
import org.sagebionetworks.web.client.view.SynapseWikiView;
import org.sagebionetworks.web.client.view.SynapseWikiViewImpl;
import org.sagebionetworks.web.client.view.TeamSearchView;
import org.sagebionetworks.web.client.view.TeamSearchViewImpl;
import org.sagebionetworks.web.client.view.TeamView;
import org.sagebionetworks.web.client.view.TeamViewImpl;
import org.sagebionetworks.web.client.view.TrashView;
import org.sagebionetworks.web.client.view.TrashViewImpl;
import org.sagebionetworks.web.client.view.WikiView;
import org.sagebionetworks.web.client.view.WikiViewImpl;
import org.sagebionetworks.web.client.view.table.ColumnFactory;
import org.sagebionetworks.web.client.view.table.ColumnFactoryImpl;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.view.users.PasswordResetViewImpl;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterAccountViewImpl;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTrackerImpl;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressViewImpl;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.asynch.NumberFormatProvider;
import org.sagebionetworks.web.client.widget.asynch.NumberFormatProviderImpl;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider;
import org.sagebionetworks.web.client.widget.asynch.TimerProviderImpl;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbView;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbViewImpl;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsListView;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsListViewImpl;
import org.sagebionetworks.web.client.widget.entity.AnnotationsWidget;
import org.sagebionetworks.web.client.widget.entity.AnnotationsWidgetView;
import org.sagebionetworks.web.client.widget.entity.AnnotationsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.AttachmentsView;
import org.sagebionetworks.web.client.widget.entity.AttachmentsViewImpl;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetView;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityAccessRequirementsWidgetView;
import org.sagebionetworks.web.client.widget.entity.EntityAccessRequirementsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityIconsCache;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyFormView;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyFormViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxView;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.entity.EvaluationLinksListView;
import org.sagebionetworks.web.client.widget.entity.EvaluationLinksListViewImpl;
import org.sagebionetworks.web.client.widget.entity.EvaluationListView;
import org.sagebionetworks.web.client.widget.entity.EvaluationListViewImpl;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitterView;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitterViewImpl;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidgetView;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelperImpl;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.MyEvaluationEntitiesListView;
import org.sagebionetworks.web.client.widget.entity.MyEvaluationEntitiesListViewImpl;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.RestrictionWidgetView;
import org.sagebionetworks.web.client.widget.entity.RestrictionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidgetView;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidgetView;
import org.sagebionetworks.web.client.widget.entity.SnapshotWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.TutorialWizardView;
import org.sagebionetworks.web.client.widget.entity.TutorialWizardViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiAttachmentsView;
import org.sagebionetworks.web.client.widget.entity.WikiAttachmentsViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidgetViewImpl;
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
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerView;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightControllerImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidgetView;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoWidgetView;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidgetView;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.UploaderView;
import org.sagebionetworks.web.client.widget.entity.download.UploaderViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManagerView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManagerViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.BookmarkConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.BookmarkConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigViewImpl;
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
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuView;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetView;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.BookmarkWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.BookmarkWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.EmptyWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.EmptyWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.OldImageWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.OldImageWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesViewImpl;
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
import org.sagebionetworks.web.client.widget.login.LoginModalView;
import org.sagebionetworks.web.client.widget.login.LoginModalViewImpl;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;
import org.sagebionetworks.web.client.widget.login.LoginWidgetViewImpl;
import org.sagebionetworks.web.client.widget.modal.ModalWindowView;
import org.sagebionetworks.web.client.widget.modal.ModalWindowViewImpl;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationView;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationViewImpl;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;
import org.sagebionetworks.web.client.widget.preview.CytoscapeWidgetView;
import org.sagebionetworks.web.client.widget.preview.CytoscapeWidgetViewImpl;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.search.HomeSearchBoxView;
import org.sagebionetworks.web.client.widget.search.HomeSearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.search.SearchBoxView;
import org.sagebionetworks.web.client.widget.search.SearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBoxViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListEditorView;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListEditorViewImpl;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadgeView;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadgeViewImpl;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGridView;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGridViewImpl;
import org.sagebionetworks.web.client.widget.statictable.StaticTableView;
import org.sagebionetworks.web.client.widget.statictable.StaticTableViewImpl;
import org.sagebionetworks.web.client.widget.table.FocusSetter;
import org.sagebionetworks.web.client.widget.table.FocusSetterImpl;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandlerImpl;
import org.sagebionetworks.web.client.widget.table.TableListWidgetView;
import org.sagebionetworks.web.client.widget.table.TableListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalView;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.CreateDownloadPage;
import org.sagebionetworks.web.client.widget.table.modal.download.CreateDownloadPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.CreateDownloadPageView;
import org.sagebionetworks.web.client.widget.table.modal.download.CreateDownloadPageViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadFilePage;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadFilePageImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadFilePageView;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadFilePageViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsView;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVAppendPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVAppendPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVAppendPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVAppendPageViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPageViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidgetImpl;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputView;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.RowView;
import org.sagebionetworks.web.client.widget.table.v2.results.RowViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageView;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactoryImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCellImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCellImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditor;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewerImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewBase;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewBaseImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewImpl;
import org.sagebionetworks.web.client.widget.team.BigTeamBadgeView;
import org.sagebionetworks.web.client.widget.team.BigTeamBadgeViewImpl;
import org.sagebionetworks.web.client.widget.team.InviteWidgetView;
import org.sagebionetworks.web.client.widget.team.InviteWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidgetView;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.MemberListWidgetView;
import org.sagebionetworks.web.client.widget.team.MemberListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidgetView;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidgetView;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidgetView;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadgeView;
import org.sagebionetworks.web.client.widget.team.TeamBadgeViewImpl;
import org.sagebionetworks.web.client.widget.team.TeamListWidgetView;
import org.sagebionetworks.web.client.widget.team.TeamListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.WizardProgressWidgetView;
import org.sagebionetworks.web.client.widget.team.WizardProgressWidgetViewImpl;
import org.sagebionetworks.web.client.widget.upload.FileInputView;
import org.sagebionetworks.web.client.widget.upload.FileInputViewImpl;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileInputWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl;
import org.sagebionetworks.web.client.widget.user.BigUserBadgeView;
import org.sagebionetworks.web.client.widget.user.BigUserBadgeViewImpl;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.client.widget.user.UserBadgeViewImpl;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidgetView;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidgetViewImpl;

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
		
		bind(ResourceLoaderImpl.class).in(Singleton.class);
		bind(ResourceLoader.class).to(ResourceLoaderImpl.class);
		
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
		
		bind(AnnotationsWidget.class).in(Singleton.class);
		bind(AnnotationsWidgetViewImpl.class).in(Singleton.class);
		bind(AnnotationsWidgetView.class).to(AnnotationsWidgetViewImpl.class);
		
		
		//file history
		bind(FileHistoryWidget.class).in(Singleton.class);
		bind(FileHistoryWidgetViewImpl.class).in(Singleton.class);
		bind(FileHistoryWidgetView.class).to(FileHistoryWidgetViewImpl.class);
		
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
		
		// ClientCache
		bind(ClientCacheImpl.class).in(Singleton.class);
		bind(ClientCache.class).to(ClientCacheImpl.class);

		// Storage wrapper
		bind(StorageImpl.class).in(Singleton.class);
		bind(StorageWrapper.class).to(StorageImpl.class);
		
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
		
		// LoginView
		bind(LoginViewImpl.class).in(Singleton.class);
		bind(LoginView.class).to(LoginViewImpl.class);
		
		// PasswordResetView
		bind(PasswordResetViewImpl.class).in(Singleton.class);
		bind(PasswordResetView.class).to(PasswordResetViewImpl.class);
		
		// NewAccountView
		bind(NewAccountViewImpl.class).in(Singleton.class);
		bind(NewAccountView.class).to(NewAccountViewImpl.class);

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
		bind(ChallengeOverviewViewImpl.class).in(Singleton.class);
		bind(ChallengeOverviewView.class).to(ChallengeOverviewViewImpl.class);	
		
		//Help
		bind(HelpViewImpl.class).in(Singleton.class);
		bind(HelpView.class).to(HelpViewImpl.class);	
		
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
		
		// QuizView
		bind(QuizViewImpl.class).in(Singleton.class);
		bind(QuizView.class).to(QuizViewImpl.class);
		
		//Certificate place
		bind(CertificateView.class).to(CertificateViewImpl.class);
		
		// Certificate
		bind(CertificateWidgetView.class).to(CertificateWidgetViewImpl.class);		
		
		//Account
		bind(AccountViewImpl.class).in(Singleton.class);
		bind(AccountView.class).to(AccountViewImpl.class);
		
		//ChangeUsername
		bind(ChangeUsernameViewImpl.class).in(Singleton.class);
		bind(ChangeUsernameView.class).to(ChangeUsernameViewImpl.class);
		
		// Trash
		bind(TrashViewImpl.class).in(Singleton.class);
		bind(TrashView.class).to(TrashViewImpl.class);

		/*
		 * Factories
		 */
		// editor
		bind(EditorFactoryImpl.class).in(Singleton.class);
		bind(EditorFactory.class).to(EditorFactoryImpl.class);
		// renderer
		bind(RendererFactoryImpl.class).in(Singleton.class);
		bind(RendererFactory.class).to(RendererFactoryImpl.class);
		// table
		bind(TableColumnRendererFactoryImpl.class).in(Singleton.class);
		bind(TableColumnRendererFactory.class).to(TableColumnRendererFactoryImpl.class);
		
		// Asynchronous progress
		bind(TimerProvider.class).to(TimerProviderImpl.class);
		bind(NumberFormatProvider.class).to(NumberFormatProviderImpl.class);
		bind(AsynchronousProgressView.class).to(AsynchronousProgressViewImpl.class);
		bind(AsynchronousJobTracker.class).to(AsynchronousJobTrackerImpl.class);
		
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
		
		// ACL Editor
		bind(AccessControlListEditorView.class).to(AccessControlListEditorViewImpl.class);
		bind(AccessControlListModalWidget.class).to(AccessControlListModalWidgetImpl.class);
		bind(AccessControlListModalWidgetView.class).to(AccessControlListModalWidgetViewImpl.class);
		
		// Sharing Permissions Grid
		bind(SharingPermissionsGridView.class).to(SharingPermissionsGridViewImpl.class);
		
		// Evaluation ACL Editor
		bind(EvaluationAccessControlListEditorView.class).to(EvaluationAccessControlListEditorViewImpl.class);
		
		// basic pagination
		bind(BasicPaginationView.class).to(BasicPaginationViewImpl.class);
		bind(PaginationWidget.class).to(BasicPaginationWidget.class);
		
		// EntityPageTop
		bind(EntityPageTopViewImpl.class).in(Singleton.class);
		bind(EntityPageTopView.class).to(EntityPageTopViewImpl.class);
		
		// Preview
		bind(PreviewWidgetViewImpl.class).in(Singleton.class);
		bind(PreviewWidgetView.class).to(PreviewWidgetViewImpl.class);
		
		// ActionMenu
		bind(ActionMenuViewImpl.class).in(Singleton.class);
		bind(ActionMenuView.class).to(ActionMenuViewImpl.class);
		
		// ActionMenu V2
		bind(ActionMenuWidget.class).to(ActionMenuWidgetImpl.class);
		bind(ActionMenuWidgetView.class).to(ActionMenuWidgetViewImpl.class);
		
		bind(EntityActionController.class).to(EntityActionControllerImpl.class);
		bind(EntityActionControllerView.class).to(EntityActionControllerViewImpl.class);
		bind(PreflightController.class).to(PreflightControllerImpl.class);
		
		// FileBox
		bind(LocationableTitleBarViewImpl.class).in(Singleton.class);
		bind(LocationableTitleBarView.class).to(LocationableTitleBarViewImpl.class);
		
		// FileBox
		bind(FileTitleBarViewImpl.class).in(Singleton.class);
		bind(FileTitleBarView.class).to(FileTitleBarViewImpl.class);
				
		// Search Box
		bind(SearchBoxViewImpl.class).in(Singleton.class);
		bind(SearchBoxView.class).to(SearchBoxViewImpl.class);
		
		// User Suggest Box
		bind(UserGroupSuggestBoxView.class).to(UserGroupSuggestBoxViewImpl.class);

		// Home Search Box
		bind(HomeSearchBoxViewImpl.class).in(Singleton.class);
		bind(HomeSearchBoxView.class).to(HomeSearchBoxViewImpl.class);
		
		bind(MultipartUploader.class).to(MultipartUploaderImpl.class);
		bind(FileInputWidget.class).to(FileInputWidgetImpl.class);
		bind(FileInputView.class).to(FileInputViewImpl.class);

		// LocationableUploader
		bind(UploaderView.class).to(UploaderViewImpl.class);
		
		bind(QuizInfoWidgetView.class).to(QuizInfoViewImpl.class);

		// EntityTreeBrowser
		bind(EntityTreeBrowserView.class).to(EntityTreeBrowserViewImpl.class);

		// MyEntitiesBrowser
		bind(MyEntitiesBrowserView.class).to(MyEntitiesBrowserViewImpl.class);

		// Attachments		
		bind(AttachmentsView.class).to(AttachmentsViewImpl.class);

		// Wiki Attachments		
		bind(WikiAttachmentsView.class).to(WikiAttachmentsViewImpl.class);

		bind(WikiHistoryWidgetView.class).to(WikiHistoryWidgetViewImpl.class);
		
		//Evaluation selector
		bind(EvaluationListView.class).to(EvaluationListViewImpl.class);
		
		//Single Evaluation link selection
		bind(EvaluationLinksListView.class).to(EvaluationLinksListViewImpl.class);
		
		//My Evaluations
		bind(MyEvaluationEntitiesListView.class).to(MyEvaluationEntitiesListViewImpl.class);
		
		//Administer Evaluations list 
		bind(AdministerEvaluationsListView.class).to(AdministerEvaluationsListViewImpl.class);
		
		
		// SnapshotWidget		
		bind(SnapshotWidgetView.class).to(SnapshotWidgetViewImpl.class);

		// EntitySearchBox		
		bind(EntitySearchBoxView.class).to(EntitySearchBoxViewImpl.class);

		// EntityMetadata
		bind(EntityMetadataViewImpl.class).in(Singleton.class);
		bind(EntityMetadataView.class).to(EntityMetadataViewImpl.class);
		
		// ProfileFormView
		bind(ProfileFormView.class).to(ProfileFormViewImpl.class);		

		// API Table Column manager	
		bind(APITableColumnManagerView.class).to(APITableColumnManagerViewImpl.class);

		//single subpages view
		bind(WikiSubpagesViewImpl.class).in(Singleton.class);
		bind(WikiSubpagesView.class).to(WikiSubpagesViewImpl.class);
		
		//Widget Registration
		bind(WidgetRegistrarImpl.class).in(Singleton.class);
		bind(WidgetRegistrar.class).to(WidgetRegistrarImpl.class);
		
		// UI Widget Descriptor editor
		bind(BookmarkConfigView.class).to(BookmarkConfigViewImpl.class);
		bind(BaseEditWidgetDescriptorView.class).to(BaseEditWidgetDescriptorViewImpl.class);
		bind(ReferenceConfigView.class).to(ReferenceConfigViewImpl.class);
		bind(YouTubeConfigView.class).to(YouTubeConfigViewImpl.class);
		bind(OldImageConfigView.class).to(OldImageConfigViewImpl.class);
		bind(ImageConfigView.class).to(ImageConfigViewImpl.class);
		bind(AttachmentConfigView.class).to(AttachmentConfigViewImpl.class);
		bind(ProvenanceConfigView.class).to(ProvenanceConfigViewImpl.class);
		bind(LinkConfigView.class).to(LinkConfigViewImpl.class);
		bind(TabbedTableConfigView.class).to(TabbedTableConfigViewImpl.class);
		bind(APITableConfigView.class).to(APITableConfigViewImpl.class);
		bind(QueryTableConfigView.class).to(QueryTableConfigViewImpl.class);
		bind(EntityListConfigView.class).to(EntityListConfigViewImpl.class);
		bind(ShinySiteConfigView.class).to(ShinySiteConfigViewImpl.class);
		bind(ButtonLinkConfigView.class).to(ButtonLinkConfigViewImpl.class);
		bind(VideoConfigView.class).to(VideoConfigViewImpl.class);
		
		// UI Widget Renderers
		bind(BookmarkWidgetView.class).to(BookmarkWidgetViewImpl.class);
		bind(ReferenceWidgetView.class).to(ReferenceWidgetViewImpl.class);
		bind(YouTubeWidgetView.class).to(YouTubeWidgetViewImpl.class);
		bind(OldImageWidgetView.class).to(OldImageWidgetViewImpl.class);
		bind(EntityListWidgetView.class).to(EntityListWidgetViewImpl.class);
		bind(ShinySiteWidgetView.class).to(ShinySiteWidgetViewImpl.class);		
		bind(ImageWidgetView.class).to(ImageWidgetViewImpl.class);
		bind(AttachmentPreviewWidgetView.class).to(AttachmentPreviewWidgetViewImpl.class);
		bind(APITableWidgetView.class).to(APITableWidgetViewImpl.class);
		bind(TableOfContentsWidgetView.class).to(TableOfContentsWidgetViewImpl.class);
		bind(WikiFilesPreviewWidgetView.class).to(WikiFilesPreviewWidgetViewImpl.class);
		bind(ButtonLinkWidgetView.class).to(ButtonLinkWidgetViewImpl.class);
		bind(EmptyWidgetView.class).to(EmptyWidgetViewImpl.class);
		bind(VideoWidgetView.class).to(VideoWidgetViewImpl.class);
		
		
		// ProvenanceWidget
		bind(ProvenanceWidgetView.class).to(ProvenanceWidgetViewImpl.class);
		
		// MarkdownEditorWidget
		bind(MarkdownEditorWidgetView.class).to(MarkdownEditorWidgetViewImpl.class);
		
		// FilesBrowser
		bind(FilesBrowserView.class).to(FilesBrowserViewImpl.class);
		
		// Entity Finder
		bind(EntityFinderView.class).to(EntityFinderViewImpl.class);		

		bind(EvaluationSubmitterView.class).to(EvaluationSubmitterViewImpl.class);
		
		bind(FavoriteWidgetView.class).to(FavoriteWidgetViewImpl.class);
		
		bind(DoiWidgetView.class).to(DoiWidgetViewImpl.class);
		
		bind(WikiPageWidgetView.class).to(WikiPageWidgetViewImpl.class);
		bind(UserBadgeView.class).to(UserBadgeViewImpl.class);
		bind(BigUserBadgeView.class).to(BigUserBadgeViewImpl.class);
		
		bind(EntityIconsCache.class).in(Singleton.class);
		
		bind(EntityBadgeView.class).to(EntityBadgeViewImpl.class);
		
		bind(TutorialWizardView.class).to(TutorialWizardViewImpl.class);
		
		bind(CytoscapeWidgetView.class).to(CytoscapeWidgetViewImpl.class);
		
		bind(PublicPrivateBadgeView.class).to(PublicPrivateBadgeViewImpl.class);
		
		/*
		 * Modal wizard stuff.		
		 */
		bind(ModalWizardView.class).to(ModalWizardViewImpl.class);
		bind(ModalWizardWidget.class).to(ModalWizardWidgetImpl.class);
		
		/*
		 * TableEntity related bindings
		 */
		bind(TableListWidgetView.class).to(TableListWidgetViewImpl.class);
		bind(CreateTableModalWidget.class).to(CreateTableModalWidgetImpl.class);
		bind(ColumnModelsViewBase.class).to(ColumnModelsViewBaseImpl.class);
		bind(ColumnModelsView.class).to(ColumnModelsViewImpl.class);
		bind(ColumnModelTableRowEditor.class).to(ColumnModelTableRowEditorImpl.class);
		bind(ColumnModelTableRowViewer.class).to(ColumnModelTableRowViewerImpl.class);
		bind(TableEntityWidgetView.class).to(TableEntityWidgetViewImpl.class);
		bind(RowView.class).to(RowViewImpl.class);
		bind(TablePageView.class).to(TablePageViewImpl.class);
		bind(TableQueryResultView.class).to(TableQueryResultViewImpl.class);
		bind(QueryResultEditorView.class).to(QueryResultEditorViewImpl.class);
		bind(CellFactory.class).to(CellFactoryImpl.class);
		bind(QueryInputView.class).to(QueryInputViewImpl.class);
		bind(JobTrackingWidget.class).to(AsynchronousProgressWidget.class);
		bind(CreateTableModalView.class).to(CreateTableModalViewImpl.class);
		bind(UploadTableModalWidget.class).to(UploadTableModalWidgetImpl.class);
		bind(UploadCSVPreviewPage.class).to(UploadCSVPreviewPageImpl.class);
		bind(CSVOptionsView.class).to(CSVOptionsViewImpl.class);
		bind(CSVOptionsWidget.class).to(CSVOptionsWidgetImpl.class);
		bind(UploadCSVPreviewPageView.class).to(UploadCSVPreviewPageViewImpl.class);
		bind(UploadPreviewView.class).to(UploadPreviewViewImpl.class);
		bind(UploadPreviewWidget.class).to(UploadPreviewWidgetImpl.class);
		bind(UploadCSVFilePage.class).to(UploadCSVFilePageImpl.class);
		bind(UploadCSVFinishPage.class).to(UploadCSVFinishPageImpl.class);
		bind(UploadCSVFinishPageView.class).to(UploadCSVFinishPageViewImpl.class);
		bind(UploadCSVAppendPage.class).to(UploadCSVAppendPageImpl.class);
		bind(UploadCSVAppendPageView.class).to(UploadCSVAppendPageViewImpl.class);
		
		bind(CreateDownloadPage.class).to(CreateDownloadPageImpl.class);
		bind(CreateDownloadPageView.class).to(CreateDownloadPageViewImpl.class);
		
		bind(DownloadFilePage.class).to(DownloadFilePageImpl.class);
		bind(DownloadFilePageView.class).to(DownloadFilePageViewImpl.class);
		bind(DownloadTableQueryModalWidget.class).to(DownloadTableQueryModalWidgetImpl.class);
		
		// Keyboard navigation
		bind(KeyboardNavigationHandler.class).to(KeyboardNavigationHandlerImpl.class);
		bind(FocusSetter.class).to(FocusSetterImpl.class);
		
		/*
		 * TableEntity cell bindings.
		 */
		bind(StringEditorCell.class).to(StringEditorCellImpl.class);
		bind(StringRendererCell.class).to(StringRendererCellImpl.class);

		/*
		 * Teams Places
		 */
		// Team Page
		bind(TeamViewImpl.class).in(Singleton.class);
		bind(TeamView.class).to(TeamViewImpl.class);

		// Team Search Page
		bind(TeamSearchViewImpl.class).in(Singleton.class);
		bind(TeamSearchView.class).to(TeamSearchViewImpl.class);
		
		// People Search Page
		bind(PeopleSearchViewImpl.class).in(Singleton.class);
		bind(PeopleSearchView.class).to(PeopleSearchViewImpl.class);

		/*
		 * Teams Widgets
		 */
		// Open Team Invitations widget
		bind(OpenTeamInvitationsWidgetView.class).to(OpenTeamInvitationsWidgetViewImpl.class);
		
		// Pending Team Join Requests widget
		bind(OpenMembershipRequestsWidgetView.class).to(OpenMembershipRequestsWidgetViewImpl.class);
		
		// Current User Invites widget
		bind(OpenUserInvitationsWidgetView.class).to(OpenUserInvitationsWidgetViewImpl.class);
		
		// Team List widget (link to search teams page, optionally can create team)
		bind(TeamListWidgetView.class).to(TeamListWidgetViewImpl.class);
		
		// User Group List widget
		bind(UserGroupListWidgetView.class).to(UserGroupListWidgetViewImpl.class);

		// Member List widget
		bind(MemberListWidgetView.class).to(MemberListWidgetViewImpl.class);
		
		//Invite Team member widget
		bind(InviteWidgetView.class).to(InviteWidgetViewImpl.class);
		
		//Request Team membership widget
		bind(JoinTeamWidgetView.class).to(JoinTeamWidgetViewImpl.class);
		//Submit to evaluation widget
		bind(SubmitToEvaluationWidgetView.class).to(SubmitToEvaluationWidgetViewImpl.class);
		//Team renderer
		bind(TeamBadgeView.class).to(TeamBadgeViewImpl.class);
		bind(BigTeamBadgeView.class).to(BigTeamBadgeViewImpl.class);
		
		
		bind(UserTeamConfigView.class).to(UserTeamConfigViewImpl.class);
		
		bind(RestrictionWidgetView.class).to(RestrictionWidgetViewImpl.class);

		bind(SharingAndDataUseConditionWidgetView.class).to(SharingAndDataUseConditionWidgetViewImpl.class);
		
		bind(WizardProgressWidgetView.class).to(WizardProgressWidgetViewImpl.class);
		bind(EntityAccessRequirementsWidgetView.class).to(EntityAccessRequirementsWidgetViewImpl.class);
		bind(UploadDialogWidgetView.class).to(UploadDialogWidgetViewImpl.class);
		
		bind(LoginModalView.class).to(LoginModalViewImpl.class);
		
	}

}
