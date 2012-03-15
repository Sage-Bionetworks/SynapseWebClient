package org.sagebionetworks.web.client;

import java.io.StringWriter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Provides logging for client-side code.
 * The log message is sent to the server, where it is logged with Log4J.
 * 
 * @author jmhill
 *
 */
public class ClientLogger {
	
	private SynapseClientAsync synapseClient;
	
	@Inject
	public ClientLogger(SynapseClientAsync synapseClient){
		this.synapseClient = synapseClient;
	}
	
	/**
	 * Log a debug message in the server-side log.
	 * @param message
	 */
	public void debug(String message){
		this.synapseClient.logDebug(message, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				// Nothing to do here.
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Nothing to do here.
			}
		});
	}
	
	/**
	 * Log an info message in the server-side log.
	 * @param message
	 */
	public void info(String message){
		this.synapseClient.logInfo(message, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				// Nothing to do here.
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Nothing to do here.
			}
		});
	}
	
	/**
	 * Log an error message in the server-side log.
	 * @param message
	 */
	public void error(String message){
		this.synapseClient.logError(message, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				// Nothing to do here.
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Nothing to do here.
			}
		});
	}
	
	/**
	 * Log an error message in the server-side log.
	 * Since a Java stack trace is not an option of the client-side code, this method captures some of 
	 * the basic information need to print a rudimentary stack trace.
	 * @param message
	 */
	public void error(String message, String exceptionClassName, String sourceClassName, String methodName, int lineNumber){
		StringBuilder builder = new StringBuilder();
		builder.append(exceptionClassName);
		builder.append(": ");
		builder.append(message);
		builder.append("\n\tat ");
		builder.append(sourceClassName);
		builder.append(".");
		builder.append(methodName);
		builder.append("(");
		builder.append(sourceClassName);
		builder.append(".");
		builder.append("java");
		builder.append(":");
		builder.append(lineNumber);
		builder.append(")\n");
		this.synapseClient.logError(builder.toString(), new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				// Nothing to do here.
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Nothing to do here.
			}
		});
	}

}
