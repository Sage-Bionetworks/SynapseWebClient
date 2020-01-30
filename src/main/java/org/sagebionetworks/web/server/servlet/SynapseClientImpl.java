package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Whitelist;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ResponseMessage;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMemberTypeFilterOptions;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.NewUserSignedToken;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.dao.WikiPageKeyHelper;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.file.BatchFileHandleCopyRequest;
import org.sagebionetworks.repo.model.file.BatchFileHandleCopyResult;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleCopyRequest;
import org.sagebionetworks.repo.model.file.FileHandleCopyResult;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.message.MessageToUser;
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.repo.model.principal.AliasCheckRequest;
import org.sagebionetworks.repo.model.principal.AliasCheckResponse;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.repo.model.principal.PrincipalAliasRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAliasResponse;
import org.sagebionetworks.repo.model.project.ProjectSettingsType;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.repo.model.project.UploadDestinationListSetting;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.table.ColumnChange;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnModelPage;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.TableSchemaChangeRequest;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.TransformSqlWithFacetsRequest;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.table.query.TableQueryParser;
import org.sagebionetworks.util.SerializationUtils;
import org.sagebionetworks.web.client.SynapseClient;
import org.sagebionetworks.web.shared.AccessRequirementUtils;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.OpenTeamInvitationBundle;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.TableQueryParseException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import com.google.gwt.thirdparty.guava.common.base.Supplier;
import com.google.gwt.thirdparty.guava.common.base.Suppliers;
import au.com.bytecode.opencsv.CSVReader;

public class SynapseClientImpl extends SynapseClientBase implements SynapseClient, TokenProvider {

	private static final Integer MAX_LIMIT = 300;
	public static final Integer ZERO_OFFSET = 0;

	public static final String DEFAULT_STORAGE_ID_PROPERTY_KEY = "org.sagebionetworks.portal.synapse_storage_id";
	public static final String HTML_TEAM_ID_PROPERTY_KEY = "org.sagebionetworks.portal.html_team_id";

	public static final String SYN_PREFIX = "syn";
	public static final Charset MESSAGE_CHARSET = Charset.forName("UTF-8");
	public static final ContentType HTML_MESSAGE_CONTENT_TYPE = ContentType.create("text/html", MESSAGE_CHARSET);
	public static final ContentType PLAIN_MESSAGE_CONTENT_TYPE = ContentType.create("text/plain", MESSAGE_CHARSET);

	public static final long LIMIT_50 = 50;
	static private Log log = LogFactory.getLog(SynapseClientImpl.class);

	private final Supplier<Set<String>> htmlTeamMembersCache = Suppliers.memoizeWithExpiration(teamMembersSupplier(), 1, TimeUnit.HOURS);

	public Set<String> getHtmlTeamMembers() {
		return htmlTeamMembersCache.get();
	}

	private Supplier<Set<String>> teamMembersSupplier() {
		return new Supplier<Set<String>>() {
			public Set<String> get() {
				Set<String> userIdSet = new HashSet<String>();
				try {
					org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
					long currentOffset = 0;
					List<TeamMember> teamMembers = null;
					do {
						org.sagebionetworks.reflection.model.PaginatedResults<TeamMember> teamMembersPaginatedResults = synapseClient.getTeamMembers(htmlTeamId, null, LIMIT_50, currentOffset);
						teamMembers = teamMembersPaginatedResults.getResults();
						for (TeamMember teamMember : teamMembers) {
							userIdSet.add(teamMember.getMember().getOwnerId());
						}
						currentOffset += LIMIT_50;
					} while (teamMembers != null && !teamMembers.isEmpty());
				} catch (SynapseException e) {
					log.error(e.getMessage());

				}
				return userIdSet;
			}
		};
	}

	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	private volatile HashMap<String, org.sagebionetworks.web.shared.WikiPageKey> pageName2WikiKeyMap;
	private volatile HashSet<String> wikiBasedEntities;

