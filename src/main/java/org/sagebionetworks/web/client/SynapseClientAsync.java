
package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.table.QueryDetails;
import org.sagebionetworks.web.shared.table.QueryResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
	
public interface SynapseClientAsync {

	void getEntity(String entityId, AsyncCallback<EntityWrapper> callback);
	
	void getEntityForVersion(String entityId, Long versionNumber, AsyncCallback<EntityWrapper> callback);
	
	void getEntityBundle(String entityId, int partsMask, AsyncCallback<EntityBundleTransport> callback);
	
	void getEntityBundleForVersion(String entityId, Long versionNumber, int partsMask, AsyncCallback<EntityBundleTransport> callback);

	void getEntityVersions(String entityId, int offset, int limit, AsyncCallback<String> callback);

	void updateEntity(String entityJson, AsyncCallback<EntityWrapper> callback);

	void getEntityPath(String entityId, AsyncCallback<EntityWrapper> callback);

	void search(String searchQueryJson, AsyncCallback<EntityWrapper> callback);

	void junk(SerializableWhitelist l,
			AsyncCallback<SerializableWhitelist> callback);

	void getEntityReferencedBy(String entityId, AsyncCallback<String> callback);

	void logDebug(String message, AsyncCallback<Void> callback);

	void logError(String message, AsyncCallback<Void> callback);

	void logInfo(String message, AsyncCallback<Void> callback);

	void getRepositoryServiceUrl(AsyncCallback<String> callback);

	void createOrUpdateEntity(String entityJson, String annoJson,
			boolean isNew, AsyncCallback<String> callback);
	
	void getEntityTypeBatch(List<String> entityIds, AsyncCallback<String> callback);
	
	void getEntityHeaderBatch(String referenceList,
			AsyncCallback<String> callback);

	void getEntityHeaderBatch(List<String> entityIds, AsyncCallback<List<String>> callback);
	
	void deleteEntityById(String entityId, AsyncCallback<Void> callback);
	
	void deleteEntityById(String entityId, Boolean skipTrashCan, AsyncCallback<Void> callback);

	void deleteEntityVersionById(String entityId, Long versionNumber, AsyncCallback<Void> callback);

	void getUserProfile(AsyncCallback<String> callback);
	
	void getUserProfile(String userId, AsyncCallback<String> callback);
	
	void getTeam(String teamId, AsyncCallback<String> callback);
	
	void getUserGroupHeadersById(List<String> ids, AsyncCallback<EntityWrapper> headers);
	
	void updateUserProfile(String userProfileJson, AsyncCallback<Void> callback);
	
	void createUserProfileAttachmentPresignedUrl(String id, String tokenOrPreviewId, AsyncCallback<String> callback);
	
	void additionalEmailValidation(String userId, String emailAddress, String callbackUrl, AsyncCallback<Void> callback);
	
	void addEmail(String emailValidationToken, AsyncCallback<Void> callback);
	
	void getNotificationEmail(AsyncCallback<String> callback);
	
	void setNotificationEmail(String email, AsyncCallback<Void> callback);
	
	public void getNodeAcl(String id, AsyncCallback<EntityWrapper> callback);
	
	public void createAcl(EntityWrapper acl, AsyncCallback<EntityWrapper> callback);
	
	public void updateAcl(EntityWrapper acl, AsyncCallback<EntityWrapper> callback);
	
	public void updateAcl(EntityWrapper acl, boolean recursive, AsyncCallback<EntityWrapper> callback);
	
	public void deleteAcl(String ownerEntityId, AsyncCallback<EntityWrapper> callback);

	public void hasAccess(String ownerEntityId, String accessType, AsyncCallback<Boolean> callback);
	
	public void hasAccess(String ownerId, String ownerType, String accessType,AsyncCallback<Boolean> callback);

	public void getAllUsers(AsyncCallback<EntityWrapper> callback);
	
	public void createAccessRequirement(EntityWrapper arEW, AsyncCallback<EntityWrapper> callback);

	public void createLockAccessRequirement(String entityId, AsyncCallback<EntityWrapper> callback);
	
	public void getUnmetAccessRequirements(String entityId, AsyncCallback<AccessRequirementsTransport> callback);
	
	/**
	 * 
	 * @param evalId the evaluation identifier
	 * @param callback returns VariableContentPaginatedResults<AccessRequirement> json
	 */
	public void getUnmetEvaluationAccessRequirements(String evalId, AsyncCallback<String> callback);
	
	public void getUnmetTeamAccessRequirements(String teamId, AsyncCallback<String> callback);
	public void getTeamAccessRequirements(String teamId, AsyncCallback<String> callback);
	public void getAllEntityUploadAccessRequirements(String entityId, AsyncCallback<String> callback);
	
	public void createAccessApproval(EntityWrapper aaEW, AsyncCallback<EntityWrapper> callback);

