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
        WAKELOCK,
        DIALOG_INFO,
        TRACK_CONTROL_START,
        TRACK_CONTROL_PAUSE,
        TRACK_CONTROL_RESUME,
        TRACK_CONTROL_STOP,
        TRACK_CONTROL_ADD_WPT,
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

