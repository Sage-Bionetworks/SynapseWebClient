package org.sagebionetworks.web.client;

import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.gwt.client.schema.adapter.JSONArrayGwt;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.ClientCacheImpl;
import org.sagebionetworks.web.client.cache.EntityId2BundleCache;
import org.sagebionetworks.web.client.cache.EntityId2BundleCacheImpl;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cache.SessionStorageImpl;
import org.sagebionetworks.web.client.cache.StorageImpl;
import org.sagebionetworks.web.client.cache.StorageWrapper;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.cookie.GWTCookieImpl;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenterEventBinder;
import org.sagebionetworks.web.client.presenter.EntityPresenterEventBinderImpl;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.SignedTokenPresenter;
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
import org.sagebionetworks.web.client.view.AccountView;
import org.sagebionetworks.web.client.view.AccountViewImpl;
import org.sagebionetworks.web.client.view.ChallengeOverviewView;
import org.sagebionetworks.web.client.view.ChallengeOverviewViewImpl;
import org.sagebionetworks.web.client.view.ChangeUsernameView;
import org.sagebionetworks.web.client.view.ChangeUsernameViewImpl;
import org.sagebionetworks.web.client.view.ComingSoonView;
import org.sagebionetworks.web.client.view.ComingSoonViewImpl;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.DivViewImpl;
import org.sagebionetworks.web.client.view.DownView;
import org.sagebionetworks.web.client.view.DownViewImpl;
import org.sagebionetworks.web.client.view.EmailInvitationView;
import org.sagebionetworks.web.client.view.EmailInvitationViewImpl;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.view.EntityViewImpl;
import org.sagebionetworks.web.client.view.ErrorView;
import org.sagebionetworks.web.client.view.ErrorViewImpl;
import org.sagebionetworks.web.client.view.HelpView;
import org.sagebionetworks.web.client.view.HelpViewImpl;
import org.sagebionetworks.web.client.view.HomeView;
import org.sagebionetworks.web.client.view.HomeViewImpl;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.view.LoginViewImpl;
import org.sagebionetworks.web.client.view.MapView;
import org.sagebionetworks.web.client.view.MapViewImpl;
import org.sagebionetworks.web.client.view.NewAccountView;
import org.sagebionetworks.web.client.view.NewAccountViewImpl;
import org.sagebionetworks.web.client.view.PasswordResetSignedTokenView;
import org.sagebionetworks.web.client.view.PasswordResetSignedTokenViewImpl;
import org.sagebionetworks.web.client.view.PeopleSearchView;
import org.sagebionetworks.web.client.view.PeopleSearchViewImpl;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.view.PlaceViewImpl;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.view.ProfileViewImpl;
import org.sagebionetworks.web.client.view.QuestionContainerWidgetView;
import org.sagebionetworks.web.client.view.QuestionContainerWidgetViewImpl;
import org.sagebionetworks.web.client.view.QuizView;
import org.sagebionetworks.web.client.view.QuizViewImpl;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.view.SearchViewImpl;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.client.view.SettingsViewImpl;
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
import org.sagebionetworks.web.client.view.WikiDiffView;
import org.sagebionetworks.web.client.view.WikiDiffViewImpl;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.view.users.PasswordResetViewImpl;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterAccountViewImpl;
import org.sagebionetworks.web.client.view.users.RegisterWidgetView;
import org.sagebionetworks.web.client.view.users.RegisterWidgetViewImpl;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.ButtonImpl;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.CopyTextModalImpl;
import org.sagebionetworks.web.client.widget.DownloadSpeedTester;
import org.sagebionetworks.web.client.widget.DownloadSpeedTesterImpl;
import org.sagebionetworks.web.client.widget.FileHandleWidgetView;
import org.sagebionetworks.web.client.widget.FileHandleWidgetViewImpl;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainerView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainerViewImpl;
import org.sagebionetworks.web.client.widget.QuarantinedEmailModal;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.RadioWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidgetViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectWidgetViewImpl;
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
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionWizardStep2View;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionWizardStep2ViewImpl;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectWizardStep1View;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectWizardStep1ViewImpl;
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
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModalView;
import org.sagebionetworks.web.client.widget.doi.CreateOrUpdateDoiModalViewImpl;
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
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxView;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidgetView;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItemView;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItemViewImpl;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetView;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.ProjectBadgeView;
import org.sagebionetworks.web.client.widget.entity.ProjectBadgeViewImpl;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalViewImpl;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialogView;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialogViewImpl;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidget;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidgetView;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidgetViewImpl;
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
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserViewImpl;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserView;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowserViewImpl;
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
import org.sagebionetworks.web.client.widget.entity.editor.UserSelectorView;
import org.sagebionetworks.web.client.widget.entity.editor.UserSelectorViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.UserTeamConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListView;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadMenuItemView;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadMenuItemViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.S3DirectLoginDialog;
import org.sagebionetworks.web.client.widget.entity.file.S3DirectLoginDialogImpl;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.DownloadListWidgetView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.DownloadListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationRowView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationRowViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationTableView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationTableViewImpl;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummaryView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummaryViewImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetView;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetViewImpl;
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
import org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.ReferenceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.SRCDemoWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.SRCDemoWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.SynapseTableFormWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.SynapseTableFormWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountView;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberRowWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberRowWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMembersWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMembersWidgetViewImpl;
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
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModalView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModalViewImpl;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationListView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationListViewImpl;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterViewImpl;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.FooterViewImpl;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMapView;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMapViewImpl;
import org.sagebionetworks.web.client.widget.header.Header;
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
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetView;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetViewImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalView;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalViewImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidgetImpl;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetViewImpl;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertView;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertViewImpl;
import org.sagebionetworks.web.client.widget.search.SearchBoxView;
import org.sagebionetworks.web.client.widget.search.SearchBoxViewImpl;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxView;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBoxViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorViewImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidgetImpl;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadgeView;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadgeViewImpl;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGridView;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGridViewImpl;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidgetView;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidgetViewImpl;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidgetView;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidgetViewImpl;
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidgetView;
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.subscription.TopicRowWidgetView;
import org.sagebionetworks.web.client.widget.subscription.TopicRowWidgetViewImpl;
import org.sagebionetworks.web.client.widget.subscription.TopicWidgetView;
import org.sagebionetworks.web.client.widget.subscription.TopicWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.FocusSetter;
import org.sagebionetworks.web.client.widget.table.FocusSetterImpl;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandlerImpl;
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
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1View;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1ViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2View;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2ViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ScopeWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ScopeWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsView;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsViewImpl;
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
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidgetViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LargeStringCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LargeStringCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LinkCellRendererView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LinkCellRendererViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEdtiorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.NumberCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.NumberCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCellView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCellViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesView;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewerImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewBase;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewBaseImpl;
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
import org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.client.widget.user.UserBadgeViewImpl;
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
		bind(AuthenticationController.class).to(AuthenticationControllerImpl.class).in(Singleton.class);;

		// GlobalApplicationState
		bind(GlobalApplicationState.class).to(GlobalApplicationStateImpl.class).in(Singleton.class);
		bind(GlobalApplicationStateView.class).to(GlobalApplicationStateViewImpl.class);

		bind(LazyLoadCallbackQueue.class).to(LazyLoadCallbackQueueImpl.class).in(Singleton.class);

		bind(ResourceLoader.class).to(ResourceLoaderImpl.class).in(Singleton.class);

		// Header & Footer
		bind(Header.class).in(Singleton.class);
		bind(HeaderView.class).to(HeaderViewImpl.class).in(Singleton.class);
		bind(Footer.class).in(Singleton.class);
		bind(FooterView.class).to(FooterViewImpl.class).in(Singleton.class);

		// JSONAdapters
		bind(JSONObjectAdapter.class).to(JSONObjectGwt.class);
		bind(JSONArrayAdapter.class).to(JSONArrayGwt.class);

		// cache place presenters
		bind(ProfilePresenter.class).in(Singleton.class);
		bind(EntityPresenter.class).in(Singleton.class);
		bind(DownPresenter.class).in(Singleton.class);
		bind(SignedTokenPresenter.class).in(Singleton.class);

		bind(AnnotationsRendererWidgetView.class).to(AnnotationsRendererWidgetViewImpl.class);
		bind(VersionHistoryWidgetView.class).to(VersionHistoryWidgetViewImpl.class);

		// GWT utility methods
		bind(GWTWrapper.class).to(GWTWrapperImpl.class).in(Singleton.class);
		bind(GWTTimer.class).to(GWTTimerImpl.class);

		bind(SessionDetector.class).in(Singleton.class);

		// RequestBuilder
		bind(RequestBuilderWrapper.class).to(RequestBuilderWrapperImpl.class);

		// Adapter factoyr
		bind(AdapterFactory.class).to(GwtAdapterFactory.class);

		// ClientCache
		bind(ClientCache.class).to(ClientCacheImpl.class).in(Singleton.class);

		// Storage wrapper
		bind(StorageWrapper.class).to(StorageImpl.class).in(Singleton.class);;

		/*
		 * Vanilla Implementation binding
		 */

		// JSNI impls
		bind(SynapseJSNIUtils.class).to(SynapseJSNIUtilsImpl.class).in(Singleton.class);

		/*
		 * Places
		 */

		// The home page
		bind(HomeView.class).to(HomeViewImpl.class).in(Singleton.class);

		// EntityView
		bind(EntityView.class).to(EntityViewImpl.class).in(Singleton.class);

		// LoginView
		bind(LoginView.class).to(LoginViewImpl.class).in(Singleton.class);

		// PasswordResetView
		bind(PasswordResetView.class).to(PasswordResetViewImpl.class).in(Singleton.class);

		// NewAccountView
		bind(NewAccountView.class).to(NewAccountViewImpl.class).in(Singleton.class);

		// RegisterAccountView
		bind(RegisterAccountView.class).to(RegisterAccountViewImpl.class).in(Singleton.class);

		bind(RegisterWidgetView.class).to(RegisterWidgetViewImpl.class);

		// ProfileView
		bind(ProfileView.class).to(ProfileViewImpl.class).in(Singleton.class);

		// SettingsView
		bind(SettingsView.class).to(SettingsViewImpl.class).in(Singleton.class);

		// CominSoonView
		bind(ComingSoonView.class).to(ComingSoonViewImpl.class).in(Singleton.class);

		// BCCOverviewView
		bind(ChallengeOverviewViewImpl.class).in(Singleton.class);
		bind(ChallengeOverviewView.class).to(ChallengeOverviewViewImpl.class);

		// Help
		bind(HelpView.class).to(HelpViewImpl.class).in(Singleton.class);

		// SearchView
		bind(SearchView.class).to(SearchViewImpl.class).in(Singleton.class);

		// Down
		bind(DownView.class).to(DownViewImpl.class).in(Singleton.class);

		// Synapse Wiki Pages
		bind(SynapseWikiView.class).to(SynapseWikiViewImpl.class);

		// QuizView
		bind(QuizView.class).to(QuizViewImpl.class).in(Singleton.class);

		// Certificate
		bind(CertificateWidgetView.class).to(CertificateWidgetViewImpl.class);

		// Account
		bind(AccountView.class).to(AccountViewImpl.class).in(Singleton.class);

		// ChangeUsername
		bind(ChangeUsernameView.class).to(ChangeUsernameViewImpl.class).in(Singleton.class);

		// SignedToken
		bind(SignedTokenView.class).to(SignedTokenViewImpl.class).in(Singleton.class);

		// Trash
		bind(TrashView.class).to(TrashViewImpl.class).in(Singleton.class);

		// Asynchronous progress
		bind(TimerProvider.class).to(TimerProviderImpl.class);
		bind(NumberFormatProvider.class).to(NumberFormatProviderImpl.class);
		bind(AsynchronousProgressView.class).to(AsynchronousProgressViewImpl.class);
		bind(AsynchronousJobTracker.class).to(AsynchronousJobTrackerImpl.class);

		// EmailInvitation
		bind(EmailInvitationView.class).to(EmailInvitationViewImpl.class).in(Singleton.class);

		/*
		 * Widgets
		 */

		// QuestionContainerWidget
		bind(QuestionContainerWidgetView.class).to(QuestionContainerWidgetViewImpl.class);

		// DoiWidget
		bind(DoiWidgetV2View.class).to(DoiWidgetV2ViewImpl.class);
		bind(CreateOrUpdateDoiModalView.class).to(CreateOrUpdateDoiModalViewImpl.class);

		// LoginWidget
		bind(LoginWidgetView.class).to(LoginWidgetViewImpl.class).in(Singleton.class);

		// Breadcrumb
		bind(BreadcrumbView.class).to(BreadcrumbViewImpl.class);

		// Bind the cookie provider
		bind(GWTCookieImpl.class).in(Singleton.class);
		bind(CookieProvider.class).to(GWTCookieImpl.class);

		// ACL Editor
		bind(AccessControlListEditorView.class).to(AccessControlListEditorViewImpl.class);
		bind(AccessControlListModalWidget.class).to(AccessControlListModalWidgetImpl.class);
		bind(AccessControlListModalWidgetView.class).to(AccessControlListModalWidgetViewImpl.class);

		bind(EvaluationAccessControlListModalWidget.class).to(EvaluationAccessControlListModalWidgetImpl.class);

		// Sharing Permissions Grid
		bind(SharingPermissionsGridView.class).to(SharingPermissionsGridViewImpl.class);


		// basic pagination
		bind(BasicPaginationView.class).to(BasicPaginationViewImpl.class);

		// EntityPageTop
		bind(EntityPageTopView.class).to(EntityPageTopViewImpl.class);

		// Preview
		bind(PreviewWidgetView.class).to(PreviewWidgetViewImpl.class);

		// ActionMenu V2
		bind(ActionMenuWidget.class).to(ActionMenuWidgetImpl.class);
		bind(ActionMenuWidgetView.class).to(ActionMenuWidgetViewImpl.class);

		bind(EntityActionController.class).to(EntityActionControllerImpl.class);
		bind(EntityActionControllerView.class).to(EntityActionControllerViewImpl.class);
		bind(PreflightController.class).to(PreflightControllerImpl.class);
		bind(CertifiedUserController.class).to(CertifiedUserControllerImpl.class);

		bind(BigPromptModalView.class).to(BigPromptModalViewImpl.class);
		bind(PromptForValuesModalView.class).to(PromptForValuesModalViewImpl.class);
		bind(RenameEntityModalWidget.class).to(RenameEntityModalWidgetImpl.class);

		// Rejected Reason
		bind(RejectReasonView.class).to(RejectReasonViewImpl.class);

		// FileBox
		bind(FileTitleBarView.class).to(FileTitleBarViewImpl.class).in(Singleton.class);
		bind(BasicTitleBarView.class).to(BasicTitleBarViewImpl.class);

		// Search Box
		bind(SearchBoxView.class).to(SearchBoxViewImpl.class).in(Singleton.class);

		// User Suggest Box
		bind(SynapseSuggestBoxView.class).to(SynapseSuggestBoxViewImpl.class);

		bind(MultipartUploader.class).to(MultipartUploaderImpl.class);
		bind(FileInputView.class).to(FileInputViewImpl.class);

		bind(FileHandleUploadView.class).to(FileHandleUploadViewImpl.class);
		bind(FileHandleUploadWidget.class).to(FileHandleUploadWidgetImpl.class);

		// LocationableUploader
		bind(UploaderView.class).to(UploaderViewImpl.class);

		bind(QuizInfoWidgetView.class).to(QuizInfoViewImpl.class);

		// EntityTreeBrowser
		bind(EntityTreeBrowserView.class).to(EntityTreeBrowserViewImpl.class);

		// MyEntitiesBrowser
		bind(MyEntitiesBrowserView.class).to(MyEntitiesBrowserViewImpl.class);

		// Wiki Attachments
		bind(WikiAttachmentsView.class).to(WikiAttachmentsViewImpl.class);

		bind(WikiHistoryWidgetView.class).to(WikiHistoryWidgetViewImpl.class);

		// Evaluation selector
		bind(EvaluationListView.class).to(EvaluationListViewImpl.class);

		// Administer Evaluations list
		bind(AdministerEvaluationsListView.class).to(AdministerEvaluationsListViewImpl.class);

		// EntitySearchBox
		bind(EntitySearchBoxView.class).to(EntitySearchBoxViewImpl.class);

		// EntityMetadata
		bind(EntityMetadataView.class).to(EntityMetadataViewImpl.class);

		bind(UserProfileEditorWidget.class).to(UserProfileEditorWidgetImpl.class);
		bind(UserProfileEditorWidgetView.class).to(UserProfileEditorWidgetViewImpl.class);
		bind(UserProfileModalWidget.class).to(UserProfileModalWidgetImpl.class);
		bind(UserProfileModalView.class).to(UserProfileModalViewImpl.class);
		bind(ProfileImageView.class).to(ProfileImageViewImpl.class);
		bind(ProfileImageWidget.class).to(ProfileImageWidgetImpl.class);

		// API Table Column manager
		bind(APITableColumnManagerView.class).to(APITableColumnManagerViewImpl.class);
		bind(APITableColumnConfigView.class).to(APITableColumnConfigViewImpl.class);

		// single subpages view
		bind(WikiSubpagesView.class).to(WikiSubpagesViewImpl.class);

		// SubPages Order Editor
		bind(WikiSubpagesOrderEditorView.class).to(WikiSubpagesOrderEditorViewImpl.class);

		// SubPages Order Editor Tree
		bind(WikiSubpageOrderEditorTreeView.class).to(WikiSubpageOrderEditorTreeViewImpl.class);

		// SubPages Navigation Tree
		bind(WikiSubpageNavigationTreeView.class).to(WikiSubpageNavigationTreeViewImpl.class);

		// Widget Registration
		bind(WidgetRegistrar.class).to(WidgetRegistrarImpl.class).in(Singleton.class);

		// UI Widget Descriptor editor
		bind(BaseEditWidgetDescriptorView.class).to(BaseEditWidgetDescriptorViewImpl.class);
		bind(ReferenceConfigView.class).to(ReferenceConfigViewImpl.class);
		bind(ImageConfigView.class).to(ImageConfigViewImpl.class).in(Singleton.class);
		bind(AttachmentConfigView.class).to(AttachmentConfigViewImpl.class).in(Singleton.class);
		bind(ProvenanceConfigView.class).to(ProvenanceConfigViewImpl.class);
		bind(LinkConfigView.class).to(LinkConfigViewImpl.class);
		bind(DetailsSummaryConfigView.class).to(DetailsSummaryConfigViewImpl.class);
		bind(TabbedTableConfigView.class).to(TabbedTableConfigViewImpl.class);
		bind(APITableConfigView.class).to(APITableConfigViewImpl.class);
		bind(QueryTableConfigView.class).to(QueryTableConfigViewImpl.class);
		bind(EntityListConfigView.class).to(EntityListConfigViewImpl.class);
		bind(ShinySiteConfigView.class).to(ShinySiteConfigViewImpl.class);
		bind(ButtonLinkConfigView.class).to(ButtonLinkConfigViewImpl.class);
		bind(EvaluationSubmissionConfigView.class).to(EvaluationSubmissionConfigViewImpl.class);
		bind(VideoConfigView.class).to(VideoConfigViewImpl.class);
		bind(TableQueryResultWikiView.class).to(TableQueryResultWikiViewImpl.class);

		// UI Widget Renderers
		bind(BookmarkWidgetView.class).to(BookmarkWidgetViewImpl.class);
		bind(ReferenceWidgetView.class).to(ReferenceWidgetViewImpl.class);
		bind(EntityListWidgetView.class).to(EntityListWidgetViewImpl.class);
		bind(IFrameView.class).to(IFrameViewImpl.class);
		bind(ImageWidgetView.class).to(ImageWidgetViewImpl.class);
		bind(AttachmentPreviewWidgetView.class).to(AttachmentPreviewWidgetViewImpl.class);
		bind(APITableWidgetView.class).to(APITableWidgetViewImpl.class);
		bind(TableOfContentsWidgetView.class).to(TableOfContentsWidgetViewImpl.class);
		bind(WikiFilesPreviewWidgetView.class).to(WikiFilesPreviewWidgetViewImpl.class);
		bind(ButtonLinkWidgetView.class).to(ButtonLinkWidgetViewImpl.class);
		bind(EmptyWidgetView.class).to(EmptyWidgetViewImpl.class);
		bind(VideoWidgetView.class).to(VideoWidgetViewImpl.class);
		bind(TeamMemberCountView.class).to(TeamMemberCountViewImpl.class);


		// ProvenanceWidget
		bind(ProvenanceWidgetView.class).to(ProvenanceWidgetViewImpl.class);

		// MarkdownWidget
		bind(MarkdownWidgetView.class).to(MarkdownWidgetViewImpl.class);

		// MarkdownEditorWidget
		bind(MarkdownEditorWidgetView.class).to(MarkdownEditorWidgetViewImpl.class);

		// FilesBrowser
		bind(FilesBrowserView.class).to(FilesBrowserViewImpl.class);

		// Entity Finder
		bind(EntityFinderView.class).to(EntityFinderViewImpl.class);

		// MoreTreeItem
		bind(MoreTreeItemView.class).to(MoreTreeItemViewImpl.class);

		bind(EvaluationSubmitterView.class).to(EvaluationSubmitterViewImpl.class);

		bind(FavoriteWidgetView.class).to(FavoriteWidgetViewImpl.class);

		bind(WikiPageWidgetView.class).to(WikiPageWidgetViewImpl.class);
		bind(UserBadgeView.class).to(UserBadgeViewImpl.class);
		bind(EmailInvitationBadgeView.class).to(EmailInvitationBadgeViewImpl.class);

		bind(EntityBadgeView.class).to(EntityBadgeViewImpl.class);

		bind(TutorialWizardView.class).to(TutorialWizardViewImpl.class);

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
		bind(ColumnModelsViewBase.class).to(ColumnModelsViewBaseImpl.class);
		bind(ColumnModelsView.class).to(ColumnModelsViewImpl.class);
		bind(ColumnModelTableRowEditorView.class).to(ColumnModelTableRowEditorViewImpl.class);
		bind(ColumnModelTableRowEditorWidget.class).to(ColumnModelTableRowEditorWidgetImpl.class);
		bind(ColumnModelTableRowViewer.class).to(ColumnModelTableRowViewerImpl.class);
		bind(TableEntityWidgetView.class).to(TableEntityWidgetViewImpl.class);
		bind(RowView.class).to(RowViewImpl.class);
		bind(TablePageView.class).to(TablePageViewImpl.class);
		bind(TableQueryResultView.class).to(TableQueryResultViewImpl.class);
		bind(QueryResultEditorView.class).to(QueryResultEditorViewImpl.class);
		bind(QueryInputView.class).to(QueryInputViewImpl.class);
		bind(JobTrackingWidget.class).to(AsynchronousProgressWidget.class);
		bind(UploadTableModalWidget.class).to(UploadTableModalWidgetImpl.class);
		bind(UploadCSVPreviewPage.class).to(UploadCSVPreviewPageImpl.class);
		bind(CSVOptionsView.class).to(CSVOptionsViewImpl.class);
		bind(UploadCSVPreviewPageView.class).to(UploadCSVPreviewPageViewImpl.class);
		bind(UploadPreviewView.class).to(UploadPreviewViewImpl.class);
		bind(UploadPreviewWidget.class).to(UploadPreviewWidgetImpl.class);
		bind(UploadCSVFilePage.class).to(UploadCSVFilePageImpl.class);
		bind(UploadCSVFinishPage.class).to(UploadCSVFinishPageImpl.class);
		bind(UploadCSVFinishPageView.class).to(UploadCSVFinishPageViewImpl.class);
		bind(UploadCSVAppendPage.class).to(UploadCSVAppendPageImpl.class);
		bind(UploadCSVAppendPageView.class).to(UploadCSVAppendPageViewImpl.class);
		bind(SortableTableHeader.class).to(SortableTableHeaderImpl.class);
		bind(StaticTableHeader.class).to(StaticTableHeaderImpl.class);

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
		bind(LinkCellRendererView.class).to(LinkCellRendererViewImpl.class);
		bind(StringRendererCellView.class).to(StringRendererCellViewImpl.class);
		bind(CellEditorView.class).to(CellEditorViewImpl.class);
		bind(NumberCellEditorView.class).to(NumberCellEditorViewImpl.class);
		bind(ListCellEdtiorView.class).to(ListCellEditorViewImpl.class);
		bind(DateCellEditorView.class).to(DateCellEditorViewImpl.class);
		bind(UserIdCellEditorView.class).to(UserIdCellEditorViewImpl.class);
		bind(FileCellEditorView.class).to(FileCellEditorViewImpl.class);
		bind(FileCellRendererView.class).to(FileCellRendererViewImpl.class);
		bind(EntityIdCellRendererView.class).to(EntityIdCellRendererViewImpl.class);
		bind(LargeStringCellEditorView.class).to(LargeStringCellEditorViewImpl.class);

		/*
		 * Teams Places
		 */
		// Team Page
		bind(TeamView.class).to(TeamViewImpl.class).in(Singleton.class);

		// Team Search Page
		bind(TeamSearchView.class).to(TeamSearchViewImpl.class).in(Singleton.class);

		bind(MapView.class).to(MapViewImpl.class);

		// People Search Page
		bind(PeopleSearchView.class).to(PeopleSearchViewImpl.class).in(Singleton.class);

		/*
		 * Teams Widgets
		 */

		// Team Action Menu Items
		bind(TeamEditModalWidgetView.class).to(TeamEditModalWidgetViewImpl.class);
		bind(TeamLeaveModalWidgetView.class).to(TeamLeaveModalWidgetViewImpl.class);
		bind(TeamDeleteModalWidgetView.class).to(TeamDeleteModalWidgetViewImpl.class);

		// Open Team Invitations widget
		bind(OpenTeamInvitationsWidgetView.class).to(OpenTeamInvitationsWidgetViewImpl.class);

		// Pending Team Join Requests widget
		bind(OpenMembershipRequestsWidgetView.class).to(OpenMembershipRequestsWidgetViewImpl.class);

		// Current User Invites widget
		bind(OpenUserInvitationsWidgetView.class).to(OpenUserInvitationsWidgetViewImpl.class);

		// Team List widget (link to search teams page, optionally can create team)
		bind(TeamListWidgetView.class).to(TeamListWidgetViewImpl.class);

		// Member List widget
		bind(MemberListWidgetView.class).to(MemberListWidgetViewImpl.class);

		// Invite Team member widget
		bind(InviteWidgetView.class).to(InviteWidgetViewImpl.class);

		// Request Team membership widget
		bind(JoinTeamWidgetView.class).to(JoinTeamWidgetViewImpl.class);

		// Join Team Button Config widget
		bind(JoinTeamConfigEditorView.class).to(JoinTeamConfigEditorViewImpl.class);

		// Submit to evaluation widget
		bind(SubmitToEvaluationWidgetView.class).to(SubmitToEvaluationWidgetViewImpl.class);
		// Team renderer
		bind(TeamBadgeView.class).to(TeamBadgeViewImpl.class);
		bind(BigTeamBadgeView.class).to(BigTeamBadgeViewImpl.class);


		bind(UserTeamConfigView.class).to(UserTeamConfigViewImpl.class);

		bind(SharingAndDataUseConditionWidgetView.class).to(SharingAndDataUseConditionWidgetViewImpl.class);

		bind(WizardProgressWidgetView.class).to(WizardProgressWidgetViewImpl.class);
		bind(UploadDialogWidgetView.class).to(UploadDialogWidgetViewImpl.class);
		bind(AddFolderDialogWidgetView.class).to(AddFolderDialogWidgetViewImpl.class);

		bind(LoginModalView.class).to(LoginModalViewImpl.class);

		bind(ImageParamsPanelView.class).to(ImageParamsPanelViewImpl.class);
		bind(RegisterTeamDialogView.class).to(RegisterTeamDialogViewImpl.class);
		bind(EditRegisteredTeamDialogView.class).to(EditRegisteredTeamDialogViewImpl.class);
		bind(ChallengeTeamsView.class).to(ChallengeTeamsViewImpl.class);
		bind(ChallengeBadgeView.class).to(ChallengeBadgeViewImpl.class);
		bind(ProjectBadgeView.class).to(ProjectBadgeViewImpl.class);
		bind(TableQueryResultWikiWidgetView.class).to(TableQueryResultWikiWidgetViewImpl.class);

		bind(SingleButtonView.class).to(SingleButtonViewImpl.class);
		bind(UserListView.class).to(UserListViewImpl.class);

		bind(AnnotationTransformer.class).to(AnnotationTransformerImpl.class).in(Singleton.class);
		bind(AnnotationEditorView.class).to(AnnotationEditorViewImpl.class);
		bind(EditAnnotationsDialogView.class).to(EditAnnotationsDialogViewImpl.class);

		bind(AnnotationCellFactory.class).to(AnnotationCellFactoryImpl.class).in(Singleton.class);
		bind(EntityId2BundleCache.class).to(EntityId2BundleCacheImpl.class).in(Singleton.class);

		bind(VersionHistoryRowView.class).to(VersionHistoryRowViewImpl.class);
		bind(SynapseStandaloneWikiView.class).to(SynapseStandaloneWikiViewImpl.class);

		bind(SynapseAlertView.class).to(SynapseAlertViewImpl.class);
		bind(SynapseAlert.class).to(SynapseAlertImpl.class);

		bind(ProvenanceEditorWidgetView.class).to(ProvenanceEditorWidgetViewImpl.class);
		bind(ProvenanceListWidgetView.class).to(ProvenanceListWidgetViewImpl.class);
		bind(ProvenanceURLDialogWidgetView.class).to(ProvenanceURLDialogWidgetViewImpl.class);
		bind(EntityRefProvEntryView.class).to(EntityRefProvEntryViewImpl.class);
		bind(URLProvEntryView.class).to(URLProvEntryViewImpl.class);
		bind(StorageLocationWidgetView.class).to(StorageLocationWidgetViewImpl.class);
		bind(ErrorView.class).to(ErrorViewImpl.class);
		bind(PreviewConfigView.class).to(PreviewConfigViewImpl.class);
		bind(SynapseFormConfigView.class).to(SynapseFormConfigViewImpl.class);

		bind(EditFileMetadataModalView.class).to(EditFileMetadataModalViewImpl.class);
		bind(EditFileMetadataModalWidget.class).to(EditFileMetadataModalWidgetImpl.class);
		bind(EditProjectMetadataModalView.class).to(EditProjectMetadataModalViewImpl.class);
		bind(EditProjectMetadataModalWidget.class).to(EditProjectMetadataModalWidgetImpl.class);
		bind(BiodallianceWidgetView.class).to(BiodallianceWidgetViewImpl.class);
		bind(BiodallianceSourceEditorView.class).to(BiodallianceSourceEditorViewImpl.class);
		bind(BiodallianceEditorView.class).to(BiodallianceEditorViewImpl.class);

		bind(TabView.class).to(TabViewImpl.class);
		bind(TabsView.class).to(TabsViewImpl.class);

		bind(FilesTabView.class).to(FilesTabViewImpl.class);
		bind(TablesTabView.class).to(TablesTabViewImpl.class);
		bind(ChallengeTabView.class).to(ChallengeTabViewImpl.class);
		bind(DiscussionTabView.class).to(DiscussionTabViewImpl.class);
		bind(DockerTabView.class).to(DockerTabViewImpl.class);
		bind(ModifiedCreatedByWidgetView.class).to(ModifiedCreatedByWidgetViewImpl.class);
		bind(FileHandleListView.class).to(FileHandleListViewImpl.class);
		bind(ACTView.class).to(ACTViewImpl.class);
		bind(CytoscapeConfigView.class).to(CytoscapeConfigViewImpl.class);
		bind(CytoscapeView.class).to(CytoscapeViewImpl.class);

		// discussion
		bind(DiscussionThreadModalView.class).to(DiscussionThreadModalViewImpl.class);
		bind(ReplyModalView.class).to(ReplyModalViewImpl.class);
		bind(DiscussionThreadListWidgetView.class).to(DiscussionThreadListWidgetViewImpl.class);
		bind(DiscussionThreadListItemWidgetView.class).to(DiscussionThreadListItemWidgetViewImpl.class);
		bind(SingleDiscussionThreadWidgetView.class).to(SingleDiscussionThreadWidgetViewImpl.class);
		bind(ReplyWidgetView.class).to(ReplyWidgetViewImpl.class);
		bind(ForumWidgetView.class).to(ForumWidgetViewImpl.class);
		bind(NewReplyWidgetView.class).to(NewReplyWidgetViewImpl.class);

		// docker
		bind(DockerRepoListWidgetView.class).to(DockerRepoListWidgetViewImpl.class);
		bind(DockerRepoWidgetView.class).to(DockerRepoWidgetViewImpl.class);
		bind(AddExternalRepoModalView.class).to(AddExternalRepoModalViewImpl.class);
		bind(DockerCommitRowWidgetView.class).to(DockerCommitRowWidgetViewImpl.class);
		bind(DockerCommitListWidgetView.class).to(DockerCommitListWidgetViewImpl.class);

		bind(SessionStorage.class).to(SessionStorageImpl.class);
		bind(SynapseForumView.class).to(SynapseForumViewImpl.class);
		bind(WikiMarkdownEditorView.class).to(WikiMarkdownEditorViewImpl.class);
		bind(StuAlertView.class).to(StuAlertViewImpl.class);

		bind(SynapseTableFormWidgetView.class).to(SynapseTableFormWidgetViewImpl.class);
		bind(RowFormView.class).to(RowFormViewImpl.class);
		bind(RadioCellEditorView.class).to(RadioCellEditorViewImpl.class);

		bind(MarkdownIt.class).to(MarkdownItImpl.class);
		bind(SubscriptionView.class).to(SubscriptionViewImpl.class);
		bind(TopicWidgetView.class).to(TopicWidgetViewImpl.class);
		bind(SubscribeButtonWidgetView.class).to(SubscribeButtonWidgetViewImpl.class);
		bind(SubscriptionListWidgetView.class).to(SubscriptionListWidgetViewImpl.class);
		bind(TopicRowWidgetView.class).to(TopicRowWidgetViewImpl.class);
		bind(RefreshAlertView.class).to(RefreshAlertViewImpl.class);

		bind(UserSelectorView.class).to(UserSelectorViewImpl.class);
		bind(CreateTableViewWizardStep1View.class).to(CreateTableViewWizardStep1ViewImpl.class);
		bind(EntityContainerListWidgetView.class).to(EntityContainerListWidgetViewImpl.class);
		bind(ScopeWidgetView.class).to(ScopeWidgetViewImpl.class);
		bind(CopyTextModal.class).to(CopyTextModalImpl.class);

		bind(EvaluationEditorModalView.class).to(EvaluationEditorModalViewImpl.class);
		bind(LoadMoreWidgetContainerView.class).to(LoadMoreWidgetContainerViewImpl.class);
		bind(RadioWidget.class).to(RadioWidgetViewImpl.class);

		bind(FileClientsHelpView.class).to(FileClientsHelpViewImpl.class);
		bind(ContainerClientsHelp.class).to(ContainerClientsHelpImpl.class);
		bind(FileDownloadMenuItemView.class).to(FileDownloadMenuItemViewImpl.class);
		bind(CreateTableViewWizardStep2View.class).to(CreateTableViewWizardStep2ViewImpl.class);
		bind(ChallengeWidgetView.class).to(ChallengeWidgetViewImpl.class);
		bind(SelectTeamModalView.class).to(SelectTeamModalViewImpl.class);
		bind(ApproveUserAccessModalView.class).to(ApproveUserAccessModalViewImpl.class);
		bind(UserBadgeListView.class).to(UserBadgeListViewImpl.class);
		bind(EntityListRowBadgeView.class).to(EntityListRowBadgeViewImpl.class);

		bind(LazyLoadWikiWidgetWrapperView.class).to(LazyLoadWikiWidgetWrapperViewImpl.class);

		bind(EntityHeaderAsyncHandler.class).to(EntityHeaderAsyncHandlerImpl.class).in(Singleton.class);
		bind(VersionedEntityHeaderAsyncHandler.class).to(VersionedEntityHeaderAsyncHandlerImpl.class).in(Singleton.class);

		bind(GoogleMapView.class).to(GoogleMapViewImpl.class);

		bind(FileHandleAsyncHandler.class).to(FileHandleAsyncHandlerImpl.class).in(Singleton.class);
		bind(PresignedURLAsyncHandler.class).to(PresignedURLAsyncHandlerImpl.class).in(Singleton.class);
		bind(PresignedAndFileHandleURLAsyncHandler.class).to(PresignedAndFileHandleURLAsyncHandlerImpl.class).in(Singleton.class);

		bind(UserProfileAsyncHandler.class).to(UserProfileAsyncHandlerImpl.class).in(Singleton.class);

		bind(TeamAsyncHandler.class).to(TeamAsyncHandlerImpl.class).in(Singleton.class);

		bind(UserGroupHeaderAsyncHandler.class).to(UserGroupHeaderAsyncHandlerImpl.class).in(Singleton.class);

		bind(UserGroupHeaderFromAliasAsyncHandler.class).to(UserGroupHeaderFromAliasAsyncHandlerImpl.class).in(Singleton.class);

		bind(DivView.class).to(DivViewImpl.class);
		bind(FacetColumnResultValuesView.class).to(FacetColumnResultValuesViewImpl.class);

		bind(ViewDefaultColumns.class).in(Singleton.class);
		bind(SubscribersWidgetView.class).to(SubscribersWidgetViewImpl.class);
		bind(PlaceView.class).to(PlaceViewImpl.class);
		bind(ManagedACTAccessRequirementWidgetView.class).to(ManagedACTAccessRequirementWidgetViewImpl.class);
		bind(ACTAccessRequirementWidgetView.class).to(ACTAccessRequirementWidgetViewImpl.class);
		bind(TermsOfUseAccessRequirementWidgetView.class).to(TermsOfUseAccessRequirementWidgetViewImpl.class);
		bind(CreateResearchProjectWizardStep1View.class).to(CreateResearchProjectWizardStep1ViewImpl.class);
		bind(CreateDataAccessSubmissionWizardStep2View.class).to(CreateDataAccessSubmissionWizardStep2ViewImpl.class);
		bind(FileHandleWidgetView.class).to(FileHandleWidgetViewImpl.class);
		bind(CreateAccessRequirementStep1View.class).to(CreateAccessRequirementStep1ViewImpl.class);
		bind(CreateManagedACTAccessRequirementStep2View.class).to(CreateManagedACTAccessRequirementStep2ViewImpl.class);
		bind(CreateBasicAccessRequirementStep2View.class).to(CreateBasicAccessRequirementStep2ViewImpl.class);
		bind(Button.class).to(ButtonImpl.class);

		bind(IsACTMemberAsyncHandler.class).to(IsACTMemberAsyncHandlerImpl.class).in(Singleton.class);

		bind(PopupUtilsView.class).to(PopupUtilsViewImpl.class).in(Singleton.class);
		bind(ProfileCertifiedValidatedView.class).to(ProfileCertifiedValidatedViewImpl.class);
		bind(ACTDataAccessSubmissionsView.class).to(ACTDataAccessSubmissionsViewImpl.class);
		bind(RestrictionWidgetView.class).to(RestrictionWidgetViewImpl.class);
		bind(ACTDataAccessSubmissionWidgetView.class).to(ACTDataAccessSubmissionWidgetViewImpl.class);
		bind(OpenSubmissionWidgetView.class).to(OpenSubmissionWidgetViewImpl.class);
		bind(LockAccessRequirementWidgetView.class).to(LockAccessRequirementWidgetViewImpl.class);
		bind(ImageUploadView.class).to(ImageUploadViewImpl.class);
		bind(RevokeUserAccessModalView.class).to(RevokeUserAccessModalViewImpl.class);
		bind(PlotlyWidgetView.class).to(PlotlyWidgetViewImpl.class);
		bind(PlotlyConfigView.class).to(PlotlyConfigViewImpl.class);

		bind(DateTimeUtils.class).to(DateTimeUtilsImpl.class).in(Singleton.class);
		bind(ACTAccessApprovalsView.class).to(ACTAccessApprovalsViewImpl.class);
		bind(AccessorGroupView.class).to(AccessorGroupViewImpl.class);
		bind(SelfSignAccessRequirementWidgetView.class).to(SelfSignAccessRequirementWidgetViewImpl.class);
		bind(SubjectWidgetView.class).to(SubjectWidgetViewImpl.class);
		bind(AwsLoginView.class).to(AwsLoginViewImpl.class);
		bind(TeamMemberRowWidgetView.class).to(TeamMemberRowWidgetViewImpl.class);
		bind(TeamMembersWidgetView.class).to(TeamMembersWidgetViewImpl.class);
		bind(FileViewClientsHelp.class).to(FileViewClientsHelpImpl.class);
		bind(EmailAddressesWidgetView.class).to(EmailAddressesWidgetViewImpl.class);
		bind(SRCDemoWidgetView.class).to(SRCDemoWidgetViewImpl.class);

		// Synapse js client
		bind(SynapseJavascriptClient.class).in(Singleton.class);
		bind(SynapseJavascriptFactory.class).in(Singleton.class);

		bind(HtmlPreviewView.class).to(HtmlPreviewViewImpl.class);
		bind(S3DirectLoginDialog.class).to(S3DirectLoginDialogImpl.class);
		bind(WikiPageDeleteConfirmationDialogView.class).to(WikiPageDeleteConfirmationDialogViewImpl.class);
		bind(WikiDiffView.class).to(WikiDiffViewImpl.class);
		bind(SynapseProperties.class).to(SynapsePropertiesImpl.class).in(Singleton.class);
		bind(Moment.class).to(MomentImpl.class);
		bind(DownloadSpeedTester.class).to(DownloadSpeedTesterImpl.class);
		bind(PackageSizeSummaryView.class).to(PackageSizeSummaryViewImpl.class);
		bind(DownloadListWidgetView.class).to(DownloadListWidgetViewImpl.class);
		bind(FileHandleAssociationTableView.class).to(FileHandleAssociationTableViewImpl.class);
		bind(FileHandleAssociationRowView.class).to(FileHandleAssociationRowViewImpl.class);
		bind(AddToDownloadListView.class).to(AddToDownloadListViewImpl.class);
		bind(EntityPresenterEventBinder.class).to(EntityPresenterEventBinderImpl.class);
		bind(Linkify.class).to(LinkifyImpl.class);
		bind(PasswordResetSignedTokenView.class).to(PasswordResetSignedTokenViewImpl.class);
		bind(TeamProjectsModalWidgetView.class).to(TeamProjectsModalWidgetViewImpl.class);
		bind(ContainerItemCountWidgetView.class).to(ContainerItemCountWidgetViewImpl.class);
		bind(StatisticsPlotWidgetView.class).to(StatisticsPlotWidgetViewImpl.class);
		bind(QuarantinedEmailModal.class).in(Singleton.class);
	}
}