	public void updateExternalLocationable(String entityId, String externalUrl, String name, AsyncCallback<EntityWrapper> callback);
	
	public void updateExternalFile(String entityId, String externalUrl, String name, AsyncCallback<EntityWrapper> callback) throws RestServiceException;
	
	public void createExternalFile(String parentEntityId, String externalUrl, String name, AsyncCallback<EntityWrapper> callback) throws RestServiceException;

	public void markdown2Html(String markdown, Boolean isPreview, Boolean isAlpha, String clientHostString, AsyncCallback<String> callback);
	
	void getActivityForEntityVersion(String entityId, Long versionNumber, AsyncCallback<String> callback);

	void getActivityForEntity(String entityId, AsyncCallback<String> callback);

	void getActivity(String activityId, AsyncCallback<String> callback);
	
	void removeAttachmentFromEntity(String entityId, String attachmentName, AsyncCallback<EntityWrapper> callback) throws RestServiceException;
	
	public void getJSONEntity(String repoUri, AsyncCallback<String> callback);
	
	//wiki crud
	public void createWikiPage(String ownerId, String ownerType, String wikiPageJson, AsyncCallback<String> callback);
	public void getWikiPage(WikiPageKey key, AsyncCallback<String> callback);
	public void updateWikiPage(String ownerId, String ownerType, String wikiPageJson, AsyncCallback<String> callback);
	public void deleteWikiPage(WikiPageKey key, AsyncCallback<Void> callback);
	
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
	public void getV2WikiPageAsV1(WikiPageKey key, AsyncCallback<String> callback);
	public void getVersionOfV2WikiPageAsV1(WikiPageKey key, Long version, AsyncCallback<String> callback);
	
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

	void getOpenInvitations(String userId, AsyncCallback<List<MembershipInvitationBundle>> callback);
	void getOpenTeamInvitations(String teamId, Integer limit, Integer offset, AsyncCallback<List<MembershipInvitationBundle>> callback);
	void getOpenRequests(String teamId, AsyncCallback<List<MembershipRequestBundle>> callback);
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
	void getChunkedFileToken(String fileName,  String contentType, String contentMD5, AsyncCallback<String> callback) throws RestServiceException;
	void getChunkedPresignedUrl(String requestJson, AsyncCallback<String> callback) throws RestServiceException;
	void combineChunkedFileUpload(List<String> requests, AsyncCallback<String> callback) throws RestServiceException;
	void getUploadDaemonStatus(String daemonId,AsyncCallback<String> callback) throws RestServiceException;
	void getFileEntityIdWithSameName(String fileName, String parentEntityId, AsyncCallback<String> callback);
	void setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId, boolean isRestricted,AsyncCallback<String> callback) throws RestServiceException;
	
	
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
	void createSubmission(String submissionJson, String etag, AsyncCallback<String> callback) throws RestServiceException;
	
	
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

	/**
	 * Return a property from portal.properties.  Returns null if the property key is not defined
	 * @param callback
	 */
	void getSynapseProperty(String key, AsyncCallback<String> callback);

	void getAPIKey(AsyncCallback<String> callback);

	void getColumnModelsForTableEntity(String tableEntityId, AsyncCallback<List<String>> asyncCallback);

	void createColumnModel(String columnModelJson, AsyncCallback<String> callback);
	
	void sendMessage(Set<String> recipients, String subject, String message, AsyncCallback<String> callback);
	
	void isAliasAvailable(String alias, String aliasType, AsyncCallback<Boolean> callback);

	void executeTableQuery(String query, QueryDetails modifyingQueryDetails, boolean includeTotalRowCount, AsyncCallback<QueryResult> callback);

	void sendRowsToTable(String rowSet, AsyncCallback<String> callback);
	
	void getHelpPages(AsyncCallback<HashMap<String, WikiPageKey>> callback);

	void deleteApiKey(AsyncCallback<String> callback);

	void deleteRowsFromTable(String toDelete, AsyncCallback<String> callback);

	void getTableFileHandle(String fileHandlesToFindRowReferenceSet, AsyncCallback<String> callback);
	
	/**
	 * Set a table's schema. Any ColumnModel that does not have an ID will be
	 * treated as a column add.
	 * 
	 * @param tableId
	 * @param schemaJSON
	 * @param callback
	 */
	void setTableSchema(String tableId, List<String> schemaJSON,
			AsyncCallback<List<String>> callback);

	void purgeTrashForUser(String entityId, AsyncCallback<Void> callback);
	
	void purgeTrashForUser(AsyncCallback<Void> callback);

	void moveToTrash(String entityId, AsyncCallback<Void> callback);

	void restoreFromTrash(String entityId, String newParentId, AsyncCallback<Void> callback);
	
	void viewTrashForUser(long offset, long limit,
			AsyncCallback<String> callback);

	void purgeMultipleTrashedEntitiesForUser(Set<String> entityIds, AsyncCallback<Void> callback);

}
