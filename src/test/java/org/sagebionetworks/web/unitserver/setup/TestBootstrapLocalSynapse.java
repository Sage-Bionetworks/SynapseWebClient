package org.sagebionetworks.web.unitserver.setup;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseAdminClientImpl;
import org.sagebionetworks.client.SynapseClientImpl;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.NewIntegrationTestUser;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldCondition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.EntityType;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.versionInfo.SynapseVersionInfo;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * This is an executable that can be used to setup test users, projects, and
 * access restrictions on a locally ruining instance of Synapse.
 * 
 * All methods are idempotent so it will create objects that do not exist and find
 * objects that do exist.
 * 
 * Note: StackConfiguration provides all endpoints used by this class. If your settings.xml
 * set to point to staging endpoints, this utility will not work.
 * 
 * All created users will have a password="password"
 * 
 * @author jhill
 *
 */
public class TestBootstrapLocalSynapse {

	private static final String UPLOAD_RESTRCTION_OWNER = "upload-restriction-owner";
	private static final String DOWNLOAD_RESTRCTION_OWNER = "download-restriction-owner";
	private static final String CERTIFIED_USER = "certifiedUser";
	private static final String NOT_CERTIFIED_USER = "notCertifiedUser";
	private static final String PASSWORD = "password";
	private static final Long ACT_TEAM_ID=464532L;
	SynapseAdminClientImpl adminClient = new SynapseAdminClientImpl();
	SynapseClientImpl synapseClient = new SynapseClientImpl();
	List<UserData> userData = new LinkedList<UserData>();
	List<ProjectData> projectData = new LinkedList<ProjectData>();
	
	TestBootstrapLocalSynapse() throws SynapseException, JSONObjectAdapterException{
		adminClient = new SynapseAdminClientImpl();
		adminClient.setAuthEndpoint(StackConfiguration.getAuthenticationServicePrivateEndpoint());
		adminClient.setRepositoryEndpoint(StackConfiguration.getRepositoryServiceEndpoint());
		adminClient.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
		adminClient.setUserName(StackConfiguration.getMigrationAdminUsername());
		adminClient.setApiKey(StackConfiguration.getMigrationAdminAPIKey());
		SynapseVersionInfo info = adminClient.getVersionInfo();
		System.out.println(info);
		synapseClient = new SynapseAdminClientImpl();
		synapseClient.setAuthEndpoint(StackConfiguration.getAuthenticationServicePrivateEndpoint());
		synapseClient.setRepositoryEndpoint(StackConfiguration.getRepositoryServiceEndpoint());
		synapseClient.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
	}
	
