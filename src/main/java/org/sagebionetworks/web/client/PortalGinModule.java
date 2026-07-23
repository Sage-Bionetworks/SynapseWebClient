package org.sagebionetworks.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.gwt.client.schema.adapter.JSONArrayGwt;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.analytics.SearchAnalyticsClient;
import org.sagebionetworks.web.client.analytics.SearchAnalyticsClientImpl;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.ClientCacheImpl;
import org.sagebionetworks.web.client.cache.EntityId2BundleCache;
import org.sagebionetworks.web.client.cache.EntityId2BundleCacheImpl;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cache.SessionStorageImpl;
import org.sagebionetworks.web.client.cache.StorageImpl;
import org.sagebionetworks.web.client.cache.StorageWrapper;
import org.sagebionetworks.web.client.context.KeyFactoryProvider;
import org.sagebionetworks.web.client.context.KeyFactoryProviderImpl;
import org.sagebionetworks.web.client.context.QueryClientProvider;
import org.sagebionetworks.web.client.context.QueryClientProviderImpl;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProviderImpl;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.cookie.GWTCookieImpl;
import org.sagebionetworks.web.client.presenter.EntityPresenterEventBinder;
import org.sagebionetworks.web.client.presenter.EntityPresenterEventBinderImpl;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.ResourceLoaderImpl;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.transform.JsoProviderImpl;
import org.sagebionetworks.web.client.view.ACTAccessApprovalsView;
import org.sagebionetworks.web.client.view.ACTAccessApprovalsViewImpl;
import org.sagebionetworks.web.client.view.ACTDataAccessSubmissionsView;
import org.sagebionetworks.web.client.view.ACTDataAccessSubmissionsViewImpl;
import org.sagebionetworks.web.client.view.ACTView;
import org.sagebionetworks.web.client.view.ACTViewImpl;
import org.sagebionetworks.web.client.view.AccessRequirementsSRCView;
import org.sagebionetworks.web.client.view.AccessRequirementsSRCViewImpl;
import org.sagebionetworks.web.client.view.AccountView;
import org.sagebionetworks.web.client.view.AccountViewImpl;
import org.sagebionetworks.web.client.view.CertificationQuizView;
import org.sagebionetworks.web.client.view.CertificationQuizViewImpl;
import org.sagebionetworks.web.client.view.ChallengeOverviewView;
import org.sagebionetworks.web.client.view.ChallengeOverviewViewImpl;
import org.sagebionetworks.web.client.view.ChangeUsernameView;
import org.sagebionetworks.web.client.view.ChangeUsernameViewImpl;
import org.sagebionetworks.web.client.view.ChatView;
import org.sagebionetworks.web.client.view.ChatViewImpl;
import org.sagebionetworks.web.client.view.ComingSoonView;
import org.sagebionetworks.web.client.view.ComingSoonViewImpl;
import org.sagebionetworks.web.client.view.DataAccessApprovalTokenView;
import org.sagebionetworks.web.client.view.DataAccessApprovalTokenViewImpl;
import org.sagebionetworks.web.client.view.DataAccessManagementView;
import org.sagebionetworks.web.client.view.DataAccessManagementViewImpl;
import org.sagebionetworks.web.client.view.DataCatalogPageView;
import org.sagebionetworks.web.client.view.DataCatalogPageViewImpl;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.DivViewImpl;
import org.sagebionetworks.web.client.view.DownloadCartPageView;
import org.sagebionetworks.web.client.view.DownloadCartPageViewImpl;
import org.sagebionetworks.web.client.view.EmailInvitationView;
import org.sagebionetworks.web.client.view.EmailInvitationViewImpl;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.view.EntityViewImpl;
import org.sagebionetworks.web.client.view.ErrorView;
import org.sagebionetworks.web.client.view.ErrorViewImpl;
import org.sagebionetworks.web.client.view.FollowingPageView;
import org.sagebionetworks.web.client.view.FollowingPageViewImpl;
import org.sagebionetworks.web.client.view.GridPageView;
import org.sagebionetworks.web.client.view.GridPageViewImpl;
import org.sagebionetworks.web.client.view.HelpView;
import org.sagebionetworks.web.client.view.HelpViewImpl;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.client.view.HomeViewImpl;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.view.LoginViewImpl;
import org.sagebionetworks.web.client.view.MapView;
import org.sagebionetworks.web.client.view.MapViewImpl;
import org.sagebionetworks.web.client.view.OAuthClientEditorView;
import org.sagebionetworks.web.client.view.OAuthClientEditorViewImpl;
import org.sagebionetworks.web.client.view.PasswordResetSignedTokenView;
import org.sagebionetworks.web.client.view.PasswordResetSignedTokenViewImpl;
import org.sagebionetworks.web.client.view.PeopleSearchView;
import org.sagebionetworks.web.client.view.PeopleSearchViewImpl;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.view.PlaceViewImpl;
import org.sagebionetworks.web.client.view.PlansView;
import org.sagebionetworks.web.client.view.PlansViewImpl;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.view.ProfileViewImpl;
import org.sagebionetworks.web.client.view.SearchV2View;
import org.sagebionetworks.web.client.view.SearchV2ViewImpl;
import org.sagebionetworks.web.client.view.SignedTokenView;
import org.sagebionetworks.web.client.view.SignedTokenViewImpl;
import org.sagebionetworks.web.client.view.SubscriptionView;
import org.sagebionetworks.web.client.view.SubscriptionViewImpl;
import org.sagebionetworks.web.client.view.SynapseForumView;
import org.sagebionetworks.web.client.view.SynapseForumViewImpl;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiView;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiViewImpl;
import org.sagebionetworks.web.client.view.SynapseWikiView;
import org.sagebionetworks.web.client.view.SynapseWikiViewImpl;
import org.sagebionetworks.web.client.view.TeamSearchView;
import org.sagebionetworks.web.client.view.TeamSearchViewImpl;
import org.sagebionetworks.web.client.view.TeamView;
import org.sagebionetworks.web.client.view.TeamViewImpl;
import org.sagebionetworks.web.client.view.TrashView;
import org.sagebionetworks.web.client.view.TrashViewImpl;
import org.sagebionetworks.web.client.view.TrustCenterView;
import org.sagebionetworks.web.client.view.TrustCenterViewImpl;
import org.sagebionetworks.web.client.view.UserAccessRequestHistoryView;
import org.sagebionetworks.web.client.view.UserAccessRequestHistoryViewImpl;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.view.users.PasswordResetViewImpl;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.ButtonImpl;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParserView;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParserViewImpl;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.CopyTextModalImpl;
import org.sagebionetworks.web.client.widget.CreateGridSessionDialog;
import org.sagebionetworks.web.client.widget.CreateGridSessionDialogImpl;
import org.sagebionetworks.web.client.widget.CreateTableFromCsvDialog;
import org.sagebionetworks.web.client.widget.CreateTableFromCsvDialogImpl;
import org.sagebionetworks.web.client.widget.CsvPreview;
import org.sagebionetworks.web.client.widget.CsvPreviewImpl;
import org.sagebionetworks.web.client.widget.DownloadSpeedTester;
import org.sagebionetworks.web.client.widget.DownloadSpeedTesterImpl;
import org.sagebionetworks.web.client.widget.EntityCitation;
import org.sagebionetworks.web.client.widget.EntityCitationImpl;
import org.sagebionetworks.web.client.widget.EntityTypeIcon;
import org.sagebionetworks.web.client.widget.EntityTypeIconImpl;
import org.sagebionetworks.web.client.widget.FileHandleWidgetView;
import org.sagebionetworks.web.client.widget.FileHandleWidgetViewImpl;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainerView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainerViewImpl;
import org.sagebionetworks.web.client.widget.ProjectInfo;
import org.sagebionetworks.web.client.widget.ProjectInfoImpl;
import org.sagebionetworks.web.client.widget.ProjectVisibilityChip;
import org.sagebionetworks.web.client.widget.ProjectVisibilityChipImpl;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.RadioWidgetViewImpl;
import org.sagebionetworks.web.client.widget.ShareThisPage;
import org.sagebionetworks.web.client.widget.ShareThisPageImpl;
import org.sagebionetworks.web.client.widget.UpdateTableWithCsvDialog;
import org.sagebionetworks.web.client.widget.UpdateTableWithCsvDialogImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.EntitySubjectsWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.EntitySubjectsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupView;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementStep1View;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementStep1ViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateBasicAccessRequirementStep2View;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateBasicAccessRequirementStep2ViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep2View;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep2ViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep3View;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep3ViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTrackerImpl;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressViewImpl;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.asynch.NumberFormatProvider;
import org.sagebionetworks.web.client.widget.asynch.NumberFormatProviderImpl;
import org.sagebionetworks.web.client.widget.asynch.PresignedAndFileHandleURLAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.PresignedAndFileHandleURLAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.TeamAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.TeamAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider;
import org.sagebionetworks.web.client.widget.asynch.TimerProviderImpl;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderFromAliasAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderFromAliasAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.VersionedEntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.VersionedEntityHeaderAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidgetView;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceEditorView;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceEditorViewImpl;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditorView;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditorViewImpl;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbView;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbViewImpl;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelpImpl;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelpView;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelpViewImpl;
import org.sagebionetworks.web.client.widget.clienthelp.FileViewClientsHelp;
import org.sagebionetworks.web.client.widget.clienthelp.FileViewClientsHelpImpl;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListItemWidgetView;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListItemWidgetViewImpl;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidgetView;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.discussion.ForumWidgetView;
import org.sagebionetworks.web.client.widget.discussion.ForumWidgetViewImpl;
import org.sagebionetworks.web.client.widget.discussion.NewReplyWidgetView;
import org.sagebionetworks.web.client.widget.discussion.NewReplyWidgetViewImpl;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidgetView;
import org.sagebionetworks.web.client.widget.discussion.ReplyWidgetViewImpl;
import org.sagebionetworks.web.client.widget.discussion.SingleDiscussionThreadWidgetView;
import org.sagebionetworks.web.client.widget.discussion.SingleDiscussionThreadWidgetViewImpl;
import org.sagebionetworks.web.client.widget.discussion.SubscribersWidgetView;
import org.sagebionetworks.web.client.widget.discussion.SubscribersWidgetViewImpl;
import org.sagebionetworks.web.client.widget.discussion.modal.DiscussionThreadModalView;
import org.sagebionetworks.web.client.widget.discussion.modal.DiscussionThreadModalViewImpl;
import org.sagebionetworks.web.client.widget.discussion.modal.ReplyModalView;
import org.sagebionetworks.web.client.widget.discussion.modal.ReplyModalViewImpl;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidgetView;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.docker.DockerCommitRowWidgetView;
import org.sagebionetworks.web.client.widget.docker.DockerCommitRowWidgetViewImpl;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidgetView;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidgetView;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidgetViewImpl;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModalView;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModalViewImpl;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2View;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2ViewImpl;
import org.sagebionetworks.web.client.widget.entity.BigPromptModalView;
import org.sagebionetworks.web.client.widget.entity.BigPromptModalViewImpl;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadgeView;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadgeViewImpl;
import org.sagebionetworks.web.client.widget.entity.ContainerItemCountWidgetView;
import org.sagebionetworks.web.client.widget.entity.ContainerItemCountWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalView;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalViewImpl;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditFileMetadataModalWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalView;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalViewImpl;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidget;
import org.sagebionetworks.web.client.widget.entity.EditProjectMetadataModalWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialogView;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialogViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadgeView;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadgeViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityModalWidgetView;
import org.sagebionetworks.web.client.widget.entity.EntityModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxView;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityViewScopeEditorModalWidgetView;
import org.sagebionetworks.web.client.widget.entity.EntityViewScopeEditorModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidgetView;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.ProjectBadgeView;
import org.sagebionetworks.web.client.widget.entity.ProjectBadgeViewImpl;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalConfigurationImpl;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalViewImpl;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialogView;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialogViewImpl;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidgetView;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.SqlDefinedEditorModalWidgetView;
import org.sagebionetworks.web.client.widget.entity.SqlDefinedEditorModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.TutorialWizardView;
import org.sagebionetworks.web.client.widget.entity.TutorialWizardViewImpl;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryRowView;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryRowViewImpl;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryWidgetView;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiAttachmentsView;
import org.sagebionetworks.web.client.widget.entity.WikiAttachmentsViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditorView;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditorViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiPageDeleteConfirmationDialogView;
import org.sagebionetworks.web.client.widget.entity.WikiPageDeleteConfirmationDialogViewImpl;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModalView;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModalViewImpl;
import org.sagebionetworks.web.client.widget.entity.act.RejectDataAccessRequestModalView;
import org.sagebionetworks.web.client.widget.entity.act.RejectDataAccessRequestModalViewImpl;
import org.sagebionetworks.web.client.widget.entity.act.RejectReasonView;
import org.sagebionetworks.web.client.widget.entity.act.RejectReasonViewImpl;
import org.sagebionetworks.web.client.widget.entity.act.RevokeUserAccessModalView;
import org.sagebionetworks.web.client.widget.entity.act.RevokeUserAccessModalViewImpl;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeListView;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeListViewImpl;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationCellFactory;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationCellFactoryImpl;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditorView;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationEditorViewImpl;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformerImpl;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidgetView;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialogView;
import org.sagebionetworks.web.client.widget.entity.annotation.EditAnnotationsDialogViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidgetView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.CertifiedUserController;
import org.sagebionetworks.web.client.widget.entity.controller.CertifiedUserControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerView;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEditorWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEditorWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceURLDialogWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceURLDialogWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlertView;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlertViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryViewImpl;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidgetView;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.AwsLoginView;
import org.sagebionetworks.web.client.widget.entity.download.AwsLoginViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidgetView;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoWidgetView;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidgetV2;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidgetView;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.download.UploaderView;
import org.sagebionetworks.web.client.widget.entity.download.UploaderViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManagerView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnManagerViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.CytoscapeConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.CytoscapeConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.DetailsSummaryConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.DetailsSummaryConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.EvaluationSubmissionConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.EvaluationSubmissionConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ImageParamsPanelView;
import org.sagebionetworks.web.client.widget.entity.editor.ImageParamsPanelViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.PlotlyConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.PlotlyConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.PreviewConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.PreviewConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ReferenceConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.SynapseFormConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.SynapseFormConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.TableQueryResultWikiView;
import org.sagebionetworks.web.client.widget.entity.editor.TableQueryResultWikiViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.TeamSelectEditorView;
import org.sagebionetworks.web.client.widget.entity.editor.TeamSelectEditorViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.UserSelectorView;
import org.sagebionetworks.web.client.widget.entity.editor.UserSelectorViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2Impl;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadMenuItemView;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadMenuItemViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.ProjectTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.ProjectTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.S3DirectLoginDialog;
import org.sagebionetworks.web.client.widget.entity.file.S3DirectLoginDialogImpl;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummaryView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummaryViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuView;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuViewImpl;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.BookmarkWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.BookmarkWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ButtonLinkWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeTeamsView;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeTeamsViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.CytoscapeView;
import org.sagebionetworks.web.client.widget.entity.renderer.CytoscapeViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.EmptyWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.EmptyWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewView;
import org.sagebionetworks.web.client.widget.entity.renderer.HtmlPreviewViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.IFrameView;
import org.sagebionetworks.web.client.widget.entity.renderer.IFrameViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.IntendedDataUseReportWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.IntendedDataUseReportWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.SynapseTableFormWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.SynapseTableFormWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TIFFPreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.TIFFPreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountView;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListRowWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListRowWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListView;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.VideoWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTreeView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTreeViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTreeView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTreeViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesViewImpl;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidgetView;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTabViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTabViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTabViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTabViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.MetadataTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.MetadataTabViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.TabView;
import org.sagebionetworks.web.client.widget.entity.tabs.TabViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTabViewImpl;
import org.sagebionetworks.web.client.widget.entity.tabs.TabsView;
import org.sagebionetworks.web.client.widget.entity.tabs.TabsViewImpl;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsListView;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsListViewImpl;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidgetView;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidgetViewImpl;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationFinderView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationFinderViewImpl;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationListView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationListViewImpl;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterViewImpl;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditorModalWidgetView;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditorModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.FooterViewImpl;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMapView;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMapViewImpl;
import org.sagebionetworks.web.client.widget.header.HeaderView;
import org.sagebionetworks.web.client.widget.header.HeaderViewImpl;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadCallbackQueue;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadCallbackQueueImpl;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapperView;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapperViewImpl;
import org.sagebionetworks.web.client.widget.login.LoginModalView;
import org.sagebionetworks.web.client.widget.login.LoginModalViewImpl;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;
import org.sagebionetworks.web.client.widget.login.LoginWidgetViewImpl;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.client.widget.modal.DialogView;
import org.sagebionetworks.web.client.widget.pageprogress.PageProgressWidgetView;
import org.sagebionetworks.web.client.widget.pageprogress.PageProgressWidgetViewImpl;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationView;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationViewImpl;
import org.sagebionetworks.web.client.widget.profile.EmailAddressesWidgetView;
import org.sagebionetworks.web.client.widget.profile.EmailAddressesWidgetViewImpl;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedView;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedViewImpl;
import org.sagebionetworks.web.client.widget.profile.ProfileImageView;
import org.sagebionetworks.web.client.widget.profile.ProfileImageViewImpl;
import org.sagebionetworks.web.client.widget.profile.ProfileImageWidget;
import org.sagebionetworks.web.client.widget.profile.ProfileImageWidgetImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileWidgetImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileWidgetView;
import org.sagebionetworks.web.client.widget.profile.UserProfileWidgetViewImpl;
import org.sagebionetworks.web.client.widget.provenance.v2.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.v2.ProvenanceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertView;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertViewImpl;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.sharing.EntityAccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.EntityAccessControlListModalWidgetImpl;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidgetImpl;
import org.sagebionetworks.web.client.widget.sharing.OpenDataView;
import org.sagebionetworks.web.client.widget.sharing.OpenDataViewImpl;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadgeView;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadgeViewImpl;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGridView;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGridViewImpl;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidgetView;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidgetViewImpl;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidgetView;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidgetViewImpl;
import org.sagebionetworks.web.client.widget.subscription.TopicWidgetView;
import org.sagebionetworks.web.client.widget.subscription.TopicWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.TableListWidgetView;
import org.sagebionetworks.web.client.widget.table.TableListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.api.APITableWidgetView;
import org.sagebionetworks.web.client.widget.table.api.APITableWidgetViewImpl;
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
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityViewScopeWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityViewScopeWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.fileview.SubmissionViewScopeWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.SubmissionViewScopeWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidgetImpl;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.TotalVisibleResultsWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.TotalVisibleResultsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.RowFormView;
import org.sagebionetworks.web.client.widget.table.v2.results.RowFormViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.RowView;
import org.sagebionetworks.web.client.widget.table.v2.results.RowViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeaderImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.StaticTableHeader;
import org.sagebionetworks.web.client.widget.table.v2.results.StaticTableHeaderImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageView;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateListRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateListRendererCellViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EditJSONListModalView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EditJSONListModalViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdListRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdListRendererCellViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LargeStringCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LargeStringCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LinkCellRendererView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LinkCellRendererViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.NumberCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.NumberCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringListRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringListRendererCellViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCellViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdListRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdListRendererCellViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewerImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewImpl;
import org.sagebionetworks.web.client.widget.team.BigTeamBadgeView;
import org.sagebionetworks.web.client.widget.team.BigTeamBadgeViewImpl;
import org.sagebionetworks.web.client.widget.team.EmailInvitationBadgeView;
import org.sagebionetworks.web.client.widget.team.EmailInvitationBadgeViewImpl;
import org.sagebionetworks.web.client.widget.team.InviteWidgetView;
import org.sagebionetworks.web.client.widget.team.InviteWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.JoinTeamConfigEditorView;
import org.sagebionetworks.web.client.widget.team.JoinTeamConfigEditorViewImpl;
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
import org.sagebionetworks.web.client.widget.team.SelectTeamModalView;
import org.sagebionetworks.web.client.widget.team.SelectTeamModalViewImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadgeView;
import org.sagebionetworks.web.client.widget.team.TeamBadgeViewImpl;
import org.sagebionetworks.web.client.widget.team.TeamListWidgetView;
import org.sagebionetworks.web.client.widget.team.TeamListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.WizardProgressWidgetView;
import org.sagebionetworks.web.client.widget.team.WizardProgressWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidgetView;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidgetView;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidgetView;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.team.controller.TeamProjectsModalWidgetView;
import org.sagebionetworks.web.client.widget.team.controller.TeamProjectsModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.upload.FileHandleListView;
import org.sagebionetworks.web.client.widget.upload.FileHandleListViewImpl;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadViewImpl;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.FileInputView;
import org.sagebionetworks.web.client.widget.upload.FileInputViewImpl;
import org.sagebionetworks.web.client.widget.upload.ImageUploadView;
import org.sagebionetworks.web.client.widget.upload.ImageUploadViewImpl;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.MultipartUploaderImplV2;
import org.sagebionetworks.web.client.widget.upload.SRCUploadFileWrapper;
import org.sagebionetworks.web.client.widget.upload.SRCUploadFileWrapperImpl;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.client.widget.user.UserBadgeViewImpl;

