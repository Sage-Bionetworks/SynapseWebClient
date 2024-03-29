package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandleCopyRequest;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.OpenTeamInvitationBundle;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public interface SynapseClientAsync {
  void deleteEntityVersionById(
    String entityId,
    Long versionNumber,
    AsyncCallback<Void> callback
  );

  void updateUserProfile(
    UserProfile userProfileJson,
    AsyncCallback<Void> callback
  );

  void addEmail(
    EmailValidationSignedToken emailValidationSignedToken,
    AsyncCallback<Void> callback
  );

  void setNotificationEmail(String email, AsyncCallback<Void> callback);

  void removeEmail(String email, AsyncCallback<Void> callback);

  void getEntityBenefactorAcl(
    String id,
    AsyncCallback<AccessControlList> callback
  );

  void createAcl(
    AccessControlList acl,
    AsyncCallback<AccessControlList> callback
  );

  void updateAcl(
    AccessControlList acl,
    AsyncCallback<AccessControlList> callback
  );

  void getTeamAcl(String teamId, AsyncCallback<AccessControlList> callback);

  void deleteAcl(
    String ownerEntityId,
    AsyncCallback<AccessControlList> callback
  );

  void hasAccess(
    String ownerId,
    String ownerType,
    String accessType,
    AsyncCallback<Boolean> callback
  );

  void createOrUpdateAccessRequirement(
    AccessRequirement arEW,
    AsyncCallback<AccessRequirement> callback
  );

  void getTeamAccessRequirements(
    String teamId,
    AsyncCallback<List<AccessRequirement>> callback
  );

  void createAccessApproval(
    AccessApproval aaEW,
    AsyncCallback<AccessApproval> callback
  );

  void updateExternalFile(
    String entityId,
    String externalUrl,
    String name,
    String contentType,
    Long fileSize,
    String md5,
    Long storageLocationId,
    AsyncCallback<Entity> callback
  ) throws RestServiceException;

  void createExternalFile(
    String parentEntityId,
    String externalUrl,
    String name,
    String contentType,
    Long fileSize,
    String md5,
    Long storageLocationId,
    AsyncCallback<Entity> callback
  ) throws RestServiceException;

  void getWikiAttachmentHandles(
    WikiPageKey key,
    AsyncCallback<FileHandleResults> callback
  );

  void restoreV2WikiPage(
    String ownerId,
    String ownerType,
    String wikiId,
    Long versionToUpdate,
    AsyncCallback<V2WikiPage> callback
  );

  void deleteV2WikiPage(WikiPageKey key, AsyncCallback<Void> callback);

  void getV2WikiAttachmentHandles(
    WikiPageKey key,
    AsyncCallback<FileHandleResults> callback
  );

  void getV2WikiHistory(
    WikiPageKey key,
    Long limit,
    Long offset,
    AsyncCallback<PaginatedResults<V2WikiHistorySnapshot>> callback
  );

  void createV2WikiPageWithV1(
    String ownerId,
    String ownerType,
    WikiPage wikiPage,
    AsyncCallback<WikiPage> callback
  );

  void updateV2WikiPageWithV1(
    String ownerId,
    String ownerType,
    WikiPage wikiPage,
    AsyncCallback<WikiPage> callback
  );

  void addFavorite(String entityId, AsyncCallback<EntityHeader> callback);

  void removeFavorite(String entityId, AsyncCallback<Void> callback);

  /**
   * TEAMS
   */
  /////////////////
  void deleteTeam(String teamId, AsyncCallback<Void> callback);

  void getTeamsBySearch(
    String searchTerm,
    Integer limit,
    Integer offset,
    AsyncCallback<PaginatedResults<Team>> callback
  );

  void getTeamBundle(
    String userId,
    String teamId,
    boolean isLoggedIn,
    AsyncCallback<TeamBundle> callback
  );

  void getOpenInvitations(
    String userId,
    AsyncCallback<ArrayList<OpenUserInvitationBundle>> callback
  );

  void getOpenTeamInvitations(
    String teamId,
    Integer limit,
    Integer offset,
    AsyncCallback<ArrayList<OpenTeamInvitationBundle>> callback
  );

  void resendTeamInvitation(
    String membershipInvitationId,
    String hostPageBaseURL,
    AsyncCallback<Void> callback
  );

  void getOpenRequests(
    String teamId,
    AsyncCallback<List<MembershipRequestBundle>> callback
  );

  void updateTeam(
    Team team,
    AccessControlList teamAcl,
    AsyncCallback<Team> callback
  );

  void getTeamMemberCount(String teamId, AsyncCallback<Long> callback);

  void requestMembership(
    String currentUserId,
    String teamId,
    String message,
    String hostPageBaseURL,
    Date expiresOn,
    AsyncCallback<TeamMembershipStatus> callback
  );

  /**
   * The PassingRecord that documents when a user was certified is returned. Otherwise, a
   * NotFoundException is thrown.
   *
   * @param userId
   * @param callback
   */
  void getCertifiedUserPassingRecord(
    String userId,
    AsyncCallback<String> callback
  );

  void getCertificationQuiz(AsyncCallback<String> callback);

  void submitCertificationQuizResponse(
    QuizResponse response,
    AsyncCallback<PassingRecord> callback
  );

  void getFileEntityIdWithSameName(
    String fileName,
    String parentEntityId,
    AsyncCallback<String> callback
  );

  void setFileEntityFileHandle(
    String fileHandleId,
    String entityId,
    String parentEntityId,
    AsyncCallback<String> callback
  );

  void handleSignedToken(
    SignedTokenInterface signedToken,
    String hostPageBaseURL,
    AsyncCallback<ResponseMessage> callback
  );

  void hexDecodeAndDeserialize(
    String signedTokenString,
    AsyncCallback<SignedTokenInterface> callback
  );

  void hexDecodeAndDeserializeAccountCreationToken(
    String tokenString,
    AsyncCallback<AccountCreationToken> callback
  );

  void getAPIKey(AsyncCallback<String> callback);

  void sendMessage(
    Set<String> recipients,
    String subject,
    String message,
    String hostPageBaseURL,
    AsyncCallback<String> callback
  );

  void sendMessageToEntityOwner(
    String entityId,
    String subject,
    String messageBody,
    String hostPageBaseURL,
    AsyncCallback<String> callback
  );

  void isAliasAvailable(
    String alias,
    String aliasType,
    AsyncCallback<Boolean> callback
  );

  void getPageNameToWikiKeyMap(
    AsyncCallback<HashMap<String, WikiPageKey>> callback
  );

  void deleteApiKey(AsyncCallback<String> callback);

  /**
   * Set a table's schema. Creates any necessary ColumnModels (for create and update), and figures out
   * necessary ColumnChanges to transform oldSchema into newSchema.
   *
   * @param tableId
   * @param oldSchema
   * @param newSchema
   * @param callback
   */
  void getTableUpdateTransactionRequest(
    String tableId,
    List<ColumnModel> oldSchema,
    List<ColumnModel> newSchema,
    AsyncCallback<TableUpdateTransactionRequest> callback
  );

  void restoreFromTrash(
    String entityId,
    String newParentId,
    AsyncCallback<Void> callback
  );

  void viewTrashForUser(
    long offset,
    long limit,
    AsyncCallback<PaginatedResults<TrashedEntity>> callback
  );

  void purgeMultipleTrashedEntitiesForUser(
    Set<String> entityIds,
    AsyncCallback<Void> callback
  );

  void createTableColumns(
    List<ColumnModel> value,
    AsyncCallback<List<ColumnModel>> asyncCallback
  );

  void getHost(String urlString, AsyncCallback<String> callback);

  void updateFileEntity(
    FileEntity toUpdate,
    FileHandleCopyRequest copyRequest,
    AsyncCallback<Entity> callback
  );

  void moveEntity(
    String entityId,
    String newParentEntityId,
    AsyncCallback<Entity> callback
  );

  void createStorageLocationSetting(
    String parentEntityId,
    StorageLocationSetting setting,
    AsyncCallback<Void> callback
  );

  void getMyLocationSettingBanners(AsyncCallback<List<String>> callback);

  void isTeamMember(
    String userId,
    Long groupPrincipalId,
    AsyncCallback<Boolean> callback
  );

  void setIsTeamAdmin(
    String currentUserId,
    String targetUserId,
    String teamId,
    boolean isTeamAdmin,
    AsyncCallback<Void> callback
  );

  void getUserProfileFromUsername(
    String username,
    AsyncCallback<UserProfile> callback
  );

  void deleteAccessRequirement(
    Long accessRequirementId,
    AsyncCallback<Void> callback
  );

  void deleteAccessApprovals(
    String accessRequirement,
    String accessorId,
    AsyncCallback<Void> asyncCallback
  );

  void isUserAllowedToRenderHTML(
    String userId,
    AsyncCallback<Boolean> callback
  );

  void isChallenge(String id, AsyncCallback<Boolean> callback);

  void addTeamMember(
    String userGroupId,
    String teamId,
    String message,
    String hostPageBaseURL,
    AsyncCallback<Void> callback
  );

  /**
   * If successful, will return the new file handle ID
   *
   * @param fileHandle
   * @param callback
   */
  void createExternalObjectStoreFileHandle(
    ExternalObjectStoreFileHandle fileHandle,
    AsyncCallback<String> callback
  );

  void parseCsv(
    String csvPreviewText,
    char delimiter,
    AsyncCallback<ArrayList<String[]>> callback
  );
}
