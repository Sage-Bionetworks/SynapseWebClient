
package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;
	
public interface SynapseClientAsync {

	void getEntity(String entityId, AsyncCallback<EntityWrapper> callback);
	
	void getEntityForVersion(String entityId, Long versionNumber, AsyncCallback<EntityWrapper> callback);
	
	void getEntityBundle(String entityId, int partsMask, AsyncCallback<EntityBundleTransport> callback);
	
	void getEntityBundleForVersion(String entityId, Long versionNumber, int partsMask, AsyncCallback<EntityBundleTransport> callback);

	void getEntityVersions(String entityId, int offset, int limit, AsyncCallback<String> callback);

	void updateEntity(String entityJson, AsyncCallback<EntityWrapper> callback);

	void getEntityPath(String entityId, AsyncCallback<EntityPath> callback);

	void search(SearchQuery searchQuery, AsyncCallback<SearchResults> callback);

	void junk(SerializableWhitelist l,
			AsyncCallback<SerializableWhitelist> callback);

	void getEntityReferencedBy(String entityId, AsyncCallback<String> callback);

	void logDebug(String message, AsyncCallback<Void> callback);

	void logError(String message, AsyncCallback<Void> callback);
	
	/**
	 * 
	 * @param message 
	 * @param label If a stack trace, should not contain the stack trace message (as it would be too specific)
	 * @param callback
	 */
	void logErrorToRepositoryServices(String message, String label, AsyncCallback<Void> callback);
	
	void logInfo(String message, AsyncCallback<Void> callback);

	void getRepositoryServiceUrl(AsyncCallback<String> callback);

	void createOrUpdateEntity(String entityJson, String annoJson,
			boolean isNew, AsyncCallback<String> callback);
	
	void getEntityTypeBatch(List<String> entityIds, AsyncCallback<String> callback);
	
	void getEntityHeaderBatch(String referenceList,
			AsyncCallback<String> callback);

	void getEntityHeaderBatch(List<String> entityIds, AsyncCallback<ArrayList<EntityHeader>> callback);
	
	void deleteEntityById(String entityId, AsyncCallback<Void> callback);
	
	void deleteEntityById(String entityId, Boolean skipTrashCan, AsyncCallback<Void> callback);

	void deleteEntityVersionById(String entityId, Long versionNumber, AsyncCallback<Void> callback);

	void getUserProfile(AsyncCallback<UserProfile> callback);
	
	void getUserProfile(String userId, AsyncCallback<UserProfile> callback);
	
	void getTeam(String teamId, AsyncCallback<Team> callback);
	
	void getUserGroupHeadersById(ArrayList<String> ids, AsyncCallback<UserGroupHeaderResponsePage> headers);
	
	void updateUserProfile(UserProfile userProfileJson, AsyncCallback<Void> callback);
	
	void getUserGroupHeadersByPrefix(String prefix, long limit, long offset, AsyncCallback<UserGroupHeaderResponsePage> callback);
	
	void createUserProfileAttachmentPresignedUrl(String id, String tokenOrPreviewId, AsyncCallback<String> callback);
	
	void additionalEmailValidation(String userId, String emailAddress, String callbackUrl, AsyncCallback<Void> callback);
	
	void addEmail(String emailValidationToken, AsyncCallback<Void> callback);
	
	void getNotificationEmail(AsyncCallback<String> callback);
	
	void setNotificationEmail(String email, AsyncCallback<Void> callback);
	
	public void getNodeAcl(String id, AsyncCallback<AccessControlList> callback);
	
	public void createAcl(AccessControlList acl, AsyncCallback<AccessControlList> callback);
	
	public void updateAcl(AccessControlList acl, AsyncCallback<AccessControlList> callback);
	
	public void updateAcl(AccessControlList acl, boolean recursive, AsyncCallback<AccessControlList> callback);
	
	public void deleteAcl(String ownerEntityId, AsyncCallback<AccessControlList> callback);

	public void hasAccess(String ownerEntityId, String accessType, AsyncCallback<Boolean> callback);
	
	public void hasAccess(String ownerId, String ownerType, String accessType,AsyncCallback<Boolean> callback);

	public void getAllUsers(AsyncCallback<EntityWrapper> callback);
	
	public void createAccessRequirement(AccessRequirement arEW, AsyncCallback<AccessRequirement> callback);

