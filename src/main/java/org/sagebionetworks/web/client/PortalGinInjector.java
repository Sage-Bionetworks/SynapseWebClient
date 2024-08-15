package org.sagebionetworks.web.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.context.QueryClientProvider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.presenter.ACTAccessApprovalsPresenter;
import org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionDashboardPresenter;
import org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionsPresenter;
import org.sagebionetworks.web.client.presenter.ACTPresenter;
import org.sagebionetworks.web.client.presenter.AccessRequirementPresenter;
import org.sagebionetworks.web.client.presenter.AccessRequirementsPresenter;
import org.sagebionetworks.web.client.presenter.AccountPresenter;
import org.sagebionetworks.web.client.presenter.BulkPresenterProxy;
import org.sagebionetworks.web.client.presenter.ChallengeOverviewPresenter;
import org.sagebionetworks.web.client.presenter.ChangeUsernamePresenter;
import org.sagebionetworks.web.client.presenter.ComingSoonPresenter;
import org.sagebionetworks.web.client.presenter.DataAccessApprovalTokenPresenter;
import org.sagebionetworks.web.client.presenter.DataAccessManagementPresenter;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.presenter.DownloadCartPresenter;
import org.sagebionetworks.web.client.presenter.EmailInvitationPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.ErrorPresenter;
import org.sagebionetworks.web.client.presenter.FollowingPagePresenter;
import org.sagebionetworks.web.client.presenter.HelpPresenter;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.presenter.MapPresenter;
import org.sagebionetworks.web.client.presenter.NewAccountPresenter;
import org.sagebionetworks.web.client.presenter.OAuthClientEditorPresenter;
import org.sagebionetworks.web.client.presenter.PasswordResetSignedTokenPresenter;
import org.sagebionetworks.web.client.presenter.PeopleSearchPresenter;
import org.sagebionetworks.web.client.presenter.PersonalAccessTokensPresenter;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.QuizPresenter;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SignedTokenPresenter;
import org.sagebionetworks.web.client.presenter.SubscriptionPresenter;
import org.sagebionetworks.web.client.presenter.SynapseForumPresenter;
import org.sagebionetworks.web.client.presenter.SynapseStandaloneWikiPresenter;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenter;
import org.sagebionetworks.web.client.presenter.TeamPresenter;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.presenter.TrashPresenter;
import org.sagebionetworks.web.client.presenter.TwoFactorAuthPresenter;
import org.sagebionetworks.web.client.presenter.WikiDiffPresenter;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParser;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.EntityTypeIcon;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.QuarantinedEmailModal;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateOrUpdateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.LegacyCreateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidget;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.InlineAsynchronousProgressViewImpl;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceEditor;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditor;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListItemWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.EditDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.docker.DockerCommitRowWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModal;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
import org.sagebionetworks.web.client.widget.entity.*;
import org.sagebionetworks.web.client.widget.entity.SqlDefinedEditorModalWidget;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.act.RejectReasonWidget;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeItem;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditor;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialog;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidgetView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.CreateDatasetOrCollection;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryView;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.CytoscapeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.DetailsSummaryConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EvaluationSubmissionConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageLinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LeaderboardConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.PlotlyConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.PreviewConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.SynapseFormConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TableQueryResultWikiEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TeamSelectEditor;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadHandlerWidget;
import org.sagebionetworks.web.client.widget.entity.file.ProjectTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.S3DirectLoginDialog;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.BookmarkWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.CancelControlWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeParticipantsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeTeamsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.CytoscapeWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EmptyWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.IntendedDataUseReportWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.NbConvertPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.PDFPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.RegisterChallengeTeamWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SynapseTableFormWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TIFFPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMembersWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListRowWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTabView;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorReactComponentPage;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationRowWidget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapper;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileWidget;
import org.sagebionetworks.web.client.widget.provenance.v2.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.refresh.DiscussionThreadCountAlert;
import org.sagebionetworks.web.client.widget.refresh.EntityRefreshAlert;
import org.sagebionetworks.web.client.widget.refresh.ReplyCountAlert;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.AclAddPeoplePanel;
import org.sagebionetworks.web.client.widget.sharing.EntityAccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.EntityAccessControlListModalWidgetImpl;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGrid;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidget;
import org.sagebionetworks.web.client.widget.table.TableEntityListGroupItem;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.api.APITableWidget;
import org.sagebionetworks.web.client.widget.table.explore.TableEntityWidgetV2;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TotalVisibleResultsWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.RowWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.StaticTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanFormCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateListRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DoubleCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EditJSONListModal;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdListRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumFormCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.IntegerCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LargeStringCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LinkCellRendererView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringListRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdListRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.EmailInvitationBadge;
import org.sagebionetworks.web.client.widget.team.JoinTeamConfigEditor;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationWidget;
import org.sagebionetworks.web.client.widget.team.SelectTeamModal;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.team.UserTeamBadge;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamProjectsModalWidget;
import org.sagebionetworks.web.client.widget.upload.CroppedImageUploadViewImpl;
import org.sagebionetworks.web.client.widget.upload.FileHandleLink;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.ImageUploadView;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionModalViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionRowViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;