/**
 * Dagger module replacing the former GIN PortalGinModule.
 * Generated from the original bind(I).to(Impl) declarations.
 */
@Module
public abstract class PortalGinModule {

  @Binds
  @Singleton
  abstract EventBus bindEventBus(SimpleEventBus impl);

  @Binds
  abstract JsoProvider bindJsoProvider(JsoProviderImpl impl);

  @Binds
  @Singleton
  abstract AuthenticationController bindAuthenticationController(
    AuthenticationControllerImpl impl
  );

  @Binds
  @Singleton
  abstract GlobalApplicationState bindGlobalApplicationState(
    GlobalApplicationStateImpl impl
  );

  @Binds
  abstract GlobalApplicationStateView bindGlobalApplicationStateView(
    GlobalApplicationStateViewImpl impl
  );

  @Binds
  @Singleton
  abstract LazyLoadCallbackQueue bindLazyLoadCallbackQueue(
    LazyLoadCallbackQueueImpl impl
  );

  @Binds
  @Singleton
  abstract ResourceLoader bindResourceLoader(ResourceLoaderImpl impl);

  @Binds
  @Singleton
  abstract HeaderView bindHeaderView(HeaderViewImpl impl);

  @Binds
  @Singleton
  abstract FooterView bindFooterView(FooterViewImpl impl);

