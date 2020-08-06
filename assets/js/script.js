/*
 * Jaro 2020.
 * Auxtoro estas Dmitrij Kobilin. 
 *
 * Nenia rajtigilo ekzistas.
 * Faru bone, ne faru malbone.
 */
"use strict";

/*
 *
 */
var Application = function()
{
    var app = this;

    this.controls = {};
    this.display = {};
    this.display.stat = {};

    new BlockElement("div", ["mainPanel"], document.body)
        .apply(function(mainPanel) {
            app.mainPanel = mainPanel;

            /* Moving time. */
            mainPanel.mkChild("div", ["panel", "movingTime"])
                .mkChild("div", ["title"], "Moving time")
                .mkSibling("div", ["display"], "")
                    .apply(function(block) {
                        app.display.movingTime = {};
                        app.display.movingTime.block = block;
                    });
            /* Status bar */
            mainPanel
                .mkChild("div", ["panel", "statusBar"])
                    .mkChild("div", [], "")
                        .apply(function(block) {
                            app.display.stat.provider = {};
                            app.display.stat.provider.block = block;
                        })
                    .mkSibling("div", [])
                        .apply(function(block) {
                            app.display.stat.state = {};
                            app.display.stat.state.block = block;
                        })
                    .mkSibling("div", [])
                        .apply(function(block) {
                            app.display.stat.points = {};
                            app.display.stat.points.block = block;
                        })
                    ;
            /*
             * Controls:
             *
             */
            mainPanel
                .mkChild("div", ["recordControls"])
                    .mkChild("button", ["control"], "&#x25CF")
                        .apply(function(block) {
                            app.controls.record = {}
                            app.controls.record.block = block;
                            block.addEvent("click", function() {
                                app.startRecord();
                            });
                        })
                    .mkSibling("button", ["control"], "&#x2016")
                        .apply(function(block) {
                            app.controls.pause = {}
                            app.controls.pause.block = block;
                            block.addEvent("click", function() {
                                app.pauseRecord();
                            });
                        }).disable()
                    .mkSibling("button", ["control"], "&#x25BA")
                        .apply(function(block) {
                            app.controls.resume = {}
                            app.controls.resume.block = block;
                            block.addEvent("click", function() {
                                app.resumeRecord();
                            });
                        }).disable()
                    .mkSibling("button", ["control"], "&#x25A0")
                        .apply(function(block) {
                            app.controls.stop = {}
                            app.controls.stop.block = block;
                            block.addEvent("click", function() {
                                app.stopRecord();
                            });
                        }).disable();


            /*
             * Distance, elevation.
             */
            mainPanel.mkChild("div", ["dualDisplay"])
                .mkChild("div", [])
                    .mkChild("div", ["title"], "Distance, km") 
                    .mkSibling("div", ["display", "speed"]) 
                    .apply(function(block) {
                        app.display.distance = {};
                        app.display.distance.block = block;
                    })
            .getParentN(2)
                .mkChild("div", [])
                    .mkChild("div", ["title"], "Elevation, m") 
                    .mkSibling("div", ["display", "speed"]) 
                    .apply(function(block) {
                        app.display.elevation = {};
                        app.display.elevation.block = block;
                    });
            /*
             * Current speed, average speed.
             */
            mainPanel.mkChild("div", ["panel", "dualDisplay"])
                .mkChild("div", [])
                    .mkChild("div", ["title"], "Speed, km/h") 
                    .mkSibling("div", ["display", "speed", "currentSpeed"]) 
                    .apply(function(block) {
                        app.display.currentSpeed = {};
                        app.display.currentSpeed.block = block;
                    })
            .getParentN(2)
                .mkChild("div", [])
                    .mkChild("div", ["title"], "Average speed, km/h") 
                    .mkSibling("div", ["display", "speed"]) 
                    .apply(function(block) {
                        app.display.averageSpeed = {};
                        app.display.averageSpeed.block = block;
                    });
            /*
             * Latitude, longitude, altitude.
             */
            mainPanel.mkChild("div", ["panel", "tableDisplay"])
                .mkChild("div", [])
                    .mkChild("div", ["title"], "Lat:") 
                    .mkSibling("div", ["display"]) 
                    .apply(function(block) {
                        app.display.latitude = {};
                        app.display.latitude.block = block;
                    })
                    .mkSibling("div", ["display"]) 
                    .apply(function(block) {
                        app.display.latitudeArc = {};
                        app.display.latitudeArc.block = block;
                    })
                .getParent()
            .getParent()
                .mkChild("div", [])
                    .mkChild("div", ["title"], "Lon:") 
                    .mkSibling("div", ["display"]) 
                    .apply(function(block) {
                        app.display.longitude = {};
                        app.display.longitude.block = block;
                    })
                    .mkSibling("div", ["display"]) 
                    .apply(function(block) {
                        app.display.longitudeArc = {};
                        app.display.longitudeArc.block = block;
                    })
                .getParent()
            .getParent()
                .mkChild("div", [])
                    .mkChild("div", ["title"], "Alt:") 
                    .mkSibling("div", ["display"]) 
                    .apply(function(block) {
                        app.display.altitude = {};
                        app.display.altitude.block = block;
                    })
                .getParent()
            .getParent()
                .mkChild("div", [])
                    .mkChild("div", ["title"], "+Code:") 
                    .mkSibling("div", ["display"]) 
                    .apply(function(block) {
                        app.display.pluscode = {};
                        app.display.pluscode.block = block;
                    })
                ;
            /*
             * Controls:
             *     * Night mode.
             *     * Keep screen on.
             */
            mainPanel
                .mkChild("div", ["panel", "bottomControls"])
                    .mkChild("button", ["control"])
                        .apply(function(block) {
                            app.controls.night = {};
                            app.controls.night.block = block;
                            block.addEvent("click", function() {
                                app.switchNight();
                            });
                        })
                    .mkSibling("button", ["control"])
                        .apply(function(block) {
                            app.controls.screenHold = {};
                            app.controls.screenHold.block = block;
                            block.addEvent("click", function() {
                                app.switchScreenHold();
                            });
                        });

        });

    this.switchNight(false);
    this.switchScreenHold(false);

    this.applyData(null);
}
/*
 *
 */
