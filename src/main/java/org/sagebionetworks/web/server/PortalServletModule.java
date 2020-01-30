package org.sagebionetworks.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.server.servlet.AliasRedirectorServlet;
import org.sagebionetworks.web.server.servlet.ChallengeClientImpl;
import org.sagebionetworks.web.server.servlet.DataAccessClientImpl;
import org.sagebionetworks.web.server.servlet.DiscussionForumClientImpl;
import org.sagebionetworks.web.server.servlet.DiscussionMessageServlet;
import org.sagebionetworks.web.server.servlet.FileEntityResolverServlet;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.server.servlet.FileHandleServlet;
import org.sagebionetworks.web.server.servlet.FileUploaderJnlp;
import org.sagebionetworks.web.server.servlet.InitSessionServlet;
import org.sagebionetworks.web.server.servlet.LinkedInServiceImpl;
import org.sagebionetworks.web.server.servlet.ProjectAliasServlet;
import org.sagebionetworks.web.server.servlet.SlackServlet;
import org.sagebionetworks.web.server.servlet.StackConfigServiceImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.UserAccountServiceImpl;
import org.sagebionetworks.web.server.servlet.UserProfileClientImpl;
import org.sagebionetworks.web.server.servlet.VersionsServlet;
import org.sagebionetworks.web.server.servlet.filter.DigitalHealthFilter;
import org.sagebionetworks.web.server.servlet.filter.DreamFilter;
import org.sagebionetworks.web.server.servlet.filter.MHealthFilter;
import org.sagebionetworks.web.server.servlet.filter.PlacesRedirectFilter;
import org.sagebionetworks.web.server.servlet.filter.RPCValidationFilter;
import org.sagebionetworks.web.server.servlet.filter.RegisterAccountFilter;
import org.sagebionetworks.web.server.servlet.filter.TimingFilter;
import org.sagebionetworks.web.server.servlet.oauth2.OAuth2AliasServlet;
import org.sagebionetworks.web.server.servlet.oauth2.OAuth2SessionServlet;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.WithTokenizers;
import com.google.gwt.user.server.rpc.XsrfTokenServiceServlet;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

/**
 * Binds the service servlets to their paths and any other Guice binding required on the server
 * side.
 * 
 * @author jmhill
 * 
 */
public class PortalServletModule extends ServletModule {

	private static Logger logger = Logger.getLogger(PortalServletModule.class.getName());

