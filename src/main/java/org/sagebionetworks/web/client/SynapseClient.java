
package org.sagebionetworks.web.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.client.exceptions.SynapseException;
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
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("synapse")	
public interface SynapseClient extends RemoteService {

	public EntityWrapper getEntity(String entityId) throws RestServiceException;
	
	public EntityWrapper getEntityForVersion(String entityId, Long versionNumber) throws RestServiceException;
		
	public String getEntityVersions(String entityId, int offset, int limit) throws RestServiceException;

	public void deleteEntityById(String entityId) throws RestServiceException;
	
	public void deleteEntityById(String entityId, Boolean skipTrashCan) throws RestServiceException;
	
	public void deleteEntityVersionById(String entityId, Long versionNumber) throws RestServiceException;
	
	public void moveToTrash(String entityId) throws RestServiceException;

	public void restoreFromTrash(String entityId, String newParentId) throws RestServiceException;

	public String viewTrashForUser(long offset, long limit) throws RestServiceException;
	
	public void purgeTrashForUser() throws RestServiceException;

	public void purgeTrashForUser(String entityId) throws RestServiceException;
	
	public void purgeMultipleTrashedEntitiesForUser(Set<String> entityIds) throws RestServiceException;
	
	public EntityWrapper getEntityPath(String entityId) throws RestServiceException;
	
	public EntityWrapper search(String searchQueryJson) throws RestServiceException; 
	
	public String getEntityTypeBatch(List<String> entityIds) throws RestServiceException;
	
	public String getEntityHeaderBatch(String referenceList) throws RestServiceException;
	
	public List<String> getEntityHeaderBatch(List<String> entityIds) throws RestServiceException;
	
	public SerializableWhitelist junk(SerializableWhitelist l);
	
	/**
	 * Updates the entity in the repo  
	 * @param entityJson - the JSON string representing the entity
	 * @return the updated version of the entity
	 * @throws RestServiceException
	 */
	public EntityWrapper updateEntity(String entityJson) throws RestServiceException;
	
	/**
	 * Get a bundle of information about an entity in a single call
	 * @param entityId
	 * @return
	 * @throws RestServiceException 
	 * @throws SynapseException 
	 */
	public EntityBundleTransport getEntityBundle(String entityId, int partsMask) throws RestServiceException;

	/**
	 * Get a bundle of information about an entity in a single call
	 * @param entityId
	 * @return
	 * @throws RestServiceException 
	 * @throws SynapseException 
	 */
	public EntityBundleTransport getEntityBundleForVersion(String entityId, Long versionNumber, int partsMask) throws RestServiceException;

	public String getEntityReferencedBy(String entityId) throws RestServiceException;
	
	/**
	 * Log a debug message in the server-side log.
	 * @param message
	 */
	public void logDebug(String message);
	
	/**
	 * Log an error message in the server-side log.
	 * @param message
	 */
	public void logError(String message);
	

	/**
	 * Log an info message in the server-side log.
	 * @param message
	 */
	public void logInfo(String message);
	
	/**
	 * Get the repository service URL
	 * @return
	 */
	public String getRepositoryServiceUrl();
	

	/**
	 * Create or update an entity
	 * @param entityJson
	 * @param annoJson
	 * @param isNew
	 * @return
	 * @throws RestServiceException 
	 */
	public String createOrUpdateEntity(String entityJson, String annoJson, boolean isNew) throws RestServiceException;

	/**
	 * Returns the user's profile object in json string
	 * @return
	 * @throws RestServiceException
	 */
	public String getUserProfile() throws RestServiceException;
	
	/**
	 * Returns the specified user's profile object in json string
	 * @return
	 * @throws RestServiceException
	 */
	public String getUserProfile(String userId) throws RestServiceException;
	
	/**
	 * Return the specified team object in json string
	 * @param teamId
	 * @return
	 * @throws RestServiceException
	 */
	public String getTeam(String teamId) throws RestServiceException;
	