/**
 * The root portal dependency injection root.
 *
 * @author jmhill
 *
 */
@GinModules(PortalGinModule.class)
public interface PortalGinInjector extends Ginjector {
  BulkPresenterProxy getBulkPresenterProxy();

  GlobalApplicationState getGlobalApplicationState();

  HomePresenter getHomePresenter();

  EntityPresenter getEntityPresenter();

  LoginPresenter getLoginPresenter();

  PasswordResetSignedTokenPresenter getPasswordResetSignedTokenPresenter();

  AuthenticationController getAuthenticationController();

  PasswordResetPresenter getPasswordResetPresenter();

  RegisterAccountPresenter getRegisterAccountPresenter();

  ProfilePresenter getProfilePresenter();

  ComingSoonPresenter getComingSoonPresenter();

  ChallengeOverviewPresenter getChallengeOverviewPresenter();

  HelpPresenter getHelpPresenter();

  SearchPresenter getSearchPresenter();

  SynapseWikiPresenter getSynapseWikiPresenter();

  DownPresenter getDownPresenter();

  TeamPresenter getTeamPresenter();

  MapPresenter getMapPresenter();

  QuizPresenter getQuizPresenter();

  AccountPresenter getAccountPresenter();

  NewAccountPresenter getNewAccountPresenter();

  SignedTokenPresenter getSignedTokenPresenter();

  ErrorPresenter getErrorPresenter();

  DownloadCartPresenter getDownloadCartPresenter();

  ChangeUsernamePresenter getChangeUsernamePresenter();

  TrashPresenter getTrashPresenter();

  TeamSearchPresenter getTeamSearchPresenter();

  PeopleSearchPresenter getPeopleSearchPresenter();

  SynapseStandaloneWikiPresenter getSynapseStandaloneWikiPresenter();

  EventBus getEventBus();

  MarkdownWidget getMarkdownWidget();

  ACTPresenter getACTPresenter();

  AccessRequirementsPresenter getAccessRequirementsPresenter();
  AccessRequirementPresenter getAccessRequirementPresenter();

  ACTDataAccessSubmissionsPresenter getACTDataAccessSubmissionsPresenter();

  ACTDataAccessSubmissionDashboardPresenter getACTDataAccessSubmissionDashboardPresenter();

  SynapseForumPresenter getSynapseForumPresenter();

  SubscriptionPresenter getSubscriptionPresenter();

  ACTAccessApprovalsPresenter getACTAccessApprovalsPresenter();

  WikiDiffPresenter getWikiDiffPresenter();

  EmailInvitationPresenter getEmailInvitationPresenter();
  DataAccessApprovalTokenPresenter getDataAccessApprovalTokenPresenter();

  DataAccessManagementPresenter getDataAccessManagementPresenter();

  OAuthClientEditorPresenter getOAuthClientEditorPresenter();

