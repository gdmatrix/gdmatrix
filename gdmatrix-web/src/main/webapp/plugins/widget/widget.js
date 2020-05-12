// Copyright (c) 2006 Sébastien Gruhier (http://xilinus.com, http://itseb.com)
// 
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
// 
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// VERSION 1.1-trunk

if(typeof Draggable == 'undefined')
  throw("widget.js requires dragdrop.js library");

if(typeof Builder == 'undefined')
  throw("widget.js requires builder.js library");

// Xilinus namespace
if(typeof Xilinus == 'undefined')
  Xilinus = {}

Builder.dump();

Xilinus.Widget = Class.create();  
Xilinus.Widget.lastId = 0;

Object.extend(Xilinus.Widget.prototype, {
  initialize: function(id) {
    this._id = id;    
    this._div = document.getElementById(id);
    
    this._headerDiv   = document.getElementById(this._getId("header"));
    this._dragAreaDiv = document.getElementById(this._getId("dragArea"));
    this._contentDiv = document.getElementById(this._getId("content"));
    this._footerDiv  = document.getElementById(this._getId("footer"));

    this._div.widget = this;
    return this;
  },
  
  getElement: function() {
    return $(this._getId()) || $(this._div);
  },
  
  setHeader: function(title) {
    $(this._headerDiv).update(title);
    return this;
  },

  getHeader: function() {
    return $(this._headerDiv)
  },
  
  setDragArea: function(title) {
    $(this._dragAreaDiv).update(title);
    return this;        
  },

  getDragArea: function() {
    return $(this._dragAreaDiv)
  },

  setFooter: function(title) {
    $(this._footerDiv).update(title);
    return this;
  },
  
  getFooter: function() {
    return $(this._footerDiv)
  },
  
  setContent: function(title) {
    $(this._contentDiv).update(title);  
    return this;
  },
   
  getContent: function() {
    return $(this._contentDiv)
  },

  enableCloseButton: function() {
    formId = document.forms[0].id;
    closeButton = $(formId + ":" + this._id + "_close");
    if (closeButton != null) closeButton.style.visibility = "visible";
  },

  enableFoldButton: function() {
    formId = document.forms[0].id;
    foldButton = $(formId + ":" + this._id + "_fold");
    if (foldButton != null) foldButton.style.visibility = "visible";
  },

  enableUnfoldButton: function() {
    formId = document.forms[0].id;
    unfoldButton = $(formId + ":" + this._id + "_unfold");
    if (unfoldButton != null) unfoldButton.style.visibility = "visible";
  },

  updateHeight: function() {
    $(this._contentDiv).setStyle({height: null})
      
    var h = $(this._contentDiv).getHeight();
    $(this._contentDiv).setStyle({height: h + "px"})
  },
  
  // PRIVATE FUNCTIONS
  _getId: function(prefix) { 
      return (prefix ? prefix + "_" : "") + this._id;
  }
});

Effect.SwitchOff2 = function(element, portal) {
  element = $(element);
  var oldOpacity = element.getInlineOpacity();
  return new Effect.Appear(element, Object.extend({
    duration: 0.4,
    from: 0,
    transition: Effect.Transitions.flicker,
    afterFinishInternal: function(effect) {
      new Effect.Scale(effect.element, 1, {
        duration: 0.3, scaleFromCenter: true,
        scaleX: false, scaleContent: false, restoreAfterFinish: true,
        beforeSetup: function(effect) {
          effect.element.makePositioned().makeClipping();
        },
        afterFinishInternal: function(effect)
        {
          effect.element.remove();
          portal._updateColumnsHeight();
          portal.options.onUpdate(portal);
        }
      })
    }
  }, arguments[1] || {}));
}