  @Binds
  abstract JSONObjectAdapter bindJSONObjectAdapter(JSONObjectGwt impl);

  @Binds
  abstract JSONArrayAdapter bindJSONArrayAdapter(JSONArrayGwt impl);

  @Binds
  abstract AnnotationsRendererWidgetView bindAnnotationsRendererWidgetView(
    AnnotationsRendererWidgetViewImpl impl
  );

  @Binds
  abstract VersionHistoryWidgetView bindVersionHistoryWidgetView(
    VersionHistoryWidgetViewImpl impl
  );

  @Binds
  @Singleton
  abstract GWTWrapper bindGWTWrapper(GWTWrapperImpl impl);

  @Binds
  abstract GWTTimer bindGWTTimer(GWTTimerImpl impl);

  @Binds
  abstract RequestBuilderWrapper bindRequestBuilderWrapper(
    RequestBuilderWrapperImpl impl
  );

  @Binds
  abstract AdapterFactory bindAdapterFactory(GwtAdapterFactory impl);

  @Binds
  @Singleton
  abstract ClientCache bindClientCache(ClientCacheImpl impl);

  @Binds
  @Singleton
  abstract StorageWrapper bindStorageWrapper(StorageImpl impl);

  @Binds
  @Singleton
  abstract SynapseJSNIUtils bindSynapseJSNIUtils(SynapseJSNIUtilsImpl impl);

  @Binds
  @Singleton
  abstract SynapseJsInteropUtils bindSynapseJsInteropUtils(
    SynapseJsInteropUtilsImpl impl
  );

  @Binds
  @Singleton
  abstract HomeView bindHomeView(HomeViewImpl impl);

  @Binds
  @Singleton
  abstract EntityView bindEntityView(EntityViewImpl impl);

  @Binds
  @Singleton
  abstract LoginView bindLoginView(LoginViewImpl impl);

  @Binds
  @Singleton
  abstract PasswordResetView bindPasswordResetView(PasswordResetViewImpl impl);

  @Binds
  @Singleton
  abstract ProfileView bindProfileView(ProfileViewImpl impl);

  @Binds
  @Singleton
  abstract ComingSoonView bindComingSoonView(ComingSoonViewImpl impl);

  @Binds
  abstract ChallengeOverviewView bindChallengeOverviewView(
    ChallengeOverviewViewImpl impl
  );

  @Binds
  @Singleton
  abstract HelpView bindHelpView(HelpViewImpl impl);

  @Binds
  @Singleton
  abstract TrustCenterView bindTrustCenterView(TrustCenterViewImpl impl);

  @Binds
  abstract SynapseWikiView bindSynapseWikiView(SynapseWikiViewImpl impl);

  @Binds
  abstract CertificateWidgetView bindCertificateWidgetView(
    CertificateWidgetViewImpl impl
  );

  @Binds
  @Singleton
  abstract AccountView bindAccountView(AccountViewImpl impl);

  @Binds
  @Singleton
  abstract ChangeUsernameView bindChangeUsernameView(
    ChangeUsernameViewImpl impl
  );

  @Binds
  @Singleton
  abstract SignedTokenView bindSignedTokenView(SignedTokenViewImpl impl);

  @Binds
  @Singleton
  abstract DataAccessApprovalTokenView bindDataAccessApprovalTokenView(
    DataAccessApprovalTokenViewImpl impl
  );

  @Binds
  @Singleton
  abstract TrashView bindTrashView(TrashViewImpl impl);

  @Binds
  abstract TimerProvider bindTimerProvider(TimerProviderImpl impl);

  @Binds
  abstract NumberFormatProvider bindNumberFormatProvider(
    NumberFormatProviderImpl impl
  );

  @Binds
  abstract AsynchronousProgressView bindAsynchronousProgressView(
    AsynchronousProgressViewImpl impl
  );

  @Binds
  abstract AsynchronousJobTracker bindAsynchronousJobTracker(
    AsynchronousJobTrackerImpl impl
  );

  @Binds
  @Singleton
  abstract EmailInvitationView bindEmailInvitationView(
    EmailInvitationViewImpl impl
  );

  @Binds
  @Singleton
  abstract DataAccessManagementView bindDataAccessManagementView(
    DataAccessManagementViewImpl impl
  );

  @Binds
  @Singleton
  abstract UserAccessRequestHistoryView bindUserAccessRequestHistoryView(
    UserAccessRequestHistoryViewImpl impl
  );

  @Binds
  @Singleton
  abstract OAuthClientEditorView bindOAuthClientEditorView(
    OAuthClientEditorViewImpl impl
  );

  @Binds
  @Singleton
  abstract CertificationQuizView bindCertificationQuizView(
    CertificationQuizViewImpl impl
  );

  @Binds
  abstract DoiWidgetV2View bindDoiWidgetV2View(DoiWidgetV2ViewImpl impl);

  @Binds
  @Singleton
  abstract LoginWidgetView bindLoginWidgetView(LoginWidgetViewImpl impl);

  @Binds
  abstract BreadcrumbView bindBreadcrumbView(BreadcrumbViewImpl impl);

  @Binds
  abstract CookieProvider bindCookieProvider(GWTCookieImpl impl);

