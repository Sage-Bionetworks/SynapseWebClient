package org.sagebionetworks.web.server;

import com.google.gwt.user.server.rpc.jakarta.XsrfTokenServiceServlet;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sagebionetworks.ConfigurationProperties;
import org.sagebionetworks.ConfigurationPropertiesImpl;
import org.sagebionetworks.LoggerProvider;
import org.sagebionetworks.LoggerProviderImpl;
import org.sagebionetworks.PropertyProvider;
import org.sagebionetworks.PropertyProviderImpl;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.StackConfigurationImpl;
import org.sagebionetworks.StackEncrypter;
import org.sagebionetworks.StackEncrypterImpl;
import org.sagebionetworks.aws.AwsClientFactory;
import org.sagebionetworks.aws.SynapseS3Client;
import org.sagebionetworks.aws.v2.AwsClientFactoryV2;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.server.servlet.AliasRedirectorServlet;
import org.sagebionetworks.web.server.servlet.AppConfigServlet;
import org.sagebionetworks.web.server.servlet.CdnRedirectorServlet;
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
import org.sagebionetworks.web.server.servlet.StackVersionProvider;
import org.sagebionetworks.web.server.servlet.StackVersionProviderImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.SynapseProviderImpl;
import org.sagebionetworks.web.server.servlet.UserAccountServiceImpl;
import org.sagebionetworks.web.server.servlet.UserProfileClientImpl;
import org.sagebionetworks.web.server.servlet.VersionsServlet;
import org.sagebionetworks.web.server.servlet.ViteHTMLGenerator;
import org.sagebionetworks.web.server.servlet.ViteHTMLGeneratorImpl;
import org.sagebionetworks.web.server.servlet.ViteManifestProvider;
import org.sagebionetworks.web.server.servlet.ViteManifestProviderImpl;
import org.sagebionetworks.web.server.servlet.filter.AmpADFilter;
import org.sagebionetworks.web.server.servlet.filter.CORSFilter;
import org.sagebionetworks.web.server.servlet.filter.DigitalHealthFilter;
import org.sagebionetworks.web.server.servlet.filter.DreamFilter;
import org.sagebionetworks.web.server.servlet.filter.GWTAllCacheFilter;
import org.sagebionetworks.web.server.servlet.filter.GWTCacheControlFilter;
import org.sagebionetworks.web.server.servlet.filter.HSTSFilter;
import org.sagebionetworks.web.server.servlet.filter.HtmlInjectionFilter;
import org.sagebionetworks.web.server.servlet.filter.JavaScriptContentTypeFilter;
import org.sagebionetworks.web.server.servlet.filter.MHealthFilter;
import org.sagebionetworks.web.server.servlet.filter.RPCValidationFilter;
import org.sagebionetworks.web.server.servlet.filter.RegisterAccountFilter;
import org.sagebionetworks.web.server.servlet.filter.SSLFilter;
import org.sagebionetworks.web.server.servlet.filter.TimingFilter;
import org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter;
import org.sagebionetworks.web.server.servlet.oauth2.OAuth2AliasServlet;
import org.sagebionetworks.web.server.servlet.oauth2.OAuth2SessionServlet;
import org.sagebionetworks.web.shared.WebConstants;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.kms.KmsClient;

/**
 * Binds the service servlets to their paths and any other Guice binding required on the server
 * side.
 *
 * @author jmhill
 *
 */
public class PortalServletModule extends ServletModule {

  private static Logger logger = Logger.getLogger(
    PortalServletModule.class.getName()
  );

  private void bindDependencies() {
    // The Rest template provider should be a singleton.
    bind(RestTemplateProvider.class)
      .to(RestTemplateProviderImpl.class)
      .in(Singleton.class);

    bind(LoggerProvider.class).to(LoggerProviderImpl.class);
    bind(PropertyProvider.class).to(PropertyProviderImpl.class);
    bind(ConfigurationProperties.class).to(ConfigurationPropertiesImpl.class);
    bind(StackConfiguration.class).to(StackConfigurationImpl.class);
    bind(StackEncrypter.class).to(StackEncrypterImpl.class);
    bind(SynapseProvider.class).to(SynapseProviderImpl.class);
    bind(StackVersionProvider.class)
      .to(StackVersionProviderImpl.class)
      .in(Singleton.class);

    // JSONObjectAdapter
    bind(JSONObjectAdapter.class).to(JSONObjectAdapterImpl.class);

    bind(ViteManifestProvider.class)
      .to(ViteManifestProviderImpl.class)
      .in(Singleton.class);
    bind(ViteHTMLGenerator.class).to(ViteHTMLGeneratorImpl.class);
  }