	/**
	 * Main entry.  Call to trigger the idempotent bootstrap.
	 * @param args
	 * @throws SynapseException
	 */
	public static void main(String[] args) throws Exception {
		TestBootstrapLocalSynapse  tbls = new TestBootstrapLocalSynapse();
		// notCertifiedUser
		boolean accetsTermsOfUse = true;
		boolean isCertifiedUser = false;
		boolean isAccessComplianceTeamMember = false;
		Long notCertifiedUserId = tbls.createOrGetUser(NOT_CERTIFIED_USER, accetsTermsOfUse, isCertifiedUser, isAccessComplianceTeamMember);
		
		// certifiedUser
		accetsTermsOfUse = true;
		isCertifiedUser = true;
		isAccessComplianceTeamMember = false;
		Long certifiedUserId =  tbls.createOrGetUser(CERTIFIED_USER, accetsTermsOfUse, isCertifiedUser, isAccessComplianceTeamMember);
		
		// upload-restriction-owner
		accetsTermsOfUse = true;
		isCertifiedUser = true;
		isAccessComplianceTeamMember = true;
		tbls.createOrGetUser(UPLOAD_RESTRCTION_OWNER, accetsTermsOfUse, isCertifiedUser, isAccessComplianceTeamMember);
		
		// download-restriction-owner
		accetsTermsOfUse = true;
		isCertifiedUser = true;
		isAccessComplianceTeamMember = true;
		tbls.createOrGetUser(DOWNLOAD_RESTRCTION_OWNER, accetsTermsOfUse, isCertifiedUser, isAccessComplianceTeamMember);
		
		boolean isPublic = true;
		tbls.createOrGetProject("A Public Project", CERTIFIED_USER, isPublic);
		isPublic = false;
		tbls.createOrGetProject("A Private Project", CERTIFIED_USER, isPublic);
		isPublic = true;
		String uploadId = tbls.createOrGetProject("Has Upload Restrictions", UPLOAD_RESTRCTION_OWNER, isPublic);
		isPublic = true;
		String downloadId = tbls.createOrGetProject("Has Download Restrictions", DOWNLOAD_RESTRCTION_OWNER, isPublic);
		// Add access restrictions.
		tbls.addAccessRestriction(uploadId, UPLOAD_RESTRCTION_OWNER, ACCESS_TYPE.UPLOAD, "Do you agree that uploading is the best?");
		tbls.addAccessRestriction(downloadId, DOWNLOAD_RESTRCTION_OWNER, ACCESS_TYPE.DOWNLOAD, "Do you agree that downloading is the best?");
		// Grant both users access to the two projects
		tbls.grantAccess(uploadId, certifiedUserId, ACCESS_TYPE.DOWNLOAD, ACCESS_TYPE.UPDATE, ACCESS_TYPE.READ, ACCESS_TYPE.DELETE, ACCESS_TYPE.CREATE);
		tbls.grantAccess(uploadId, notCertifiedUserId, ACCESS_TYPE.DOWNLOAD, ACCESS_TYPE.UPDATE, ACCESS_TYPE.READ, ACCESS_TYPE.DELETE, ACCESS_TYPE.CREATE);
		tbls.grantAccess(downloadId, certifiedUserId, ACCESS_TYPE.DOWNLOAD, ACCESS_TYPE.UPDATE, ACCESS_TYPE.READ, ACCESS_TYPE.DELETE, ACCESS_TYPE.CREATE);
		tbls.grantAccess(downloadId, notCertifiedUserId, ACCESS_TYPE.DOWNLOAD, ACCESS_TYPE.UPDATE, ACCESS_TYPE.READ, ACCESS_TYPE.DELETE, ACCESS_TYPE.CREATE);
		tbls.printResults();
	}
	
	/**
	 * Add an access restriction if it does not already exist.
	 * @param entityId
	 * @param userName
	 * @param type
	 * @param termsOfUse
	 * @throws SynapseException
	 */
	private void addAccessRestriction(String entityId,
			String userName, ACCESS_TYPE type, String termsOfUse) throws SynapseException {
		loginAsUser(userName);
		RestrictableObjectDescriptor rod = new RestrictableObjectDescriptor();
		rod.setId(entityId);
		rod.setType(RestrictableObjectType.ENTITY);
		org.sagebionetworks.reflection.model.PaginatedResults<AccessRequirement> vcpr = synapseClient.getAccessRequirements(rod);
		if(vcpr != null && vcpr.getTotalNumberOfResults() > 0){
			for(AccessRequirement ar: vcpr.getResults()){
				if(ar.getAccessType().equals(type)){
					for(RestrictableObjectDescriptor erod: ar.getSubjectIds()){
						if(erod.getId().equals(entityId)){
							// Restriction already set.
							return;
						}
					}
				}
			}
		}
		TermsOfUseAccessRequirement tou = new TermsOfUseAccessRequirement();
		List<RestrictableObjectDescriptor> list = new LinkedList<RestrictableObjectDescriptor>();
		list.add(rod);
		tou.setSubjectIds(list);
		tou.setTermsOfUse(termsOfUse);
		tou.setAccessType(type);
		synapseClient.createAccessRequirement(tou);
	}

	/**
	 * Print the user information.
	 */
	private void printResults() {
		System.out.println("Users:");
		for(UserData ud: userData){
			System.out.println(ud);
		}
		System.out.println("Projects:");
		for(ProjectData pd: projectData){
			System.out.println(pd);
		}
	}

