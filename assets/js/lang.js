/*
 *
 */
"use strict";

/*
 *
 */
var Lang = function() {
    this.select();
}

Lang.prototype.select = function(value) {
    if (navigator !== undefined && navigator.language)
    {
        console.log("navigator.language", navigator.language)
        var v = navigator.language.substring(0, 2).toLowerCase();
        switch (v)
        {
            case "ru":
                value = "ru";
                break;
            case "en":
            case "us":
            case "gb":
                value = "en";
                break;
        }
    }
    if (value === null)
        this.currentLang = "en";
    else
        this.currentLang = value;

//    var reload = false;
//    if (value === undefined)
//    {
//        value = localStorage.getItem("currentLang");
//        if (value !== null)
//        {
//            this.currentLang = value;
//        } else {
//            value = null;
//            if (navigator !== undefined && navigator.language)
//            {
//                console.log("navigator.language", navigator.language)
//                var v = navigator.language.substring(0, 2).toLowerCase();
//                switch (v)
//                {
//                    case "ru":
//                        value = "ru";
//                        break;
//                    case "en":
//                    case "us":
//                    case "gb":
//                        value = "en";
//                        break;
//                }
//            }
//            if (value === null)
//                this.currentLang = "en";
//            else
//                this.currentLang = value;
//        }
//    } else {
//        reload = true;
//    }
//
//    localStorage.setItem("currentLang", value);
//    if (reload)
//        location.reload();
}

Lang.prototype.msgCat = function(handle, handle2) {
    var l = this[this.currentLang];

    if (l && l[handle])
    {
        if (handle2 !== undefined && l[handle][handle2])
            return l[handle][handle2];
        else
            return l[handle];
    } else {
        return handle;
    }
}

/* :18,$!sort */
Lang.prototype.ru = {};
Lang.prototype.ru["January"] = "Январь";
Lang.prototype.ru["February"] = "Февраль";
Lang.prototype.ru["March"] = "Март";
Lang.prototype.ru["April"] = "Апрель";
Lang.prototype.ru["May"] = "Май";
Lang.prototype.ru["June"] = "Июнь";
Lang.prototype.ru["July"] = "Июль";
Lang.prototype.ru["August"] = "Август";
Lang.prototype.ru["September"] = "Сентябрь";
Lang.prototype.ru["October"] = "Октябрь";
Lang.prototype.ru["November"] = "Ноябрь";
Lang.prototype.ru["December"] = "Декабрь";
Lang.prototype.ru["Mo"] = "Пн";
Lang.prototype.ru["Tu"] = "Вт";
Lang.prototype.ru["We"] = "Ср";
Lang.prototype.ru["Th"] = "Чт";
Lang.prototype.ru["Fr"] = "Пт";
Lang.prototype.ru["St"] = "Сб";
Lang.prototype.ru["Sn"] = "Вс";

var lang = new Lang();

