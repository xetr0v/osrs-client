package org;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;

/**
 * oldrsclient
 * 4.3.2013
 */
public abstract class AbstractAppletStub implements AppletStub {

    public abstract boolean isActive();
    public abstract URL getDocumentBase();
    public abstract URL getCodeBase();
    public abstract String getParameter(String name);
    public abstract AppletContext getAppletContext();
    public abstract void appletResize(int width, int height);
}