	public void createLockAccessRequirement(String entityId, AsyncCallback<EntityWrapper> callback);
	
	public void getUnmetAccessRequirements(String entityId, AsyncCallback<AccessRequirementsTransport> callback);
	
	/**
	 * 
	 * @param evalId the evaluation identifier
	 * @param callback returns VariableContentPaginatedResults<AccessRequirement> json
	 */
	public void getUnmetEvaluationAccessRequirements(String evalId, AsyncCallback<String> callback);
	
	public void getTeamAccessRequirements(String teamId, AsyncCallback<List<AccessRequirement>> callback);
	public void getAllEntityUploadAccessRequirements(String entityId, AsyncCallback<String> callback);
	
	public void createAccessApproval(EntityWrapper aaEW, AsyncCallback<EntityWrapper> callback);

	public void updateExternalLocationable(String entityId, String externalUrl, String name, AsyncCallback<EntityWrapper> callback);
	
	public void updateExternalFile(String entityId, String externalUrl, String name, AsyncCallback<EntityWrapper> callback) throws RestServiceException;
	
	public void createExternalFile(String parentEntityId, String externalUrl, String name, AsyncCallback<EntityWrapper> callback) throws RestServiceException;

	public void markdown2Html(String markdown, Boolean isPreview, Boolean isAlpha, String clientHostString, AsyncCallback<String> callback);
	
	void getActivityForEntityVersion(String entityId, Long versionNumber, AsyncCallback<Activity> callback);

	void getActivityForEntity(String entityId, AsyncCallback<Activity> callback);

	void getActivity(String activityId, AsyncCallback<Activity> callback);
	
	void removeAttachmentFromEntity(String entityId, String attachmentName, AsyncCallback<EntityWrapper> callback) throws RestServiceException;
	
	public void getJSONEntity(String repoUri, AsyncCallback<String> callback);
	
	//wiki crud
	public void getWikiHeaderTree(String ownerId, String ownerType, AsyncCallback<String> callback);
	public void getWikiAttachmentHandles(WikiPageKey key, AsyncCallback<String> callback);
	public void getFileEndpoint(AsyncCallback<String> callback);

	 // V2 Wiki crud
    public void createV2WikiPage(String ownerId, String ownerType, String wikiPageJson, AsyncCallback<String> callback);
    public void getV2WikiPage(WikiPageKey key, AsyncCallback<String> callback);
    public void getVersionOfV2WikiPage(WikiPageKey key, Long version, AsyncCallback<String> callback);
    public void updateV2WikiPage(String ownerId, String ownerType, String wikiPageJson, AsyncCallback<String> callback);
    public void restoreV2WikiPage(String ownerId, String ownerType, String wikiId, Long versionToUpdate, AsyncCallback<String> callback);
    public void deleteV2WikiPage(WikiPageKey key, AsyncCallback<Void> callback);
    public void getV2WikiHeaderTree(String ownerId, String ownerType, AsyncCallback<String> callback);
    public void getV2WikiAttachmentHandles(WikiPageKey key, AsyncCallback<String> callback);
    public void getVersionOfV2WikiAttachmentHandles(WikiPageKey key, Long version, AsyncCallback<String> callback);
    public void getV2WikiHistory(WikiPageKey key, Long limit, Long offset, AsyncCallback<String> callback);

	public void getMarkdown(WikiPageKey key, AsyncCallback<String> callback);
	public void getVersionOfMarkdown(WikiPageKey key, Long version, AsyncCallback<String> callback);
	public void zipAndUploadFile(String content, String fileName, AsyncCallback<String> callback);
	
	public void createV2WikiPageWithV1(String ownerId, String ownerType, String wikiPageJson, AsyncCallback<String> callback);
	public void updateV2WikiPageWithV1(String ownerId, String ownerType, String wikiPageJson, AsyncCallback<String> callback);
	public void getV2WikiPageAsV1(WikiPageKey key, AsyncCallback<WikiPage> callback);
	public void getVersionOfV2WikiPageAsV1(WikiPageKey key, Long version, AsyncCallback<WikiPage> callback);
	
	public void getPlainTextWikiPage(WikiPageKey key, AsyncCallback<String> callback);	
	
	void getEntitiesGeneratedBy(String activityId, Integer limit, Integer offset, AsyncCallback<String> callback);

