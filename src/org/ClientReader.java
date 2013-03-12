package org;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.*;
import org.apache.http.params.*;
import org.apache.http.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.prop.Section;

import java.applet.Applet;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * todo: use arrays, maybe
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * TGUS US ALL SO BAAAAAAAAAAAAAAAAAAAAAAD
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * oldrsclient
 * 5.3.2013
 */
public class ClientReader implements Runnable {

    private Applet applet;
    private boolean running;
    private Map<String, Class<?>> loadedClasses;

    public ClientReader(Applet applet) {
        this.applet = applet;
        running = true;
        loadedClasses = new HashMap<String, Class<?>>();
    }

    public void run() {
        Section clientSection = Launcher.getProps().getSection("client");
        Section fieldSection = Launcher.getProps().getSection("fields");

        if(!Boolean.parseBoolean(clientSection.getProperty("api_active")))
            return;

        Map<String, Field> fields = new HashMap<String, Field>();
        String playerClass = null, entityClass = null, entityX = null, entityY = null, playerName = null;
        int areaXMultiplier = 0, areaYMultiplier = 0, entityXMultiplier = 0, entityYMultiplier = 0;
        Field fMessageSenders = null, fMessageTypes = null, fMessages = null, fPlayers = null
                , fLocalPlayer = null, fAreaX = null, fAreaY = null;
        try {
            playerClass = fieldSection.getProperty("player_class");
            loadClass(playerClass);
            playerName = fieldSection.getProperty("player_name");
            areaXMultiplier = Integer.parseInt(fieldSection.getProperty("area_x_multiplier"));
            areaYMultiplier = Integer.parseInt(fieldSection.getProperty("area_y_multiplier"));
            entityClass = fieldSection.getProperty("entity_class");
            loadClass(entityClass);
            entityX = fieldSection.getProperty("entity_x");
            entityY = fieldSection.getProperty("entity_y");
            entityXMultiplier = Integer.parseInt(fieldSection.getProperty("entity_x_multiplier"));
            entityYMultiplier = Integer.parseInt(fieldSection.getProperty("entity_y_multiplier"));

            String val = fieldSection.getProperty("chat_message_senders");
            String clazz = val.substring(0, val.indexOf('.'));
            String field = val.substring(val.indexOf('.') + 1);
            if(clazz.equals("client"))
                fMessageSenders = getField(field);
            else
                fMessageSenders = getField(loadClass(clazz), field);
            fMessageSenders.setAccessible(true);

            val = fieldSection.getProperty("chat_message_types");
            clazz = val.substring(0, val.indexOf('.'));
            field = val.substring(val.indexOf('.') + 1);
            if(clazz.equals("client"))
                fMessageTypes = getField(field);
            else
                fMessageTypes = getField(loadClass(clazz), field);
            fMessageTypes.setAccessible(true);

            val = fieldSection.getProperty("chat_messages");
            clazz = val.substring(0, val.indexOf('.'));
            field = val.substring(val.indexOf('.') + 1);
            if(clazz.equals("client"))
                fMessages = getField(field);
            else
                fMessages = getField(loadClass(clazz), field);
            fMessages.setAccessible(true);

            val = fieldSection.getProperty("area_x");
            clazz = val.substring(0, val.indexOf('.'));
            field = val.substring(val.indexOf('.') + 1);
            if(clazz.equals("client"))
                fAreaX = getField(field);
            else
                fAreaX = getField(loadClass(clazz), field);
            fAreaX.setAccessible(true);

            val = fieldSection.getProperty("area_y");
            clazz = val.substring(0, val.indexOf('.'));
            field = val.substring(val.indexOf('.') + 1);
            if(clazz.equals("client"))
                fAreaY = getField(field);
            else
                fAreaY = getField(loadClass(clazz), field);
            fAreaY.setAccessible(true);

            val = fieldSection.getProperty("players");
            clazz = val.substring(0, val.indexOf('.'));
            field = val.substring(val.indexOf('.') + 1);
            if(clazz.equals("client"))
                fPlayers = getField(field);
            else
                fPlayers = getField(loadClass(clazz), field);
            fPlayers.setAccessible(true);

            val = fieldSection.getProperty("local_player");
            clazz = val.substring(0, val.indexOf('.'));
            field = val.substring(val.indexOf('.') + 1);
            if(clazz.equals("client"))
                fLocalPlayer = getField(field);
            else
                fLocalPlayer = getField(loadClass(clazz), field);
            fLocalPlayer.setAccessible(true);
        } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println("CLIENTREADER> Unable to find fields, stopping");
            running = false;
        }

