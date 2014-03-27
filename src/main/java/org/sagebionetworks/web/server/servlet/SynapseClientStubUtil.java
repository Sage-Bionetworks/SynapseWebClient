package org.sagebionetworks.web.server.servlet;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.questionnaire.MultichoiceAnswer;
import org.sagebionetworks.repo.model.questionnaire.MultichoiceQuestion;
import org.sagebionetworks.repo.model.questionnaire.Question;
import org.sagebionetworks.repo.model.questionnaire.QuestionVariety;
import org.sagebionetworks.repo.model.questionnaire.Questionnaire;

//import java.util.logging.Logger;
//
//import org.mockito.Mockito;
//import org.sagebionetworks.client.SynapseClient;
//import org.sagebionetworks.repo.model.UserProfile;
//import org.sagebionetworks.schema.adapter.org.json.EntityFactory;

public class SynapseClientStubUtil {
//
//	private static Logger logger = Logger.getLogger(SynapseClientStubUtil.class.getName());
//	
//	private static final String token = "SESSION_TOKEN";
//	
//	private static TokenProvider tokenProvider;
//	private static ServiceUrlProvider urlProvider;
//	private static UserProfile userProfile;
//	private static EntityFactory entityFactory = new EntityFactory();
//	
//	static {
//		tokenProvider = new TokenProvider() {
//			
//			@Override
//			public String getSessionToken() {
//				return token;
//			}
//		};
//		
//		urlProvider = new ServiceUrlProvider();
//		
//		userProfile = new UserProfile();
//		userProfile.setEmail("jd@sagebase.org");
//		userProfile.setEtag("1");
//		userProfile.setFirstName("John");
//		userProfile.setLastName("Doe");
//		userProfile.setUserName("John Doe");
//		userProfile.setOwnerId("3");
//	}
//	
//	public static SynapseClient createSynapseClient() {
//		// Create a new syanpse
//		SynapseClient syn = Mockito.mock(SynapseClient.class);
//		
//		try {
//			//configure(syn);
//		} catch (Exception e) {
//			logger.warning(e.getMessage());			
//		}
//		
//		syn.setSessionToken(tokenProvider.getSessionToken());
//		syn.setRepositoryEndpoint(urlProvider
//				.getRepositoryServiceUrl());
//		syn.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
//		return syn;
//	}
//	
//	public static TokenProvider getTokenProvider() {
//		return tokenProvider;
//	}

