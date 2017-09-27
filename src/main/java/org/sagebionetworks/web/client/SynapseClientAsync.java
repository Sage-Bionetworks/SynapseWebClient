
package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.file.BatchFileRequest;
import org.sagebionetworks.repo.model.file.BatchFileResult;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandleCopyRequest;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.subscription.Etag;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnModelPage;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.view.TeamRequestBundle;
import org.sagebionetworks.web.shared.EntityBundlePlus;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.OpenTeamInvitationBundle;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;
	
public interface SynapseClientAsync {
	void getEntityBundlePlusForVersion(String entityId, Long versionNumber, int partsMask, AsyncCallback<EntityBundlePlus> callback);
	
	void getEntityVersions(String entityId, int offset, int limit,
			AsyncCallback<PaginatedResults<VersionInfo>> callback);

	void search(SearchQuery searchQuery, AsyncCallback<SearchResults> callback);
	
	void logDebug(String message, AsyncCallback<Void> callback);

	void logError(String message, AsyncCallback<Void> callback);
	
	/**
	 * 
	 * @param message
	 * @param exceptionType TODO
	 * @param t
	 * @param callback
	 */
	void logErrorToRepositoryServices(String message, String exceptionType, String exceptionMessage, StackTraceElement[] t, AsyncCallback<Void> callback);
	
	void logInfo(String message, AsyncCallback<Void> callback);

	void getRepositoryServiceUrl(AsyncCallback<String> callback);

	void createOrUpdateEntity(Entity entity, Annotations annos,
			boolean isNew, AsyncCallback<String> callback);
	
	void getEntityHeaderBatch(ReferenceList referenceList,
			AsyncCallback<PaginatedResults<EntityHeader>> callback);

	void getEntityHeaderBatch(List<String> entityIds, AsyncCallback<ArrayList<EntityHeader>> callback);
	
	void deleteEntityById(String entityId, AsyncCallback<Void> callback);
	
	void deleteEntityById(String entityId, Boolean skipTrashCan, AsyncCallback<Void> callback);

	void deleteEntityVersionById(String entityId, Long versionNumber, AsyncCallback<Void> callback);

	void getUserProfile(String userId, AsyncCallback<UserProfile> callback);
	
	void updateUserProfile(UserProfile userProfileJson, AsyncCallback<Void> callback);
	
	void additionalEmailValidation(String userId, String emailAddress, String callbackUrl, AsyncCallback<Void> callback);
	
	void addEmail(EmailValidationSignedToken emailValidationSignedToken, AsyncCallback<Void> callback);
	
	void getNotificationEmail(AsyncCallback<String> callback);
	
	void setNotificationEmail(String email, AsyncCallback<Void> callback);
	void removeEmail(String email, AsyncCallback<Void> callback);
	
	public void getEntityBenefactorAcl(String id, AsyncCallback<AccessControlList> callback);
	
	public void createAcl(AccessControlList acl, AsyncCallback<AccessControlList> callback);
	
	public void updateAcl(AccessControlList acl, boolean recursive, AsyncCallback<AccessControlList> callback);
	
	public void getTeamAcl(String teamId, AsyncCallback<AccessControlList> callback);
	
	public void deleteAcl(String ownerEntityId, AsyncCallback<AccessControlList> callback);

	public void hasAccess(String ownerId, String ownerType, String accessType,AsyncCallback<Boolean> callback);
	
	void createOrUpdateAccessRequirement(AccessRequirement arEW,
			AsyncCallback<AccessRequirement> callback);

	public void getTeamAccessRequirements(String teamId, AsyncCallback<List<AccessRequirement>> callback);
	
	void createAccessApproval(AccessApproval aaEW,
			AsyncCallback<AccessApproval> callback);
	
	public void updateExternalFile(String entityId, String externalUrl, String name, String contentType, Long fileSize, String md5, Long storageLocationId, AsyncCallback<Entity> callback) throws RestServiceException;
	
	public void createExternalFile(String parentEntityId, String externalUrl, String name, String contentType, Long fileSize, String md5, Long storageLocationId, AsyncCallback<Entity> callback) throws RestServiceException;