  /*
   * Markdown Widgets
   */
  ////// Editors
  ReferenceConfigEditor getReferenceConfigEditor();

  ProvenanceConfigEditor getProvenanceConfigEditor();

  ImageConfigEditor getImageConfigEditor();

  ImageLinkConfigEditor getImageLinkConfigEditor();

  AttachmentConfigEditor getAttachmentConfigEditor();

  LinkConfigEditor getLinkConfigEditor();

  DetailsSummaryConfigEditor getDetailsSummaryConfigEditor();

  APITableConfigEditor getSynapseAPICallConfigEditor();

  QueryTableConfigEditor getSynapseQueryConfigEditor();

  LeaderboardConfigEditor getLeaderboardConfigEditor();

  TabbedTableConfigEditor getTabbedTableConfigEditor();

  EntityTreeBrowser getEntityTreeBrowser();

  EntityListConfigEditor getEntityListConfigEditor();

  ShinySiteConfigEditor getShinySiteConfigEditor();

  ButtonLinkConfigEditor getButtonLinkConfigEditor();

  EvaluationSubmissionConfigEditor getEvaluationSubmissionConfigEditor();

  UserTeamConfigEditor getUserTeamConfigEditor();

  VideoConfigEditor getVideoConfigEditor();

  TableQueryResultWikiEditor getSynapseTableQueryResultEditor();

  TotalVisibleResultsWidget getTotalVisibleResultsWidget();

  PreviewConfigEditor getPreviewConfigEditor();

  SynapseFormConfigEditor getSynapseFormConfigEditor();

  BiodallianceEditor getBiodallianceEditor();

  BiodallianceSourceEditor getBiodallianceSourceEditor();

  CytoscapeConfigEditor getCytoscapeConfigEditor();

  PlotlyConfigEditor getPlotlyConfigEditor();

  TeamSelectEditor getTeamSelectEditor();

  ////// Renderers
  BookmarkWidget getBookmarkRenderer();

  ReferenceWidget getReferenceRenderer();

  TutorialWizard getTutorialWidgetRenderer();

  org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget getProvenanceRenderer();
  ProvenanceWidget getProvenanceRendererV2();

  AdministerEvaluationsList getAdministerEvaluationsList();

  ImageWidget getImageRenderer();

  AttachmentPreviewWidget getAttachmentPreviewRenderer();

  APITableWidget getSynapseAPICallRenderer();

  TableOfContentsWidget getTableOfContentsRenderer();

  WikiFilesPreviewWidget getWikiFilesPreviewRenderer();

  EntityListWidget getEntityListRenderer();

  ShinySiteWidget getShinySiteRenderer();

  JoinTeamWidget getJoinTeamWidget();

  SubmitToEvaluationWidget getEvaluationSubmissionWidget();

  EmptyWidget getEmptyWidget();

  ButtonLinkWidget getButtonLinkWidget();

  VideoWidget getVideoWidget();

  TableQueryResultWikiWidget getSynapseTableQueryResultWikiWidget();

  RegisterChallengeTeamWidget getRegisterChallengeTeamWidget();

  ChallengeTeamsWidget getChallengeTeamsWidget();

  ChallengeParticipantsWidget getChallengeParticipantsWidget();

  BiodallianceWidget getBiodallianceRenderer();

  CytoscapeWidget getCytoscapeRenderer();

  SynapseTableFormWidget getSynapseTableFormWidget();

  TeamMembersWidget getTeamMembersWidget();

  TeamMemberCountWidget getTeamMemberCountWidget();

  PlotlyWidget getPlotlyWidget();

  LazyLoadWikiWidgetWrapper getLazyLoadWikiWidgetWrapper();

  ////// API Table Column Editor
  APITableColumnConfigView getAPITableColumnConfigView();

  // Other widgets
  UserBadge getUserBadgeWidget();

  EmailInvitationBadge getEmailInvitationBadgeWidget();

  VersionTimer getVersionTimer();

  SessionDetector getSessionDetector();

