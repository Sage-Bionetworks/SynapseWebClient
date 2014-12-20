
package org.sagebionetworks.web.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
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
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
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
import org.sagebionetworks.web.shared.exceptions.ResultNotReadyException;

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
	
	public EntityPath getEntityPath(String entityId) throws RestServiceException;
	
	public SearchResults search(SearchQuery searchQuery) throws RestServiceException; 
	
	public String getEntityTypeBatch(List<String> entityIds) throws RestServiceException;
	
	public String getEntityHeaderBatch(String referenceList) throws RestServiceException;
	
	public ArrayList<EntityHeader> getEntityHeaderBatch(List<String> entityIds) throws RestServiceException;
	
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
	 * @throws RestServiceException 
	 */
	public void logError(String message) throws RestServiceException;
	
	/**
	 * Log an error message to Synapse repository services.  
	 * **NOTE** This should only be called if Synapse repository services was not involved, an error that could effect other clients.
	 * @param message
	 */
	public void logErrorToRepositoryServices(String message, String stacktrace) throws RestServiceException;

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
	 * Returns the user's profile object
	 * @return
	 * @throws RestServiceException
	 */
	public UserProfile getUserProfile() throws RestServiceException;
	
	/**
	 * Returns the specified user's profile object in json string
	 * @return
	 * @throws RestServiceException
	 */
	public UserProfile getUserProfile(String userId) throws RestServiceException;
	
	/**
	 * Return the specified team object in json string
	 * @param teamId
	 * @return
	 * @throws RestServiceException
	 */
	public Team getTeam(String teamId) throws RestServiceException;
	
	/**
	 * Batch get headers for users/groups matching a list of Synapse IDs.
	 * 
	 * @param ids
	 * @return
	 * @throws RestServiceException
	 */
	public UserGroupHeaderResponsePage getUserGroupHeadersById(ArrayList<String> ids) throws RestServiceException;

	/**
	 * Updates the user's profile json object 
	 * @param userProfileJson json object of the user's profile
	 * @throws RestServiceException
	 */
	public void updateUserProfile(UserProfile userProfile) throws RestServiceException;
	
	public UserGroupHeaderResponsePage getUserGroupHeadersByPrefix(String prefix, long limit, long offset) throws RestServiceException;
	
	public void additionalEmailValidation(String userId, String emailAddress, String callbackUrl) throws RestServiceException;
	
	public void addEmail(String emailValidationToken) throws RestServiceException;
	
	public String getNotificationEmail() throws RestServiceException;
	
	public void setNotificationEmail(String email) throws RestServiceException;
	
	public AccessControlList getNodeAcl(String id) throws RestServiceException;
	
	public AccessControlList createAcl(AccessControlList acl) throws RestServiceException;
	
	/**
	 * Update an ACL. Default to non-recursive application.
	 */
	public AccessControlList updateAcl(AccessControlList acl) throws RestServiceException;
	
	/**
	 * Update an entity's ACL. If 'recursive' is set to true, then any child 
	 * ACLs will be deleted, such that all child entities inherit this ACL. 
	 */
	public AccessControlList updateAcl(AccessControlList aclEW, boolean recursive) throws RestServiceException;

	public AccessControlList deleteAcl(String ownerEntityId) throws RestServiceException;

	public boolean hasAccess(String ownerEntityId, String accessType) throws RestServiceException;
	
	public boolean hasAccess(String ownerId, String ownerType, String accessType) throws RestServiceException;

	public EntityWrapper getAllUsers() throws RestServiceException;
	
	public String createUserProfileAttachmentPresignedUrl(String id,
			String tokenOrPreviewId) throws RestServiceException;

	AccessRequirement createAccessRequirement(AccessRequirement arEW)
			throws RestServiceException;
	
	EntityWrapper createLockAccessRequirement(String entityId) throws RestServiceException;

	AccessRequirementsTransport getUnmetAccessRequirements(String entityId, ACCESS_TYPE accessType)
			throws RestServiceException;
	
	String getUnmetEvaluationAccessRequirements(String evalId)
			throws RestServiceException;

	List<AccessRequirement> getTeamAccessRequirements(String teamId) throws RestServiceException;
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
	
	public Activity getActivityForEntity(String entityId) throws RestServiceException;
	
	public Activity getActivityForEntityVersion(String entityId, Long versionNumber) throws RestServiceException;
	
	public Activity getActivity(String activityId) throws RestServiceException;
	
	public String getEntitiesGeneratedBy(String activityId, Integer limit, Integer offset) throws RestServiceException;

	public EntityWrapper removeAttachmentFromEntity(String entityId, String attachmentName) throws RestServiceException;
	public String getJSONEntity(String repoUri) throws RestServiceException;
	
	//wiki crud
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
	public V2WikiOrderHint getV2WikiOrderHint(WikiPageKey key) throws RestServiceException;
	public V2WikiOrderHint updateV2WikiOrderHint(V2WikiOrderHint toUpdate) throws RestServiceException;
    public String getV2WikiAttachmentHandles(WikiPageKey key) throws RestServiceException;
    public String getVersionOfV2WikiAttachmentHandles(WikiPageKey key, Long version) throws RestServiceException;
    public String getV2WikiHistory(WikiPageKey key, Long limit, Long offset) throws RestServiceException;
    
	public String getMarkdown(WikiPageKey key)throws IOException, RestServiceException;
	public String getVersionOfMarkdown(WikiPageKey key, Long version) throws IOException, RestServiceException;
	public String zipAndUploadFile(String content, String fileName)throws IOException, RestServiceException;
	
	public String createV2WikiPageWithV1(String ownerId, String ownerType, String wikiPageJson) throws IOException, RestServiceException;
	public String updateV2WikiPageWithV1(String ownerId, String ownerType, String wikiPageJson) throws IOException, RestServiceException;
	public WikiPage getV2WikiPageAsV1(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException, IOException;
	public WikiPage getVersionOfV2WikiPageAsV1(org.sagebionetworks.web.shared.WikiPageKey key, Long version) throws RestServiceException, IOException;
	
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
	public ArrayList<MembershipInvitationBundle> getOpenInvitations(String userId) throws RestServiceException;
	public ArrayList<MembershipInvitationBundle> getOpenTeamInvitations(String teamId, Integer limit, Integer offset) throws RestServiceException;
	public ArrayList<MembershipRequestBundle> getOpenRequests(String teamId) throws RestServiceException;
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
	
	public ChunkedFileToken getChunkedFileToken(String fileName, String contentType, String contentMD5) throws RestServiceException;
	public String getChunkedPresignedUrl(ChunkRequest chunkRequest) throws RestServiceException;
	public UploadDaemonStatus combineChunkedFileUpload(List<ChunkRequest> requests) throws RestServiceException;
	public UploadDaemonStatus getUploadDaemonStatus(String daemonId) throws RestServiceException;
	public String getFileEntityIdWithSameName(String fileName, String parentEntityId) throws RestServiceException, SynapseException;
	public String setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId) throws RestServiceException;
	
	public String getEntityDoi(String entityId, Long versionNumber) throws RestServiceException;
	public void createDoi(String entityId, Long versionNumber) throws RestServiceException;
	
	public String getFileEntityTemporaryUrlForVersion(String entityId, Long versionNumber) throws RestServiceException;
	
	public String getEvaluations(List<String> evaluationIds) throws RestServiceException;
	
	public String getAvailableEvaluations() throws RestServiceException;
	public String getAvailableEvaluations(Set<String> targetEvaluationIds) throws RestServiceException;
	
	public ArrayList<String> getSharableEvaluations(String entityId) throws RestServiceException;
	
	public Submission createSubmission(Submission submission, String etag) throws RestServiceException;
	
	public String getUserEvaluationPermissions(String evalId) throws RestServiceException; 
	public String getEvaluationAcl(String evalId) throws RestServiceException;
	public String updateEvaluationAcl(String aclJson) throws RestServiceException;
	
	public String getAvailableEvaluationsSubmitterAliases() throws RestServiceException;

	public Boolean hasSubmitted()	throws RestServiceException;
		
	public String getSynapseVersions() throws RestServiceException;
	
	public HashMap<String, String> getSynapseProperties();
	
	public String getAPIKey() throws RestServiceException;
	
	public List<String> getColumnModelsForTableEntity(String tableEntityId) throws RestServiceException;
	
	public String createColumnModel(String columnModelJson) throws RestServiceException;

	public String sendMessage(Set<String> recipients, String subject, String message) throws RestServiceException;
	
	public Boolean isAliasAvailable(String alias, String aliasType) throws RestServiceException;
		
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
	public void setTableSchema(TableEntity entity, List<ColumnModel> newSchema)
			throws RestServiceException;
	
	/**
	 * Validate a table query.
	 * @param sql
	 */
	public void validateTableQuery(String sql) throws RestServiceException;
	/**
	 * For the given table SQL toggle the sort on the given column and return the modified SQL.
	 * @param sql
	 * @param header
	 * @return
	 * @throws RestServiceException
	 */
	public String toggleSortOnTableQuery(String sql, String header) throws RestServiceException;
	/**
	 * Get the sort info for this table.
	 * @param sql
	 * @return
	 * @throws RestServiceException
	 */
	public List<SortItem> getSortFromTableQuery(String sql) throws RestServiceException;
	/**
	 * Apply PartialRowSet to a table entity.
	 * 
	 * @param deltaJson
	 * @throws RestServiceException 
	 */
	public void applyTableDelta(PartialRowSet delta) throws RestServiceException;
	
	/**
	 * Start a new Asynchronous job of a the given type with the provided request JSON.
	 * @param type The type of job to run.
	 * @param bodyJSON The JSON of the AsynchronousRequestBody.
	 * @return
	 * @throws RestServiceException
	 */
	public String startAsynchJob(AsynchType type, AsynchronousRequestBody body) throws RestServiceException;
	
	/**
	 * Get the results of an Asynchronous job identified by the provided jobId.
	 * @param type
	 * @param jobId
	 * @return
	 * @throws RestServiceException
	 * @throws ResultNotReadyException Thrown when the job is not ready.  The status JOSN of this exception
	 * is of type AsynchronousJobStatus.
	 */
	public AsynchronousResponseBody getAsynchJobResults(AsynchType type, String jobId) throws RestServiceException, ResultNotReadyException;

	/**
	 * Execute a generic entity entity query.
	 * @param query
	 * @return
	 * @throws RestServiceException 
	 */
	public EntityQueryResults executeEntityQuery(EntityQuery query) throws RestServiceException;

	/**
	 * Create or update an Entity.
	 * @param entity
	 * @param annoJson
	 * @param isNew
	 * @return
	 * @throws RestServiceException 
	 */
	public TableEntity createTableEntity(TableEntity entity) throws RestServiceException;

	/**
	 * Get the file Handle given its ID.
	 * Note: Only the creator of the FileHandle can get the FileHandle with this method.
	 * 
	 * @param fileHandleId
	 * @return
	 * @throws RestServiceException 
	 */
	FileHandle getFileHandle(String fileHandleId) throws RestServiceException;
	
	
	String createFileHandleURL(String fileHandleId) throws RestServiceException;

	/**
	 * Create a list of columns.
	 * 
	 * @param value
	 * @return
	 * @throws RestServiceException 
	 */
	List<ColumnModel> createTableColumns(List<ColumnModel> value) throws RestServiceException;
	
	/**
	 * Return the upload destinations associated with this parent entity (container)
	 * @param parentEntityId
	 * @return
	 * @throws RestServiceException
	 */
	public List<UploadDestination> getUploadDestinations(String parentEntityId) throws RestServiceException;
	ProjectPagedResults getMyProjects(int limit, int offset) throws RestServiceException;
	ProjectPagedResults getProjectsForTeam(String teamId, int limit, int offset) throws RestServiceException;
	ProjectPagedResults getUserProjects(String userId, int limit, int offset) throws RestServiceException;
	
	String getHost(String urlString) throws RestServiceException;
}