	/**
	 * Batch get headers for users/groups matching a list of Synapse IDs.
	 * 
	 * @param ids
	 * @return
	 * @throws RestServiceException
	 */
	public EntityWrapper getUserGroupHeadersById(List<String> ids) throws RestServiceException;

	/**
	 * Updates the user's profile json object 
	 * @param userProfileJson json object of the user's profile
	 * @throws RestServiceException
	 */
	public void updateUserProfile(String userProfileJson) throws RestServiceException;
	
	public void additionalEmailValidation(String userId, String emailAddress, String callbackUrl) throws RestServiceException;
	
	public void addEmail(String emailValidationToken) throws RestServiceException;
	
	public String getNotificationEmail() throws RestServiceException;
	
	public void setNotificationEmail(String email) throws RestServiceException;
	
	public EntityWrapper getNodeAcl(String id) throws RestServiceException;
	
	public EntityWrapper createAcl(EntityWrapper acl) throws RestServiceException;
	
	/**
	 * Update an ACL. Default to non-recursive application.
	 */
	public EntityWrapper updateAcl(EntityWrapper acl) throws RestServiceException;
	
	/**
	 * Update an entity's ACL. If 'recursive' is set to true, then any child 
	 * ACLs will be deleted, such that all child entities inherit this ACL. 
	 */
	public EntityWrapper updateAcl(EntityWrapper aclEW, boolean recursive) throws RestServiceException;

	public EntityWrapper deleteAcl(String ownerEntityId) throws RestServiceException;

	public boolean hasAccess(String ownerEntityId, String accessType) throws RestServiceException;
	
	public boolean hasAccess(String ownerId, String ownerType, String accessType) throws RestServiceException;

	public EntityWrapper getAllUsers() throws RestServiceException;
	
	public String createUserProfileAttachmentPresignedUrl(String id,
			String tokenOrPreviewId) throws RestServiceException;

	EntityWrapper createAccessRequirement(EntityWrapper arEW)
			throws RestServiceException;
	
	EntityWrapper createLockAccessRequirement(String entityId) throws RestServiceException;

	AccessRequirementsTransport getUnmetAccessRequirements(String entityId)
			throws RestServiceException;
	
	String getUnmetEvaluationAccessRequirements(String evalId)
			throws RestServiceException;

	String getUnmetTeamAccessRequirements(String teamId) throws RestServiceException;
	String getTeamAccessRequirements(String teamId) throws RestServiceException;
	String getAllEntityUploadAccessRequirements(String entityId) throws RestServiceException;
	
	EntityWrapper createAccessApproval(EntityWrapper aaEW)
			throws RestServiceException;
	
	public EntityWrapper updateExternalLocationable(String entityId, String externalUrl, String name) throws RestServiceException;
	
	public EntityWrapper updateExternalFile(String entityId, String externalUrl, String name) throws RestServiceException;
	
	public EntityWrapper createExternalFile(String parentEntityId, String externalUrl, String name) throws RestServiceException;
	
	/**
	 * convenience method for converting markdown to html
	 * @param markdown
	 * @return
	 */
	public String markdown2Html(String markdown, Boolean isPreview, Boolean isAlpha, String clientHostString) throws RestServiceException;
	
	public String getActivityForEntity(String entityId) throws RestServiceException;
	
	public String getActivityForEntityVersion(String entityId, Long versionNumber) throws RestServiceException;
	
	public String getActivity(String activityId) throws RestServiceException;
	
	public String getEntitiesGeneratedBy(String activityId, Integer limit, Integer offset) throws RestServiceException;

	public EntityWrapper removeAttachmentFromEntity(String entityId, String attachmentName) throws RestServiceException;
	public String getJSONEntity(String repoUri) throws RestServiceException;
	
