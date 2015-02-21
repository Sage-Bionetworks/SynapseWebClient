package org.sagebionetworks.web.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ChallengeClient;
import org.sagebionetworks.web.server.servlet.ChallengeClientImpl;
import org.sagebionetworks.web.server.servlet.FileAttachmentServlet;
import org.sagebionetworks.web.server.servlet.FileHandleServlet;
import org.sagebionetworks.web.server.servlet.FileUpload;
import org.sagebionetworks.web.server.servlet.FileUploaderJnlp;
import org.sagebionetworks.web.server.servlet.JiraClientImpl;
import org.sagebionetworks.web.server.servlet.JiraJavaClient;
import org.sagebionetworks.web.server.servlet.JiraJavaClientImpl;
import org.sagebionetworks.web.server.servlet.LayoutServiceImpl;
import org.sagebionetworks.web.server.servlet.LicenseServiceImpl;
import org.sagebionetworks.web.server.servlet.LinkedInServiceImpl;
import org.sagebionetworks.web.server.servlet.NcboSearchService;
import org.sagebionetworks.web.server.servlet.ProjectServiceImpl;
import org.sagebionetworks.web.server.servlet.RssServiceImpl;
import org.sagebionetworks.web.server.servlet.SearchServiceImpl;
import org.sagebionetworks.web.server.servlet.SimpleSearchService;
import org.sagebionetworks.web.server.servlet.StackConfigServiceImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.UserAccountServiceImpl;
import org.sagebionetworks.web.server.servlet.UserProfileAttachmentServlet;
import org.sagebionetworks.web.server.servlet.filter.CRCSCFilter;
import org.sagebionetworks.web.server.servlet.filter.DreamFilter;
import org.sagebionetworks.web.server.servlet.filter.RPCValidationFilter;
import org.sagebionetworks.web.server.servlet.filter.TimingFilter;
import org.sagebionetworks.web.server.servlet.openid.OpenIDServlet;
import org.sagebionetworks.web.server.servlet.openid.OpenIDUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

/**
 * Binds the service servlets to their paths and any other 
 * Guice binding required on the server side.
 *  
 * @author jmhill
 * 
 */
public class PortalServletModule extends ServletModule {
	
	private static Logger logger = Logger.getLogger(PortalServletModule.class.getName());
	private boolean debugRpcInitialized = false;
	@Override
	protected void configureServlets() {
		debugCopyCodeServerRPCPolicies();
//		// This is not working yet
//		filter("/Portal/*").through(SimpleAuthFilter.class);
//		bind(SimpleAuthFilter.class).in(Singleton.class);
		
		// filter all call through this filter
		filter("/Portal/*").through(TimingFilter.class);
		bind(TimingFilter.class).in(Singleton.class);
		// This supports RPC
		filter("/Portal/*").through(RPCValidationFilter.class);
		bind(RPCValidationFilter.class).in(Singleton.class);

		bind(DreamFilter.class).in(Singleton.class);
		filter("/dream").through(DreamFilter.class);
		
		bind(CRCSCFilter.class).in(Singleton.class);
		filter("/crcsc").through(CRCSCFilter.class);

		// Setup the Synapse service
		bind(SynapseClientImpl.class).in(Singleton.class);
		serve("/Portal/synapse").with(SynapseClientImpl.class);
		
		// Setup the Challenge service
		bind(ChallengeClientImpl.class).in(Singleton.class);
		serve("/Portal/challenge").with(ChallengeClientImpl.class);
		
		// Setup the Search service
		bind(SearchServiceImpl.class).in(Singleton.class);
		serve("/Portal/search").with(SearchServiceImpl.class);
		// setup the project service
		bind(ProjectServiceImpl.class).in(Singleton.class);
		serve("/Portal/project").with(ProjectServiceImpl.class);
	
		// setup the layout service
		bind(LayoutServiceImpl.class).in(Singleton.class);
		serve("/Portal/layout").with(LayoutServiceImpl.class);
		
		// Setup the License service mapping
		bind(LicenseServiceImpl.class).in(Singleton.class);
		serve("/Portal/license").with(LicenseServiceImpl.class);
		
		// Setup the User Account service mapping
		bind(UserAccountServiceImpl.class).in(Singleton.class);
		serve("/Portal/users").with(UserAccountServiceImpl.class);
	
		// Setup the User Account service mapping
		bind(StackConfigServiceImpl.class).in(Singleton.class);
		serve("/Portal/stackConfig").with(StackConfigServiceImpl.class);
	
		// setup the NCBO servlet
		bind(NcboSearchService.class).in(Singleton.class);
		serve("/Portal/ncbo/search").with(NcboSearchService.class);
		
		// setup the Simple Search servlet
		bind(SimpleSearchService.class).in(Singleton.class);
		serve("/Portal/simplesearch").with(SimpleSearchService.class);
				
		// setup GWTupload
		bind(FileUpload.class).in(Singleton.class);
		serve("/Portal/" + WebConstants.LEGACY_DATA_UPLOAD_SERVLET).with(FileUpload.class);

		// Setup the File Uploader JNLP mapping
		bind(FileUploaderJnlp.class).in(Singleton.class);
		serve("/Portal/fileUploaderJnlp").with(FileUploaderJnlp.class);
		
		// Attachments
		bind(FileAttachmentServlet.class).in(Singleton.class);
		serve("/Portal/attachment").with(FileAttachmentServlet.class);
		
		// FileHandle upload
		bind(FileHandleServlet.class).in(Singleton.class);
		serve("/Portal/"+WebConstants.FILE_HANDLE_UPLOAD_SERVLET).with(FileHandleServlet.class);
		
		// User Profile Attachment (photo)
		bind(UserProfileAttachmentServlet.class).in(Singleton.class);
		serve("/Portal/profileAttachment").with(UserProfileAttachmentServlet.class);
		
		// Setup the LinkedIn service mapping
		bind(LinkedInServiceImpl.class).in(Singleton.class);
		serve("/Portal/linkedin").with(LinkedInServiceImpl.class);
		
		//Jira client service mapping
		bind(JiraClientImpl.class).in(Singleton.class);
		serve("/Portal/jira").with(JiraClientImpl.class);
		bind(JiraJavaClient.class).to(JiraJavaClientImpl.class);
		
		// Setup the Rss service mapping
		bind(RssServiceImpl.class).in(Singleton.class);
		serve("/Portal/rss").with(RssServiceImpl.class);
				
		// Setup the OpenID service mapping
		bind(OpenIDServlet.class).in(Singleton.class);
		serve(WebConstants.OPEN_ID_URI).with(OpenIDServlet.class);
		serve(OpenIDUtils.OPENID_CALLBACK_URI).with(OpenIDServlet.class);
		
		// The Rest template provider should be a singleton.
		bind(RestTemplateProviderImpl.class).in(Singleton.class);
		bind(RestTemplateProvider.class).to(RestTemplateProviderImpl.class);
		// Bind the properties from the config file
		bindPropertiesFromFile("ServerConstants.properties");
		
		// Bind the ConlumnConfig to singleton
		bind(ColumnConfigProvider.class).in(Singleton.class);
		
		// JSONObjectAdapter
		bind(JSONObjectAdapter.class).to(JSONObjectAdapterImpl.class);
	}
	
