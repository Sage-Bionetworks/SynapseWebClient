
package org.sagebionetworks.web.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.client.exceptions.SynapseException;
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
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
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
import org.sagebionetworks.web.shared.exceptions.ResultNotReadyException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("synapseclient")	
public interface SynapseClient extends RemoteService{

	PaginatedResults<VersionInfo> getEntityVersions(String entityId, int offset, int limit) throws RestServiceException;

	void deleteEntityVersionById(String entityId, Long versionNumber) throws RestServiceException;

	void restoreFromTrash(String entityId, String newParentId) throws RestServiceException;

	PaginatedResults<TrashedEntity> viewTrashForUser(long offset, long limit) throws RestServiceException;

	void purgeTrashForUser() throws RestServiceException;

	void purgeMultipleTrashedEntitiesForUser(Set<String> entityIds) throws RestServiceException;

	SearchResults search(SearchQuery searchQuery) throws RestServiceException;

	PaginatedResults<EntityHeader> getEntityHeaderBatch(ReferenceList referenceList) throws RestServiceException;
	
	/**
	 * Update an entity.
	 * @param toUpdate
	 * @return
	 * @throws RestServiceException
	 */
	Entity updateEntity(Entity toUpdate) throws RestServiceException;
	
	/**
	 * Get the repository service URL
	 * @return
	 */
	String getRepositoryServiceUrl();
	

	/**
	 * Create or update an entity
	 * @param entity
	 * @param annos
	 * @param isNew
	 * @return
	 * @throws RestServiceException 
	 */
	String createOrUpdateEntity(Entity entity, Annotations annos, boolean isNew) throws RestServiceException;

	/**
	 * Updates the user's profile json object 
	 * @param userProfile json object of the user's profile
	 * @throws RestServiceException
	 */
	void updateUserProfile(UserProfile userProfile) throws RestServiceException;

	void additionalEmailValidation(String userId, String emailAddress, String callbackUrl) throws RestServiceException;

	void addEmail(EmailValidationSignedToken emailValidationSignedToken) throws RestServiceException;

	String getNotificationEmail() throws RestServiceException;

	void setNotificationEmail(String email) throws RestServiceException;

	AccessControlList getEntityBenefactorAcl(String id) throws RestServiceException;

	AccessControlList createAcl(AccessControlList acl) throws RestServiceException;
	
	/**
	 * Update an entity's ACL. If 'recursive' is set to true, then any child 
	 * ACLs will be deleted, such that all child entities inherit this ACL. 
	 */
	AccessControlList updateAcl(AccessControlList aclEW, boolean recursive) throws RestServiceException;

	AccessControlList getTeamAcl(String teamId) throws RestServiceException;

	AccessControlList deleteAcl(String ownerEntityId) throws RestServiceException;

	boolean hasAccess(String ownerId, String ownerType, String accessType) throws RestServiceException;

	AccessRequirement createOrUpdateAccessRequirement(AccessRequirement arEW) throws RestServiceException;
	
	List<AccessRequirement> getTeamAccessRequirements(String teamId) throws RestServiceException;

	Activity getActivityForEntity(String entityId) throws RestServiceException;

	Activity getActivityForEntityVersion(String entityId, Long versionNumber) throws RestServiceException;

	Activity getActivity(String activityId) throws RestServiceException;

	PaginatedResults<Reference> getEntitiesGeneratedBy(String activityId, Integer limit, Integer offset) throws RestServiceException;

	String getRootWikiId(String ownerId, String ownerType) throws RestServiceException;
	FileHandleResults getWikiAttachmentHandles(WikiPageKey key) throws RestServiceException;
	
	 // V2 Wiki crud
	 V2WikiPage restoreV2WikiPage(String ownerId, String ownerType, String wikiId, Long versionToUpdate) throws RestServiceException;
	void deleteV2WikiPage(WikiPageKey key) throws RestServiceException;
	List<V2WikiHeader> getV2WikiHeaderTree(String ownerId, String ownerType) throws RestServiceException;
	V2WikiOrderHint getV2WikiOrderHint(WikiPageKey key) throws RestServiceException;
	V2WikiOrderHint updateV2WikiOrderHint(V2WikiOrderHint toUpdate) throws RestServiceException;
	FileHandleResults getV2WikiAttachmentHandles(WikiPageKey key) throws RestServiceException;
	PaginatedResults<V2WikiHistorySnapshot> getV2WikiHistory(WikiPageKey key, Long limit, Long offset) throws RestServiceException;

