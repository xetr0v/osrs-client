package org.osrs.client;

import org.osrs.client.prop.DefaultProperties;
import org.osrs.client.upd.Updater;
import org.osrs.client.prop.Properties;
import org.osrs.client.prop.Section;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * oldrsclient
 * 4.3.2013
 */
public class Launcher {

    private static Properties props;
    private static ClientReader clientReader;
    private static Thread clientThread;
    private static URLClassLoader classLoader;

    public static void main(String args[]) throws Exception {// no exceptio nhandling 4 u
        props = new Properties();

        try {
            props.load("oldrsclient.properties");
        } catch(FileNotFoundException ex) {
            System.err.println("Properties file not found! Generating default settings");
            props = DefaultProperties.get();
            props.save("oldrsclient.properties");
            Updater upd = new Updater();
            upd.update();
        }

        Section launcherSection = props.getSection("launcher");

        URL baseURL = new URL(launcherSection.getProperty("base_url"));

        Launcher launcher = new Launcher();

        Applet applet = null;
        while(applet == null) {
            try {
                if(!new File("gamepack.jar").exists())
                    throw new Exception("");
                applet = launcher.loadGame(new ClientStub(props.getSection("applet").getEntries(), baseURL, baseURL));
            } catch(Exception ex) {
                ex.printStackTrace();
                if(classLoader != null)
                    classLoader.close();
                System.err.println("Unable to load applet!");
                Updater upd = new Updater();
                upd.update();
            }
        }

        clientReader = new ClientReader(applet);
        clientThread = new Thread(clientReader);
        clientThread.start();

        JFrame frame = new JFrame("Old School RuneScape Game");

        Container frameContainer = frame.getContentPane();
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.CENTER;
        gridBagLayout.setConstraints(frameContainer, gridBagConstraints);
        frameContainer.setLayout(gridBagLayout);

        frameContainer.add(applet);
        frameContainer.setBackground(Color.black);
        frame.setContentPane(frameContainer);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public Applet loadGame(AbstractAppletStub appletStub)
            throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        classLoader = new URLClassLoader(new URL[] {new File("gamepack.jar").toURI().toURL()});
        Class<?> appletClass = classLoader.loadClass("client");
        Applet applet = (Applet) appletClass.newInstance();
        applet.setStub(appletStub);
        applet.init();
        applet.start();
        return applet;
    }

    public static Properties getProps() {
        return props;
    }

    public static URLClassLoader getClassLoader() {
        return classLoader;
    }
}