  WebStorageMaxSizeDetector getWebStorageMaxSizeDetector();

  SynapseStatusDetector getSynapseStatusDetector();

  SynapseAlert getSynapseAlertWidget();

  EntityRefProvEntryView getEntityRefEntry();

  URLProvEntryView getURLEntry();

  ProvenanceListWidget getProvenanceListWidget();

  PreviewWidget getPreviewWidget();

  UserBadgeItem getUserBadgeItem();

  // TableEntity V2
  ColumnModelsView createNewColumnModelsView();

  ColumnModelsWidget createNewColumnModelsWidget();

  ColumnModelTableRowViewer createNewColumnModelTableRowViewer();

  ColumnModelTableRowEditorWidget createColumnModelEditorWidget();

  TableEntityWidgetV2 createNewTableEntityWidgetV2();

  RowWidget createRowWidget();

  QueryResultEditorWidget createNewQueryResultEditorWidget();

  // TableEntity V2 cells
  StringRendererCellView createStringRendererCellView();
  StringListRendererCellView createStringListRendererCellView();
  UserIdListRendererCellView createUserIdListRendererCellView();
  EntityIdListRendererCellView createEntityIdListRendererCellView();
  DateListRendererCellView createDateListRendererCellView();

  StringEditorCell createStringEditorCell();

  JSONListCellEditor createJSONListCellEditor();

  EntityIdCellEditor createEntityIdCellEditor();

  EntityIdCellRenderer createEntityIdCellRenderer();

  EnumCellEditor createEnumCellEditor();

  EnumFormCellEditor createEnumFormCellEditor();

  BooleanCellEditor createBooleanCellEditor();

  BooleanFormCellEditor createBooleanFormCellEditor();

  DateCellEditor createDateCellEditor();

  DateCellRenderer createDateCellRenderer();

  DoubleCellEditor createDoubleCellEditor();

  IntegerCellEditor createIntegerCellEditor();

  LinkCellRendererView createLinkCellRenderer();

  FileCellEditor createFileCellEditor();

  FileCellRenderer createFileCellRenderer();

  UserIdCellRenderer createUserIdCellRenderer();

  UserIdCellEditor createUserIdCellEditor();

  LargeStringCellEditor createLargeTextFormCellEditor();

  ListCellEditorView createListCellEditorView();
  RadioCellEditorView createRadioCellEditorView();

  // Asynchronous
  AsynchronousProgressWidget creatNewAsynchronousProgressWidget();

  InlineAsynchronousProgressViewImpl getInlineAsynchronousProgressView();

  UserTeamBadge getUserTeamBadgeWidget();

  TeamBadge getTeamBadgeWidget();

  BigTeamBadge getBigTeamBadgeWidget();

  ChallengeBadge getChallengeBadgeWidget();

  ProjectBadge getProjectBadgeWidget();

  EntityTreeItem getEntityTreeItemWidget();

  MoreTreeItem getMoreTreeWidget();

  TableListWidget getTableListWidget();

  CookieProvider getCookieProvider();

  SessionStorage getSessionStorage();

  Header getHeader();

  Footer getFooter();

  SortableTableHeader createSortableTableHeader();

  StaticTableHeader createStaticTableHeader();

  EvaluationSubmitter getEvaluationSubmitter();

  RegisterTeamDialog getRegisterTeamDialog();

  AnnotationEditor getAnnotationEditor();

  EditJSONListModal getEditJsonModal();

  CommaSeparatedValuesParser getCommaSeparatedValuesParser();

  VersionHistoryRowView getFileHistoryRow();

  VersionHistoryWidget getVersionHistoryWidget();

  JoinTeamConfigEditor getJoinTeamConfigEditor();

  ModifiedCreatedByWidget getModifiedCreatedByWidget();

  FileHandleLink getFileHandleLink();

  VerificationSubmissionWidget getVerificationSubmissionWidget();

  VerificationSubmissionModalViewImpl getVerificationSubmissionModalViewImpl();

  VerificationSubmissionRowViewImpl getVerificationSubmissionRowViewImpl();