	/**
	 * Create a new User.
	 * @param username
	 * @param acceptsTermsOfUse
	 * @param isCertfiedUser
	 * @return
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	private Long createOrGetUser(String username, boolean acceptsTermsOfUse, boolean isCertfiedUser, boolean isAccessComplianceTeamMember) throws SynapseException {
		try {
			// Create a new users
			Session session = new Session();
			session.setAcceptsTermsOfUse(true);
			NewIntegrationTestUser nu = new NewIntegrationTestUser();
			nu.setSession(session);
			nu.setEmail(username + "@sagebase.org");
			nu.setUsername(username);
			nu.setPassword(PASSWORD);
			Long principalId = adminClient.createUser(nu);
			adminClient.setCertifiedUserStatus(principalId.toString(), isCertfiedUser);
			System.out.println("Created user: "+username+". Id: "+principalId);
			this.userData.add(new UserData(principalId.toString(), username));
			if(isAccessComplianceTeamMember){
				adminClient.addTeamMember(ACT_TEAM_ID.toString(), principalId.toString());
			}
			return principalId;
		} catch (Exception e) {
			return findUser(username);
		} 
	}
	
	/**
	 * Create or get a project.
	 * @param projectName
	 * @param userName
	 * @param isPublic
	 * @throws SynapseException
	 */
	public String createOrGetProject(String projectName, String userName, boolean isPublic) throws SynapseException{
		loginAsUser(userName);
		Project project = new Project();
		project.setName(projectName);
		try {
			project = synapseClient.createEntity(project);
			this.projectData.add(new ProjectData(projectName, project.getId()));
			if(isPublic){
				grantAccess(project.getId(), AuthorizationConstants.BOOTSTRAP_PRINCIPAL.PUBLIC_GROUP.getPrincipalId(), ACCESS_TYPE.READ);
			}
			return project.getId();
		} catch (Exception e) {
			String projectId = findProject(projectName);
			this.projectData.add(new ProjectData(projectName, projectId));
			return projectId;
		}

	}
	
	private void grantAccess(String entityId, Long principalId, ACCESS_TYPE...types) throws SynapseException{
		AccessControlList acl = adminClient.getACL(entityId);
		ResourceAccess ra = null;
		// Does this user already have a ResourceAccess?
		for(ResourceAccess currentRa: acl.getResourceAccess()){
			if(currentRa.getPrincipalId().equals(principalId)){
				ra = currentRa;
			}
		}
		if(ra == null){
			// Need to create a ResourceAccess for this user.
			ra = new ResourceAccess();
			ra.setAccessType(new HashSet<ACCESS_TYPE>());
			ra.setPrincipalId(principalId);
			acl.getResourceAccess().add(ra);
		}
		for(ACCESS_TYPE type: types){
			ra.getAccessType().add(type);
		}
		adminClient.updateACL(acl);
	}

	/**
	 * Find a project by name
	 * @param projectName
	 * @return
	 * @throws SynapseException
	 */
	private String findProject(String projectName) throws SynapseException {
		EntityQuery query = new EntityQuery();
		query.setFilterByType(EntityType.project);
		Condition condition = new EntityFieldCondition();
		condition.setOperator(Operator.EQUALS);
		List<Condition> conditions = new LinkedList<Condition>();
		conditions.add(EntityQueryUtils.buildCondition(EntityFieldName.name, Operator.EQUALS, projectName));
		query.setConditions(conditions);
		String projectId = synapseClient.entityQuery(query).getEntities().get(0).getId();
		return projectId;
	}

	/**
	 * Login as a user.
	 * @param userName
	 * @throws SynapseException
	 */
	private void loginAsUser(String userName) throws SynapseException {
		try {
			synapseClient.logout();
		} catch (Exception e) {}
		synapseClient.login(userName, PASSWORD);
	}
	
	/**
	 * Find a user given a username.
	 * @param username
	 * @return
	 * @throws SynapseException
	 */
	private Long findUser(String username) throws SynapseException{
		org.sagebionetworks.reflection.model.PaginatedResults<UserProfile> pr = adminClient.getUsers(0, Integer.MAX_VALUE);
		if(pr != null && pr.getResults() != null){
			for(UserProfile up: pr.getResults()){
				if(up.getUserName().equals(username)){
					System.out.println("Found user: "+username+". Id: "+up.getOwnerId());
					this.userData.add(new UserData(up.getOwnerId(), username));
					return Long.parseLong(up.getOwnerId());
				}
			}
		}
		throw new IllegalArgumentException("Cannot create or find username: "+username);
	}
	
	private class UserData {
		String userId;
		String userName;
		
		public UserData(String userId, String userName) {
			super();
			this.userId = userId;
			this.userName = userName;
		}

		@Override
		public String toString() {
			return "UserData [userId=" + userId + ", userName=" + userName
					+ "]";
		}
	}
	
	private class ProjectData{
		String name;
		String id;
		public ProjectData(String name, String id) {
			super();
			this.name = name;
			this.id = id;
		}
		@Override
		public String toString() {
			return "ProjectData [name=" + name + ", id=" + id + "]";
		}
	}

}
