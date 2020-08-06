/*
 * Jaroj 2019-2020.
 * Auxtoro estas Dmitrij Kobilin. 
 *
 * Nenia rajtigilo ekzistas.
 * Faru bone, ne faru malbone.
 */
"use strict";

/*
 *
 */
var BlockElement = function(tagName, classList, htmlParentElement, innerHTML, events, blockParent) {
    this.element = document.createElement(tagName ? tagName : "div");

    this.blockParent = blockParent;

    if (htmlParentElement)
    {
        htmlParentElement.appendChild(this.element);
        this.htmlParentElement = htmlParentElement;
    }

    var opt = {
        classList: classList,
        innerHTML: innerHTML,
        events: events,
    }

    this.mutate(opt);
}
BlockElement.prototype.mkChild = function(tagName, classList, innerHTML, events) {
    if (Object.getPrototypeOf(this).constructor === BlockElementE)
    {
        return this.mkChildE({
            tagName: tagName,
            classList: classList,
            innerHTML: innerHTML,
            events: events,
        });
    } else {
        return new this.constructor(tagName, classList, this.element, innerHTML, events, this);
    }
}
BlockElement.prototype.mkChildOf = function(n, tagName, classList, innerHTML, events) {
    var blockParent = this;
    while (--n)
        blockParent = blockParent.getParent();
    return blockParent.mkChild(tagName, classList, innerHTML, events);
}
BlockElement.prototype.mkSibling = function(tagName, classList, innerHTML, events) {
    return this.getParent().mkChild(tagName, classList, innerHTML, events);
}
BlockElement.prototype.getParent = function() {
    return this.blockParent;
}
BlockElement.prototype.getParentN = function(n) {
    var blockParent = this;
    while (n--)
        blockParent = blockParent.getParent();
    return blockParent;
}
BlockElement.prototype.mapChilds = function(array, iterator) {
    if (array && iterator)
        array.forEach(iterator.bind(this, this));
    return this;
}
BlockElement.prototype.mapNchilds = function(n, iterator) {
    if (n > 0 && iterator)
    {
        for (var index = 0; index < n; index++)
            iterator.call(this, this, index);
    }
    return this;
}
BlockElement.prototype.apply = function(apply) {
    apply.call(this, this);
    return this;
}
BlockElement.prototype.mutate = function(opt) {
    opt = opt || {};

    if (opt.classList)
    {
        var classList = []
        for (var i = 0; i < this.element.classList.length; i++)
            classList.push(this.element.classList.item(i));
        classList.forEach(function(c) {
            this.removeClass(c);
        }.bind(this));
        opt.classList.forEach(function(c) {
            this.addClass(c);
        }.bind(this));
    }

    if (opt.innerHTML !== undefined && opt.innerHTML !== null)
        this.setInner(opt.innerHTML);

    if (opt.attrs)
    {
        Object.keys(opt.attrs).forEach(function(key) {
            this.element.setAttribute(key, opt.attrs[key]); 
        }.bind(this));
    }
    if (opt.element)
    {
        Object.keys(opt.element).forEach(function(key) {
            this.element[key] = opt.element[key]; 
        }.bind(this));
    }
    if (opt.events)
    {
        Object.keys(opt.events).forEach(function(key) {
            this.element.addEventListener(key, opt.events[key]); 
        }.bind(this));
    }
    if (opt.style)
    {
        Object.keys(opt.style).forEach(function(key) {
            this.element.style[key] = opt.style[key];
        }.bind(this));
    }
    return this;
}
BlockElement.prototype.shown = function() {
    return !this.element.classList.contains("displayNone");
}
BlockElement.prototype.hide = function() {
    this.element.classList.add("displayNone");
    return this;
}
BlockElement.prototype.show = function() {
    this.element.classList.remove("displayNone");
    return this;
}
BlockElement.prototype.toggle = function() {
    if (this.shown())
        this.hide();
    else
        this.show();
    return this;
}
BlockElement.prototype.addClass = function(value) {
    this.element.classList.add(value);
    return this;
}
BlockElement.prototype.removeClass = function(value) {
    this.element.classList.remove(value);
    return this;
}
BlockElement.prototype.toggleClass = function(value) {
    this.element.classList.toggle(value);
    return this;
}
BlockElement.prototype.disable = function() {
    this.element.setAttribute("disabled", "");
    return this;
}
BlockElement.prototype.enable = function() {
    this.element.removeAttribute("disabled");
    return this;
}
BlockElement.prototype.setInner = function(value, ref) {
    this.element.innerHTML = value;
}
BlockElement.prototype.setValue = function(value, ref) {
    this.element.value = value;
}
Object.defineProperty(BlockElement.prototype, "innerHTML", {
    set: function innerHTML(value) {
        this.setInner(value);
    },
    get: function () {
        return this.element.innerHTML;
    },
});
Object.defineProperty(BlockElement.prototype, "value", {
    set: function value(v) {
        this.setValue(v);
    },
    get: function () {
        return this.element.value;
    },
});
BlockElement.prototype.destroy = function() {
    if (this.htmlParentElement && this.htmlParentElement.contains(this.element))
        this.htmlParentElement.removeChild(this.element);
}
BlockElement.prototype.addEvent = function(eventName, command) {
    this.element.addEventListener(eventName, command);
    return this;
}
BlockElement.prototype.mkChildE = function(opt) {
    opt.htmlParentElement = this.element;
    opt.blockParent = this;
    return new BlockElementE(opt);
}
BlockElement.prototype.mkChildOfE = function(n, opt) {
    var blockParent = this;
    while (--n)
        blockParent = blockParent.getParent();
    return blockParent.mkChildE(opt);
}
BlockElement.prototype.mkSiblingE = function(opt) {
    return this.getParent().mkChildE(opt);
}
/*
 *
 */
var BlockElementE = function(opt) {
    this.opt = opt;

    BlockElement.call(this,
            opt.tagName,
            opt.classList,
            opt.htmlParentElement,
            opt.innerHTML,
            opt.events,
            opt.blockParent);

    this.mutate(opt);
}
BlockElementE.prototype = Object.create(BlockElement.prototype);
BlockElementE.prototype.constructor = BlockElementE;