  @Binds
  abstract AccessControlListEditorView bindAccessControlListEditorView(
    AccessControlListEditorViewImpl impl
  );

  @Binds
  abstract EntityAccessControlListModalWidget bindEntityAccessControlListModalWidget(
    EntityAccessControlListModalWidgetImpl impl
  );

  @Binds
  abstract AccessControlListModalWidgetView bindAccessControlListModalWidgetView(
    AccessControlListModalWidgetViewImpl impl
  );

  @Binds
  abstract EvaluationAccessControlListModalWidget bindEvaluationAccessControlListModalWidget(
    EvaluationAccessControlListModalWidgetImpl impl
  );

  @Binds
  abstract SharingPermissionsGridView bindSharingPermissionsGridView(
    SharingPermissionsGridViewImpl impl
  );

  @Binds
  abstract BasicPaginationView bindBasicPaginationView(
    BasicPaginationViewImpl impl
  );

  @Binds
  abstract EntityPageTopView bindEntityPageTopView(EntityPageTopViewImpl impl);

  @Binds
  abstract PreviewWidgetView bindPreviewWidgetView(PreviewWidgetViewImpl impl);

  @Binds
  abstract EntityActionMenu bindEntityActionMenu(EntityActionMenuImpl impl);

  @Binds
  abstract EntityActionMenuView bindEntityActionMenuView(
    EntityActionMenuViewImpl impl
  );

  @Binds
  abstract EntityActionController bindEntityActionController(
    EntityActionControllerImpl impl
  );

  @Binds
  abstract EntityActionControllerView bindEntityActionControllerView(
    EntityActionControllerViewImpl impl
  );

  @Binds
  abstract PreflightController bindPreflightController(
    PreflightControllerImpl impl
  );

  @Binds
  abstract CertifiedUserController bindCertifiedUserController(
    CertifiedUserControllerImpl impl
  );

  @Binds
  abstract BigPromptModalView bindBigPromptModalView(
    BigPromptModalViewImpl impl
  );

  @Binds
  abstract PromptForValuesModalView bindPromptForValuesModalView(
    PromptForValuesModalViewImpl impl
  );

  @Binds
  abstract PromptForValuesModalView.Configuration.Builder bindPromptForValuesModalViewConfigurationBuilder(
    PromptForValuesModalConfigurationImpl.Builder impl
  );

  @Binds
  abstract RenameEntityModalWidget bindRenameEntityModalWidget(
    RenameEntityModalWidgetImpl impl
  );

  @Binds
  abstract RejectReasonView bindRejectReasonView(RejectReasonViewImpl impl);

  @Binds
  abstract ProjectTitleBarView bindProjectTitleBarView(
    ProjectTitleBarViewImpl impl
  );

  @Binds
  abstract BasicTitleBarView bindBasicTitleBarView(BasicTitleBarViewImpl impl);

  @Binds
  abstract RejectDataAccessRequestModalView bindRejectDataAccessRequestModalView(
    RejectDataAccessRequestModalViewImpl impl
  );

  @Binds
  abstract SynapseSuggestBoxView bindSynapseSuggestBoxView(
    SynapseSuggestBoxViewImpl impl
  );

  @Binds
  abstract MultipartUploader bindMultipartUploader(
    MultipartUploaderImplV2 impl
  );

  @Binds
  abstract FileInputView bindFileInputView(FileInputViewImpl impl);

  @Binds
  abstract FileHandleUploadView bindFileHandleUploadView(
    FileHandleUploadViewImpl impl
  );

  @Binds
  abstract FileHandleUploadWidget bindFileHandleUploadWidget(
    FileHandleUploadWidgetImpl impl
  );

  @Binds
  @Singleton
  abstract UploaderView bindUploaderView(UploaderViewImpl impl);

  @Binds
  abstract QuizInfoWidgetView bindQuizInfoWidgetView(QuizInfoViewImpl impl);

  @Binds
  abstract WikiAttachmentsView bindWikiAttachmentsView(
    WikiAttachmentsViewImpl impl
  );

  @Binds
  abstract WikiHistoryWidgetView bindWikiHistoryWidgetView(
    WikiHistoryWidgetViewImpl impl
  );

  @Binds
  abstract EvaluationListView bindEvaluationListView(
    EvaluationListViewImpl impl
  );

  @Binds
  abstract AdministerEvaluationsListView bindAdministerEvaluationsListView(
    AdministerEvaluationsListViewImpl impl
  );

  @Binds
  abstract EntitySearchBoxView bindEntitySearchBoxView(
    EntitySearchBoxViewImpl impl
  );

  @Binds
  abstract EntityMetadataView bindEntityMetadataView(
    EntityMetadataViewImpl impl
  );

  @Binds
  abstract UserProfileWidget bindUserProfileWidget(UserProfileWidgetImpl impl);

  @Binds
  abstract UserProfileWidgetView bindUserProfileWidgetView(
    UserProfileWidgetViewImpl impl
  );

  @Binds
  abstract ProfileImageView bindProfileImageView(ProfileImageViewImpl impl);

  @Binds
  abstract ProfileImageWidget bindProfileImageWidget(
    ProfileImageWidgetImpl impl
  );

  @Binds
  abstract APITableColumnManagerView bindAPITableColumnManagerView(
    APITableColumnManagerViewImpl impl
  );

  @Binds
  abstract APITableColumnConfigView bindAPITableColumnConfigView(
    APITableColumnConfigViewImpl impl
  );

  @Binds
  abstract WikiSubpagesView bindWikiSubpagesView(WikiSubpagesViewImpl impl);

  @Binds
  abstract WikiSubpagesOrderEditorView bindWikiSubpagesOrderEditorView(
    WikiSubpagesOrderEditorViewImpl impl
  );

  @Binds
  abstract WikiSubpageOrderEditorTreeView bindWikiSubpageOrderEditorTreeView(
    WikiSubpageOrderEditorTreeViewImpl impl
  );

  @Binds
  abstract WikiSubpageNavigationTreeView bindWikiSubpageNavigationTreeView(
    WikiSubpageNavigationTreeViewImpl impl
  );

  @Binds
  @Singleton
  abstract WidgetRegistrar bindWidgetRegistrar(WidgetRegistrarImpl impl);

  @Binds
  abstract BaseEditWidgetDescriptorView bindBaseEditWidgetDescriptorView(
    BaseEditWidgetDescriptorViewImpl impl
  );

  @Binds
  abstract ReferenceConfigView bindReferenceConfigView(
    ReferenceConfigViewImpl impl
  );

  @Binds
  @Singleton
  abstract ImageConfigView bindImageConfigView(ImageConfigViewImpl impl);

  @Binds
  @Singleton
  abstract AttachmentConfigView bindAttachmentConfigView(
    AttachmentConfigViewImpl impl
  );

  @Binds
  abstract ProvenanceConfigView bindProvenanceConfigView(
    ProvenanceConfigViewImpl impl
  );

  @Binds
  abstract LinkConfigView bindLinkConfigView(LinkConfigViewImpl impl);

  @Binds
  abstract DetailsSummaryConfigView bindDetailsSummaryConfigView(
    DetailsSummaryConfigViewImpl impl
  );

  @Binds
  abstract TabbedTableConfigView bindTabbedTableConfigView(
    TabbedTableConfigViewImpl impl
  );

  @Binds
  abstract APITableConfigView bindAPITableConfigView(
    APITableConfigViewImpl impl
  );

  @Binds
  abstract QueryTableConfigView bindQueryTableConfigView(
    QueryTableConfigViewImpl impl
  );

  @Binds
  abstract EntityListConfigView bindEntityListConfigView(
    EntityListConfigViewImpl impl
  );

  @Binds
  abstract ShinySiteConfigView bindShinySiteConfigView(
    ShinySiteConfigViewImpl impl
  );

  @Binds
  abstract ButtonLinkConfigView bindButtonLinkConfigView(
    ButtonLinkConfigViewImpl impl
  );

  @Binds
  abstract EvaluationSubmissionConfigView bindEvaluationSubmissionConfigView(
    EvaluationSubmissionConfigViewImpl impl
  );

  @Binds
  abstract VideoConfigView bindVideoConfigView(VideoConfigViewImpl impl);

  @Binds
  abstract TableQueryResultWikiView bindTableQueryResultWikiView(
    TableQueryResultWikiViewImpl impl
  );

  @Binds
  abstract TeamSelectEditorView bindTeamSelectEditorView(
    TeamSelectEditorViewImpl impl
  );

  @Binds
  abstract BookmarkWidgetView bindBookmarkWidgetView(
    BookmarkWidgetViewImpl impl
  );

  @Binds
  abstract ReferenceWidgetView bindReferenceWidgetView(
    ReferenceWidgetViewImpl impl
  );

  @Binds
  abstract EntityListWidgetView bindEntityListWidgetView(
    EntityListWidgetViewImpl impl
  );

  @Binds
  abstract IFrameView bindIFrameView(IFrameViewImpl impl);

  @Binds
  abstract ImageWidgetView bindImageWidgetView(ImageWidgetViewImpl impl);

  @Binds
  abstract AttachmentPreviewWidgetView bindAttachmentPreviewWidgetView(
    AttachmentPreviewWidgetViewImpl impl
  );

  @Binds
  abstract APITableWidgetView bindAPITableWidgetView(
    APITableWidgetViewImpl impl
  );

  @Binds
  abstract TableOfContentsWidgetView bindTableOfContentsWidgetView(
    TableOfContentsWidgetViewImpl impl
  );