	void getActivityForEntityVersion(String entityId, Long versionNumber, AsyncCallback<Activity> callback);

	void getActivityForEntity(String entityId, AsyncCallback<Activity> callback);

	void getActivity(String activityId, AsyncCallback<Activity> callback);
		
	public void getJSONEntity(String repoUri, AsyncCallback<String> callback);
	
	public void getRootWikiId(String ownerId, String ownerType, AsyncCallback<String> callback);
	public void getWikiAttachmentHandles(WikiPageKey key, AsyncCallback<FileHandleResults> callback);
	
	void restoreV2WikiPage(String ownerId, String ownerType, String wikiId,
			Long versionToUpdate, AsyncCallback<V2WikiPage> callback);
    public void deleteV2WikiPage(WikiPageKey key, AsyncCallback<Void> callback);
    void getV2WikiHeaderTree(String ownerId, String ownerType,
			AsyncCallback<List<V2WikiHeader>> callback);
    public void getV2WikiOrderHint(WikiPageKey key, AsyncCallback<V2WikiOrderHint> callback);
    public void updateV2WikiOrderHint(V2WikiOrderHint toUpdate, AsyncCallback<V2WikiOrderHint> callback);
    void getV2WikiAttachmentHandles(WikiPageKey key,
			AsyncCallback<FileHandleResults> callback);
    void getV2WikiHistory(WikiPageKey key, Long limit, Long offset,
			AsyncCallback<PaginatedResults<V2WikiHistorySnapshot>> callback);

	public void createV2WikiPageWithV1(String ownerId, String ownerType, WikiPage wikiPage, AsyncCallback<WikiPage> callback);
	public void updateV2WikiPageWithV1(String ownerId, String ownerType, WikiPage wikiPage, AsyncCallback<WikiPage> callback);
	
	void getEntitiesGeneratedBy(String activityId, Integer limit,
			Integer offset, AsyncCallback<PaginatedResults<Reference>> callback);

	void addFavorite(String entityId, AsyncCallback<EntityHeader> callback);

	void removeFavorite(String entityId, AsyncCallback<Void> callback);
	
	/**
	 * TEAMS
	 */
	/////////////////
	void createTeam(String teamName,AsyncCallback<String> callback);
	void deleteTeam(String teamId,AsyncCallback<Void> callback);
	void getTeams(String userId, Integer limit, Integer offset,
			AsyncCallback<PaginatedResults<Team>> callback);
	void getTeamsBySearch(String searchTerm, Integer limit, Integer offset,
			AsyncCallback<PaginatedResults<Team>> callback);
	void getTeamBundle(String userId, String teamId, boolean isLoggedIn, AsyncCallback<TeamBundle> callback);
	void getOpenRequestCount(String currentUserId, String teamId, AsyncCallback<Long> callback);
	void getTeamsForUser(String userId, boolean includeRequestCount,
			AsyncCallback<List<TeamRequestBundle>> asyncCallback);
	void getOpenInvitations(String userId, AsyncCallback<ArrayList<OpenUserInvitationBundle>> callback);
	void getOpenTeamInvitations(String teamId, Integer limit, Integer offset, AsyncCallback<ArrayList<OpenTeamInvitationBundle>> callback);
	void getOpenRequests(String teamId, AsyncCallback<List<MembershipRequestBundle>> callback);
	void deleteMembershipInvitation(String invitationId, AsyncCallback<Void> callback);
	void updateTeam(Team team, AccessControlList teamAcl, AsyncCallback<Team> callback);
	void deleteTeamMember(String currentUserId, String targetUserId, String teamId, AsyncCallback<Void> callback);
	void getTeamMembers(String teamId, String fragment, Integer limit, Integer offset, AsyncCallback<TeamMemberPagedResults> callback);	
	void getTeamMemberCount(String teamId, AsyncCallback<Long> callback);
	void requestMembership(String currentUserId, String teamId, String message, String hostPageBaseURL, Date expiresOn, AsyncCallback<TeamMembershipStatus> callback);
	
	void deleteOpenMembershipRequests(String currentUserId, String teamId, AsyncCallback<Void> callback);
	void inviteMember(String userGroupId, String teamId, String message, String hostPageBaseURL, AsyncCallback<Void> callback);
	/////////////////
	