	void addFavorite(String entityId, AsyncCallback<String> callback);

	void removeFavorite(String entityId, AsyncCallback<Void> callback);

	void getFavorites(Integer limit, Integer offset, AsyncCallback<String> callback);
	
	/**
	 * TEAMS
	 */
	/////////////////
	void createTeam(String teamName,AsyncCallback<String> callback);
	void deleteTeam(String teamId,AsyncCallback<Void> callback);
	void getTeams(String userId, Integer limit, Integer offset, AsyncCallback<String> callback);
	void getTeamsForUser(String userId, AsyncCallback<ArrayList<String>> callback);
	void getTeamsBySearch(String searchTerm, Integer limit, Integer offset, AsyncCallback<String> callback);
	void getTeamBundle(String userId, String teamId, boolean isLoggedIn, AsyncCallback<TeamBundle> callback);
	void getOpenRequestCount(String currentUserId, String teamId, AsyncCallback<Long> callback);

	void getOpenInvitations(String userId, AsyncCallback<ArrayList<MembershipInvitationBundle>> callback);
	void getOpenTeamInvitations(String teamId, Integer limit, Integer offset, AsyncCallback<ArrayList<MembershipInvitationBundle>> callback);
	void getOpenRequests(String teamId, AsyncCallback<ArrayList<MembershipRequestBundle>> callback);
	void deleteMembershipInvitation(String invitationId, AsyncCallback<Void> callback);
	void updateTeam(String teamJson, AsyncCallback<String> callback);
	void deleteTeamMember(String currentUserId, String targetUserId, String teamId, AsyncCallback<Void> callback);
	void setIsTeamAdmin(String currentUserId, String targetUserId, String teamId, boolean isTeamAdmin, AsyncCallback<Void> callback);
	void getTeamMembers(String teamId, String fragment, Integer limit, Integer offset, AsyncCallback<String> callback);	
//	void getTeamMembershipState(String currentUserId, String teamId, AsyncCallback<String> callback);
	void requestMembership(String currentUserId, String teamId, String message, AsyncCallback<Void> callback);
	
	void deleteOpenMembershipRequests(String currentUserId, String teamId, AsyncCallback<Void> callback);
	void inviteMember(String userGroupId, String teamId, String message, AsyncCallback<Void> callback);
	/////////////////
	
	/**
	 * The PassingRecord that documents when a user was certified is returned.  Otherwise, a NotFoundException is thrown.
	 * @param userId
	 * @param callback
	 */
	void getCertifiedUserPassingRecord(String userId, AsyncCallback<String> callback);
	void getCertificationQuiz(AsyncCallback<String> callback);
	void submitCertificationQuizResponse(String quizResponseJson, AsyncCallback<String> callback);
	
	void getFavoritesList(Integer limit, Integer offset, AsyncCallback<ArrayList<String>> callback);