  @Binds
  abstract WikiFilesPreviewWidgetView bindWikiFilesPreviewWidgetView(
    WikiFilesPreviewWidgetViewImpl impl
  );

  @Binds
  abstract ButtonLinkWidgetView bindButtonLinkWidgetView(
    ButtonLinkWidgetViewImpl impl
  );

  @Binds
  abstract EmptyWidgetView bindEmptyWidgetView(EmptyWidgetViewImpl impl);

  @Binds
  abstract VideoWidgetView bindVideoWidgetView(VideoWidgetViewImpl impl);

  @Binds
  abstract TeamMemberCountView bindTeamMemberCountView(
    TeamMemberCountViewImpl impl
  );

  @Binds
  abstract TIFFPreviewWidgetView bindTIFFPreviewWidgetView(
    TIFFPreviewWidgetViewImpl impl
  );

  @Binds
  abstract org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView bindProvenanceWidgetViewV1(
    org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetViewImpl impl
  );

  @Binds
  abstract ProvenanceWidgetView bindProvenanceWidgetViewV2(
    ProvenanceWidgetViewImpl impl
  );

  @Binds
  abstract MarkdownWidgetView bindMarkdownWidgetView(
    MarkdownWidgetViewImpl impl
  );

  @Binds
  abstract MarkdownEditorWidgetView bindMarkdownEditorWidgetView(
    MarkdownEditorWidgetViewImpl impl
  );

  @Binds
  abstract FilesBrowserView bindFilesBrowserView(FilesBrowserViewImpl impl);

  @Binds
  abstract EvaluationSubmitterView bindEvaluationSubmitterView(
    EvaluationSubmitterViewImpl impl
  );

  @Binds
  abstract FavoriteWidgetView bindFavoriteWidgetView(
    FavoriteWidgetViewImpl impl
  );

  @Binds
  abstract WikiPageWidgetView bindWikiPageWidgetView(
    WikiPageWidgetViewImpl impl
  );

  @Binds
  abstract UserBadgeView bindUserBadgeView(UserBadgeViewImpl impl);

  @Binds
  abstract EmailInvitationBadgeView bindEmailInvitationBadgeView(
    EmailInvitationBadgeViewImpl impl
  );

  @Binds
  abstract EntityBadgeView bindEntityBadgeView(EntityBadgeViewImpl impl);

  @Binds
  abstract TutorialWizardView bindTutorialWizardView(
    TutorialWizardViewImpl impl
  );

  @Binds
  abstract PublicPrivateBadgeView bindPublicPrivateBadgeView(
    PublicPrivateBadgeViewImpl impl
  );

  @Binds
  abstract ModalWizardView bindModalWizardView(ModalWizardViewImpl impl);

  @Binds
  abstract ModalWizardWidget bindModalWizardWidget(ModalWizardWidgetImpl impl);

  @Binds
  abstract TableListWidgetView bindTableListWidgetView(
    TableListWidgetViewImpl impl
  );

  @Binds
  abstract ColumnModelsView bindColumnModelsView(ColumnModelsViewImpl impl);

  @Binds
  abstract ColumnModelTableRowEditorView bindColumnModelTableRowEditorView(
    ColumnModelTableRowEditorViewImpl impl
  );

  @Binds
  abstract ColumnModelTableRowEditorWidget bindColumnModelTableRowEditorWidget(
    ColumnModelTableRowEditorWidgetImpl impl
  );

  @Binds
  abstract ColumnModelTableRowViewer bindColumnModelTableRowViewer(
    ColumnModelTableRowViewerImpl impl
  );

  @Binds
  abstract ColumnModelsEditorWidgetView bindColumnModelsEditorWidgetView(
    ColumnModelsEditorWidgetViewImpl impl
  );

  @Binds
  abstract TableEntityWidgetView bindTableEntityWidgetView(
    TableEntityWidgetViewImpl impl
  );

  @Binds
  abstract RowView bindRowView(RowViewImpl impl);

  @Binds
  abstract TablePageView bindTablePageView(TablePageViewImpl impl);

  @Binds
  abstract QueryResultEditorView bindQueryResultEditorView(
    QueryResultEditorViewImpl impl
  );

  @Binds
  abstract JobTrackingWidget bindJobTrackingWidget(
    AsynchronousProgressWidget impl
  );

  @Binds
  abstract SortableTableHeader bindSortableTableHeader(
    SortableTableHeaderImpl impl
  );

  @Binds
  abstract StaticTableHeader bindStaticTableHeader(StaticTableHeaderImpl impl);

  @Binds
  abstract TotalVisibleResultsWidgetView bindTotalVisibleResultsWidgetView(
    TotalVisibleResultsWidgetViewImpl impl
  );

  @Binds
  abstract CreateDownloadPage bindCreateDownloadPage(
    CreateDownloadPageImpl impl
  );

  @Binds
  abstract CreateDownloadPageView bindCreateDownloadPageView(
    CreateDownloadPageViewImpl impl
  );

  @Binds
  abstract DownloadFilePage bindDownloadFilePage(DownloadFilePageImpl impl);

  @Binds
  abstract DownloadFilePageView bindDownloadFilePageView(
    DownloadFilePageViewImpl impl
  );

  @Binds
  abstract DownloadTableQueryModalWidget bindDownloadTableQueryModalWidget(
    DownloadTableQueryModalWidgetImpl impl
  );

  @Binds
  abstract LinkCellRendererView bindLinkCellRendererView(
    LinkCellRendererViewImpl impl
  );

  @Binds
  abstract StringRendererCellView bindStringRendererCellView(
    StringRendererCellViewImpl impl
  );

  @Binds
  abstract StringListRendererCellView bindStringListRendererCellView(
    StringListRendererCellViewImpl impl
  );

  @Binds
  abstract DateListRendererCellView bindDateListRendererCellView(
    DateListRendererCellViewImpl impl
  );

  @Binds
  abstract UserIdListRendererCellView bindUserIdListRendererCellView(
    UserIdListRendererCellViewImpl impl
  );

  @Binds
  abstract EntityIdListRendererCellView bindEntityIdListRendererCellView(
    EntityIdListRendererCellViewImpl impl
  );

  @Binds
  abstract CellEditorView bindCellEditorView(CellEditorViewImpl impl);

  @Binds
  abstract NumberCellEditorView bindNumberCellEditorView(
    NumberCellEditorViewImpl impl
  );

  @Binds
  abstract ListCellEditorView bindListCellEditorView(
    ListCellEditorViewImpl impl
  );

  @Binds
  abstract DateCellEditorView bindDateCellEditorView(
    DateCellEditorViewImpl impl
  );

  @Binds
  abstract UserIdCellEditorView bindUserIdCellEditorView(
    UserIdCellEditorViewImpl impl
  );

  @Binds
  abstract FileCellEditorView bindFileCellEditorView(
    FileCellEditorViewImpl impl
  );

  @Binds
  abstract FileCellRendererView bindFileCellRendererView(
    FileCellRendererViewImpl impl
  );

  @Binds
  abstract EntityIdCellRendererView bindEntityIdCellRendererView(
    EntityIdCellRendererViewImpl impl
  );

  @Binds
  abstract LargeStringCellEditorView bindLargeStringCellEditorView(
    LargeStringCellEditorViewImpl impl
  );

  @Binds
  abstract JSONListCellEditorView bindJSONListCellEditorView(
    JSONListCellEditorViewImpl impl
  );

  @Binds
  @Singleton
  abstract EditJSONListModalView bindEditJSONListModalView(
    EditJSONListModalViewImpl impl
  );

  @Binds
  @Singleton
  abstract TeamView bindTeamView(TeamViewImpl impl);

  @Binds
  @Singleton
  abstract TeamSearchView bindTeamSearchView(TeamSearchViewImpl impl);

  @Binds
  abstract MapView bindMapView(MapViewImpl impl);

  @Binds
  @Singleton
  abstract PeopleSearchView bindPeopleSearchView(PeopleSearchViewImpl impl);

  @Binds
  abstract TeamEditModalWidgetView bindTeamEditModalWidgetView(
    TeamEditModalWidgetViewImpl impl
  );

  @Binds
  abstract TeamLeaveModalWidgetView bindTeamLeaveModalWidgetView(
    TeamLeaveModalWidgetViewImpl impl
  );

  @Binds
  abstract TeamDeleteModalWidgetView bindTeamDeleteModalWidgetView(
    TeamDeleteModalWidgetViewImpl impl
  );

  @Binds
  abstract OpenTeamInvitationsWidgetView bindOpenTeamInvitationsWidgetView(
    OpenTeamInvitationsWidgetViewImpl impl
  );

  @Binds
  abstract OpenMembershipRequestsWidgetView bindOpenMembershipRequestsWidgetView(
    OpenMembershipRequestsWidgetViewImpl impl
  );

  @Binds
  abstract OpenUserInvitationsWidgetView bindOpenUserInvitationsWidgetView(
    OpenUserInvitationsWidgetViewImpl impl
  );

  @Binds
  abstract TeamListWidgetView bindTeamListWidgetView(
    TeamListWidgetViewImpl impl
  );

  @Binds
  abstract MemberListWidgetView bindMemberListWidgetView(
    MemberListWidgetViewImpl impl
  );

  @Binds
  abstract InviteWidgetView bindInviteWidgetView(InviteWidgetViewImpl impl);