	/*
	 * Private Methods
	 */
//	private static void configure(Synapse syn) throws Exception {
//		configureLogin(syn);		
//		configureSearchResults(syn);
//		configureGetEntityById(syn);
//		configureGetEntityBundle(syn);
//		configureGetEntityVersions(syn);
//		
//	}
//
//	private static void configureGetEntityBundle(Synapse syn) throws Exception {
//		EntityBundle bundle = makeEmptyEntityBundle("syn1234", "syn 1234 name");
//		bundle.getPermissions().setCanAddChild(true);
//		bundle.getPermissions().setCanChangePermissions(true);
//		bundle.getPermissions().setCanDelete(true);
//		bundle.getPermissions().setCanDownload(true);
//		bundle.getPermissions().setCanEdit(true);
//		bundle.getPermissions().setCanEnableInheritance(true);
//		bundle.getPermissions().setCanPublicRead(true);
//		bundle.getPermissions().setCanView(true);
//		when(syn.getEntityBundle(eq(bundle.getEntity().getId()), anyInt())).thenReturn(bundle);
//	}
//
//	private static void configureGetEntityVersions(Synapse syn)
//			throws SynapseException {
//		PaginatedResults<VersionInfo> versions = new PaginatedResults<VersionInfo>();
//		List<VersionInfo> infos = new ArrayList<VersionInfo>();
//		VersionInfo info;
//		
//		info = new VersionInfo();
//		info.setId("1");
//		info.setVersionNumber(1L);
//		info.setVersionLabel("one");
//		infos.add(info);
//		
//		info = new VersionInfo();
//		info.setId("2");
//		info.setVersionNumber(2L);
//		info.setVersionLabel("2");
//		infos.add(info);
//		
//		info = new VersionInfo();
//		info.setId("3");
//		info.setVersionNumber(3L);
//		info.setVersionLabel("three");
//		infos.add(info);
//		
//		versions.setResults(infos);
//		versions.setTotalNumberOfResults(3);
//		when(syn.getEntityVersions(eq("syn1234"), anyInt(), anyInt())).thenReturn(versions);
//	}
//
//	private static void configureGetEntityById(Synapse syn)
//			throws SynapseException {
//		Entity syn1234 = makeEntity("syn1234", "Synapse id 1234");		
//		when(syn.getEntityById("syn1234")).thenReturn(syn1234);
//	}
//
//	private static void configureSearchResults(Synapse syn) throws Exception {
//		SearchResults searchResults = new SearchResults();
//		searchResults.setFound(1L);
//		searchResults.setStart(0L);
//		List<Hit> hits = new ArrayList<Hit>();
//		Hit hit = new Hit();
//		hit.setName("Name of Entity");
//		hit.setId("syn12345");
//		hit.setEtag("1");
//		searchResults.setHits(hits);
//		when(syn.search(any(SearchQuery.class))).thenReturn(searchResults);
//	}
//
//	private static void configureLogin(Synapse syn) throws Exception {		
//		UserSessionData usd = new UserSessionData();
//		usd.setIsSSO(false);
//		usd.setSessionToken(tokenProvider.getSessionToken());
//		usd.setProfile(userProfile);
//		when(syn.login(anyString(), anyString())).thenReturn(usd);
//		when(syn.login(anyString(), anyString(), anyBoolean())).thenReturn(usd);
//	}
//
//
//	/*
//	 * Helpers
//	 */
//	private static Entity makeEntity(String id, String name) {
//		Entity entity = new Data();
//		entity.setEntityType(Data.class.getName());
//		entity.setName(name);
//		entity.setId(id);
//		entity.setEtag("1");
//		entity.setCreatedBy(userProfile.getOwnerId());
//		entity.setCreatedOn(new Date());
//		entity.setModifiedBy(userProfile.getOwnerId());
//		entity.setModifiedOn(new Date());
//		((Data)entity).setVersionNumber(1L);
//		return entity;
//	}
//
//	private static EntityBundle makeEmptyEntityBundle(String id, String name) {
//		EntityBundle bundle = new EntityBundle();
//		bundle.setEntity(makeEntity(id, name));
//		Annotations annotations = new Annotations();
//		bundle.setAnnotations(annotations);
//		UserEntityPermissions uep = new UserEntityPermissions();
//		uep.setCanAddChild(false);
//		uep.setCanChangePermissions(false);
//		uep.setCanDelete(false);
//		uep.setCanDownload(false);
//		uep.setCanEdit(false);
//		uep.setCanEnableInheritance(false);
//		uep.setCanPublicRead(false);
//		uep.setOwnerPrincipalId(1L);
//		bundle.setPermissions(uep);		
//		EntityPath path = new EntityPath();
//		bundle.setPath(path);
//		PaginatedResults<EntityHeader> refby = new PaginatedResults<EntityHeader>();
//		bundle.setReferencedBy(refby);
//		bundle.setHasChildren(false);
//		AccessControlList acl = new AccessControlList();
//		bundle.setAccessControlList(acl);
//		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
//		bundle.setAccessRequirements(accessRequirements);
//		List<AccessRequirement> unmetAccessRequirements = new ArrayList<AccessRequirement>();
//		bundle.setUnmetAccessRequirements(unmetAccessRequirements);
//		return bundle;
//	}

	
	public static MultichoiceAnswer getAnswer(long answerIndex, String prompt) {
		MultichoiceAnswer a = new MultichoiceAnswer();
		a.setAnswerIndex(answerIndex);
		a.setPrompt(prompt);
		return a;
	}
	
	public static Questionnaire mockQuestionnaire() {
		//the mock questionnaire has 2 varieties of a single question.  One will be randomly selected client-side.  One is exclusive, one is not.
		Questionnaire questionnaire = new Questionnaire();
		questionnaire.setHeader("Certification");
		List<QuestionVariety> questionVarieties = new ArrayList<QuestionVariety>();
		QuestionVariety qv = new QuestionVariety();
		List<Question> questionOptions = new ArrayList<Question>();
		MultichoiceQuestion q1 = new MultichoiceQuestion();
		q1.setExclusive(true);
		q1.setQuestionIndex(0L);
		q1.setPrompt("What... is the air-speed velocity of an unladen swallow?");
		List<MultichoiceAnswer> answers = new ArrayList<MultichoiceAnswer>();
		long answerIndex = 0L;
		answers.add(getAnswer(answerIndex++, "42 m/s"));
		answers.add(getAnswer(answerIndex++, "African or European?"));
		answers.add(getAnswer(answerIndex++, "Huh? I... I don't know that!"));
		q1.setAnswers(answers);
		questionOptions.add(q1);
		
		q1 = new MultichoiceQuestion();
		q1.setExclusive(false);
		q1.setQuestionIndex(1L);
		q1.setPrompt("What... is the air-speed velocity of an unladen finch?");
		answers = new ArrayList<MultichoiceAnswer>();
		answerIndex = 0L;
		answers.add(getAnswer(answerIndex++, "18 m/s"));
		answers.add(getAnswer(answerIndex++, "This is silly"));
		answers.add(getAnswer(answerIndex++, "Huh? I... I don't know that!"));
		q1.setAnswers(answers);
		questionOptions.add(q1);
		
		qv.setQuestionOptions(questionOptions);
		questionVarieties.add(qv);
		questionnaire.setQuestions(questionVarieties);
		return questionnaire;
	}
}