	void getDescendants(String nodeId, int pageSize, String lastDescIdExcl, AsyncCallback<String> callback);
	void getChunkedFileToken(String fileName,  String contentType, String contentMD5, AsyncCallback<ChunkedFileToken> callback) throws RestServiceException;
	void getChunkedPresignedUrl(ChunkRequest request, AsyncCallback<String> callback) throws RestServiceException;
	void combineChunkedFileUpload(List<ChunkRequest> requests, AsyncCallback<UploadDaemonStatus> callback) throws RestServiceException;
	void getUploadDaemonStatus(String daemonId,AsyncCallback<UploadDaemonStatus> callback) throws RestServiceException;
	void getFileEntityIdWithSameName(String fileName, String parentEntityId, AsyncCallback<String> callback);
	void setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId, AsyncCallback<String> callback) throws RestServiceException;
	
	
	void getEntityDoi(String entityId, Long versionNumber, AsyncCallback<String> callback);
	void createDoi(String entityId, Long versionNumber, AsyncCallback<Void> callback);

	void getFileEntityTemporaryUrlForVersion(String entityId, Long versionNumber, AsyncCallback<String> callback);
	void getEvaluations(List<String> evaluationIds, AsyncCallback<String> callback) throws RestServiceException;
	void getAvailableEvaluations(AsyncCallback<String> callback) throws RestServiceException;
	void getAvailableEvaluations(Set<String> targetEvaluationIds, AsyncCallback<String> callback) throws RestServiceException;
	void getSharableEvaluations(String entityId, AsyncCallback<ArrayList<String>> callback);
	
	/**
	 * Create a new Submission object.  Callback returning the updated version of the Submission object
	 * @param submissionJson
	 * @param etag
	 * @param callback
	 */
	void createSubmission(Submission submission, String etag, AsyncCallback<Submission> callback) throws RestServiceException;
	
	
	void getUserEvaluationPermissions(String evalId, AsyncCallback<String> callback); 
	void getEvaluationAcl(String evalId, AsyncCallback<String> callback);
	void updateEvaluationAcl(String aclJson, AsyncCallback<String> callback);
	
	
	/**
	 * Get all unique submission user aliases associated to the available evaluations (OPEN evaluations that the current user has joined).
	 * The return list is sorted by Submission created date.
	 * @param callback
	 * @throws RestServiceException
	 */
	void getAvailableEvaluationsSubmitterAliases(AsyncCallback<String> callback) throws RestServiceException;

	/**
	 * Return true if the current user has created at least one submission in the given evaluations
	 * @param evaluationIds
	 * @param callback
	 * @throws RestServiceException
	 */
	void hasSubmitted(AsyncCallback<Boolean> callback)	throws RestServiceException;
	
	void getSynapseVersions(AsyncCallback<String> callback);

	void getSynapseProperties(AsyncCallback<HashMap<String, String>> callback);

	void getAPIKey(AsyncCallback<String> callback);

	void getColumnModelsForTableEntity(String tableEntityId, AsyncCallback<List<String>> asyncCallback);

	void createColumnModel(String columnModelJson, AsyncCallback<String> callback);
	
	void sendMessage(Set<String> recipients, String subject, String message, AsyncCallback<String> callback);
	
	void isAliasAvailable(String alias, String aliasType, AsyncCallback<Boolean> callback);

	void sendRowsToTable(String rowSet, AsyncCallback<String> callback);
	
	void getHelpPages(AsyncCallback<HashMap<String, WikiPageKey>> callback);

	void deleteApiKey(AsyncCallback<String> callback);

	void deleteRowsFromTable(String toDelete, AsyncCallback<String> callback);

	void getTableFileHandle(String fileHandlesToFindRowReferenceSet, AsyncCallback<String> callback);
	
	/**
	 * Set a table's schema. Any ColumnModel that does not have an ID will be
	 * treated as a column add.
	 * 
	 * @param entity
	 * @param newSchema
	 * @param callback
	 */
	void setTableSchema(TableEntity entity, List<ColumnModel> newSchema,
			AsyncCallback<Void> callback);
	
	/**
	 * Apply a PartialRowSet to a table.
	 * @param deltaJson
	 * @param callback
	 */
	void applyTableDelta(PartialRowSet delta, AsyncCallback<Void> callback);
	
	/**
	 * Validate a table query.
	 * @param sql
	 * @param callback
	 */
	void validateTableQuery(String sql, AsyncCallback<Void> callback);

	void purgeTrashForUser(String entityId, AsyncCallback<Void> callback);
	
	void purgeTrashForUser(AsyncCallback<Void> callback);

	void moveToTrash(String entityId, AsyncCallback<Void> callback);

	void restoreFromTrash(String entityId, String newParentId, AsyncCallback<Void> callback);
	
	void viewTrashForUser(long offset, long limit,
			AsyncCallback<String> callback);

	void purgeMultipleTrashedEntitiesForUser(Set<String> entityIds, AsyncCallback<Void> callback);

	void startAsynchJob(AsynchType type, AsynchronousRequestBody body,
			AsyncCallback<String> callback);

	void getAsynchJobResults(AsynchType type, String jobId,
			AsyncCallback<AsynchronousResponseBody> callback);

	void executeEntityQuery(EntityQuery query,
			AsyncCallback<EntityQueryResults> callback);

	void createTableEntity(TableEntity entity,
			AsyncCallback<TableEntity> callback);

	void getFileHandle(String fileHandleId, AsyncCallback<FileHandle> callback);
	
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

	void getMyProjects(int limit, int offset, AsyncCallback<ProjectPagedResults> projectHeaders);
	void getUserProjects(String userId, int limit, int offset, AsyncCallback<ProjectPagedResults> projectHeaders);
}