  // discussion
  DiscussionThreadListItemWidget createThreadListItemWidget();

  ReplyWidget createReplyWidget();

  EntityRefreshAlert getEntityRefreshAlert();

  ReplyCountAlert getReplyCountAlert();

  DiscussionThreadCountAlert getDiscussionThreadCountAlert();

  // docker
  DockerRepoWidget createNewDockerRepoWidget();

  DockerCommitRowWidget createNewDockerCommitRowWidget();

  LoginWidget getLoginWidget();

  FileClientsHelp getFileClientsHelp();

  LoadMoreWidgetContainer getLoadMoreProjectsWidgetContainer();

  RadioWidget createNewRadioWidget();

  EntityListRowBadge getEntityListRowBadge();

  CancelControlWidget getCancelControlWidget();

  DiscussionTabView getDiscussionTabView();

  ForumWidget getForumWidget();

  DockerTabView getDockerTabView();

  DockerRepoListWidget getDockerRepoListWidget();

  Breadcrumb getBreadcrumb();

  SynapseClientAsync getSynapseClientAsync();

  SynapseJavascriptClient getSynapseJavascriptClient();

  StuAlert getStuAlert();

  FilesTabView getFilesTabView();

  BasicTitleBar getBasicTitleBar();

  ProjectTitleBar getProjectTitleBar();

  EntityMetadata getEntityMetadata();

  FilesBrowser getFilesBrowser();

  WikiPageWidget getWikiPageWidget();

  DiscussionThreadListWidget getDiscussionThreadListWidget();

  ChallengeTabView getChallengeTabView();

  ChallengeWidget getChallengeWidget();

  TablesTabView getTablesTabView();

  QueryTokenProvider getQueryTokenProvider();

  PersonalAccessTokensPresenter getPersonalAccessTokensPresenter();

  AccessControlListModalWidget getAccessControlListModalWidget();

  EntityAccessControlListModalWidget getEntityAccessControlListModalWidget();

  RenameEntityModalWidget getRenameEntityModalWidget();

  EditFileMetadataModalWidget getEditFileMetadataModalWidget();

  EditProjectMetadataModalWidget getEditProjectMetadataModalWidget();

  EntityFinderWidget.Builder getEntityFinderBuilder();

  EntityFinderWidgetView getEntityFinderWidgetView();

  UploadDialogWidget getUploadDialogWidget();

  WikiMarkdownEditor getWikiMarkdownEditor();

  AddFolderDialogWidget getAddFolderDialogWidget();

  ProvenanceEditorWidget getProvenanceEditorWidget();

  StorageLocationWidget getStorageLocationWidget();

  EvaluationEditorModal getEvaluationEditorModal();

  SelectTeamModal getSelectTeamModal();

  CreateOrUpdateDoiModal getCreateOrUpdateDoiModal();

  ApproveUserAccessModal getApproveUserAccessModal();

  ChallengeClientAsync getChallengeClientAsync();

  EntityIdCellRenderer getEntityIdCellRenderer();

  UserIdCellRenderer getUserIdCellRenderer();

  ManagedACTAccessRequirementWidget getManagedACTAccessRequirementWidget();

  ACTAccessRequirementWidget getACTAccessRequirementWidget();

  LockAccessRequirementWidget getLockAccessRequirementWidget();

  TermsOfUseAccessRequirementWidget getTermsOfUseAccessRequirementWidget();

  FileHandleWidget getFileHandleWidget();

  LegacyCreateAccessRequirementWizard getLegacyCreateAccessRequirementWizard();

  CreateOrUpdateAccessRequirementWizard getCreateOrUpdateAccessRequirementWizard();

  ProfileCertifiedValidatedWidget getProfileCertifiedValidatedWidget();

  ACTDataAccessSubmissionWidget getACTDataAccessSubmissionWidget();

  OpenSubmissionWidget getOpenSubmissionWidget();

  DateTimeUtils getDateTimeUtils();

  AccessorGroupWidget getAccessorGroupWidget();

