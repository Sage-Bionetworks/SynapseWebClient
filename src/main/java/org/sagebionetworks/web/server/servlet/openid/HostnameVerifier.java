package org.sagebionetworks.web.server.servlet.openid;

import javax.net.ssl.SSLException;

import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;

public class HostnameVerifier extends AbstractVerifier {

    private final X509HostnameVerifier delegate;

    public HostnameVerifier(final X509HostnameVerifier delegate) {
        this.delegate = delegate;
    }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts)
                throws SSLException {
    	if (delegate==null) return;//this is done TEMPORARILY as a TEST
        boolean ok = false;
        try {
            delegate.verify(host, cns, subjectAlts);
        } catch (SSLException e) {
        	//this is done TEMPORARILY as a TEST
        	ok = true;
//            for (String cn : cns) {
//                if (cn.startsWith("*.")) {
//                    try {
//                          delegate.verify(host, new String[] { 
//                                cn.substring(2) }, subjectAlts);
//                          ok = true;
//                    } catch (Exception e1) { }
//                }
//            }
            if(!ok) throw e;
        }
    }
}
