package org.osrs.client.prop;

/**
 * osrs-client
 * 13.3.2013
 */
public class DefaultProperties extends Properties {

    public static Properties get() {
        Properties p = new Properties();
        Section s = new Section("launcher");
        s.putProperty("alt_worldlist", "http://guarded-thicket-8695.herokuapp.com/worldlist/raw");
        s.putProperty("use_alt_worldlist", "true");
        s.putProperty("base_url", "http://oldschool3.runescape.com");
        p.putSection(s);

        s = new Section("client");
        s.putProperty("message_regex", "((.*)(((b|s)\\d+)|(buy|sell)|(wtb|wts)|(buying|selling))(.*))+");
        s.putProperty("api_key", "724738957923129");
        s.putProperty("api_uri", "/api/submit");
        s.putProperty("api_host", "runefeed.herokuapp.com");
        s.putProperty("api_active", "true");
        p.putSection(s);

        p.putSection(new Section("applet"));
        p.putSection(new Section("fields"));

        return p;
    }
}
