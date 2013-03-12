package org;

import java.applet.AppletContext;
import java.net.URL;
import java.util.Map;

/**
 * oldrsclient
 * 4.3.2013
 */
public class ClientStub extends AbstractAppletStub {

    private Map<String, String> params;
    private URL documentBase;
    private URL codeBase;
    private ClientContext context;
    private boolean usealt;
    private String alturl;

    public ClientStub(Map<String, String> params, URL documentBase, URL codeBase) {
        this.params = params;
        this.documentBase = documentBase;
        this.codeBase = codeBase;
        context = new ClientContext();
        usealt = Boolean.parseBoolean(Launcher.getProps().getSection("launcher").getProperty("use_alt_worldlist"));
        alturl = Launcher.getProps().getSection("launcher").getProperty("alt_worldlist");
    }

    public boolean isActive() {
        System.out.println("APPLET> ClientStub.isActive()");
        return true;// ?
    }

    public URL getDocumentBase() {
        return documentBase;
    }

    public URL getCodeBase() {
        return codeBase;
    }

    public String getParameter(String name) {
        String param = params.get(name);
        if(param.indexOf("slr.ws") > 0 && usealt)
            param = alturl;
        return param;
    }

    public AppletContext getAppletContext() {
        return context;
    }

    public void appletResize(int width, int height) {
        System.out.println("APPLET> ClientStub.appletResize(" + width + ", " + height + ")");
    }
}