Application.prototype.switchNight = function(value)
{
    if (value !== undefined)
    {
        this.controls.night.value = value;
        if (value)
            document.body.classList.add("night");
    } else {
        document.body.classList.toggle("night");
        this.controls.night.value = this.controls.night.value ? false : true;
    }
    if (this.controls.night.value)
    {
        this.controls.night.block.setInner("&#x1F319");
    } else {
        this.controls.night.block.setInner("&#x2600");
    }

//    /* XXX */
//    this.controls.night.block.disable();
}
/*
 *
 */
Application.prototype.switchScreenHold = function(value)
{
    if (value !== undefined)
    {
        this.controls.screenHold.value = value;
    } else {
        this.controls.screenHold.value = this.controls.screenHold.value ? false : true;
        value = this.controls.screenHold.value;
    }
    if (this.controls.screenHold.value)
    {
        this.controls.screenHold.block.setInner("&#x2588");
    } else {
        this.controls.screenHold.block.setInner("&nbsp;");
    }
    android.keepScreenOn(value);
}
/*
 *
 */
Application.prototype.applyData = function(data)
{
    if (data === undefined)
        return;
    if (data === null)
    {
        data = {};
        data.currentSpeed = null;
        data.averageSpeed = null;
        data.movingTime   = null;
        data.distance     = null;
        data.elevation    = null;
        data.latitude     = null;
        data.longitude    = null;
        data.altitude     = null;
        data.points     = null;
    }

    if (data.points !== undefined)
        this.display.stat.points.block.setInner(data.points === null ? "-" : data.points);
    if (data.currentSpeed !== undefined)
        this.display.currentSpeed.block.setInner(util.formatSpeed(data.currentSpeed));
    if (data.averageSpeed !== undefined)
        this.display.averageSpeed.block.setInner(util.formatSpeed(data.averageSpeed));
    if (data.movingTime !== undefined)
        this.display.movingTime.block.setInner(util.formatTime(data.movingTime));
    if (data.distance !== undefined)
        this.display.distance.block.setInner(util.formatSpeed(data.distance));
    if (data.elevation !== undefined)
        this.display.elevation.block.setInner(
                data.elevation === null ? "--" : data.elevation);
    if (data.latitude !== undefined)
    {
        this.display.latitude.block.setInner(data.latitude === null ? "--" : data.latitude);
        this.display.latitudeArc.block.setInner(data.latitude === null ? "--" : util.degms(data.latitude));
    }
    if (data.longitude !== undefined)
    {
        this.display.longitude.block.setInner(data.longitude === null ? "--" : data.longitude);
        this.display.longitudeArc.block.setInner(data.longitude === null ? "--" : util.degms(data.longitude));
    }
    if (data.altitude !== undefined)
        this.display.altitude.block.setInner(
                data.altitude === null ? "--" : data.altitude);
    if (data.latitude !== undefined && data.longitude !== undefined)
    {
        this.display.pluscode.block.setInner(
                data.latitude === null ? "--" :
                util.plusCode(data.latitude, data.longitude)
        );
    }
}
/*
 *
 */
Application.prototype.startRecord = function()
{
    app.controls.record.block.disable();
    android.startTrack();
}
/*
 *
 */
Application.prototype.pauseRecord = function()
{
    android.pauseTrack();
}
/*
 *
 */
Application.prototype.resumeRecord = function()
{
    app.controls.pause.block.enable();
    app.controls.resume.block.disable();
    app.controls.stop.block.disable();

    android.resumeTrack();
}
/*
 *
 */