Xilinus.Portal = Class.create()
Object.extend(Xilinus.Portal.prototype, {
  lastEvent: null,   
  widgets:   null,
  columns:   null, 

  initialize: function(widgetContainerId, options) {
    this.options = Object.extend({
                     url:          null,          // Url called by Ajax.Request after a drop
                     onOverWidget: null,          // Called when the mouse goes over a widget
                     onOutWidget:  null,          // Called when the mouse goes out of a widget                                           
                     onChange:     null,          // Called a widget has been move during drag and drop 
                     onUpdate:     null,          // Called a widget has been move after drag and drop
                     removeEffect: Effect.SwitchOff2,
                     accept: null
                   }, options)

    this._id = widgetContainerId;
    this._columns = new Array();
    this._widgets = new Array();

    var widgetContainer = document.getElementById(widgetContainerId);
    var numColumns = 0;
    for (i = 0; i < widgetContainer.childNodes.length; i++)
    {
      node = widgetContainer.childNodes.item(i);
      if (node.nodeName.toUpperCase() == 'DIV')
      {
        this._columns[numColumns++] = node;
      }
    }

    this._columns.each(function(element)
      {Droppables.add(element, {onHover: this.onHover.bind(this),
                                overlap: "vertical", 
                                accept: this.options.accept})}.bind(this));  

    this._outTimer  = null;
    
    // Draggable calls makePositioned for IE fix (??), I had to remove it for all browsers fix :) to handle properly zIndex
    this._columns.invoke("undoPositioned");    
    
    this._currentOverWidget = null; 
    this._widgetMouseOver = this.widgetMouseOver.bindAsEventListener(this);
    this._widgetMouseOut  = this.widgetMouseOut.bindAsEventListener(this);
    
    Draggables.addObserver({onEnd: this.endDrag.bind(this), onStart: this.startDrag.bind(this)});    
  },
  
  add: function(widget, draggable) {
    draggable = typeof draggable == "undefined" ? true : draggable
    // Add to widgets list
    this._widgets.push(widget);
    if (this.options.accept)
      widget.getElement().addClassName(this.options.accept)
    widget.updateHeight();
    
    // Make header draggable   
    if (draggable) {
      if (widget.getDragArea())
      {
        widget.draggable = new Draggable(widget.getElement(),{handle: widget._dragAreaDiv, revert: false});
        widget.getDragArea().addClassName("widget_draggable");
      }
      else
      {
        widget.draggable = new Draggable(widget.getElement(),{handle: widget._headerDiv, revert: false});
        widget.getHeader().addClassName("widget_draggable");
      }
    }
    
    // Update columns heights
    this._updateColumnsHeight();

    // enable close button
    widget.enableCloseButton();
    // enable fold button
    widget.enableFoldButton();
    // enable unfold button
    widget.enableUnfoldButton();

    // Add mouse observers  
    if (this.options.onOverWidget)
      widget.getElement().immediateDescendants().invoke("observe", "mouseover", this._widgetMouseOver);
    if (this.options.onOutWidget)
      widget.getElement().immediateDescendants().invoke("observe", "mouseout",  this._widgetMouseOut);
  },  
  
  remove: function(widget) {
    // Remove from the list
    this._widgets.reject(function(w) {return w == widget});

    // Remove observers
    if (this.options.onOverWidget)
      widget.getElement().immediateDescendants().invoke("stopObserving", "mouseover", this._widgetMouseOver);
    if (this.options.onOutWidget)
      widget.getElement().immediateDescendants().invoke("stopObserving", "mouseout",  this._widgetMouseOut);

    // Remove draggable
    if (widget.draggable)
      widget.draggable.destroy();
      
    // apply effect
    this.options.removeEffect(widget.getElement(), this);

    // Update columns heights
    this._updateColumnsHeight();
  },
                    
  serialize: function()
  {
    parameters = "";
    var i = 0;
    for (i = 0; i < this._columns.length; i++)
    {
      if (i > 0) parameters += "|";
      column = this._columns[i];
      parameters += this.getElementWidgetIds(column).join();
    }
    return parameters;
  },
  
  getElementWidgetIds: function(element)
  {
    var resultArr = [];
    for (var i = 0; i < element.childNodes.length; i++)
    {
      var auxElement = element.childNodes.item(i);
      if (auxElement.tagName == 'UL' || auxElement.tagName == 'LI')
      {
        resultArr = resultArr.concat(this.getElementWidgetIds(auxElement));
      }
      else
      {
        if (auxElement.id == undefined || auxElement.id == "") {}
        else
        {
          resultArr.push(auxElement.id);
        }
      }
    }
    return resultArr;
  },
          
  addWidgetControls: function(element)
  {
    $(element).observe("mouseover", this._widgetMouseOver); 
    $(element).observe("mouseout", this._widgetMouseOut); 
  },
  
  // EVENTS CALLBACKS
  widgetMouseOver: function(event) {
    this._clearTimer();
      
    var element =  Event.element(event).up(".widget");
    if (this._currentOverWidget == null || this._currentOverWidget != element) {
      if (this._currentOverWidget && this._currentOverWidget != element)
        this.options.onOutWidget(this, this._currentOverWidget.widget)    
        
      this._currentOverWidget = element;
      this.options.onOverWidget(this, element.widget)
    }
  },

  widgetMouseOut: function(event) { 
    this._clearTimer();
    var element =  Event.element(event).up(".widget"); 
    this._outTimer = setTimeout(this._doWidgetMouseOut.bind(this, element), 100);
  },
  
  _doWidgetMouseOut: function(element) {
    this._currentOverWidget = null;
    this.options.onOutWidget(this, element.widget)    
  },                                               
  
  // DRAGGABLE OBSERVER CALLBACKS
  startDrag: function(eventName, draggable) {    
    var widget = draggable.element;
    
    if (!this._widgets.find(function(w) {return w == widget.widget}))
      return;

    var column = widget.parentNode;
    
    // Create and insert ghost widget
    var ghost = DIV({className: 'widget_ghost'}, "");
    $(ghost).setStyle({height: (widget.getHeight() - this._correctGhostHeight(widget) - 8)  + 'px'});

    column.insertBefore(ghost, widget);  

    // IE Does not absolutize properly the widget, needs to set width before
    widget.setStyle({width: widget.getWidth() + "px"});
    
    // Absolutize and move widget on body
    Position.absolutize(widget);  
    document.body.appendChild(widget);   
    
    // Store ghost to drag widget for later use
    draggable.element.ghost = ghost; 
    
    // Store current position
    this._savePosition = this.serialize();
  },   

  endDrag: function(eventName, draggable) {
    var widget = draggable.element;      
    if (!this._widgets.find(function(w) {return w == widget.widget}))
      return;
    
    var column = widget.ghost.parentNode;
    
    column.insertBefore(draggable.element, widget.ghost); 
    widget.ghost.remove();   
    
    if (Prototype.Browser.Opera)     
      widget.setStyle({top: 0, left: 0, width: "100%", height: widget._originalHeight, zIndex: null, opacity: null, position: "relative"})
    else
      widget.setStyle({top: null, left: null, width: null, height: widget._originalHeight, zIndex: null, opacity: null, position: "relative"})
    
    widget.ghost = null;    
    widget.widget.updateHeight();
    this._updateColumnsHeight();
    
    // Fire events if changed
    if (this._savePosition != this.serialize()) {      
      if (this.options.url)  
        new Ajax.Request(this.options.url, {parameters: this.serialize()});
      
      if (this.options.onUpdate)
        this.options.onUpdate(this);
    }
  },

  onHover: function(dragWidget, dropon, overlap) {
    var offset = Position.cumulativeOffset(dropon);
    var x = offset[0] + 10;
    var y = offset[1] + (1 - overlap) * dropon.getHeight();

    // Check over ghost widget
    if (Position.within(dragWidget.ghost, x, y))
      return;
      
    // Find if it's overlapping a widget
    var found = false;
    var moved = false;
    for (var index = 0, len = this._widgets.length; index < len; ++index) {
      var w = this._widgets[index].getElement();
      if (w ==  dragWidget || w.parentNode != dropon)   
        continue;        

      if (Position.within(w, x, y)) {    
        var overlapPos = Position.overlap( 'vertical', w);
        // Bottom of the widget
        if (overlapPos < 0.5) {
          // Check if the ghost widget is not already below this widget
          if (w.next() != dragWidget.ghost) {
            w.parentNode.insertBefore(dragWidget.ghost, w.next());   
            moved = true;
          }
        } 
        // Top of the widget
        else {              
          // Check if the ghost widget is not already above this widget
          if (w.previous() != dragWidget.ghost) {      
            w.parentNode.insertBefore(dragWidget.ghost, w);   
            moved = true;
          }
        }        
        found = true;
        break;
      }
    }
    // Not found a widget
    if (! found) {
      // Check if dropon has ghost widget
      if (dragWidget.ghost.parentNode != dropon) {
        // Get last widget bottom value
        var last = dropon.immediateDescendants().last();
        var yLast = last ? Position.cumulativeOffset(last)[1] + last.getHeight() : 0; 
        if (y > yLast && last != dragWidget.ghost) {
          dropon.appendChild(dragWidget.ghost);
          moved = true;
        }
      }
    }  
    if (moved && this.options.onChange) 
      this.options.onChange(this)            

    this._updateColumnsHeight();
  },                
  
  // PRIVATE FUNCTIONS
  _updateColumnsHeight: function() { 
    var h = 0;
    this._columns.each(function(col) {
            h = Math.max(h, col.immediateDescendants().inject(0, function(sum, element) { 
              return sum + element.getHeight(); 
            }));          
    })
    this._columns.invoke("setStyle", {height: h + 'px'})
    },
  
  _clearTimer: function() {
    if (this._outTimer) {
      clearTimeout(this._outTimer);
      this._outTimer = null;
    }                        
  },
  
  _correctGhostHeight: function(widget) {
    var result = 0;
    if (widget.firstChild) //widget_padding
    {      
      result = result + this._getExtraHeight(widget.firstChild);
      if (widget.firstChild.firstChild) //widget_background
      {
        result = result + this._getExtraHeight(widget.firstChild.firstChild);
      }
    }
    return result;
  },

  _getExtraHeight: function(divElement)
  {
    var result = 0;
    if (divElement)
    {      
      if (window.getComputedStyle) //IE9 and newer browsers
      {
        var compStyle = window.getComputedStyle(divElement, "");
      }
      else //IE8 and older browsers
      {
        var compStyle = divElement.currentStyle;
      }
      var paddingTop = compStyle.paddingTop;
      if (paddingTop.indexOf('px') > 0) result = result + parseInt(paddingTop.substring(0, paddingTop.length - 2));
      var paddingBottom = compStyle.paddingBottom;
      if (paddingBottom.indexOf('px') > 0) result = result + parseInt(paddingBottom.substring(0, paddingBottom.length - 2));
    }
    return result;
  }  
  
});