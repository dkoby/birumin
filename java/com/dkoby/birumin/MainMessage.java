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
public class MainMessage {
    public MsgType msgType;
    public Object obj;

    public enum MsgType {
        SCREEN_ON,
        TRACK_START,
        TRACK_PAUSE,
        TRACK_RESUME,
        TRACK_STOP,
        TRACK_UPDATE,
    }

    /*
     *
     */
    public MainMessage(MsgType type) {
        this.msgType = type;
        this.obj     = null;
    }
    /*
     *
     */
    public MainMessage(MsgType type, Object obj) {
        this.msgType = type;
        this.obj     = obj;
    }

}