Application.prototype.stopRecord = function()
{
    new Dialog("Stop track recording?", function(yesno) {
        if (yesno == "yes")
        {
            android.stopTrack();
        } else {

        }
    });
}
/*
 *
 */
Application.prototype.statusUpdate = function()
{
//    console.log("STATUS " + android.getStatus());
    var stat = JSON.parse(android.getStatus());

    if (stat.track === undefined)
        return;

    if (stat.track.provider)
        app.display.stat.provider.block.innerHTML = stat.track.provider.toUpperCase();

    if (stat.track.state === undefined) 
    {
        this.applyData(null);
        return;
    }

    /*
     * NEW,
     * GET_POSITION,
     * RECORD,
     * PAUSE,
     * SAVE,
     * DONE,
     * ERROR,
     */
    {
        var s = "-";
        switch (stat.track.state)
        {
            case "NEW"         : s = "-"; break;
            case "GET_POSITION": s = "get position"; break;
            case "RECORD"      : s = "record"; break;
            case "PAUSE"       : s = "pause"; break;
            case "SAVE"        : s = "save"; break;
            case "ERROR"       : s = "error"; break;
            case "DONE"        : s = "done"; break;
            case "CACNEL"      : s = "cancel"; break;
        }
        app.display.stat.state.block.innerHTML = s.toUpperCase();
    }

    switch (stat.track.state)
    {
        case "NEW"         :
            app.controls.record.block.removeClass("record");
            app.controls.record.block.enable();
            app.controls.pause.block.disable();
            app.controls.resume.block.disable();
            app.controls.stop.block.disable();
            break;
        case "GET_POSITION":
            app.controls.record.block.removeClass("record");
            app.controls.record.block.disable();
            app.controls.pause.block.disable();
            app.controls.resume.block.disable();
            app.controls.stop.block.enable();
            break;
        case "RECORD"         :
            app.controls.record.block.addClass("record");
            app.controls.record.block.disable();
            app.controls.pause.block.enable();
            app.controls.resume.block.disable();
            app.controls.stop.block.disable();
            break;
        case "PAUSE"       :
            app.controls.record.block.addClass("record");
            app.controls.record.block.disable();
            app.controls.pause.block.disable();
            app.controls.resume.block.enable();
            app.controls.stop.block.enable();
            break;
        case "SAVE"        :
            app.controls.record.block.removeClass("record");
            app.controls.record.block.disable();
            app.controls.pause.block.disable();
            app.controls.resume.block.disable();
            app.controls.stop.block.disable();
            break;
        case "ERROR"       :
            app.controls.record.block.removeClass("record");
            app.controls.record.block.disable();
            app.controls.pause.block.disable();
            app.controls.resume.block.disable();
            app.controls.stop.block.disable();
            break;
        case "DONE"        :
            app.controls.record.block.removeClass("record");
            app.controls.record.block.enable();
            app.controls.pause.block.disable();
            app.controls.resume.block.disable();
            app.controls.stop.block.disable();
            break;
        case "CANCEL"       :
            app.controls.record.block.removeClass("record");
            app.controls.record.block.disable();
            app.controls.pause.block.disable();
            app.controls.resume.block.disable();
            app.controls.stop.block.disable();
            break;

    }

    {
        var data = {};
        switch (stat.track.state)
        {
            case "NEW":
            case "DONE":
            case "ERROR":
            case "CANCEL":
                this.applyData(null);
                break;
            default:
                if (stat.track.points)
                    app.display.stat.points.block.innerHTML = stat.track.points;

//                stat.track.latitude  = 56.488187;
//                stat.track.longitude = 84.948562;

                if (stat.track.latitude !== undefined)
                    data.latitude = stat.track.latitude;
                if (stat.track.longitude !== undefined)
                    data.longitude = stat.track.longitude;
                if (stat.track.altitude !== undefined)
                    data.altitude = stat.track.altitude;
                if (stat.track.elevation !== undefined)
                    data.elevation = stat.track.elevation;
                /* m/s -> km/h */
                if (stat.track.speed !== undefined)
                {
                    data.currentSpeed = stat.track.speed * 3.6;
                }
                if (stat.track.movingTime !== undefined)
                    data.movingTime = stat.track.movingTime;

                this.applyData(data);
        }
    }
}
/*
 *
 */
var Dialog = function(msg, callback)
{
    var dialogBlock;
    new BlockElement("div", ["dialog"], document.body)
        .apply(function(block) {
            dialogBlock = block;
        })
        .mkChild("div", [])
            .mkChild("div", [])
                .mkChild("div", [], msg)
            .getParent()
            .mkSibling("div", [])
                .mkChild("button", [], "OK")
                .addEvent("click", function() {
                    dialogBlock.destroy();
                    callback("yes");
                })
                .mkSibling("button", [], "Cancel")
                .addEvent("click", function() {
                    dialogBlock.destroy();
                    callback("no");
                })
    ;
}
/*
 *
 */
var util = new Util();
var app = new Application();