  AccessRequirementWidget getAccessRequirementWidget();

  SelfSignAccessRequirementWidget getSelfSignAccessRequirementWidget();

  TeamSubjectWidget getSubjectWidget();

  UserListRowWidget getUserListRowWidget();

  RequestBuilderWrapper getRequestBuilder();

  EditAnnotationsDialog getEditAnnotationsDialog();

  CreateTableViewWizard getCreateTableViewWizard();

  CreateDatasetOrCollection getCreateDatasetOrCollection();

  UploadTableModalWidget getUploadTableModalWidget();

  SqlDefinedEditorModalWidget getSqlDefinedEditorModalWidget();

  AddExternalRepoModal getAddExternalRepoModal();

  PDFPreviewWidget getPDFPreviewWidget();

  TIFFPreviewWidget getTIFFPreviewWidget();

  HtmlPreviewWidget getHtmlPreviewWidget();

  NbConvertPreviewWidget getNbConvertPreviewWidget();

  S3DirectLoginDialog getS3DirectLoginDialog();

  DownloadTableQueryModalWidget getDownloadTableQueryModalWidget();

  CopyTextModal getCopyTextModal();

  UserProfileWidget getUserProfileWidget();

  PromptForValuesModalView getPromptForValuesModal();

  PromptForValuesModalView.Configuration.Builder getPromptForValuesModalConfigurationBuilder();

  CroppedImageUploadViewImpl getCroppedImageUploadView();

  ImageUploadView getImageUploadView();

  LazyLoadHelper getLazyLoadHelper();

  SharingPermissionsGrid getSharingPermissionsGrid();

  AclAddPeoplePanel getAclAddPeoplePanel();

  FileHandleUploadWidget getFileHandleUploadWidget();

  WikiPageDeleteConfirmationDialog getWikiPageDeleteConfirmationDialog();

  WikiVersionAnchorListItem getWikiVersionAnchorListItem();

  FileDownloadHandlerWidget getFileDownloadHandlerWidget();

  SynapseProperties getSynapseProperties();

  QuizInfoDialog getQuizInfoDialog();

  EvaluationRowWidget getEvaluationRowWidget();

  EvaluationEditorReactComponentPage createEvaluationEditorReactComponentPage();

  EditDiscussionThreadModal getEditDiscussionThreadModal();

  TableEntityListGroupItem getTableEntityListGroupItem();

  SynapseJSNIUtilsImpl getSynapseJSNIUtils();

  OpenUserInvitationWidget getOpenUserInvitationWidget();

  OpenMembershipRequestWidget getOpenMembershipRequestWidget();

  OpenTeamInvitationWidget getOpenTeamInvitationWidget();

  DivView getDiv();

  DoiWidgetV2 getDoiWidget();

  TeamDeleteModalWidget getTeamDeleteModalWidget();

  TeamLeaveModalWidget getTeamLeaveModalWidget();

  TeamEditModalWidget getTeamEditModalWidget();

  TeamProjectsModalWidget getTeamProjectsModalWidget();

  StatisticsPlotWidget getStatisticsPlotWidget();

  QuarantinedEmailModal getQuarantinedEmailModal();

  ViewDefaultColumns getViewDefaultColumns();

  RejectReasonWidget getRejectReasonWidget();

  IntendedDataUseReportWidget getIntendedDataUseReportWidget();
  PresignedURLAsyncHandler getPresignedURLAsyncHandler();
  AddToDownloadListV2 getAddToDownloadListV2();
  JSONObjectAdapter getJSONObjectAdapter();

  QueryClientProvider getQueryClientProvider();

  PopupUtilsView getPopupUtils();

  ContainerClientsHelp getContainerClientsHelp();

  TwoFactorAuthPresenter getTwoFactorAuthPresenter();

  FollowingPagePresenter getFollowingPagePresenter();

  ColumnModelsEditorWidget getColumnModelsEditorWidget();

  EntityTypeIcon getEntityTypeIcon();

  FeatureFlagConfig getFeatureFlagConfig();
}