	WikiPage createV2WikiPageWithV1(String ownerId, String ownerType, WikiPage wikiPage) throws IOException, RestServiceException;
	WikiPage updateV2WikiPageWithV1(String ownerId, String ownerType, WikiPage wikiPage) throws IOException, RestServiceException;

	EntityHeader addFavorite(String entityId) throws RestServiceException;

	void removeFavorite(String entityId) throws RestServiceException;

	String createTeam(String teamName) throws RestServiceException;
	void deleteTeam(String teamId) throws RestServiceException;
	PaginatedResults<Team> getTeams(String userId, Integer limit, Integer offset) throws RestServiceException;
	List<Team> getTeamsForUser(String userId) throws RestServiceException;
	PaginatedResults<Team> getTeamsBySearch(String searchTerm, Integer limit, Integer offset) throws RestServiceException;
	TeamBundle getTeamBundle(String userId, String teamId, boolean isLoggedIn) throws RestServiceException;
	Long getOpenRequestCount(String currentUserId, String teamId) throws RestServiceException;
	ArrayList<OpenUserInvitationBundle> getOpenInvitations(String userId) throws RestServiceException;
	ArrayList<OpenTeamInvitationBundle> getOpenTeamInvitations(String teamId, Integer limit, Integer offset) throws RestServiceException;
	List<MembershipRequestBundle> getOpenRequests(String teamId) throws RestServiceException;

	void setIsTeamAdmin(String currentUserId, String targetUserId, String teamId, boolean isTeamAdmin) throws RestServiceException;
	void deleteTeamMember(String currentUserId, String targetUserId, String teamId) throws RestServiceException;
	Team updateTeam(Team team, AccessControlList teamAcl) throws RestServiceException;
	TeamMemberPagedResults getTeamMembers(String teamId, String fragment, Integer limit, Integer offset) throws RestServiceException;
	void deleteOpenMembershipRequests(String currentUserId, String teamId) throws RestServiceException;
	TeamMembershipStatus requestMembership(String currentUserId, String teamId, String message, String hostPageBaseURL, Date expiresOn) throws RestServiceException;
	void inviteMember(String userGroupId, String teamId, String message, String hostPageBaseURL) throws RestServiceException;
	void inviteNewMember(String email, String teamId, String message, String hostPageBaseURL) throws RestServiceException;

	String getCertifiedUserPassingRecord(String userId) throws RestServiceException;
	String getCertificationQuiz() throws RestServiceException;
	PassingRecord submitCertificationQuizResponse(QuizResponse response) throws RestServiceException;