        String apiHost = clientSection.getProperty("api_host");
        String apiURI = clientSection.getProperty("api_uri");
        String apiKey = clientSection.getProperty("api_key");

        /*
            635463389 * l == 30  // logged in
         */
        Pattern chatPattern = Pattern.compile(clientSection.getProperty("message_regex"));

        List<Message> messages = new ArrayList<Message>();
        List<Message> lastMessages = new ArrayList<Message>();
        while(running) {
            try {
                Thread.sleep(5000);
                String msg[] = (String[]) fMessages.get(null);
                String snd[] = (String[]) fMessageSenders.get(null);
                int type[] = (int[]) fMessageTypes.get(null);
                Object players[] = (Object[]) fPlayers.get(null);
                int areax = (Integer) fAreaX.get(null);
                int areay = (Integer) fAreaY.get(null);

                messages.clear();
                for(int i = 0; i < type.length; i++) {
                    if(type[i] == 2) {// public messages only
                        String sender = snd[i];
                        String message = msg[i].toLowerCase();
                        int playeridx = -1;
                        for(int j = 0; j < players.length; j++) {
                            if(players[j] != null)// && ((String) getField((players[j]).getClass(), playerName).get(players[j])).equalsIgnoreCase(sender))
                                playeridx = j;
                        }
                        //if(playeridx == -1) {
                        //    continue;
                        //}
                        int calcx = 0, calcy = 0;
                        if(playeridx != -1) {
                            int playerX = (Integer) getField(players[playeridx].getClass().getSuperclass(), entityX).get(players[playeridx]);
                            int playerY = (Integer) getField(players[playeridx].getClass().getSuperclass(), entityY).get(players[playeridx]);

                            calcx = (entityXMultiplier * playerX >> 7) + areaXMultiplier * areax;
                            calcy = (entityYMultiplier * playerY >> 7) + areaYMultiplier * areay;
                        }
                        if(!chatPattern.matcher(message).matches())
                            continue;
                        messages.add(new Message(snd[i], msg[i], type[i], calcx, calcy, System.currentTimeMillis() / 1000L));
                    }
                }
                if(lastMessages.size() > 100)
                    for(int i = 100; i < lastMessages.size(); i++)
                        lastMessages.remove(i);
                if(messages.size() > 0) {
                    messages.removeAll(lastMessages);
                    lastMessages.addAll(messages);

                    if(messages.size() == 0)
                        continue;

                    JSONObject jsonData = new JSONObject();
                    jsonData.put("source", "officialclient");
                    JSONArray jsonMessages = new JSONArray();
                    for(Message m : messages) {
                        JSONObject jsonMessage = new JSONObject();
                        jsonMessage.put("sender", m.sender);
                        jsonMessage.put("message", m.message);
                        if(m.playerx != 0)
                            jsonMessage.put("x", m.playerx);
                        if(m.playery != 0)
                            jsonMessage.put("y", m.playery);
                        jsonMessage.put("time", m.time);
                        jsonMessages.add(jsonMessage);
                    }
                    jsonData.put("data", jsonMessages);
                    System.out.println("pushing " + jsonData.toJSONString());
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost post = new HttpPost(apiHost + apiURI);
                    post.setEntity(new UrlEncodedFormEntity(constructParameters(apiKey, URLEncoder.encode(jsonData.toJSONString(), "UTF-8"))));
                    httpClient.execute(post);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                System.err.println("CLIENTREADER> " + ex);
                ex.printStackTrace();
            }
        }
    }

    private List<NameValuePair> constructParameters(String key, String data) {
        List<NameValuePair> out = new ArrayList<NameValuePair>();
        out.add(new BasicNameValuePair("key", key));
        out.add(new BasicNameValuePair("data", data));
        return out;
    }

    private Class<?> loadClass(String name) throws ClassNotFoundException {
        if(loadedClasses.containsKey(name))
            return loadedClasses.get(name);
        Class<?> clazz = Launcher.getClassLoader().loadClass(name);
        loadedClasses.put(name, clazz);
        return clazz;
    }

    private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private Field getField(String name) throws NoSuchFieldException {
        return getField(applet.getClass(), name);
    }



    public List<Message> removeDuplicates(List<Message> list, List<Message> oldList) {
        List<Message> newList = new ArrayList<Message>();
        for(Message old : oldList) {
            boolean add = true;
            for(Message msg : list) {
                if(msg.equals(old)) {
                    add = false;
                    break;
                }
            }
            if(add)
                newList.add(old);
        }
        return newList;
    }
}