	private void debugCopyCodeServerRPCPolicies() {
		try {
			String gwtCodeServerPort = System.getProperty("gwt.codeserver.port");
			if (!debugRpcInitialized && gwtCodeServerPort != null && gwtCodeServerPort.trim().length() > 0) { 
				//until we upgrade to gwt 2.6 or later, we need to manually copy the gwt.rpc files to the Portal directory.
				String targetDir = System.getProperty("user.dir") + "/Portal/";
				new File(targetDir).mkdirs();
				WebClient webClient = new WebClient();
				String rootPage = "http://127.0.0.1:"+gwtCodeServerPort+"/Portal/";
				HtmlPage page = webClient.getPage(rootPage);
				final List<?> anchors = page.getByXPath("//a");
				for (Object object : anchors) {
					HtmlAnchor anchor = (HtmlAnchor)object;
					String href = anchor.getHrefAttribute();
					if (href.endsWith("gwt.rpc")) {
						//now copy gwt.rpc file to the target directory!
						URL website = new URL(rootPage + href);
						ReadableByteChannel rbc = Channels.newChannel(website.openStream());
						FileOutputStream fos = new FileOutputStream(targetDir + href);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						fos.close();
					}
				}
				
				debugRpcInitialized = true;
				webClient.closeAllWindows();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * Attempt to bind all properties found in the given property file.
	 * The property file should be on the classpath.
	 * @param resourceName
	 */
	private void bindPropertiesFromFile(String resourceName){
		InputStream in = PortalServletModule.class.getClassLoader().getResourceAsStream(resourceName);
		if(in != null){
			try{
				Properties props = new Properties();
				// First load the properties from the server config file.
				props.load(in);
				// Override any property that is in the System properties.
				Properties systemProps = System.getProperties();
				Iterator<Object> it = systemProps.keySet().iterator();
				while(it.hasNext()){
					Object obKey = it.next();
					if(obKey instanceof String){
						String key = (String) obKey;
						// Add all system properites
						String newValue = systemProps.getProperty(key);
						String previous = (String) props.setProperty(key, newValue);
						if(previous != null){
							logger.info("Overriding a ServerConstants.properties key: "+key+" with a value from System.properties(). New value: "+newValue);
						}
					}
				}
				// Bind the properties
				Names.bindProperties(binder(),props);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}finally{
				try {
					in.close();
				} catch (IOException e) {}
			}
		}else{
			logger.severe("Cannot find property file on classpath: "+resourceName); 
		}
	}

}