	//wiki crud
	public String createWikiPage(String ownerId, String ownerType, String wikiPageJson) throws RestServiceException;
	public String getWikiPage(WikiPageKey key)  throws RestServiceException;
	public String updateWikiPage(String ownerId, String ownerType, String wikiPageJson)  throws RestServiceException;
	public void deleteWikiPage(WikiPageKey key)  throws RestServiceException;
	
	public String getWikiHeaderTree(String ownerId, String ownerType) throws RestServiceException;
	
	public String getWikiAttachmentHandles(WikiPageKey key) throws RestServiceException;
	
	 // V2 Wiki crud
    public String createV2WikiPage(String ownerId, String ownerType, String wikiPageJson) throws RestServiceException;
    public String getV2WikiPage(WikiPageKey key) throws RestServiceException;
    public String getVersionOfV2WikiPage(WikiPageKey key, Long version) throws RestServiceException;
    public String updateV2WikiPage(String ownerId, String ownerType, String wikiPageJson) throws RestServiceException;
    public String restoreV2WikiPage(String ownerId, String ownerType, String wikiId, Long versionToUpdate) throws RestServiceException;
    public void deleteV2WikiPage(WikiPageKey key) throws RestServiceException;
    public String getV2WikiHeaderTree(String ownerId, String ownerType) throws RestServiceException;
    public String getV2WikiAttachmentHandles(WikiPageKey key) throws RestServiceException;
    public String getVersionOfV2WikiAttachmentHandles(WikiPageKey key, Long version) throws RestServiceException;
    public String getV2WikiHistory(WikiPageKey key, Long limit, Long offset) throws RestServiceException;
    
	public String getMarkdown(WikiPageKey key)throws IOException, RestServiceException;
	public String getVersionOfMarkdown(WikiPageKey key, Long version) throws IOException, RestServiceException;
	public String zipAndUploadFile(String content, String fileName)throws IOException, RestServiceException;
	
