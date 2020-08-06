/*
 * Jaro 2020.
 * Auxtoro estas Dmitrij Kobilin. 
 *
 * Nenia rajtigilo ekzistas.
 * Faru bone, ne faru malbone.
 */
"use strict";

var Util = function()
{
}
Util.prototype.formatTime = function(sec) {
    var a = [];
    var h, m, s;

    if (sec === null)
    {
        h = "--";
        m = "--";
        s = "--";
    } else {
        h = this.stringPadLeft(Math.floor((sec / 3600)), "0", 2);
        m = this.stringPadLeft(Math.floor((sec % 3600) / 60), "0", 2);
        s = this.stringPadLeft(Math.floor((sec % 3600) % 60), "0", 2);
    }
        
    a.push(h, m, s);
    return a.join(":");
}
Util.prototype.formatSpeed = function(value) {
    var s = [];
    if (value === null)
        return "--";

    value = Math.round(value * 10);
    s.push(Math.floor(value / 10));
    s.push(value % 10);

    return s.join(".");
}
/*
 *
 */
Util.prototype.stringPadLeft = function(st, padChar, max)
{
    if (max == undefined)
        return s;

    var s = st.toString();
    return s.length >= max ? s : this.stringPadLeft(padChar + s, padChar, max);
}
/*
 *
 */
Util.prototype.degms = function(value)
{
    var num;
    var r = [];

    var d = Math.floor(value);
    var m = Math.floor((value - d) * 60);
    var s = Math.floor((value - d - m / 60) * 3600 * 100);

    r.push(this.stringPadLeft(d, " ", 3));
    r.push("°");
    r.push(this.stringPadLeft(m, "0", 2));
    r.push("'");
    r.push(this.stringPadLeft(Math.floor(s / 100), "0", 2));
    r.push(".");
    r.push(this.stringPadLeft(s % 100, "0", 2));
    r.push("\"");

    return r.join("");
}
/*
 *
 */
Util.prototype.plusCode = function(lat, lon)
{
    var enc0 = ["2", "3", "4", "5", "6", "7", "8", "9", "C", "F", "G", "H", "J", "M", "P", "Q", "R", "V", "W", "X"];
    var enc1 = [
        ["2", "3", "4", "5"],
        ["6", "7", "8", "9"],
        ["C", "F", "G", "H"],
        ["J", "M", "P", "Q"],
        ["R", "V", "W", "X"],
    ];

    lat += 90;
    lon += 180;

    var code = [];

    var vlat = 0;
    var vlon = 0;
    var plat = 20;
    var plon = 20;

    for (var i = 1; i <= 6; i++)
    {
       vlat = Math.round(Math.floor(lat / plat));
       vlon = Math.round(Math.floor(lon / plon));

       if (i < 6) {
           code.push(enc0[vlat]);
           code.push(enc0[vlon]);
       } else {
           code.push(enc1[vlat][vlon]);
       }

       lat = lat - vlat * plat;
       lon = lon - vlon * plon;

       if (i == 4) {
           code.push("+");
       }

       if (i < 5) {
           plat = plat / 20.0;
           plon = plon / 20.0;
       } else {
           plat = plat / 5.0
           plon = plon / 4.0
       }
    }

    return code.join("");
}

