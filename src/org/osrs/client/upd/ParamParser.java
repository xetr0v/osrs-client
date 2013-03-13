package org.osrs.client.upd;

import org.osrs.client.prop.Properties;
import org.osrs.client.prop.Section;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * oldrsclient
 * 5.3.2013
 */
public class ParamParser {

    private Properties props;

    public static void main(String args[]) throws IOException {
        ParamParser p = new ParamParser("oldrsclient.properties");
        p.parseAndSave();
    }

    public ParamParser(String propertyFile) throws IOException {
        props = new Properties();
        props.load(propertyFile);
    }

    public void parseAndSave() throws IOException {
        props.removeSection("applet");

        Section section = new Section("applet");
        URL url = new URL(props.getSection("launcher").getProperty("base_url") + "/jav_config.ws");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while((line = in.readLine()) != null) {// begin best parsing world
            if(line.startsWith("param=")) {
                String combination = line.substring(6);
                section.putProperty(combination.substring(0, combination.indexOf('=')),
                        combination.substring(combination.indexOf('=') + 1));
            }
        }// end best parsing world

        props.putSection(section);
        props.save(props.getFilename());
    }
}