	@Override
	protected void configureServlets() {
		// filter all call through this filter
		filter("/Portal/*").through(TimingFilter.class);
		bind(TimingFilter.class).in(Singleton.class);
		// This supports RPC
		filter("/Portal/*").through(RPCValidationFilter.class);
		bind(RPCValidationFilter.class).in(Singleton.class);

		bind(DreamFilter.class).in(Singleton.class);
		filter("/dream").through(DreamFilter.class);

		bind(DigitalHealthFilter.class).in(Singleton.class);
		filter("/digitalhealth").through(DigitalHealthFilter.class);
		bind(MHealthFilter.class).in(Singleton.class);
		filter("/mHealth").through(MHealthFilter.class);

		bind(RegisterAccountFilter.class).in(Singleton.class);
		filter("/" + RegisterAccountFilter.URL_PATH).through(RegisterAccountFilter.class);

		// Setup the Synapse service
		bind(SynapseClientImpl.class).in(Singleton.class);
		serve("/Portal/synapseclient").with(SynapseClientImpl.class);

		// Cross-Site Request Forgery protection
		bind(XsrfTokenServiceServlet.class).in(Singleton.class);
		serve("/Portal/xsrf").with(XsrfTokenServiceServlet.class);

		// Setup the Challenge service
		bind(ChallengeClientImpl.class).in(Singleton.class);
		serve("/Portal/challengeclient").with(ChallengeClientImpl.class);

		// Setup the Challenge service
		bind(DataAccessClientImpl.class).in(Singleton.class);
		serve("/Portal/dataaccessclient").with(DataAccessClientImpl.class);

		bind(UserProfileClientImpl.class).in(Singleton.class);
		serve("/Portal/userprofileclient").with(UserProfileClientImpl.class);

		// Setup the User Account service mapping
		bind(UserAccountServiceImpl.class).in(Singleton.class);
		serve("/Portal/users").with(UserAccountServiceImpl.class);

		// Setup the User Account service mapping
		bind(StackConfigServiceImpl.class).in(Singleton.class);
		serve("/Portal/stackConfig").with(StackConfigServiceImpl.class);

		// Setup the File Uploader JNLP mapping
		bind(FileUploaderJnlp.class).in(Singleton.class);
		serve("/Portal/fileUploaderJnlp").with(FileUploaderJnlp.class);

		// FileHandle upload
		bind(FileHandleServlet.class).in(Singleton.class);
		serve("/Portal/" + WebConstants.FILE_HANDLE_UPLOAD_SERVLET).with(FileHandleServlet.class);

		// Session cookie
		bind(InitSessionServlet.class).in(Singleton.class);
		serve("/Portal/" + WebConstants.SESSION_COOKIE_SERVLET).with(InitSessionServlet.class);

		// FileHandleAssociation download
		bind(FileHandleAssociationServlet.class).in(Singleton.class);
		serve("/Portal/" + WebConstants.FILE_HANDLE_ASSOCIATION_SERVLET).with(FileHandleAssociationServlet.class);

		// Slack handler
		bind(SlackServlet.class).in(Singleton.class);
		serve("/Portal/" + WebConstants.SLACK_SERVLET).with(SlackServlet.class);

		// Versions handler
		bind(VersionsServlet.class).in(Singleton.class);
		serve("/Portal/" + WebConstants.VERSIONS_SERVLET).with(VersionsServlet.class);

		// Alias resolution
		bind(AliasRedirectorServlet.class).in(Singleton.class);
		serve("/Portal/" + WebConstants.ALIAS_REDIRECTOR_SERVLET).with(AliasRedirectorServlet.class);

		// FileHandle upload
		bind(FileEntityResolverServlet.class).in(Singleton.class);
		serve("/Portal/" + WebConstants.FILE_ENTITY_RESOLVER_SERVLET).with(FileEntityResolverServlet.class);

		// Setup the LinkedIn service mapping
		bind(LinkedInServiceImpl.class).in(Singleton.class);
		serve("/Portal/linkedin").with(LinkedInServiceImpl.class);

		// Setup the Discussion Forum service mapping
		bind(DiscussionForumClientImpl.class).in(Singleton.class);
		serve("/Portal/discussionforumclient").with(DiscussionForumClientImpl.class);

		// Discussion message download
		bind(DiscussionMessageServlet.class).in(Singleton.class);
		serve("/Portal" + WebConstants.DISCUSSION_MESSAGE_SERVLET).with(DiscussionMessageServlet.class);

		// OAuth2
		bind(OAuth2SessionServlet.class).in(Singleton.class);
		serve("/Portal/oauth2callback").with(OAuth2SessionServlet.class);

		bind(OAuth2AliasServlet.class).in(Singleton.class);
		serve("/Portal/oauth2AliasCallback").with(OAuth2AliasServlet.class);


		// The Rest template provider should be a singleton.
		bind(RestTemplateProviderImpl.class).in(Singleton.class);
		bind(RestTemplateProvider.class).to(RestTemplateProviderImpl.class);
		// Bind the properties from the config file
		bindPropertiesFromFile("ServerConstants.properties");

		// JSONObjectAdapter
		bind(JSONObjectAdapter.class).to(JSONObjectAdapterImpl.class);

		handleGWTPlaces();

		// Catch-all. Note that "/*" would override all other servlet binding, and "/" overrides the default
		// handler
		// (which we need for GWT place handling).
		// This is also where project aliases are handled.
		bind(ProjectAliasServlet.class).in(Singleton.class);
		serveRegex("^\\/\\w+$").with(ProjectAliasServlet.class);
	}

	public void handleGWTPlaces() {
		bind(PlacesRedirectFilter.class).in(Singleton.class);
		Class<? extends PlaceTokenizer<?>>[] placeClasses = AppPlaceHistoryMapper.class.getAnnotation(WithTokenizers.class).value();
		for (Class<? extends PlaceTokenizer<?>> c : placeClasses) {
			String simpleName = c.getEnclosingClass().getSimpleName();
			String filterRegEx = "/" + simpleName + ":*";
			filter(filterRegEx).through(PlacesRedirectFilter.class);
		}
	}

	/**
	 * Attempt to bind all properties found in the given property file. The property file should be on
	 * the classpath.
	 * 
	 * @param resourceName
	 */
	private void bindPropertiesFromFile(String resourceName) {
		InputStream in = PortalServletModule.class.getClassLoader().getResourceAsStream(resourceName);
		if (in != null) {
			try {
				Properties props = new Properties();
				// First load the properties from the server config file.
				props.load(in);
				// Override any property that is in the System properties.
				Properties systemProps = System.getProperties();
				Iterator<Object> it = systemProps.keySet().iterator();
				while (it.hasNext()) {
					Object obKey = it.next();
					if (obKey instanceof String) {
						String key = (String) obKey;
						// Add all system properites
						String newValue = systemProps.getProperty(key);
						String previous = (String) props.setProperty(key, newValue);
						if (previous != null) {
							logger.info("Overriding a ServerConstants.properties key: " + key + " with a value from System.properties(). New value: " + newValue);
						}
					}
				}
				// Bind the properties
				Names.bindProperties(binder(), props);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		} else {
			logger.severe("Cannot find property file on classpath: " + resourceName);
		}
	}

}