	/**
	 * The PassingRecord that documents when a user was certified is returned.  Otherwise, a NotFoundException is thrown.
	 * @param userId
	 * @param callback
	 */
	void getCertifiedUserPassingRecord(String userId, AsyncCallback<String> callback);
	void getCertificationQuiz(AsyncCallback<String> callback);
	void submitCertificationQuizResponse(QuizResponse response,
			AsyncCallback<PassingRecord> callback);
	
	void getFileEntityIdWithSameName(String fileName, String parentEntityId, AsyncCallback<String> callback);
	void setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId, AsyncCallback<String> callback) throws RestServiceException;
	
	
	void getEntityDoi(String entityId, Long versionNumber,
			AsyncCallback<Doi> callback);
	void createDoi(String entityId, Long versionNumber, AsyncCallback<Void> callback);
	
	void getSynapseVersions(AsyncCallback<String> callback);

	void getSynapseProperties(AsyncCallback<HashMap<String, String>> callback);

	void handleSignedToken(SignedTokenInterface signedToken, String hostPageBaseURL, AsyncCallback<ResponseMessage> callback);
	
	void hexDecodeAndDeserialize(String tokenTypeName, String signedTokenString, AsyncCallback<SignedTokenInterface> callback);

	void hexDecodeAndDeserializeAccountCreationToken(String tokenString, AsyncCallback<AccountCreationToken> callback);

	void getAPIKey(AsyncCallback<String> callback);

	void getColumnModelsForTableEntity(String tableEntityId, AsyncCallback<List<ColumnModel>> asyncCallback);

	void createColumnModel(String columnModelJson, AsyncCallback<String> callback);
	
	void sendMessage(Set<String> recipients, String subject, String message, String hostPageBaseURL, AsyncCallback<String> callback);
	void sendMessageToEntityOwner(String entityId, String subject, String messageBody, String hostPageBaseURL, AsyncCallback<String> callback);
	
	void isAliasAvailable(String alias, String aliasType, AsyncCallback<Boolean> callback);

	void getPageNameToWikiKeyMap(AsyncCallback<HashMap<String, WikiPageKey>> callback);

	void deleteApiKey(AsyncCallback<String> callback);
	
	/**
	 * Set a table's schema. Creates any necessary ColumnModels (for create and update), and figures out necessary ColumnChanges to transform oldSchema into newSchema.
	 * 
	 * @param tableId
	 * @param oldSchema
	 * @param newSchema
	 * @param callback
	 */
	void getTableUpdateTransactionRequest(String tableId, List<ColumnModel> oldSchema, List<ColumnModel> newSchema,
			AsyncCallback<TableUpdateTransactionRequest> callback);
	
	/**
	 * Validate a table query.
	 * @param sql
	 * @param callback
	 */
	void validateTableQuery(String sql, AsyncCallback<Void> callback);
	/**
	 * For the given table SQL toggle the sort on the given column and return the modified SQL.
	 * @param sql
	 * @param header
	 * @param callback
	 */
	void toggleSortOnTableQuery(String sql, String header, AsyncCallback<String> callback);
	
	/**
	 * Parse the query and determine the sort columns.
	 * @param sql
	 * @param callback
	 */
	void getSortFromTableQuery(String sql, AsyncCallback<List<SortItem>> callback);

	void purgeTrashForUser(AsyncCallback<Void> callback);

	void restoreFromTrash(String entityId, String newParentId, AsyncCallback<Void> callback);
	
	void viewTrashForUser(long offset, long limit,
			AsyncCallback<PaginatedResults<TrashedEntity>> callback);

	void purgeMultipleTrashedEntitiesForUser(Set<String> entityIds, AsyncCallback<Void> callback);

	void startAsynchJob(AsynchType type, AsynchronousRequestBody body, AsyncCallback<String> callback);

	void getAsynchJobResults(AsynchType type, String jobId, AsynchronousRequestBody body,
			AsyncCallback<AsynchronousResponseBody> callback);

	void createEntity(Entity entity,
			AsyncCallback<Entity> callback);

	void createFileHandleURL(String fileHandleId, AsyncCallback<String> callback);

	void createTableColumns(List<ColumnModel> value,
			AsyncCallback<List<ColumnModel>> asyncCallback);
	
	/**
	 * Return the upload destinations associated with this parent entity (container)
	 * @param parentEntityId
	 * @return
	 * @throws RestServiceException
	 */
	void getUploadDestinations(String parentEntityId, AsyncCallback<List<UploadDestination>> callback);

	/**
	 * Return all projects that the current user can access, sorted by access time
	 * @param limit
	 * @param offset
	 * @param projectHeaders
	 */
	void getMyProjects(ProjectListType projectListType, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<ProjectPagedResults> projectHeaders);
	/**
	 * Return projects that the current user can access due to being on a particular team. 
	 * @param teamId
	 * @param limit
	 * @param offset
	 * @param projectHeaders
	 */
	void getProjectsForTeam(String teamId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<ProjectPagedResults> projectHeaders);
	void getUserProjects(String userId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir, AsyncCallback<ProjectPagedResults> projectHeaders);
	
	void getHost(String urlString, AsyncCallback<String> callback);

	void updateEntity(Entity toUpdate, AsyncCallback<Entity> callback);
	void updateFileEntity(FileEntity toUpdate, FileHandleCopyRequest copyRequest, AsyncCallback<Entity> callback);
	
	void moveEntity(String entityId, String newParentEntityId, AsyncCallback<Entity> callback);
	
	void updateAnnotations(String entityId, Annotations annotations, AsyncCallback<Void> callback);

	void getOrCreateActivityForEntityVersion(String entityId,
			Long versionNumber, AsyncCallback<Activity> callback);

	void putActivity(Activity update, AsyncCallback<Void> callback);

	void createStorageLocationSetting(String parentEntityId, StorageLocationSetting setting, AsyncCallback<Void> callback);

	void getStorageLocationSetting(String parentEntityId, AsyncCallback<StorageLocationSetting> callback);

	void getMyLocationSettingBanners(AsyncCallback<List<String>> callback);

	void hexDecodeLogEntry(String encodedLogEntry,
			AsyncCallback<LogEntry> callback);

	void isTeamMember(String userId, Long groupPrincipalId, AsyncCallback<Boolean> callback);
	
	void setIsTeamAdmin(String currentUserId, String targetUserId,
			String teamId, boolean isTeamAdmin, AsyncCallback<Void> callback);

	void getUserIdFromUsername(String username, AsyncCallback<String> callback);
	void getUserProfileFromUsername(String username, AsyncCallback<UserProfile> callback);

	void getEtag(String objectId, ObjectType objectType, AsyncCallback<Etag> callback);

	void getDefaultColumnsForView(ViewType type, AsyncCallback<List<ColumnModel>> callback);

	void getFileHandleAndUrlBatch(BatchFileRequest request, AsyncCallback<BatchFileResult> asyncCallback);
	
	void deleteAccessRequirement(Long accessRequirementId, AsyncCallback<Void> callback);
	
	void deleteAccessApprovals(String accessRequirement, String accessorId, AsyncCallback<Void> asyncCallback);

	void generateSqlWithFacets(String basicSql, List<FacetColumnRequest> selectedFacets, List<ColumnModel> schema,
			AsyncCallback<String> callback);

	void getPossibleColumnModelsForViewScope(ViewScope scope, String nextPageToken,
			AsyncCallback<ColumnModelPage> callback);

	void isUserAllowedToRenderHTML(String userId, AsyncCallback<Boolean> callback);
	
	void isWiki(String id, AsyncCallback<Boolean> callback);

	void isChallenge(String id, AsyncCallback<Boolean> callback);

	void addTeamMember(String userGroupId, String teamId, String message, String hostPageBaseURL,
			AsyncCallback<Void> callback);

	/**
	 * If successful, will return the new file handle ID
	 * @param fileHandle
	 * @param callback
	 */
	void createExternalObjectStoreFileHandle(ExternalObjectStoreFileHandle fileHandle, AsyncCallback<String> callback);
}