	public Project getProject(String projectId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return (Project) synapseClient.getEntityById(projectId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public EntityBundle getEntityBundle(String entityId, EntityBundleRequest bundleRequest) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getEntityBundleV2(entityId, bundleRequest);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public EntityBundle getEntityBundleForVersion(String entityId, Long versionNumber, EntityBundleRequest bundleRequest) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getEntityBundleV2(entityId, versionNumber, bundleRequest);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public SearchResults search(SearchQuery searchQuery) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.search(searchQuery);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (UnsupportedEncodingException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	/**
	 * Helper to convert from the non-gwt compatible PaginatedResults to the compatible type.
	 * 
	 * @param in
	 * @return
	 */
	public <T extends JSONEntity> PaginatedResults<T> convertPaginated(org.sagebionetworks.reflection.model.PaginatedResults<T> in) {
		return new PaginatedResults<T>(in.getResults(), in.getTotalNumberOfResults());
	}

	@Override
	public Entity moveEntity(String entityId, String newParentEntityId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			Entity entity = synapseClient.getEntityById(entityId);
			entity.setParentId(newParentEntityId);
			return synapseClient.putEntity(entity);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void deleteEntityVersionById(String entityId, Long versionNumber) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.deleteEntityVersionById(entityId, versionNumber);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<TrashedEntity> viewTrashForUser(long offset, long limit) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return convertPaginated(synapseClient.viewTrashForUser(offset, limit));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void restoreFromTrash(String entityId, String newParentId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.restoreFromTrash(entityId, newParentId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void purgeMultipleTrashedEntitiesForUser(Set<String> entityIds) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			for (String entityId : entityIds) {
				synapseClient.purgeTrashForUser(entityId);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public UserProfile getUserProfile(String userId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			UserProfile profile;
			if (userId == null) {
				profile = synapseClient.getMyProfile();
			} else {
				profile = synapseClient.getUserProfile(userId);
			}
			return profile;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public Team getTeam(String teamId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getTeam(teamId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	private String getUserIdFromUsername(String username) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PrincipalAliasRequest request = new PrincipalAliasRequest();
			request.setAlias(username);
			request.setType(AliasType.USER_NAME);
			PrincipalAliasResponse response = synapseClient.getPrincipalAlias(request);
			return response.getPrincipalId().toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public UserProfile getUserProfileFromUsername(String username) throws RestServiceException {
		String userId = getUserIdFromUsername(username);
		return getUserProfile(userId);
	}

	@Override
	public void updateUserProfile(UserProfile profile) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.updateMyProfile(profile);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void addEmail(EmailValidationSignedToken emailValidationSignedToken) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.addEmail(emailValidationSignedToken, true);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void setNotificationEmail(String email) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.setNotificationEmail(email);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void removeEmail(String email) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.removeEmail(email);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public AccessControlList getBenefactorAcl(String id) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			EntityBundleRequest bundleRequest = new EntityBundleRequest();
			bundleRequest.setIncludeBenefactorACL(true);
			return synapseClient.getEntityBundleV2(id, bundleRequest).getBenefactorAcl();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AccessControlList getEntityBenefactorAcl(String id) throws RestServiceException {
		return getBenefactorAcl(id);
	}

	@Override
	public AccessControlList createAcl(AccessControlList acl) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createACL(acl);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AccessControlList updateAcl(AccessControlList acl, boolean recursive) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateACL(acl, recursive);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public AccessControlList updateTeamAcl(AccessControlList acl) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateTeamACL(acl);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AccessControlList getTeamAcl(String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getTeamACL(teamId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AccessControlList deleteAcl(String ownerEntityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// first delete the ACL
			synapseClient.deleteACL(ownerEntityId);
			// now get the ACL governing this entity, which will be some
			// ancestor, the 'permissions benefactor'
			return getBenefactorAcl(ownerEntityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public boolean hasAccess(String ownerId, String ownerType, String accessType) throws RestServiceException {
		ObjectType type = ObjectType.valueOf(ownerType);
		ACCESS_TYPE access = ACCESS_TYPE.valueOf(accessType);
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.canAccess(ownerId, type, access);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AccessRequirement createOrUpdateAccessRequirement(AccessRequirement ar) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (ar.getId() == null) {
				return synapseClient.createAccessRequirement(ar);
			} else {
				return synapseClient.updateAccessRequirement(ar);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	private List<AccessRequirement> getAllAccessRequirements(boolean unmetOnly, RestrictableObjectDescriptor subjectId, ACCESS_TYPE accessType, org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException {
		List<AccessRequirement> allAccessRequirements = new ArrayList<AccessRequirement>();
		long offset = ZERO_OFFSET;
		boolean isDone = false;
		while (!isDone) {
			List<AccessRequirement> accessRequirments;
			if (unmetOnly) {
				accessRequirments = synapseClient.getUnmetAccessRequirements(subjectId, accessType, LIMIT_50, offset).getResults();
			} else {
				accessRequirments = synapseClient.getAccessRequirements(subjectId, LIMIT_50, offset).getResults();
			}
			isDone = accessRequirments.size() < LIMIT_50;
			allAccessRequirements.addAll(accessRequirments);
			offset += LIMIT_50;
		}
		return allAccessRequirements;
	}

	@Override
	public List<AccessRequirement> getTeamAccessRequirements(String teamId) throws RestServiceException {
		return getTeamAccessRequirements(teamId, false);
	}

	private List<AccessRequirement> getTeamAccessRequirements(String teamId, boolean unmetOnly) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(teamId);
			subjectId.setType(RestrictableObjectType.TEAM);
			List<AccessRequirement> accessRequirements = getAllAccessRequirements(unmetOnly, subjectId, ACCESS_TYPE.PARTICIPATE, synapseClient);

			return accessRequirements;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public PaginatedResults<AccessRequirement> getEntityAccessRequirements(String entityId, boolean unmetOnly, ACCESS_TYPE targetAccessType) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(entityId);
			subjectId.setType(RestrictableObjectType.ENTITY);
			List<AccessRequirement> accessRequirements = getAllAccessRequirements(unmetOnly, subjectId, targetAccessType, synapseClient);

			// filter to the targetAccessType
			if (targetAccessType != null) {
				accessRequirements = AccessRequirementUtils.filterAccessRequirements(accessRequirements, targetAccessType);
			}

			return new PaginatedResults<AccessRequirement>(accessRequirements, accessRequirements.size());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AccessApproval createAccessApproval(AccessApproval aaEW) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createAccessApproval(aaEW);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void deleteAccessApprovals(String accessRequirement, String accessorId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.revokeAccessApprovals(accessRequirement, accessorId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Entity updateExternalFile(String entityId, String externalUrl, String name, String contentType, Long fileSize, String md5, Long storageLocationId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Entity entity = synapseClient.getEntityById(entityId);
			if (!(entity instanceof FileEntity)) {
				throw new RuntimeException("Upload failed. Entity id: " + entity.getId() + " is not a File.");
			}
			ExternalFileHandle clone = createExternalFileHandle(externalUrl, name, contentType, fileSize, md5, storageLocationId, synapseClient);
			((FileEntity) entity).setDataFileHandleId(clone.getId());
			Entity updatedEntity = synapseClient.putEntity(entity);
			return updatedEntity;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	private boolean isManuallySettingExternalName(String name) {
		return name != null && name.trim().length() > 0;
	}

	public static String getFileNameFromExternalUrl(String path) {
		// grab the text between the last '/' and following '?'
		String fileName = "";
		if (path != null) {
			int lastSlash = path.lastIndexOf("/");
			if (lastSlash > -1) {
				int firstQuestionMark = path.indexOf("?", lastSlash);
				if (firstQuestionMark > -1) {
					fileName = path.substring(lastSlash + 1, firstQuestionMark);
				} else {
					fileName = path.substring(lastSlash + 1);
				}
			}
		}
		return fileName;
	}

	private ExternalFileHandle createExternalFileHandle(String externalUrl, String name, String contentType, Long fileSize, String md5, Long storageLocationId, org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException {
		boolean isManuallySettingName = isManuallySettingExternalName(name);
		ExternalFileHandle efh = new ExternalFileHandle();
		efh.setExternalURL(externalUrl.trim());
		efh.setContentMd5(md5);
		efh.setContentSize(fileSize);
		efh.setContentType(contentType);
		String fileName;
		if (isManuallySettingName) {
			fileName = name;
		} else {
			fileName = getFileNameFromExternalUrl(externalUrl);
		}
		efh.setFileName(fileName);
		efh.setStorageLocationId(storageLocationId);
		return synapseClient.createExternalFileHandle(efh);
	}

	@Override
	public Entity createExternalFile(String parentEntityId, String externalUrl, String name, String contentType, Long fileSize, String md5, Long storageLocationId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			FileEntity newEntity = new FileEntity();
			ExternalFileHandle clone = createExternalFileHandle(externalUrl, name, contentType, fileSize, md5, storageLocationId, synapseClient);
			newEntity.setDataFileHandleId(clone.getId());
			newEntity.setParentId(parentEntityId);
			newEntity.setName(clone.getFileName());
			return synapseClient.createEntity(newEntity);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	private String getRootWikiId(org.sagebionetworks.client.SynapseClient synapseClient, String ownerId, ObjectType ownerType) throws RestServiceException {
		try {
			return synapseClient.getRootWikiPageKey(ownerId, ownerType).getWikiPageId();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public FileHandleResults getWikiAttachmentHandles(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				// asking for the root. find the root id first
				String rootWikiPage = getRootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
			FileHandleResults results = synapseClient.getWikiAttachmenthHandles(properKey);
			return results;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	// V2 Wiki crud

	@Override
	public V2WikiPage restoreV2WikiPage(String ownerId, String ownerType, String wikiId, Long versionToUpdate) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.restoreV2WikiPage(ownerId, ObjectType.valueOf(ownerType), wikiId, versionToUpdate);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void deleteV2WikiPage(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
			synapseClient.deleteV2WikiPage(properKey);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public FileHandleResults getV2WikiAttachmentHandles(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				// asking for the root. find the root id first
				String rootWikiPage = getRootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
			return synapseClient.getV2WikiAttachmentHandles(properKey);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<V2WikiHistorySnapshot> getV2WikiHistory(org.sagebionetworks.web.shared.WikiPageKey key, Long limit, Long offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
			return convertPaginated(synapseClient.getV2WikiHistory(properKey, limit, offset));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public WikiPage createV2WikiPageWithV1(String ownerId, String ownerType, WikiPage page) throws IOException, RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createWikiPage(ownerId, ObjectType.valueOf(ownerType), page);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public WikiPage updateV2WikiPageWithV1(String ownerId, String ownerType, WikiPage page) throws IOException, RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateWikiPage(ownerId, ObjectType.valueOf(ownerType), page);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	private String getWikiKeyId(org.sagebionetworks.client.SynapseClient synapseClient, org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException {
		String wikiPageId = key.getWikiPageId();
		if (wikiPageId == null) {
			// asking for the root. find the root id first
			wikiPageId = getRootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
		}
		return wikiPageId;
	}

	public WikiPage getV2WikiPageAsV1(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException, IOException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), getWikiKeyId(synapseClient, key));
		try {
			return synapseClient.getWikiPage(properKey);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public EntityHeader addFavorite(String entityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.addFavorite(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void removeFavorite(String entityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.removeFavorite(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public void deleteTeam(String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.deleteTeam(teamId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public long getTeamMemberCount(String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.countTeamMembers(teamId, null);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public TeamMemberPagedResults getTeamMembers(String teamId, String fragment, TeamMemberTypeFilterOptions memberType, Integer limit, Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.reflection.model.PaginatedResults<TeamMember> members = synapseClient.getTeamMembers(teamId, fragment, memberType, limit, offset);
			List<TeamMember> teamMembers = members.getResults();

			// gather user ids to ask for all user profiles in bulk
			List<Long> userIds = new ArrayList<Long>();
			for (TeamMember member : members.getResults()) {
				userIds.add(Long.parseLong(member.getMember().getOwnerId()));
			}
			List<UserProfile> profiles = synapseClient.listUserProfiles(userIds);
			List<TeamMemberBundle> teamMemberBundles = new ArrayList<TeamMemberBundle>();
			for (int i = 0; i < userIds.size(); i++) {
				teamMemberBundles.add(new TeamMemberBundle(profiles.get(i), teamMembers.get(i).getIsAdmin(), teamMembers.get(i).getTeamId()));
			}
			TeamMemberPagedResults results = new TeamMemberPagedResults();
			results.setResults(teamMemberBundles);
			results.setTotalNumberOfResults(members.getTotalNumberOfResults());
			return results;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public List<UserProfile> listUserProfiles(List<String> userIds, org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException {
		List<Long> userIdsLong = new LinkedList<Long>();
		for (String idString : userIds) {
			userIdsLong.add(Long.parseLong(idString));
		}
		return synapseClient.listUserProfiles(userIdsLong);
	}

	public PaginatedResults<Team> getTeamsBySearch(String searchTerm, Integer limit, Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (searchTerm != null && searchTerm.trim().length() == 0)
				searchTerm = null;
			if (offset == null)
				offset = ZERO_OFFSET.intValue();
			return convertPaginated(synapseClient.getTeams(searchTerm, limit, offset));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public TeamMembershipStatus getTeamMembershipState(String currentUserId, String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getTeamMembershipStatus(teamId, currentUserId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public TeamMembershipStatus requestMembership(String currentUserId, String teamId, String message, String hostPageBaseURL, Date expiresOn) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String signedTokenEndpoint = getSignedTokenEndpoint(hostPageBaseURL);
			TeamMembershipStatus membershipStatus = synapseClient.getTeamMembershipStatus(teamId, currentUserId);
			// if we can join the team without creating the request (like if we
			// are a team admin, or there is an open invitation), then just do
			// that!
			if (membershipStatus.getCanJoin()) {
				synapseClient.addTeamMember(teamId, currentUserId, getTeamEndpoint(hostPageBaseURL), signedTokenEndpoint);
			} else if (!membershipStatus.getHasOpenRequest()) {
				// otherwise, create the request
				MembershipRequest membershipRequest = new MembershipRequest();
				membershipRequest.setMessage(message);
				membershipRequest.setTeamId(teamId);
				membershipRequest.setUserId(currentUserId);
				if (expiresOn != null) {
					membershipRequest.setExpiresOn(expiresOn);
				}

				// make new Synapse call
				synapseClient.createMembershipRequest(membershipRequest, signedTokenEndpoint, signedTokenEndpoint);
			}
			return synapseClient.getTeamMembershipStatus(teamId, currentUserId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}

	}

	@Override
	public void addTeamMember(String userGroupId, String teamId, String message, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String signedTokenEndpoint = getSignedTokenEndpoint(hostPageBaseURL);
			synapseClient.addTeamMember(teamId, userGroupId, getTeamEndpoint(hostPageBaseURL), signedTokenEndpoint);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void inviteMember(String userGroupId, String teamId, String message, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String signedTokenEndpoint = getSignedTokenEndpoint(hostPageBaseURL);

			TeamMembershipStatus membershipStatus = synapseClient.getTeamMembershipStatus(teamId, userGroupId);
			if (membershipStatus.getIsMember()) {
				return;
			}
			// if we can join the team without creating the invite (like if we
			// are a team admin, or there is an open membership request), then
			// just do that!
			if (membershipStatus.getCanJoin()) {
				synapseClient.addTeamMember(teamId, userGroupId, getTeamEndpoint(hostPageBaseURL), signedTokenEndpoint);
			} else if (!membershipStatus.getHasOpenInvitation()) {
				// check to see if there is already an open invite
				MembershipInvitation membershipInvite = new MembershipInvitation();
				membershipInvite.setMessage(message);
				membershipInvite.setTeamId(teamId);
				membershipInvite.setInviteeId(userGroupId);

				synapseClient.createMembershipInvitation(membershipInvite, signedTokenEndpoint, signedTokenEndpoint);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void inviteNewMember(String email, String teamId, String message, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			MembershipInvitation mis = new MembershipInvitation();
			mis.setInviteeEmail(email);
			mis.setTeamId(teamId);
			mis.setMessage(message);
			String signedTokenEndpoint = getSignedTokenEndpoint(hostPageBaseURL);
			synapseClient.createMembershipInvitation(mis, signedTokenEndpoint, signedTokenEndpoint);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getCertifiedUserPassingRecord(String userId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PassingRecord passingRecord = synapseClient.getCertifiedUserPassingRecord(userId);
			// This method only returns the PassingRecord if the user actually
			// passed (portal does not currently care about the top failed
			// attempt).
			if (passingRecord.getPassed() == null || !passingRecord.getPassed()) {
				throw new NotFoundException("The user has not passed the certification quiz.");
			}
			JSONObjectAdapter passingRecordJson = passingRecord.writeToJSONObject(adapterFactory.createNew());
			return passingRecordJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getCertificationQuiz() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Quiz quiz = synapseClient.getCertifiedUserTest();
			JSONObjectAdapter quizJson = quiz.writeToJSONObject(adapterFactory.createNew());
			return quizJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public PassingRecord submitCertificationQuizResponse(QuizResponse response) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.submitCertifiedUserTestResponse(response);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Boolean isTeamMember(String userId, Long groupPrincipalId) throws RestServiceException {
		Boolean isMember = null;
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			TeamMembershipStatus membershipStatus = synapseClient.getTeamMembershipStatus(groupPrincipalId.toString(), userId);
			isMember = membershipStatus.getIsMember();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
		return isMember;
	}

	@Override
	public TeamBundle getTeamBundle(String userId, String teamId, boolean isLoggedIn) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			long memberCount = synapseClient.countTeamMembers(teamId, null);
			boolean isAdmin = false;
			Team team = synapseClient.getTeam(teamId);
			TeamMembershipStatus membershipStatus = null;
			// get membership state for the current user
			if (isLoggedIn) {
				membershipStatus = synapseClient.getTeamMembershipStatus(teamId, userId);
				if (membershipStatus.getIsMember()) {
					TeamMember teamMember = synapseClient.getTeamMember(teamId, userId);
					isAdmin = teamMember.getIsAdmin();
				}
			}
			return new TeamBundle(team, memberCount, membershipStatus, isAdmin);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public List<MembershipRequestBundle> getOpenRequests(String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.reflection.model.PaginatedResults<MembershipRequest> requests = synapseClient.getOpenMembershipRequests(teamId, null, MAX_LIMIT, ZERO_OFFSET);
			// and ask for the team info for each invite, and fill that in the
			// bundle
			List<Long> userIds = new ArrayList<>();
			for (MembershipRequest request : requests.getResults()) {
				userIds.add(Long.parseLong(request.getUserId()));
			}
			Map<String, UserProfile> userId2UserProfile = getUserProfiles(userIds, synapseClient);
			ArrayList<MembershipRequestBundle> returnList = new ArrayList<MembershipRequestBundle>();
			// now go through and create a MembershipRequestBundle for each pair
			for (MembershipRequest request : requests.getResults()) {
				UserProfile profile = userId2UserProfile.get(request.getUserId());
				MembershipRequestBundle b = new MembershipRequestBundle(profile, request);
				returnList.add(b);
			}
			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	private Map<String, UserProfile> getUserProfiles(List<Long> userIds, org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException {
		Map<String, UserProfile> userId2UserProfile = new HashMap<>();
		List<UserProfile> profiles = synapseClient.listUserProfiles(userIds);
		for (UserProfile userProfile : profiles) {
			userId2UserProfile.put(userProfile.getOwnerId(), userProfile);
		}
		return userId2UserProfile;
	}

	@Override
	public ArrayList<OpenUserInvitationBundle> getOpenInvitations(String userId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.reflection.model.PaginatedResults<MembershipInvitation> invitations = synapseClient.getOpenMembershipInvitations(userId, null, MAX_LIMIT, ZERO_OFFSET);
			// and ask for the team info for each invite, and fill that in the
			// bundle

			ArrayList<OpenUserInvitationBundle> returnList = new ArrayList<OpenUserInvitationBundle>();
			// now go through and create a MembershipInvitationBundle for each
			// pair

			for (MembershipInvitation invite : invitations.getResults()) {
				Team team = synapseClient.getTeam(invite.getTeamId());
				OpenUserInvitationBundle b = new OpenUserInvitationBundle(team, invite);
				returnList.add(b);
			}

			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void resendTeamInvitation(String membershipInvitationId, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// resend the invitation. get the invitation, clear out system specified values, re-create the
			// invitation, and delete the old one.
			String signedTokenEndpoint = getSignedTokenEndpoint(hostPageBaseURL);
			MembershipInvitation membershipInvitation = synapseClient.getMembershipInvitation(membershipInvitationId);
			membershipInvitation.setCreatedBy(null);
			membershipInvitation.setCreatedOn(null);
			membershipInvitation.setId(null);

			// SWC-4360: if both the principal ID and email are set, then clear out the email in the new invite
			// to avoid a backend error.
			if (membershipInvitation.getInviteeId() != null && membershipInvitation.getInviteeEmail() != null) {
				membershipInvitation.setInviteeEmail(null);
			}

			synapseClient.createMembershipInvitation(membershipInvitation, signedTokenEndpoint, signedTokenEndpoint);
			synapseClient.deleteMembershipInvitation(membershipInvitationId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public ArrayList<OpenTeamInvitationBundle> getOpenTeamInvitations(String teamId, Integer limit, Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.reflection.model.PaginatedResults<MembershipInvitation> invitations = synapseClient.getOpenMembershipInvitationSubmissions(teamId, null, limit, offset);
			// and ask for the team info for each invite, and fill that in the
			// bundle

			ArrayList<OpenTeamInvitationBundle> returnList = new ArrayList<OpenTeamInvitationBundle>();
			// now go through and create a MembershipInvitationBundle for each
			// pair

			List<MembershipInvitation> invitationsToUsers = new ArrayList<>();
			List<MembershipInvitation> invitationsToEmails = new ArrayList<>();
			// Sort the results into the two types
			for (MembershipInvitation invite : invitations.getResults()) {
				String inviteeId = invite.getInviteeId();
				String inviteeEmail = invite.getInviteeEmail();
				if (inviteeId != null) {
					invitationsToUsers.add(invite);
				} else if (inviteeEmail != null) {
					invitationsToEmails.add(invite);
				}
			}

			// Get profiles of existing user invitees
			List<Long> userIds = new ArrayList<>();
			for (MembershipInvitation invite : invitationsToUsers) {
				userIds.add(Long.parseLong(invite.getInviteeId()));
			}
			Map<String, UserProfile> userId2UserProfile = getUserProfiles(userIds, synapseClient);
			// Add invitations to return list
			for (MembershipInvitation invite : invitationsToUsers) {
				UserProfile profile = userId2UserProfile.get(invite.getInviteeId());
				OpenTeamInvitationBundle b = new OpenTeamInvitationBundle(invite, profile);
				returnList.add(b);
			}
			for (MembershipInvitation invite : invitationsToEmails) {
				// Invitations to new users have a null profile
				returnList.add(new OpenTeamInvitationBundle(invite, null));
			}

			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void setIsTeamAdmin(String currentUserId, String targetUserId, String teamId, boolean isTeamAdmin) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.setTeamMemberPermissions(teamId, targetUserId, isTeamAdmin);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Team updateTeam(Team team, AccessControlList teamAcl) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			updateTeamAcl(teamAcl);
			return synapseClient.updateTeam(team);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	/**
	 * Gets the ID of the file entity with the given name whose parent has the given ID.
	 * 
	 * @param fileName The name of the entity to find.
	 * @param parentEntityId The ID of the parent that the found entity must have.
	 * @return The ID of the file entity with the given name and parent ID.
	 * @throws NotFoundException If no file with given name and parent ID was found.
	 * @throws ConflictException If an entity with given name and parent ID was found, but that entity
	 *         was not a File Entity.
	 */
	@Override
	public String getFileEntityIdWithSameName(String fileName, String parentEntityId) throws RestServiceException, SynapseException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String entityId = synapseClient.lookupChild(parentEntityId, fileName);
			Entity entity = synapseClient.getEntityById(entityId);
			if (!(entity instanceof FileEntity)) {
				throw new ConflictException("name conflict");
			}
			return entityId;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// create entity if we have to
			FileEntity fileEntity = null;
			FileHandle newHandle = synapseClient.getRawFileHandle(fileHandleId);
			if (entityId == null) {
				fileEntity = FileHandleServlet.getNewFileEntity(parentEntityId, fileHandleId, newHandle.getFileName(), synapseClient);
			} else {
				// get the file entity to update
				fileEntity = (FileEntity) synapseClient.getEntityById(entityId);
				// update data file handle id
				fileEntity.setDataFileHandleId(fileHandleId);
				fileEntity = synapseClient.putEntity(fileEntity);
			}
			return fileEntity.getId();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public static class PortalVersionHolder {
		private static String versionInfo = "";
		static {
			InputStream s = SynapseClientImpl.class.getResourceAsStream("/version-info.properties");
			Properties prop = new Properties();
			try {
				prop.load(s);
			} catch (IOException e) {
				throw new RuntimeException("version-info.properties file not found", e);
			}
			versionInfo = prop.getProperty("org.sagebionetworks.portal.version");
		}

		public static String getVersionInfo() {
			return versionInfo;
		}
	}

	private String getSynapseProperty(String key) {
		return PortalPropertiesHolder.getProperty(key);
	}

	public static class PortalPropertiesHolder {
		private static Properties props;
		private static HashMap<String, String> propsMap;

		static {
			InputStream s = SynapseClientImpl.class.getResourceAsStream("/portal.properties");
			props = new Properties();
			try {
				props.load(s);
			} catch (IOException e) {
				throw new RuntimeException("portal.properties file not found", e);
			}
		}

		public static String getProperty(String key) {
			return props.getProperty(key);
		}

		public static HashMap<String, String> getPropertiesMap() {
			if (propsMap == null) {
				propsMap = new HashMap<String, String>();
				for (Entry<Object, Object> entry : props.entrySet()) {
					propsMap.put(entry.getKey().toString(), entry.getValue().toString());
				}
			}

			return propsMap;
		}
	}

	public static Long defaultStorageLocation = Long.parseLong(PortalPropertiesHolder.getProperty(DEFAULT_STORAGE_ID_PROPERTY_KEY));
	public static String htmlTeamId = PortalPropertiesHolder.getProperty(HTML_TEAM_ID_PROPERTY_KEY);

	public static final String getSignedTokenEndpoint(String hostPageBaseURL) {
		return hostPageBaseURL + "#!SignedToken:";
	}

	@Override
	public ResponseMessage handleSignedToken(SignedTokenInterface signedToken, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();

		try {
			if (signedToken instanceof JoinTeamSignedToken) {
				JoinTeamSignedToken joinTeamSignedToken = (JoinTeamSignedToken) signedToken;
				return synapseClient.addTeamMember(joinTeamSignedToken, getTeamEndpoint(hostPageBaseURL), getSignedTokenEndpoint(hostPageBaseURL));
			} else if (signedToken instanceof NotificationSettingsSignedToken) {
				NotificationSettingsSignedToken notificationSignedToken = (NotificationSettingsSignedToken) signedToken;
				return synapseClient.updateNotificationSettings(notificationSignedToken);
			} else if (signedToken instanceof NewUserSignedToken) {
				// TODO
				throw new BadRequestException("Not yet implemented");
			} else {
				throw new BadRequestException("token not supported: " + signedToken.getClass().getName());
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public SignedTokenInterface hexDecodeAndDeserialize(String signedTokenString) throws RestServiceException {
		try {
			return SerializationUtils.hexDecodeAndDeserialize(signedTokenString, SignedTokenInterface.class);
		} catch (Exception e) {
			// error decoding, respond with a bad request
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public AccountCreationToken hexDecodeAndDeserializeAccountCreationToken(String tokenString) throws RestServiceException {
		try {
			return SerializationUtils.hexDecodeAndDeserialize(tokenString, AccountCreationToken.class);
		} catch (Exception e) {
			// error decoding, respond with a bad request
			throw new BadRequestException(e.getMessage());
		}
	}

	public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String enumName) {
		if (enumName == null) {
			return false;
		}
		try {
			Enum.valueOf(enumClass, enumName);
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	public static String getTeamEndpoint(String hostPageBaseURL) {
		return hostPageBaseURL + "#!Team:";
	}

	// If this method is modified, please also modify CHALLENGE_ENDPOINT in
	// org.sagebionetworks.repo.model.ServiceConstants (PLFM)
	public static String getChallengeEndpoint(String hostPageBaseURL) {
		return hostPageBaseURL + "#!Synapse:";
	}

	@Override
	public String getAPIKey() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.retrieveApiKey();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String sendMessage(Set<String> recipients, String subject, String messageBody, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			MessageToUser message = new MessageToUser();
			message.setRecipients(recipients);
			message.setSubject(subject);
			message.setNotificationUnsubscribeEndpoint(getSignedTokenEndpoint(hostPageBaseURL));
			String cleanedMessageBody = Jsoup.clean(messageBody, "", Whitelist.simpleText().addTags("br"), new OutputSettings().prettyPrint(false));
			MessageToUser sentMessage = synapseClient.sendStringMessage(message, cleanedMessageBody);
			JSONObjectAdapter sentMessageJson = sentMessage.writeToJSONObject(adapterFactory.createNew());
			return sentMessageJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String sendMessageToEntityOwner(String entityId, String subject, String messageBody, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			MessageToUser message = new MessageToUser();
			message.setSubject(subject);
			message.setNotificationUnsubscribeEndpoint(getSignedTokenEndpoint(hostPageBaseURL));
			String cleanedMessageBody = Jsoup.clean(messageBody, Whitelist.none());
			MessageToUser sentMessage = synapseClient.sendStringMessage(message, entityId, cleanedMessageBody);
			JSONObjectAdapter sentMessageJson = sentMessage.writeToJSONObject(adapterFactory.createNew());
			return sentMessageJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}



	@Override
	public Boolean isAliasAvailable(String alias, String aliasType) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createAnonymousSynapseClient();
		try {
			AliasType type = AliasType.valueOf(aliasType);
			AliasCheckRequest request = new AliasCheckRequest();
			request.setAlias(alias);
			request.setType(type);
			AliasCheckResponse response = synapseClient.checkAliasAvailable(request);
			return response.getAvailable();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public HashMap<String, org.sagebionetworks.web.shared.WikiPageKey> getPageNameToWikiKeyMap() throws RestServiceException {
		initHelpPagesMap();
		return pageName2WikiKeyMap;
	}

	private void initHelpPagesMap() {
		if (pageName2WikiKeyMap == null) {
			HashMap<String, org.sagebionetworks.web.shared.WikiPageKey> tempMap = new HashMap<String, org.sagebionetworks.web.shared.WikiPageKey>();
			HashMap<String, String> properties = PortalPropertiesHolder.getPropertiesMap();
			for (String key : properties.keySet()) {
				if (key.startsWith(WebConstants.WIKI_PROPERTIES_PACKAGE)) {
					String value = properties.get(key);
					String[] tokens = value.split("/");
					String synId = null;
					String wikiId = null;
					if (tokens.length == 2) {
						synId = tokens[0];
						wikiId = tokens[1];
					} else if (tokens.length == 1) {
						synId = value;
					}
					tempMap.put(key.substring(WebConstants.WIKI_PROPERTIES_PACKAGE.length()), new org.sagebionetworks.web.shared.WikiPageKey(synId, ObjectType.ENTITY.toString(), wikiId));
				}
			}

			pageName2WikiKeyMap = tempMap;
		}
	}

	public Set<String> getWikiBasedEntities() throws RestServiceException {
		initWikiEntities();
		return wikiBasedEntities;
	}

	private void initWikiEntities() {
		if (wikiBasedEntities == null) {
			HashSet<String> tempSet = new HashSet<String>();
			tempSet.add(getSynapseProperty(WebConstants.FORMATTING_GUIDE_ENTITY_ID_PROPERTY));
			// because wikiBasedEntities is volatile, current state will be
			// reflected in all threads
			wikiBasedEntities = tempSet;
		}
	}

	@Override
	public String deleteApiKey() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.invalidateApiKey();
			return getAPIKey();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public TableUpdateTransactionRequest getTableUpdateTransactionRequest(String tableId, List<ColumnModel> oldSchema, List<ColumnModel> proposedSchema) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// Create any models that do not have an ID, or that have changed
			Map<String, ColumnModel> oldColumnModelId2Model = new HashMap<String, ColumnModel>();
			for (ColumnModel columnModel : oldSchema) {
				oldColumnModelId2Model.put(columnModel.getId(), columnModel);
			}

			List<ColumnModel> newSchema = new ArrayList<ColumnModel>();
			for (ColumnModel m : proposedSchema) {
				// copy column model
				ColumnModel copy = new ColumnModel();
				JSONObjectAdapter adapter = adapterFactory.createNew();
				m.writeToJSONObject(adapter);
				copy.initializeFromJSONObject(adapter);

				if (copy.getId() != null) {
					// any changes to the existing column model?
					ColumnModel oldColumnModel = oldColumnModelId2Model.get(copy.getId());
					if (oldColumnModel != null && !oldColumnModel.equals(copy)) {
						copy.setId(null);
					}
				}
				newSchema.add(copy);
			}
			newSchema = synapseClient.createColumnModels(newSchema);

			List<ColumnChange> changes = new ArrayList<ColumnChange>();
			// now that all columns have been created, figure out the column changes (create, update, and no-op)
			// keep track of column ids to figure out what columns were deleted.
			Set<String> columnIds = new HashSet<String>();
			for (int i = 0; i < proposedSchema.size(); i++) {
				String oldColumnId = proposedSchema.get(i).getId();
				String newColumnId = newSchema.get(i).getId();
				columnIds.add(oldColumnId);
				columnIds.add(newColumnId);
				if (!Objects.equals(oldColumnId, newColumnId)) {
					changes.add(createNewColumnChange(oldColumnId, newColumnId));
				}
			}
			// delete columns that are not represented in the changes already (create or update)
			for (ColumnModel oldColumnModel : oldSchema) {
				String oldColumnId = oldColumnModel.getId();
				if (!columnIds.contains(oldColumnId)) {
					changes.add(createNewColumnChange(oldColumnId, null));
				}
			}

			TableUpdateTransactionRequest request = new TableUpdateTransactionRequest();
			request.setEntityId(tableId);
			List<TableUpdateRequest> requestChangeList = new ArrayList<TableUpdateRequest>();
			TableSchemaChangeRequest newTableSchemaChangeRequest = new TableSchemaChangeRequest();
			newTableSchemaChangeRequest.setEntityId(tableId);
			newTableSchemaChangeRequest.setChanges(changes);
			List<String> orderedColumnIds = new ArrayList<>();
			for (ColumnModel cm : newSchema) {
				orderedColumnIds.add(cm.getId());
			}
			newTableSchemaChangeRequest.setOrderedColumnIds(orderedColumnIds);
			requestChangeList.add(newTableSchemaChangeRequest);
			request.setChanges(requestChangeList);
			return request;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	private ColumnChange createNewColumnChange(String oldColumnId, String newColumnId) {
		ColumnChange columnChange = new ColumnChange();
		columnChange.setOldColumnId(oldColumnId);
		columnChange.setNewColumnId(newColumnId);
		return columnChange;
	}

	@Override
	public void validateTableQuery(String sql) throws RestServiceException {
		try {
			TableQueryParser.parserQuery(sql);
		} catch (Throwable e) {
			throw new TableQueryParseException(e.getMessage());
		}
	}

	public EntityChildrenResponse getEntityChildren(EntityChildrenRequest request) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getEntityChildren(request);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String createFileHandleURL(String fileHandleId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			URL url = synapseClient.getFileHandleTemporaryUrl(fileHandleId);
			return url.toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (IOException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public List<ColumnModel> createTableColumns(List<ColumnModel> models) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createColumnModels(models);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Cannot safely cast " + l + " to int without changing the value.");
		}
		return (int) l;
	}

	public String getHost(String urlString) throws RestServiceException {
		if (urlString == null || urlString.length() == 0) {
			throw new IllegalArgumentException("url is required");
		}
		// URL does not recognize sftp:// protocol. replace with http (we're after the host in this method)
		if (urlString.toLowerCase().startsWith(WebConstants.SFTP_PREFIX)) {
			urlString = "http://" + urlString.substring(WebConstants.SFTP_PREFIX.length());
		}
		try {
			URL url = new URL(urlString);
			return url.getHost();
		} catch (MalformedURLException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public List<String> getMyLocationSettingBanners() throws RestServiceException {
		try {
			Set<String> banners = new HashSet<String>();
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			List<StorageLocationSetting> existingStorageLocations = synapseClient.getMyStorageLocationSettings();
			for (StorageLocationSetting storageLocationSetting : existingStorageLocations) {
				if (storageLocationSetting.getBanner() != null) {
					banners.add(storageLocationSetting.getBanner());
				}
			}
			return new ArrayList<String>(banners);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public StorageLocationSetting getStorageLocationSetting(String parentEntityId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			UploadDestination uploadDestination = synapseClient.getDefaultUploadDestination(parentEntityId);
			if (uploadDestination == null || uploadDestination.getStorageLocationId().equals(defaultStorageLocation)) {
				// default storage location
				return null;
			}

			// else
			return synapseClient.getMyStorageLocationSetting(uploadDestination.getStorageLocationId());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void createStorageLocationSetting(String parentEntityId, StorageLocationSetting setting) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			// first, try to find a matching storage location setting for this user, and reuse
			Long locationId = null;
			if (setting != null) {
					locationId = synapseClient.createStorageLocationSetting(setting).getStorageLocationId();
			} else {
				locationId = defaultStorageLocation;
			}

			ArrayList<Long> locationIds = new ArrayList<Long>();
			locationIds.add(locationId);

			// update existing upload destination project/folder setting
			UploadDestinationListSetting projectSetting = (UploadDestinationListSetting) synapseClient.getProjectSetting(parentEntityId, ProjectSettingsType.upload);
			if (projectSetting != null) {
				projectSetting.setLocations(locationIds);
				synapseClient.updateProjectSetting(projectSetting);
			} else {
				// create new upload destination project/folder setting
				projectSetting = new UploadDestinationListSetting();
				projectSetting.setProjectId(parentEntityId);
				projectSetting.setSettingsType(ProjectSettingsType.upload);
				projectSetting.setLocations(locationIds);
				synapseClient.createProjectSetting(projectSetting);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Entity updateFileEntity(FileEntity toUpdate, FileHandleCopyRequest copyRequest) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			BatchFileHandleCopyRequest batchRequest = new BatchFileHandleCopyRequest();
			batchRequest.setCopyRequests(Collections.singletonList(copyRequest));
			BatchFileHandleCopyResult batchCopyResults = synapseClient.copyFileHandles(batchRequest);
			List<FileHandleCopyResult> copyResults = batchCopyResults.getCopyResults();
			// sanity check
			if (copyResults.size() != 1) {
				throw new UnknownErrorException("Copy file handle resulted in unexpected response list size.");
			}
			FileHandleCopyResult copyResult = copyResults.get(0);
			if (copyResult.getFailureCode() != null) {
				switch (copyResult.getFailureCode()) {
					case NOT_FOUND:
						throw new NotFoundException();
					case UNAUTHORIZED:
						throw new UnauthorizedException();
					default:
						throw new UnknownErrorException();
				}
			} else {
				FileHandle newFileHandle = copyResult.getNewFileHandle();
				toUpdate.setDataFileHandleId(newFileHandle.getId());
				return synapseClient.putEntity(toUpdate);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String generateSqlWithFacets(String basicSql, List<FacetColumnRequest> selectedFacets, List<ColumnModel> schema) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			TransformSqlWithFacetsRequest request = new TransformSqlWithFacetsRequest();
			request.setSqlToTransform(basicSql);
			request.setSelectedFacets(selectedFacets);
			request.setSchema(schema);
			return synapseClient.transformSqlRequest(request);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public ColumnModelPage getPossibleColumnModelsForViewScope(ViewScope scope, String nextPageToken) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getPossibleColumnModelsForViewScope(scope, nextPageToken);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Boolean isUserAllowedToRenderHTML(String userId) throws RestServiceException {
		return getHtmlTeamMembers().contains(userId);
	}

	@Override
	public boolean isChallenge(String projectId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return isChallenge(projectId, synapseClient);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	private boolean isChallenge(String projectId, org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException {
		// are there any evaluations that the current user can edit?
		try {
			Challenge challenge = synapseClient.getChallengeForProject(projectId);
			// found a challenge!
			// return true if user has edit access to the project
			return synapseClient.canAccess(challenge.getProjectId(), ACCESS_TYPE.UPDATE);
		} catch (Exception e) {
			return ChallengeClientImpl.getShareableEvaluations(projectId, synapseClient).size() > 0;
		}
	}

	@Override
	public void deleteAccessRequirement(Long accessRequirementId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.deleteAccessRequirement(accessRequirementId);

		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}

	}

	@Override
	public String createExternalObjectStoreFileHandle(ExternalObjectStoreFileHandle fileHandle) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.createExternalObjectStoreFileHandle(fileHandle).getId();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public ArrayList<String[]> parseCsv(String csvPreviewText, char delimiter) throws RestServiceException {
		ArrayList<String[]> results = new ArrayList<>();
		if (csvPreviewText == null || csvPreviewText.isEmpty()) {
			return results;
		}

		try (CSVReader reader = new CSVReader(new StringReader(csvPreviewText), delimiter)) {
			String[] row = null;
			while ((row = reader.readNext()) != null) {
				results.add(row);
			}
		} catch (IOException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
		return results;
	}
}
