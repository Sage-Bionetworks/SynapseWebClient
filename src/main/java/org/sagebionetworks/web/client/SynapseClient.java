
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
import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandleCopyRequest;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnModelPage;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.OpenTeamInvitationBundle;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

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
	
	/**
	 * Update an entity.
	 * @param toUpdate
	 * @return
	 * @throws RestServiceException
	 */
	Entity updateEntity(Entity toUpdate) throws RestServiceException;
	
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
	FileHandleResults getWikiAttachmentHandles(WikiPageKey key) throws RestServiceException;
	
	 // V2 Wiki crud
	 V2WikiPage restoreV2WikiPage(String ownerId, String ownerType, String wikiId, Long versionToUpdate) throws RestServiceException;
	void deleteV2WikiPage(WikiPageKey key) throws RestServiceException;
	List<V2WikiHeader> getV2WikiHeaderTree(String ownerId, String ownerType) throws RestServiceException;
	V2WikiOrderHint getV2WikiOrderHint(WikiPageKey key) throws RestServiceException;
	FileHandleResults getV2WikiAttachmentHandles(WikiPageKey key) throws RestServiceException;
	PaginatedResults<V2WikiHistorySnapshot> getV2WikiHistory(WikiPageKey key, Long limit, Long offset) throws RestServiceException;

	WikiPage createV2WikiPageWithV1(String ownerId, String ownerType, WikiPage wikiPage) throws IOException, RestServiceException;
	WikiPage updateV2WikiPageWithV1(String ownerId, String ownerType, WikiPage wikiPage) throws IOException, RestServiceException;

	EntityHeader addFavorite(String entityId) throws RestServiceException;

	void removeFavorite(String entityId) throws RestServiceException;

	void deleteTeam(String teamId) throws RestServiceException;
	PaginatedResults<Team> getTeamsBySearch(String searchTerm, Integer limit, Integer offset) throws RestServiceException;
	TeamBundle getTeamBundle(String userId, String teamId, boolean isLoggedIn) throws RestServiceException;
	ArrayList<OpenUserInvitationBundle> getOpenInvitations(String userId) throws RestServiceException;
	ArrayList<OpenTeamInvitationBundle> getOpenTeamInvitations(String teamId, Integer limit, Integer offset) throws RestServiceException;
	List<MembershipRequestBundle> getOpenRequests(String teamId) throws RestServiceException;

	void setIsTeamAdmin(String currentUserId, String targetUserId, String teamId, boolean isTeamAdmin) throws RestServiceException;
	Team updateTeam(Team team, AccessControlList teamAcl) throws RestServiceException;
	TeamMemberPagedResults getTeamMembers(String teamId, String fragment, Integer limit, Integer offset) throws RestServiceException;
	TeamMembershipStatus requestMembership(String currentUserId, String teamId, String message, String hostPageBaseURL, Date expiresOn) throws RestServiceException;
	void inviteMember(String userGroupId, String teamId, String message, String hostPageBaseURL) throws RestServiceException;
	void inviteNewMember(String email, String teamId, String message, String hostPageBaseURL) throws RestServiceException;

	String getCertifiedUserPassingRecord(String userId) throws RestServiceException;
	String getCertificationQuiz() throws RestServiceException;
	PassingRecord submitCertificationQuizResponse(QuizResponse response) throws RestServiceException;


	String getFileEntityIdWithSameName(String fileName, String parentEntityId) throws RestServiceException, SynapseException;
	String setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId) throws RestServiceException;

	String getAPIKey() throws RestServiceException;

	ResponseMessage handleSignedToken(SignedTokenInterface signedToken, String hostPageBaseURL) throws RestServiceException;

	SignedTokenInterface hexDecodeAndDeserialize(String tokenTypeName, String signedTokenString) throws RestServiceException;

	AccountCreationToken hexDecodeAndDeserializeAccountCreationToken(String tokenString) throws RestServiceException;

	List<ColumnModel> getColumnModelsForTableEntity(String tableEntityId) throws RestServiceException;

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

	Boolean isTeamMember(String userId, Long groupPrincipalId)
			throws RestServiceException;
	
	Entity moveEntity(String entityId, String newParentEntityId) throws RestServiceException;

	UserProfile getUserProfileFromUsername(String username) throws RestServiceException;

	Entity updateFileEntity(FileEntity toUpdate, FileHandleCopyRequest copyRequest) throws RestServiceException;
	
	void deleteAccessApprovals(String accessRequirement, String accessorId) throws RestServiceException;

	String generateSqlWithFacets(String basicSql, List<org.sagebionetworks.repo.model.table.FacetColumnRequest> selectedFacets, List<ColumnModel> schema) throws RestServiceException;

	ColumnModelPage getPossibleColumnModelsForViewScope(ViewScope scope, String nextPageToken)
			throws RestServiceException;

	Boolean isUserAllowedToRenderHTML(String userId) throws RestServiceException;

	long getTeamMemberCount(String teamId) throws RestServiceException;
	
	boolean isChallenge(String projectId) throws RestServiceException;

	void deleteAccessRequirement(Long accessRequirementId) throws RestServiceException;

	void addTeamMember(String userGroupId, String teamId, String message, String hostPageBaseURL) throws RestServiceException;

	String createExternalObjectStoreFileHandle(ExternalObjectStoreFileHandle fileHandle) throws RestServiceException;

	void removeEmail(String email) throws RestServiceException;

	ArrayList<String[]> parseCsv(String csvPreviewText, char delimiter) throws RestServiceException;

	void resendTeamInvitation(String membershipInvitationId, String hostPageBaseURL) throws RestServiceException;
}