  @Binds
  abstract JoinTeamWidgetView bindJoinTeamWidgetView(
    JoinTeamWidgetViewImpl impl
  );

  @Binds
  abstract JoinTeamConfigEditorView bindJoinTeamConfigEditorView(
    JoinTeamConfigEditorViewImpl impl
  );

  @Binds
  abstract SubmitToEvaluationWidgetView bindSubmitToEvaluationWidgetView(
    SubmitToEvaluationWidgetViewImpl impl
  );

  @Binds
  abstract TeamBadgeView bindTeamBadgeView(TeamBadgeViewImpl impl);

  @Binds
  abstract BigTeamBadgeView bindBigTeamBadgeView(BigTeamBadgeViewImpl impl);

  @Binds
  abstract UserTeamConfigView bindUserTeamConfigView(
    UserTeamConfigViewImpl impl
  );

  @Binds
  abstract SharingAndDataUseConditionWidgetView bindSharingAndDataUseConditionWidgetView(
    SharingAndDataUseConditionWidgetViewImpl impl
  );

  @Binds
  abstract WizardProgressWidgetView bindWizardProgressWidgetView(
    WizardProgressWidgetViewImpl impl
  );

  @Binds
  abstract UploadDialogWidget bindUploadDialogWidget(UploadDialogWidgetV2 impl);

  @Binds
  abstract UploadDialogWidgetView bindUploadDialogWidgetView(
    UploadDialogWidgetViewImpl impl
  );

  @Binds
  abstract AddFolderDialogWidgetView bindAddFolderDialogWidgetView(
    AddFolderDialogWidgetViewImpl impl
  );

  @Binds
  abstract LoginModalView bindLoginModalView(LoginModalViewImpl impl);

  @Binds
  abstract ImageParamsPanelView bindImageParamsPanelView(
    ImageParamsPanelViewImpl impl
  );

  @Binds
  abstract RegisterTeamDialogView bindRegisterTeamDialogView(
    RegisterTeamDialogViewImpl impl
  );

  @Binds
  abstract EditRegisteredTeamDialogView bindEditRegisteredTeamDialogView(
    EditRegisteredTeamDialogViewImpl impl
  );

  @Binds
  abstract ChallengeTeamsView bindChallengeTeamsView(
    ChallengeTeamsViewImpl impl
  );

  @Binds
  abstract ChallengeBadgeView bindChallengeBadgeView(
    ChallengeBadgeViewImpl impl
  );

  @Binds
  abstract ProjectBadgeView bindProjectBadgeView(ProjectBadgeViewImpl impl);

  @Binds
  abstract TableQueryResultWikiWidgetView bindTableQueryResultWikiWidgetView(
    TableQueryResultWikiWidgetViewImpl impl
  );

  @Binds
  abstract SingleButtonView bindSingleButtonView(SingleButtonViewImpl impl);

  @Binds
  @Singleton
  abstract AnnotationTransformer bindAnnotationTransformer(
    AnnotationTransformerImpl impl
  );

  @Binds
  abstract AnnotationEditorView bindAnnotationEditorView(
    AnnotationEditorViewImpl impl
  );

  @Binds
  abstract EditAnnotationsDialogView bindEditAnnotationsDialogView(
    EditAnnotationsDialogViewImpl impl
  );

  @Binds
  abstract CommaSeparatedValuesParserView bindCommaSeparatedValuesParserView(
    CommaSeparatedValuesParserViewImpl impl
  );

  @Binds
  @Singleton
  abstract AnnotationCellFactory bindAnnotationCellFactory(
    AnnotationCellFactoryImpl impl
  );

  @Binds
  @Singleton
  abstract EntityId2BundleCache bindEntityId2BundleCache(
    EntityId2BundleCacheImpl impl
  );

  @Binds
  abstract VersionHistoryRowView bindVersionHistoryRowView(
    VersionHistoryRowViewImpl impl
  );

  @Binds
  abstract SynapseStandaloneWikiView bindSynapseStandaloneWikiView(
    SynapseStandaloneWikiViewImpl impl
  );

  @Binds
  abstract SynapseAlertView bindSynapseAlertView(SynapseAlertViewImpl impl);

  @Binds
  abstract SynapseAlert bindSynapseAlert(SynapseAlertImpl impl);

  @Binds
  abstract ProvenanceEditorWidgetView bindProvenanceEditorWidgetView(
    ProvenanceEditorWidgetViewImpl impl
  );

  @Binds
  abstract ProvenanceListWidgetView bindProvenanceListWidgetView(
    ProvenanceListWidgetViewImpl impl
  );

  @Binds
  abstract ProvenanceURLDialogWidgetView bindProvenanceURLDialogWidgetView(
    ProvenanceURLDialogWidgetViewImpl impl
  );

  @Binds
  abstract EntityRefProvEntryView bindEntityRefProvEntryView(
    EntityRefProvEntryViewImpl impl
  );

  @Binds
  abstract URLProvEntryView bindURLProvEntryView(URLProvEntryViewImpl impl);

  @Binds
  abstract StorageLocationWidgetView bindStorageLocationWidgetView(
    StorageLocationWidgetViewImpl impl
  );

  @Binds
  abstract ErrorView bindErrorView(ErrorViewImpl impl);

  @Binds
  abstract PreviewConfigView bindPreviewConfigView(PreviewConfigViewImpl impl);

  @Binds
  abstract SynapseFormConfigView bindSynapseFormConfigView(
    SynapseFormConfigViewImpl impl
  );

  @Binds
  abstract DownloadCartPageView bindDownloadCartPageView(
    DownloadCartPageViewImpl impl
  );

  @Binds
  abstract DataCatalogPageView bindDataCatalogPageView(
    DataCatalogPageViewImpl impl
  );

  @Binds
  abstract EditFileMetadataModalView bindEditFileMetadataModalView(
    EditFileMetadataModalViewImpl impl
  );

  @Binds
  abstract EditFileMetadataModalWidget bindEditFileMetadataModalWidget(
    EditFileMetadataModalWidgetImpl impl
  );

  @Binds
  abstract EditProjectMetadataModalView bindEditProjectMetadataModalView(
    EditProjectMetadataModalViewImpl impl
  );

  @Binds
  abstract EditProjectMetadataModalWidget bindEditProjectMetadataModalWidget(
    EditProjectMetadataModalWidgetImpl impl
  );

  @Binds
  abstract BiodallianceWidgetView bindBiodallianceWidgetView(
    BiodallianceWidgetViewImpl impl
  );

  @Binds
  abstract BiodallianceSourceEditorView bindBiodallianceSourceEditorView(
    BiodallianceSourceEditorViewImpl impl
  );

  @Binds
  abstract BiodallianceEditorView bindBiodallianceEditorView(
    BiodallianceEditorViewImpl impl
  );

  @Binds
  abstract TabView bindTabView(TabViewImpl impl);

  @Binds
  abstract TabsView bindTabsView(TabsViewImpl impl);

  @Binds
  abstract FilesTabView bindFilesTabView(FilesTabViewImpl impl);

  @Binds
  abstract TablesTabView bindTablesTabView(TablesTabViewImpl impl);

  @Binds
  abstract ChallengeTabView bindChallengeTabView(ChallengeTabViewImpl impl);

  @Binds
  abstract DiscussionTabView bindDiscussionTabView(DiscussionTabViewImpl impl);

  @Binds
  abstract DockerTabView bindDockerTabView(DockerTabViewImpl impl);

  @Binds
  abstract ModifiedCreatedByWidgetView bindModifiedCreatedByWidgetView(
    ModifiedCreatedByWidgetViewImpl impl
  );

  @Binds
  abstract FileHandleListView bindFileHandleListView(
    FileHandleListViewImpl impl
  );

  @Binds
  abstract ACTView bindACTView(ACTViewImpl impl);

  @Binds
  abstract CytoscapeConfigView bindCytoscapeConfigView(
    CytoscapeConfigViewImpl impl
  );

  @Binds
  abstract CytoscapeView bindCytoscapeView(CytoscapeViewImpl impl);

  @Binds
  abstract DiscussionThreadModalView bindDiscussionThreadModalView(
    DiscussionThreadModalViewImpl impl
  );

  @Binds
  abstract ReplyModalView bindReplyModalView(ReplyModalViewImpl impl);

  @Binds
  abstract DiscussionThreadListWidgetView bindDiscussionThreadListWidgetView(
    DiscussionThreadListWidgetViewImpl impl
  );

  @Binds
  abstract DiscussionThreadListItemWidgetView bindDiscussionThreadListItemWidgetView(
    DiscussionThreadListItemWidgetViewImpl impl
  );

  @Binds
  abstract SingleDiscussionThreadWidgetView bindSingleDiscussionThreadWidgetView(
    SingleDiscussionThreadWidgetViewImpl impl
  );

  @Binds
  abstract ReplyWidgetView bindReplyWidgetView(ReplyWidgetViewImpl impl);

  @Binds
  abstract ForumWidgetView bindForumWidgetView(ForumWidgetViewImpl impl);

  @Binds
  abstract NewReplyWidgetView bindNewReplyWidgetView(
    NewReplyWidgetViewImpl impl
  );

  @Binds
  abstract DockerRepoListWidgetView bindDockerRepoListWidgetView(
    DockerRepoListWidgetViewImpl impl
  );

  @Binds
  abstract DockerRepoWidgetView bindDockerRepoWidgetView(
    DockerRepoWidgetViewImpl impl
  );

