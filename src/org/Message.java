package org;

/**
 * oldrsclient
 * 5.3.2013
 */
public class Message {

    public String sender, message;
    public int type, playerx, playery;
    public long time;

    public Message(String sender, String message, int type, int playerx, int playery, long time) {
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.time = time;
        this.playerx = playerx;
        this.playery = playery;
    }

    public boolean equals(Object m) {
        return sender.equals(((Message) m).sender) && message.equals(((Message) m).message) && type == ((Message) m).type;
    }
}
