/*
 * Jaro 2020.
 * Auxtoro estas Dmitrij Kobilin. 
 *
 * Nenia rajtigilo ekzistas.
 * Faru bone, ne faru malbone.
 */
package com.dkoby.birumin;

/*
 *
 */
public class TrackMessage {
    public MessageType msgType;
    public Object obj;


    public TrackMessage(MessageType msgType) {
        this.msgType = msgType;
        this.obj = null;
    }
    public TrackMessage(MessageType msgType, Object obj) {
        this.msgType = msgType;
        this.obj = obj;
    }

    public enum MessageType {
        TRACK_CONTROL_START,
        TRACK_CONTROL_RESUME,
        TRACK_CONTROL_PAUSE,
        TRACK_CONTROL_STOP,
        TRACK_CONTROL_ADD_WPT,
        TRACK_POINT_TIMEOUT,
        TRACK_UPDATE,
    }
}