  private void bindFilters() {
    filter("/*").through(SSLFilter.class);
    bind(SSLFilter.class).in(Singleton.class);

    filter("/*").through(GWTCacheControlFilter.class);
    bind(GWTCacheControlFilter.class).in(Singleton.class);

    filter("/js/*").through(GWTAllCacheFilter.class);
    filter("/images/*").through(GWTAllCacheFilter.class);
    filter("/research/*").through(GWTAllCacheFilter.class);
    bind(GWTAllCacheFilter.class).in(Singleton.class);

    filter("/*").through(JavaScriptContentTypeFilter.class);
    bind(JavaScriptContentTypeFilter.class).in(Singleton.class);

    filter("/*").through(HSTSFilter.class);
    bind(HSTSFilter.class).in(Singleton.class);

    filter("/*").through(CORSFilter.class);
    bind(CORSFilter.class).in(Singleton.class);

    filter("/*").through(XFrameOptionsFilter.class);
    bind(XFrameOptionsFilter.class).in(Singleton.class);

    filter("/Portal/*").through(TimingFilter.class);
    bind(TimingFilter.class).in(Singleton.class);
    // This supports RPC
    filter("/Portal/*").through(RPCValidationFilter.class);
    bind(RPCValidationFilter.class).in(Singleton.class);

    bind(AmpADFilter.class).in(Singleton.class);
    filter("/ampad").through(AmpADFilter.class);

    bind(DreamFilter.class).in(Singleton.class);
    filter("/dream").through(DreamFilter.class);

    bind(DigitalHealthFilter.class).in(Singleton.class);
    filter("/digitalhealth").through(DigitalHealthFilter.class);
    bind(MHealthFilter.class).in(Singleton.class);
    filter("/mHealth").through(MHealthFilter.class);

    bind(RegisterAccountFilter.class).in(Singleton.class);
    filter("/" + RegisterAccountFilter.URL_PATH)
      .through(RegisterAccountFilter.class);

    // Since the HTML Injection filter writes and flushes the response, it must be the last filter in the chain.
    filter("/*").through(HtmlInjectionFilter.class);
    bind(HtmlInjectionFilter.class).in(Singleton.class);
  }

  private void bindServices() {
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

    // Redirector to CDN
    bind(CdnRedirectorServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.CDN_REDIRECTOR_SERVLET + "/*")
      .with(CdnRedirectorServlet.class);

    // FileHandle upload
    bind(FileHandleServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.FILE_HANDLE_UPLOAD_SERVLET)
      .with(FileHandleServlet.class);

    // Session cookie
    bind(InitSessionServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.SESSION_COOKIE_SERVLET)
      .with(InitSessionServlet.class);

    // FileHandleAssociation download
    bind(FileHandleAssociationServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.FILE_HANDLE_ASSOCIATION_SERVLET)
      .with(FileHandleAssociationServlet.class);

    // Slack handler
    bind(SlackServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.SLACK_SERVLET).with(SlackServlet.class);

    // AppConfig handler
    bind(AppConfigServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.APPCONFIG_SERVLET)
      .with(AppConfigServlet.class);

    // Versions handler
    bind(VersionsServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.VERSIONS_SERVLET)
      .with(VersionsServlet.class);

    // Alias resolution
    bind(AliasRedirectorServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.ALIAS_REDIRECTOR_SERVLET)
      .with(AliasRedirectorServlet.class);

    // FileHandle upload
    bind(FileEntityResolverServlet.class).in(Singleton.class);
    serve("/Portal/" + WebConstants.FILE_ENTITY_RESOLVER_SERVLET)
      .with(FileEntityResolverServlet.class);

    // Setup the LinkedIn service mapping
    bind(LinkedInServiceImpl.class).in(Singleton.class);
    serve("/Portal/linkedin").with(LinkedInServiceImpl.class);

    // Setup the Discussion Forum service mapping
    bind(DiscussionForumClientImpl.class).in(Singleton.class);
    serve("/Portal/discussionforumclient")
      .with(DiscussionForumClientImpl.class);

    // Discussion message download
    bind(DiscussionMessageServlet.class).in(Singleton.class);
    serve("/Portal" + WebConstants.DISCUSSION_MESSAGE_SERVLET)
      .with(DiscussionMessageServlet.class);

    // OAuth2
    bind(OAuth2SessionServlet.class).in(Singleton.class);
    serve("/Portal/oauth2callback").with(OAuth2SessionServlet.class);

    bind(OAuth2AliasServlet.class).in(Singleton.class);
    serve("/Portal/oauth2AliasCallback").with(OAuth2AliasServlet.class);

    // Catch-all. Note that "/*" would override all other servlet binding, and "/" overrides the default
    // handler
    // (which we need for GWT place handling).
    // This is also where project aliases are handled.
    bind(ProjectAliasServlet.class).in(Singleton.class);
    serveRegex("^\\/\\w+$").with(ProjectAliasServlet.class);
  }

  @Override
  protected void configureServlets() {
    // Bind the properties from the config file
    bindPropertiesFromFile("ServerConstants.properties");

    bindDependencies();
    bindFilters();
    bindServices();
  }

  @Provides
  public AppConfigDataClient provideAppConfigDataClient() {
    return AwsClientFactoryV2.createAppConfigDataClient();
  }

  @Provides
  public KmsClient provideKmsClient() {
    return AwsClientFactoryV2.createKmsClient();
  }

  @Provides
  public SynapseS3Client provideAmazonS3Client() {
    return AwsClientFactory.createAmazonS3Client();
  }

  /**
   * Attempt to bind all properties found in the given property file. The property file should be on
   * the classpath.
   *
   * @param resourceName
   */
  private void bindPropertiesFromFile(String resourceName) {
    InputStream in =
      PortalServletModule.class.getClassLoader()
        .getResourceAsStream(resourceName);
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
              logger.info(
                "Overriding a ServerConstants.properties key: " +
                key +
                " with a value from System.properties(). New value: " +
                newValue
              );
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
        } catch (IOException e) {}
      }
    } else {
      logger.severe("Cannot find property file on classpath: " + resourceName);
    }
  }
}