	public String createV2WikiPageWithV1(String ownerId, String ownerType, String wikiPageJson) throws IOException, RestServiceException;
	public String updateV2WikiPageWithV1(String ownerId, String ownerType, String wikiPageJson) throws IOException, RestServiceException;
	public String getV2WikiPageAsV1(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException, IOException;
	public String getVersionOfV2WikiPageAsV1(org.sagebionetworks.web.shared.WikiPageKey key, Long version) throws RestServiceException, IOException;
	
	public String getPlainTextWikiPage(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException, IOException;
	
	public String getFileEndpoint() throws RestServiceException;
	
	public String addFavorite(String entityId) throws RestServiceException;
	
	public void removeFavorite(String entityId) throws RestServiceException;
	
	public String getFavorites(Integer limit, Integer offset) throws RestServiceException;
	
	public String createTeam(String teamName) throws RestServiceException;
	public void deleteTeam(String teamId) throws RestServiceException;
	public String getTeams(String userId, Integer limit, Integer offset) throws RestServiceException;
	public ArrayList<String> getTeamsForUser(String userId) throws RestServiceException;
	public String getTeamsBySearch(String searchTerm, Integer limit, Integer offset) throws RestServiceException;
	public TeamBundle getTeamBundle(String userId, String teamId, boolean isLoggedIn) throws RestServiceException;
	public Long getOpenRequestCount(String currentUserId, String teamId) throws RestServiceException;
	public List<MembershipInvitationBundle> getOpenInvitations(String userId) throws RestServiceException;
	public List<MembershipInvitationBundle> getOpenTeamInvitations(String teamId, Integer limit, Integer offset) throws RestServiceException;
	public List<MembershipRequestBundle> getOpenRequests(String teamId) throws RestServiceException;
	public void deleteMembershipInvitation(String invitationId) throws RestServiceException;
	public void setIsTeamAdmin(String currentUserId, String targetUserId, String teamId, boolean isTeamAdmin) throws RestServiceException;
	public void deleteTeamMember(String currentUserId, String targetUserId, String teamId) throws RestServiceException;
	public String updateTeam(String teamJson) throws RestServiceException;
	public String getTeamMembers(String teamId, String fragment, Integer limit, Integer offset) throws RestServiceException;
	public void deleteOpenMembershipRequests(String currentUserId, String teamId) throws RestServiceException;
	public void requestMembership(String currentUserId, String teamId, String message) throws RestServiceException;
	public void inviteMember(String userGroupId, String teamId, String message) throws RestServiceException;
	
	public String getCertifiedUserPassingRecord(String userId) throws RestServiceException;
	public String getCertificationQuiz() throws RestServiceException;
	public String submitCertificationQuizResponse(String quizResponseJson) throws RestServiceException; 
	
	public ArrayList<String> getFavoritesList(Integer limit, Integer offset) throws RestServiceException;
	
	public String getDescendants(String nodeId, int pageSize, String lastDescIdExcl) throws RestServiceException;
	
	public String getChunkedFileToken(String fileName, String contentType, String contentMD5) throws RestServiceException;
	public String getChunkedPresignedUrl(String requestJson) throws RestServiceException;
	public String combineChunkedFileUpload(List<String> requests) throws RestServiceException;
	public String getUploadDaemonStatus(String daemonId) throws RestServiceException;
	public String getFileEntityIdWithSameName(String fileName, String parentEntityId) throws RestServiceException;
	public String setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId, boolean isRestricted) throws RestServiceException;
	
	public String getEntityDoi(String entityId, Long versionNumber) throws RestServiceException;
	public void createDoi(String entityId, Long versionNumber) throws RestServiceException;
	
	public String getFileEntityTemporaryUrlForVersion(String entityId, Long versionNumber) throws RestServiceException;
	
	public String getEvaluations(List<String> evaluationIds) throws RestServiceException;
	
	public String getAvailableEvaluations() throws RestServiceException;
	public String getAvailableEvaluations(Set<String> targetEvaluationIds) throws RestServiceException;
	
	public ArrayList<String> getSharableEvaluations(String entityId) throws RestServiceException;
	
	public String createSubmission(String submissionJson, String etag) throws RestServiceException;
	
	public String getUserEvaluationPermissions(String evalId) throws RestServiceException; 
	public String getEvaluationAcl(String evalId) throws RestServiceException;
	public String updateEvaluationAcl(String aclJson) throws RestServiceException;
	
	public String getAvailableEvaluationsSubmitterAliases() throws RestServiceException;

	public Boolean hasSubmitted()	throws RestServiceException;
		
	public String getSynapseVersions() throws RestServiceException;
	
	public String getSynapseProperty(String key);
	
	public String getAPIKey() throws RestServiceException;
	
	public List<String> getColumnModelsForTableEntity(String tableEntityId) throws RestServiceException;
	
	public String createColumnModel(String columnModelJson) throws RestServiceException;

	public String sendMessage(Set<String> recipients, String subject, String message) throws RestServiceException;
	
	public Boolean isAliasAvailable(String alias, String aliasType) throws RestServiceException;
		
	public QueryResult executeTableQuery(String query, QueryDetails modifyingQueryDetails, boolean includeTotalRowCount) throws RestServiceException;
	
	public String sendRowsToTable(String rowSet) throws RestServiceException;
	
	public HashMap<String, WikiPageKey> getHelpPages() throws RestServiceException; 

	public String deleteApiKey() throws RestServiceException;
	
	public String deleteRowsFromTable(String toDelete) throws RestServiceException;
	
	public String getTableFileHandle(String fileHandlesToFindRowReferenceSet) throws RestServiceException;
	
	/**
	 * Set a table's schema. Any ColumnModel that does not have an ID will be
	 * treated as a column add.
	 * 
	 * @param The
	 *            ID of the table that will be updated.
	 * @param schema
	 *            Each string in the list must be a ColumnModel JSON string.
	 * @return The list of ColumnModel JSON strings.
	 * @throws RestServiceException
	 */
	public List<String> setTableSchema(String tableId, List<String> schemaJSON)
			throws RestServiceException;
}