  @Binds
  abstract AddExternalRepoModalView bindAddExternalRepoModalView(
    AddExternalRepoModalViewImpl impl
  );

  @Binds
  abstract DockerCommitRowWidgetView bindDockerCommitRowWidgetView(
    DockerCommitRowWidgetViewImpl impl
  );

  @Binds
  abstract DockerCommitListWidgetView bindDockerCommitListWidgetView(
    DockerCommitListWidgetViewImpl impl
  );

  @Binds
  abstract SessionStorage bindSessionStorage(SessionStorageImpl impl);

  @Binds
  abstract SynapseForumView bindSynapseForumView(SynapseForumViewImpl impl);

  @Binds
  abstract WikiMarkdownEditorView bindWikiMarkdownEditorView(
    WikiMarkdownEditorViewImpl impl
  );

  @Binds
  abstract StuAlertView bindStuAlertView(StuAlertViewImpl impl);

  @Binds
  abstract SynapseTableFormWidgetView bindSynapseTableFormWidgetView(
    SynapseTableFormWidgetViewImpl impl
  );

  @Binds
  abstract RowFormView bindRowFormView(RowFormViewImpl impl);

  @Binds
  abstract RadioCellEditorView bindRadioCellEditorView(
    RadioCellEditorViewImpl impl
  );

  @Binds
  abstract MarkdownIt bindMarkdownIt(MarkdownItImpl impl);

  @Binds
  abstract SubscriptionView bindSubscriptionView(SubscriptionViewImpl impl);

  @Binds
  abstract TopicWidgetView bindTopicWidgetView(TopicWidgetViewImpl impl);

  @Binds
  abstract SubscribeButtonWidgetView bindSubscribeButtonWidgetView(
    SubscribeButtonWidgetViewImpl impl
  );

  @Binds
  abstract RefreshAlertView bindRefreshAlertView(RefreshAlertViewImpl impl);

  @Binds
  abstract UserSelectorView bindUserSelectorView(UserSelectorViewImpl impl);

  @Binds
  abstract EntityContainerListWidgetView bindEntityContainerListWidgetView(
    EntityContainerListWidgetViewImpl impl
  );

  @Binds
  abstract EntityViewScopeWidgetView bindEntityViewScopeWidgetView(
    EntityViewScopeWidgetViewImpl impl
  );

  @Binds
  abstract CopyTextModal bindCopyTextModal(CopyTextModalImpl impl);

  @Binds
  abstract LoadMoreWidgetContainerView bindLoadMoreWidgetContainerView(
    LoadMoreWidgetContainerViewImpl impl
  );

  @Binds
  abstract RadioWidget bindRadioWidget(RadioWidgetViewImpl impl);

  @Binds
  abstract FileClientsHelpView bindFileClientsHelpView(
    FileClientsHelpViewImpl impl
  );

  @Binds
  abstract ContainerClientsHelp bindContainerClientsHelp(
    ContainerClientsHelpImpl impl
  );

  @Binds
  abstract FileDownloadMenuItemView bindFileDownloadMenuItemView(
    FileDownloadMenuItemViewImpl impl
  );

  @Binds
  abstract SqlDefinedEditorModalWidgetView bindSqlDefinedEditorModalWidgetView(
    SqlDefinedEditorModalWidgetViewImpl impl
  );

  @Binds
  abstract EntityViewScopeEditorModalWidgetView bindEntityViewScopeEditorModalWidgetView(
    EntityViewScopeEditorModalWidgetViewImpl impl
  );

  @Binds
  abstract SubmissionViewScopeEditorModalWidgetView bindSubmissionViewScopeEditorModalWidgetView(
    SubmissionViewScopeEditorModalWidgetViewImpl impl
  );

  @Binds
  abstract EntityModalWidgetView bindEntityModalWidgetView(
    EntityModalWidgetViewImpl impl
  );

  @Binds
  abstract ChallengeWidgetView bindChallengeWidgetView(
    ChallengeWidgetViewImpl impl
  );

  @Binds
  abstract SelectTeamModalView bindSelectTeamModalView(
    SelectTeamModalViewImpl impl
  );

  @Binds
  abstract ApproveUserAccessModalView bindApproveUserAccessModalView(
    ApproveUserAccessModalViewImpl impl
  );

  @Binds
  abstract UserBadgeListView bindUserBadgeListView(UserBadgeListViewImpl impl);

  @Binds
  abstract EntityListRowBadgeView bindEntityListRowBadgeView(
    EntityListRowBadgeViewImpl impl
  );

  @Binds
  abstract LazyLoadWikiWidgetWrapperView bindLazyLoadWikiWidgetWrapperView(
    LazyLoadWikiWidgetWrapperViewImpl impl
  );

  @Binds
  @Singleton
  abstract EntityHeaderAsyncHandler bindEntityHeaderAsyncHandler(
    EntityHeaderAsyncHandlerImpl impl
  );

  @Binds
  @Singleton
  abstract VersionedEntityHeaderAsyncHandler bindVersionedEntityHeaderAsyncHandler(
    VersionedEntityHeaderAsyncHandlerImpl impl
  );

  @Binds
  abstract GoogleMapView bindGoogleMapView(GoogleMapViewImpl impl);

  @Binds
  @Singleton
  abstract FileHandleAsyncHandler bindFileHandleAsyncHandler(
    FileHandleAsyncHandlerImpl impl
  );

  @Binds
  @Singleton
  abstract PresignedURLAsyncHandler bindPresignedURLAsyncHandler(
    PresignedURLAsyncHandlerImpl impl
  );

  @Binds
  @Singleton
  abstract PresignedAndFileHandleURLAsyncHandler bindPresignedAndFileHandleURLAsyncHandler(
    PresignedAndFileHandleURLAsyncHandlerImpl impl
  );

  @Binds
  @Singleton
  abstract UserProfileAsyncHandler bindUserProfileAsyncHandler(
    UserProfileAsyncHandlerImpl impl
  );

  @Binds
  @Singleton
  abstract TeamAsyncHandler bindTeamAsyncHandler(TeamAsyncHandlerImpl impl);

  @Binds
  @Singleton
  abstract UserGroupHeaderAsyncHandler bindUserGroupHeaderAsyncHandler(
    UserGroupHeaderAsyncHandlerImpl impl
  );

  @Binds
  @Singleton
  abstract UserGroupHeaderFromAliasAsyncHandler bindUserGroupHeaderFromAliasAsyncHandler(
    UserGroupHeaderFromAliasAsyncHandlerImpl impl
  );

  @Binds
  abstract DivView bindDivView(DivViewImpl impl);

  @Binds
  abstract SubscribersWidgetView bindSubscribersWidgetView(
    SubscribersWidgetViewImpl impl
  );

  @Binds
  abstract PlaceView bindPlaceView(PlaceViewImpl impl);

  @Binds
  abstract ManagedACTAccessRequirementWidgetView bindManagedACTAccessRequirementWidgetView(
    ManagedACTAccessRequirementWidgetViewImpl impl
  );

  @Binds
  abstract ACTAccessRequirementWidgetView bindACTAccessRequirementWidgetView(
    ACTAccessRequirementWidgetViewImpl impl
  );

  @Binds
  abstract TermsOfUseAccessRequirementWidgetView bindTermsOfUseAccessRequirementWidgetView(
    TermsOfUseAccessRequirementWidgetViewImpl impl
  );

  @Binds
  abstract FileHandleWidgetView bindFileHandleWidgetView(
    FileHandleWidgetViewImpl impl
  );

  @Binds
  abstract CreateAccessRequirementStep1View bindCreateAccessRequirementStep1View(
    CreateAccessRequirementStep1ViewImpl impl
  );

  @Binds
  abstract CreateManagedACTAccessRequirementStep2View bindCreateManagedACTAccessRequirementStep2View(
    CreateManagedACTAccessRequirementStep2ViewImpl impl
  );

  @Binds
  abstract CreateBasicAccessRequirementStep2View bindCreateBasicAccessRequirementStep2View(
    CreateBasicAccessRequirementStep2ViewImpl impl
  );

  @Binds
  abstract CreateManagedACTAccessRequirementStep3View bindCreateManagedACTAccessRequirementStep3View(
    CreateManagedACTAccessRequirementStep3ViewImpl impl
  );

  @Binds
  abstract Button bindButton(ButtonImpl impl);

  @Binds
  @Singleton
  abstract IsACTMemberAsyncHandler bindIsACTMemberAsyncHandler(
    IsACTMemberAsyncHandlerImpl impl
  );

  @Binds
  @Singleton
  abstract PopupUtilsView bindPopupUtilsView(PopupUtilsViewImpl impl);

  @Binds
  abstract ProfileCertifiedValidatedView bindProfileCertifiedValidatedView(
    ProfileCertifiedValidatedViewImpl impl
  );

  @Binds
  abstract ACTDataAccessSubmissionsView bindACTDataAccessSubmissionsView(
    ACTDataAccessSubmissionsViewImpl impl
  );

  @Binds
  abstract RestrictionWidgetView bindRestrictionWidgetView(
    RestrictionWidgetViewImpl impl
  );

  @Binds
  abstract ACTDataAccessSubmissionWidgetView bindACTDataAccessSubmissionWidgetView(
    ACTDataAccessSubmissionWidgetViewImpl impl
  );

  @Binds
  abstract OpenSubmissionWidgetView bindOpenSubmissionWidgetView(
    OpenSubmissionWidgetViewImpl impl
  );