	String getFileEntityIdWithSameName(String fileName, String parentEntityId) throws RestServiceException, SynapseException;
	String setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId) throws RestServiceException;

	Doi getEntityDoi(String entityId, Long versionNumber) throws RestServiceException;
	void createDoi(String entityId, Long versionNumber) throws RestServiceException;

	String getSynapseVersions() throws RestServiceException;

	HashMap<String, String> getSynapseProperties();

	String getAPIKey() throws RestServiceException;

	ResponseMessage handleSignedToken(SignedTokenInterface signedToken, String hostPageBaseURL) throws RestServiceException;

	SignedTokenInterface hexDecodeAndDeserialize(String tokenTypeName, String signedTokenString) throws RestServiceException;

	AccountCreationToken hexDecodeAndDeserializeAccountCreationToken(String tokenString) throws RestServiceException;

	List<ColumnModel> getColumnModelsForTableEntity(String tableEntityId) throws RestServiceException;

	String createColumnModel(String columnModelJson) throws RestServiceException;

	String sendMessage(Set<String> recipients, String subject, String message, String hostPageBaseURL) throws RestServiceException;

	String sendMessageToEntityOwner(String entityId, String subject, String messageBody, String hostPageBaseURL)
			throws RestServiceException;

	Boolean isAliasAvailable(String alias, String aliasType) throws RestServiceException;

	HashMap<String, WikiPageKey> getPageNameToWikiKeyMap() throws RestServiceException;

	String deleteApiKey() throws RestServiceException;

	TableUpdateTransactionRequest getTableUpdateTransactionRequest(String tableId, List<ColumnModel> oldSchema, List<ColumnModel> newSchema)
			throws RestServiceException;
	
	/**
	 * Validate a table query.
	 * @param sql
	 */
	void validateTableQuery(String sql) throws RestServiceException;
	/**
	 * For the given table SQL toggle the sort on the given column and return the modified SQL.
	 * @param sql
	 * @param header
	 * @return
	 * @throws RestServiceException
	 */
	String toggleSortOnTableQuery(String sql, String header) throws RestServiceException;
	/**
	 * Get the sort info for this table.
	 * @param sql
	 * @return
	 * @throws RestServiceException
	 */
	List<SortItem> getSortFromTableQuery(String sql) throws RestServiceException;
	
	/**
	 * Start a new Asynchronous job of a the given type with the provided request JSON.
	 * @param type The type of job to run.
	 * @param body The JSON of the AsynchronousRequestBody.
	 * @return
	 * @throws RestServiceException
	 */
	String startAsynchJob(AsynchType type, AsynchronousRequestBody body) throws RestServiceException;
	
	/**
	 * Get the results of an Asynchronous job identified by the provided jobId.
	 * @param type
	 * @param jobId
	 * @param body The request body
	 * @return
	 * @throws RestServiceException
	 * @throws ResultNotReadyException Thrown when the job is not ready.  The status JOSN of this exception
	 * is of type AsynchronousJobStatus.
	 */
	AsynchronousResponseBody getAsynchJobResults(AsynchType type, String jobId, AsynchronousRequestBody body) throws RestServiceException;

	/**
	 * Create or update an Entity.
	 * @param entity
	 * @return
	 * @throws RestServiceException 
	 */
	Entity createEntity(Entity entity) throws RestServiceException;

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
	List<UploadDestination> getUploadDestinations(String parentEntityId) throws RestServiceException;
	ProjectPagedResults getMyProjects(ProjectListType projectListType, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir) throws RestServiceException;
	ProjectPagedResults getProjectsForTeam(String teamId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir) throws RestServiceException;
	ProjectPagedResults getUserProjects(String userId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir) throws RestServiceException;
	
	String getHost(String urlString) throws RestServiceException;


	void updateAnnotations(String entityId, Annotations annotations) throws RestServiceException;

	AccessApproval createAccessApproval(AccessApproval aaEW) throws RestServiceException;

	Entity updateExternalFile(String entityId, String externalUrl, String name, String contentType, Long fileSize, String md5, Long storageLocationId) throws RestServiceException;

	Entity createExternalFile(String parentEntityId, String externalUrl, String name, String contentType, Long fileSize, String md5, Long storageLocationId) throws RestServiceException;

	void putActivity(Activity update) throws RestServiceException;

	Activity getOrCreateActivityForEntityVersion(String entityId,
			Long versionNumber) throws RestServiceException;

	void createStorageLocationSetting(String parentEntityId, StorageLocationSetting setting) throws RestServiceException;

	StorageLocationSetting getStorageLocationSetting(String parentEntityId) throws RestServiceException;

	List<String> getMyLocationSettingBanners() throws RestServiceException;

	LogEntry hexDecodeLogEntry(String encodedLogEntry);

	Boolean isTeamMember(String userId, Long groupPrincipalId)
			throws RestServiceException;

	EntityBundlePlus getEntityBundlePlusForVersion(String entityId, Long versionNumber, int partsMask)
			throws RestServiceException;

	Entity moveEntity(String entityId, String newParentEntityId) throws RestServiceException;

	String getUserIdFromUsername(String username) throws RestServiceException;

	Etag getEtag(String objectId, ObjectType objectType) throws RestServiceException;

	UserProfile getUserProfileFromUsername(String username) throws RestServiceException;

	Entity updateFileEntity(FileEntity toUpdate, FileHandleCopyRequest copyRequest) throws RestServiceException;
	
	void deleteAccessApprovals(String accessRequirement, String accessorId) throws RestServiceException;

	String generateSqlWithFacets(String basicSql, List<org.sagebionetworks.repo.model.table.FacetColumnRequest> selectedFacets, List<ColumnModel> schema) throws RestServiceException;

	ColumnModelPage getPossibleColumnModelsForViewScope(ViewScope scope, String nextPageToken)
			throws RestServiceException;

	Boolean isUserAllowedToRenderHTML(String userId) throws RestServiceException;

	long getTeamMemberCount(String teamId) throws RestServiceException;
	
	boolean isWiki(String projectId) throws RestServiceException;

	boolean isChallenge(String projectId) throws RestServiceException;

	void deleteAccessRequirement(Long accessRequirementId) throws RestServiceException;

	void addTeamMember(String userGroupId, String teamId, String message, String hostPageBaseURL) throws RestServiceException;

	String createExternalObjectStoreFileHandle(ExternalObjectStoreFileHandle fileHandle) throws RestServiceException;

	void removeEmail(String email) throws RestServiceException;
}