  @Binds
  abstract LockAccessRequirementWidgetView bindLockAccessRequirementWidgetView(
    LockAccessRequirementWidgetViewImpl impl
  );

  @Binds
  abstract ImageUploadView bindImageUploadView(ImageUploadViewImpl impl);

  @Binds
  abstract RevokeUserAccessModalView bindRevokeUserAccessModalView(
    RevokeUserAccessModalViewImpl impl
  );

  @Binds
  abstract PlotlyWidgetView bindPlotlyWidgetView(PlotlyWidgetViewImpl impl);

  @Binds
  abstract PlotlyConfigView bindPlotlyConfigView(PlotlyConfigViewImpl impl);

  @Binds
  @Singleton
  abstract DateTimeUtils bindDateTimeUtils(DateTimeUtilsImpl impl);

  @Binds
  abstract ACTAccessApprovalsView bindACTAccessApprovalsView(
    ACTAccessApprovalsViewImpl impl
  );

  @Binds
  abstract AccessRequirementsSRCView bindAccessRequirementsSRCView(
    AccessRequirementsSRCViewImpl impl
  );

  @Binds
  abstract AccessorGroupView bindAccessorGroupView(AccessorGroupViewImpl impl);

  @Binds
  abstract SelfSignAccessRequirementWidgetView bindSelfSignAccessRequirementWidgetView(
    SelfSignAccessRequirementWidgetViewImpl impl
  );

  @Binds
  abstract TeamSubjectWidgetView bindTeamSubjectWidgetView(
    TeamSubjectWidgetViewImpl impl
  );

  @Binds
  abstract EntitySubjectsWidgetView bindEntitySubjectsWidgetView(
    EntitySubjectsWidgetViewImpl impl
  );

  @Binds
  abstract AwsLoginView bindAwsLoginView(AwsLoginViewImpl impl);

  @Binds
  abstract UserListRowWidgetView bindUserListRowWidgetView(
    UserListRowWidgetViewImpl impl
  );

  @Binds
  abstract UserListView bindUserListView(UserListViewImpl impl);

  @Binds
  abstract FileViewClientsHelp bindFileViewClientsHelp(
    FileViewClientsHelpImpl impl
  );

  @Binds
  abstract EmailAddressesWidgetView bindEmailAddressesWidgetView(
    EmailAddressesWidgetViewImpl impl
  );

  @Binds
  abstract HtmlPreviewView bindHtmlPreviewView(HtmlPreviewViewImpl impl);

  @Binds
  abstract S3DirectLoginDialog bindS3DirectLoginDialog(
    S3DirectLoginDialogImpl impl
  );

  @Binds
  abstract WikiPageDeleteConfirmationDialogView bindWikiPageDeleteConfirmationDialogView(
    WikiPageDeleteConfirmationDialogViewImpl impl
  );

  @Binds
  @Singleton
  abstract SynapseProperties bindSynapseProperties(SynapsePropertiesImpl impl);

  @Binds
  abstract Moment bindMoment(MomentImpl impl);

  @Binds
  abstract DownloadSpeedTester bindDownloadSpeedTester(
    DownloadSpeedTesterImpl impl
  );

  @Binds
  abstract PackageSizeSummaryView bindPackageSizeSummaryView(
    PackageSizeSummaryViewImpl impl
  );

  @Binds
  abstract EntityPresenterEventBinder bindEntityPresenterEventBinder(
    EntityPresenterEventBinderImpl impl
  );

  @Binds
  abstract Linkify bindLinkify(LinkifyImpl impl);

  @Binds
  abstract PasswordResetSignedTokenView bindPasswordResetSignedTokenView(
    PasswordResetSignedTokenViewImpl impl
  );

  @Binds
  abstract TeamProjectsModalWidgetView bindTeamProjectsModalWidgetView(
    TeamProjectsModalWidgetViewImpl impl
  );

  @Binds
  abstract ContainerItemCountWidgetView bindContainerItemCountWidgetView(
    ContainerItemCountWidgetViewImpl impl
  );

  @Binds
  abstract StatisticsPlotWidgetView bindStatisticsPlotWidgetView(
    StatisticsPlotWidgetViewImpl impl
  );

  @Binds
  abstract EvaluationFinderView bindEvaluationFinderView(
    EvaluationFinderViewImpl impl
  );

  @Binds
  abstract SubmissionViewScopeWidgetView bindSubmissionViewScopeWidgetView(
    SubmissionViewScopeWidgetViewImpl impl
  );

  @Binds
  abstract PageProgressWidgetView bindPageProgressWidgetView(
    PageProgressWidgetViewImpl impl
  );

  @Binds
  abstract EntityFinderWidget bindEntityFinderWidget(
    EntityFinderWidgetImpl impl
  );

  @Binds
  abstract EntityFinderWidget.Builder bindEntityFinderWidgetBuilder(
    EntityFinderWidgetImpl.Builder impl
  );

  @Binds
  abstract EntityFinderWidgetView bindEntityFinderWidgetView(
    EntityFinderWidgetViewImpl impl
  );

  @Binds
  abstract SynapseReactClientFullContextPropsProvider bindSynapseReactClientFullContextPropsProvider(
    SynapseReactClientFullContextPropsProviderImpl impl
  );

  @Binds
  abstract AddToDownloadListV2 bindAddToDownloadListV2(
    AddToDownloadListV2Impl impl
  );

  @Binds
  abstract OpenDataView bindOpenDataView(OpenDataViewImpl impl);

  @Binds
  @Singleton
  abstract QueryClientProvider bindQueryClientProvider(
    QueryClientProviderImpl impl
  );

  @Binds
  abstract IntendedDataUseReportWidgetView bindIntendedDataUseReportWidgetView(
    IntendedDataUseReportWidgetViewImpl impl
  );

  @Binds
  abstract DialogView bindDialogView(Dialog impl);

  @Binds
  abstract ChatView bindChatView(ChatViewImpl impl);

  @Binds
  abstract PlansView bindPlansView(PlansViewImpl impl);

  @Binds
  @Singleton
  abstract FollowingPageView bindFollowingPageView(FollowingPageViewImpl impl);

  @Binds
  abstract KeyFactoryProvider bindKeyFactoryProvider(
    KeyFactoryProviderImpl impl
  );

  @Binds
  abstract SRCUploadFileWrapper bindSRCUploadFileWrapper(
    SRCUploadFileWrapperImpl impl
  );

  @Binds
  abstract EntityTypeIcon bindEntityTypeIcon(EntityTypeIconImpl impl);

  @Binds
  abstract OneSageUtils bindOneSageUtils(OneSageUtilsImpl impl);

  @Binds
  abstract SearchAnalyticsClient bindSearchAnalyticsClient(
    SearchAnalyticsClientImpl impl
  );

  @Binds
  @Singleton
  abstract GridPageView bindGridPageView(GridPageViewImpl impl);

  @Binds
  @Singleton
  abstract SearchV2View bindSearchV2View(SearchV2ViewImpl impl);

  @Binds
  abstract CreateGridSessionDialog bindCreateGridSessionDialog(
    CreateGridSessionDialogImpl impl
  );

  @Binds
  abstract EntityCitation bindEntityCitation(EntityCitationImpl impl);

  @Binds
  abstract ProjectVisibilityChip bindProjectVisibilityChip(
    ProjectVisibilityChipImpl impl
  );

  @Binds
  abstract ProjectInfo bindProjectInfo(ProjectInfoImpl impl);

  @Binds
  abstract CsvPreview bindCsvPreview(CsvPreviewImpl impl);

  @Binds
  abstract CreateTableFromCsvDialog bindCreateTableFromCsvDialog(
    CreateTableFromCsvDialogImpl impl
  );

  @Binds
  abstract UpdateTableWithCsvDialog bindUpdateTableWithCsvDialog(
    UpdateTableWithCsvDialogImpl impl
  );

  @Binds
  abstract ShareThisPage bindShareThisPage(ShareThisPageImpl impl);

  @Binds
  abstract MetadataTabView bindMetadataTabView(MetadataTabViewImpl impl);

  // GWT-RPC async services (formerly auto-generated by GIN via GWT.create)

  @Provides
  @Singleton
  static SynapseClientAsync provideSynapseClientAsync() {
    return GWT.create(SynapseClient.class);
  }

  @Provides
  @Singleton
  static ChallengeClientAsync provideChallengeClientAsync() {
    return GWT.create(ChallengeClient.class);
  }

  @Provides
  @Singleton
  static DataAccessClientAsync provideDataAccessClientAsync() {
    return GWT.create(DataAccessClient.class);
  }

  @Provides
  @Singleton
  static DiscussionForumClientAsync provideDiscussionForumClientAsync() {
    return GWT.create(DiscussionForumClient.class);
  }

  @Provides
  @Singleton
  static UserProfileClientAsync provideUserProfileClientAsync() {
    return GWT.create(UserProfileClient.class);
  }

  @Provides
  @Singleton
  static UserAccountServiceAsync provideUserAccountServiceAsync() {
    return GWT.create(UserAccountService.class);
  }

  @Provides
  @Singleton
  static StackConfigServiceAsync provideStackConfigServiceAsync() {
    return GWT.create(StackConfigService.class);
  }

  @Provides
  @Singleton
  static LinkedInServiceAsync provideLinkedInServiceAsync() {
    return GWT.create(LinkedInService.class);
  }
}
