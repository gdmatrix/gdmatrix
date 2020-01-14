/* Extended controls /
/* Copyright (c) 2006-2008 MetaCarta, Inc., published under the Clear BSD
 * license.  See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license. */

/* Delete feature Control */
OpenLayers.Control.DeleteFeature = OpenLayers.Class(OpenLayers.Control,
{
  initialize: function(layer, options)
  {
    OpenLayers.Control.prototype.initialize.apply(this, [options]);
    this.layer = layer;
    this.handler = new OpenLayers.Handler.Feature(this, layer, {click: this.clickFeature});
  },
  activate: function ()
  {
    setMode("edit");
    OpenLayers.Control.prototype.activate.apply(this, arguments);
  },
  clickFeature: function(feature)
  {
    // if feature doesn't have a fid, destroy it
    if (feature.fid == undefined)
    {
      this.layer.destroyFeatures([feature]);
    }
    else
    {
      feature.state = OpenLayers.State.DELETE;
      this.layer.events.triggerEvent("afterfeaturemodified", {feature: feature});
      feature.renderIntent = "select";
      this.layer.drawFeature(feature);
    }
  },
  setMap: function(map)
  {
    this.handler.setMap(map);
    OpenLayers.Control.prototype.setMap.apply(this, arguments);
  },
  CLASS_NAME: "OpenLayers.Control.DeleteFeature"
});



/* Draw feature extended Control */
OpenLayers.Control.DrawFeatureEx = OpenLayers.Class(OpenLayers.Control.DrawFeature,
{
  initialize: function (layer, handler, options)
  {
    OpenLayers.Control.DrawFeature.prototype.initialize.apply(this, [layer, handler, options]);
    // configure the keyboard handler
    this.keyboardCallbacks =
    {
      keydown: this.handleKeyDown
    };
    this.keyboardHandler = new OpenLayers.Handler.Keyboard(this, this.keyboardCallbacks, {});
  },
  handleKeyDown: function (evt)
  {
    switch (evt.keyCode)
    {
      case 90: // z
        if (evt.metaKey || evt.ctrlKey)
        {
          this.undo();
          handled = true;
        }
        break;
      case 89: // y
        if (evt.metaKey || evt.ctrlKey)
        {
          this.redo();
          handled = true;
        }
        break;
      case 27: // esc
        this.cancel();
        handled = true;
        break;
    }
  },
  activate: function ()
  {
    setMode("edit");
    OpenLayers.Control.DrawFeature.prototype.activate.apply(this, arguments);
    this.keyboardHandler.activate();
  },
  deactivate: function ()
  {
    OpenLayers.Control.DrawFeature.prototype.deactivate.apply(this, arguments);
    this.keyboardHandler.deactivate();
  }
});



/* GetFeatureInfo extended Control */
OpenLayers.Control.WMSGetFeatureInfoEx = OpenLayers.Class(OpenLayers.Control, {

   /**
     * APIProperty: hover
     * {Boolean} Send GetFeatureInfo requests when mouse stops moving.
     *     Default is false.
     */
    hover: false,

    /**
     * APIProperty: drillDown
     * {Boolean} Drill down over all WMS layers in the map. When
     *     using drillDown mode, hover is not possible, and an infoFormat that
     *     returns parseable features is required. Default is false.
     */
    drillDown: false,

    /**
     * APIProperty: maxFeatures
     * {Integer} Maximum number of features to return from a WMS query. This
     *     sets the feature_count parameter on WMS GetFeatureInfo
     *     requests.
     */
    maxFeatures: 10,

    /**
     * APIProperty: clickCallback
     * {String} The click callback to register in the
     *     {<OpenLayers.Handler.Click>} object created when the hover
     *     option is set to false. Default is "click".
     */
    clickCallback: "click",

    /**
     * APIProperty: output
     * {String} Either "features" or "object". When triggering a getfeatureinfo
     *     request should we pass on an array of features or an object with with
     *     a "features" property and other properties (such as the url of the
     *     WMS). Default is "features".
     */
    output: "features",

    /**
     * APIProperty: layers
     * {Array(<{url, visible, locatable, style, filter}>)} The layers to query for feature info.
     */
    layers: null,

    /**
     * APIProperty: queryVisible
     * {Boolean} If true, filter out hidden layers when searching the map for
     *     layers to query.  Default is false.
     */
    queryVisible: false,

    /**
     * APIProperty: infoFormat
     * {String} The mimetype to request from the server. If you are using
     *     drillDown mode and have multiple servers that do not share a common
     *     infoFormat, you can override the control's infoFormat by providing an
     *     INFO_FORMAT parameter in your <OpenLayers.Layer.WMS> instance(s).
     */
    infoFormat: 'text/html',

    /**
     * APIProperty: vendorParams
     * {Object} Additional parameters that will be added to the request, for
     *     WMS implementations that support them. This could e.g. look like
     * (start code)
     * {
     *     radius: 5
     * }
     * (end)
     */
    vendorParams: {},

    /**
     * APIProperty: format
     * {<OpenLayers.Format>} A format for parsing GetFeatureInfo responses.
     *     Default is <OpenLayers.Format.WMSGetFeatureInfo>.
     */
    format: null,

    /**
     * APIProperty: formatOptions
     * {Object} Optional properties to set on the format (if one is not provided
     *     in the <format> property.
     */
    formatOptions: null,

    /**
     * APIProperty: handlerOptions
     * {Object} Additional options for the handlers used by this control, e.g.
     * (start code)
     * {
     *     "click": {delay: 100},
     *     "hover": {delay: 300}
     * }
     * (end)
     */

    /**
     * Property: handler
     * {Object} Reference to the <OpenLayers.Handler> for this control
     */
    handler: null,

    /**
     * Property: hoverRequest
     * {<OpenLayers.Request>} contains the currently running hover request
     *     (if any).
     */
    hoverRequest: null,

    /**
     * APIProperty: events
     * {<OpenLayers.Events>} Events instance for listeners and triggering
     *     control specific events.
     *
     * Register a listener for a particular event with the following syntax:
     * (code)
     * control.events.register(type, obj, listener);
     * (end)
     *
     * Supported event types (in addition to those from <OpenLayers.Control.events>):
     * beforegetfeatureinfo - Triggered before the request is sent.
     *      The event object has an *xy* property with the position of the
     *      mouse click or hover event that triggers the request.
     * nogetfeatureinfo - no queryable layers were found.
     * getfeatureinfo - Triggered when a GetFeatureInfo response is received.
     *      The event object has a *text* property with the body of the
     *      response (String), a *features* property with an array of the
     *      parsed features, an *xy* property with the position of the mouse
     *      click or hover event that triggered the request, and a *request*
     *      property with the request itself. If drillDown is set to true and
     *      multiple requests were issued to collect feature info from all
     *      layers, *text* and *request* will only contain the response body
     *      and request object of the last request.
     */

    /**
     * Constructor: <OpenLayers.Control.WMSGetFeatureInfo>
     *
     * Parameters:
     * options - {Object}
     */
    initialize: function(options) {
        options = options || {};
        options.handlerOptions = options.handlerOptions || {};

        OpenLayers.Control.prototype.initialize.apply(this, [options]);

        if(!this.format) {
            this.format = new OpenLayers.Format.WMSGetFeatureInfo(
                options.formatOptions
            );
        }

        if(this.drillDown === true) {
            this.hover = false;
        }

        if(this.hover) {
            this.handler = new OpenLayers.Handler.Hover(
                   this, {
                       'move': this.cancelHover,
                       'pause': this.getInfoForHover
                   },
                   OpenLayers.Util.extend(this.handlerOptions.hover || {}, {
                       'delay': 250
                   }));
        } else {
            var callbacks = {};
            callbacks[this.clickCallback] = this.getInfoForClick;
            this.handler = new OpenLayers.Handler.Click(
                this, callbacks, this.handlerOptions.click || {});
      }
    },

    /**
     * Method: getInfoForClick
     * Called on click
     *
     * Parameters:
     * evt - {<OpenLayers.Event>}
     */
    getInfoForClick: function(evt) {
        this.events.triggerEvent("beforegetfeatureinfo", {xy: evt.xy});
        // Set the cursor to "wait" to tell the user we're working on their
        // click.
        OpenLayers.Element.addClass(this.map.viewPortDiv, "olCursorWait");
        this.request(evt.xy, {});
    },

    /**
     * Method: getInfoForHover
     * Pause callback for the hover handler
     *
     * Parameters:
     * evt - {Object}
     */
    getInfoForHover: function(evt) {      
        this.events.triggerEvent("beforegetfeatureinfo", {xy: evt.xy});
        this.request(evt.xy, {hover: true});
    },

    /**
     * Method: cancelHover
     * Cancel callback for the hover handler
     */
    cancelHover: function() {
        if (this.hoverRequest) {
            this.hoverRequest.abort();
            this.hoverRequest = null;
        }
    },

    findRequestLayers: function()
    {
      var requestLayers = [];
      var hash = {};
      var i, len, layer, key, requestLayer;
      for (i = 0, len = this.layers.length; i < len; i++)
      {
        layer = this.layers[i];
        if (layer.visible && layer.locatable)
        {
          key = layer.url + " + " + layer.name;
          requestLayer = hash[key];
          if (requestLayer == undefined) // new layer
          {
            requestLayer = {
              url: layer.url,
              name: layer.name,
              style: layer.style,
              sldUrl: layer.sldUrl,
              filterArray: []
            };
            hash[key] = requestLayer;
            requestLayers.push(requestLayer);
          }
          if (layer.filter != null)
          {
            requestLayer.filterArray.push(layer.filter);
          }
          if (layer.style != requestLayer.style)
          {
            requestLayer.style = null;
          }
          if (layer.sldUrl != requestLayer.sldUrl)
          {
            requestLayer.sldUrl = null;
          }
        }
      }
      return requestLayers;
    },

    request: function(clickPosition, options)
    {
      var requestLayers = this.findRequestLayers();

      if (requestLayers.length == 0) {
        this.events.triggerEvent("nogetfeatureinfo");
        // Reset the cursor.
        OpenLayers.Element.removeClass(this.map.viewPortDiv, "olCursorWait");
        return;
      }

      var firstLayer = this.map.layers[0];
      var projection = this.map.getProjection();
      var layerProj = firstLayer.projection;
      if (layerProj && layerProj.equals(this.map.getProjectionObject()))
      {
        projection = layerProj.getCode();
      }

      this._requestCount = 0;
      this._numRequests = requestLayers.length;
      this._features = [];

      for (i = 0, len = requestLayers.length; i < len; i++)
      {
        var layer = requestLayers[i];
        var filter = (layer.filterArray.length == 0) ?
          null : joinFilter(layer.filterArray);
        var wmsOptions = {
          url: layer.url,
          params: {
            SERVICE: "WMS",
            VERSION: "1.3.0",
            REQUEST: "GetFeatureInfo",
            BBOX: this.map.getExtent().toBBOX(null,
              firstLayer.reverseAxisOrder()),
            FEATURE_COUNT: this.maxFeatures,
            HEIGHT: this.map.getSize().h,
            WIDTH: this.map.getSize().w,
            FORMAT: 'application/vnd.ogc.gml',
            INFO_FORMAT: 'application/vnd.ogc.gml',
            LAYERS: layer.name,
            STYLES: layer.style,
            SLD: layer.sldUrl,
            QUERY_LAYERS: layer.name,
            CQL_FILTER: filter,
            SRS: projection,
            X: parseInt(clickPosition.x),
            Y: parseInt(clickPosition.y)
          },
          callback: function(request)
          {
            this.handleResponse(clickPosition, request, layer.url);
          },
          scope: this
        };
        OpenLayers.Request.GET(wmsOptions);
      }
    },

    /**
     * Method: triggerGetFeatureInfo
     * Trigger the getfeatureinfo event when all is done
     *
     * Parameters:
     * request - {XMLHttpRequest} The request object
     * xy - {<OpenLayers.Pixel>} The position on the map where the
     *     mouse event occurred.
     * features - {Array(<OpenLayers.Feature.Vector>)} or
     *     {Array({Object}) when output is "object". The object has a url and a
     *     features property which contains an array of features.
     */
    triggerGetFeatureInfo: function(request, xy, features) {
        this.events.triggerEvent("getfeatureinfo", {
            text: request.responseText,
            features: features,
            request: request,
            xy: xy
        });

        // Reset the cursor.
        OpenLayers.Element.removeClass(this.map.viewPortDiv, "olCursorWait");
    },

    /**
     * Method: handleResponse
     * Handler for the GetFeatureInfo response.
     *
     * Parameters:
     * xy - {<OpenLayers.Pixel>} The position on the map where the
     *     mouse event occurred.
     * request - {XMLHttpRequest} The request object.
     * url - {String} The url which was used for this request.
     */
    handleResponse: function(xy, request, url) {
        
        var doc = request.responseXML;
        if(!doc || !doc.documentElement) {
            doc = request.responseText;
        }
        var features = this.format.read(doc);
        this._requestCount++;
        if (this.output === "object")
        {
            this._features = (this._features || []).concat(
                {url: url, features: features}
            );
        }
        else
        {
          this._features = (this._features || []).concat(features);
        }
        if (this._requestCount === this._numRequests)
        {
          this.triggerGetFeatureInfo(request, xy, this._features.concat());
          delete this._features;
          delete this._requestCount;
          delete this._numRequests;
        }
    },

    CLASS_NAME: "OpenLayers.Control.WMSGetFeatureInfo"
});



/* Copyright 2011-2013 Xavier Mamano, http://github.com/jorix/OL-DynamicMeasure
 * Published under MIT license. */

/**
 * @requires OpenLayers/Control/Measure.js
 * @requires OpenLayers/Rule.js
 * @requires OpenLayers/StyleMap.js
 */

/**
 * Class: OpenLayers.Control.DynamicMeasure
 * Allows for drawing of features for measurements.
 *
 * Inherits from:
 *  - <OpenLayers.Control.Measure>
 */
OpenLayers.Control.DynamicMeasure =
  OpenLayers.Class(OpenLayers.Control.Measure, {

    /**
     * APIProperty: accuracy
     * {Integer} Digits measurement accuracy, default is 5.
     */
    accuracy: 5,

    /**
     * APIProperty: persist
     * {Boolean} Keep the temporary measurement after the
     *     measurement is complete.  The measurement will persist until a new
     *     measurement is started, the control is deactivated, or <cancel> is
     *     called. Default is true.
     */
    persist: true,

    /**
     * APIProperty: styles
     * {Object} Alterations of the default styles of the points lines poligons
     *     and labels text, could use keys: "Point", "Line",
     *     "Polygon", "labelSegments", "labelHeading", "labelLength" and
     *     "labelArea". Default is <OpenLayers.Control.DynamicMeasure.styles>.
     */
    styles: null,

    /**
     * APIProperty: positions
     * {Object} Alterations of the default position of the labels, could use
     *     keys: "labelSegments" & "labelHeading", with values "start" "middle"
     *     and "end" refered of the current segment; and keys: "labelLength" &
     *     "labelArea" with additional values "center" (of the feature) and
     *     "initial" (initial point of the feature) and also mentioned previus
     *     values. Default is
     *     <OpenLayers.Control.DynamicMeasure.positions>.
     */
    positions: null,

    /**
     * APIProperty: maxSegments
     * {Integer|Null} Maximum number of visible segments measures, default is 1.
     *
     * To avoid soiling the track is desirable to reduce the number of visible
     *     segments.
     */
    maxSegments: 1,

    /**
     * APIProperty: maxHeadings
     * {Integer|Null} Maximum number of visible headings measures, default is 1.
     *
     * To avoid soiling the track is desirable to reduce the number of visible
     *     segments.
     */
    maxHeadings: 1,

    /**
     * APIProperty: layerSegmentsOptions
     * {Object} Any optional properties to be set on the
     *     layer of <layerSegments> of the lengths of the segments. If set to
     *     null the layer does not act.
     *
     *     If `styleMap` options is set then the key "labelSegments" of the
     *     `styles` option is ignored.
     */
    layerSegmentsOptions: undefined,

    /**
     * APIProperty: layerHeadingOptions
     * {Object} Any optional properties to be set on the
     *     layer of <layerHeading> of the angle of the segments. If set to
     *     null the layer does not act.  Default is null, set to {} to use a
     *     <layerHeading> to show headings.
     *
     *     If `styleMap` options is set then the key "labelHeading" of the
     *     `styles` option is ignored.
     */
    layerHeadingOptions: null,

    /**
     * APIProperty: layerLengthOptions
     * {Object} Any optional properties to be set on the
     *     layer of <layerLength> of the total length. If set to null the layer
     *     does not act.
     *
     *     If `styleMap` option is set then the key "labelLength" of the
     *     `styles` option is ignored.
     */
    layerLengthOptions: undefined,

    /**
     * APIProperty: layerAreaOptions
     * {Object} Any optional properties to be set on the
     *     layer of <layerArea> of the total area. If set to null the layer does
     *     not act.
     *
     *     If `styleMap` is set then the key "labelArea" of the `styles` option
     *     is ignored.
     */
    layerAreaOptions: undefined,

    /**
     * APIProperty: drawingLayer
     * {<OpenLayers.Layer.Vector>} Drawing layer to store the drawing when
     *     finished.
     */
    drawingLayer: null,

    /**
     * APIProperty: multi
     * {Boolean} Cast features to multi-part geometries before passing to the
     *     drawing layer, only used if declared a <drawingLayer>.
     * Default is false.
     */
    multi: false,

    /**
     * Property: layerSegments
     * {<OpenLayers.Layer.Vector>} The temporary drawing layer to show the
     *     length of the segments.
     */
    layerSegments: null,

    /**
     * Property: layerLength
     * {<OpenLayers.Layer.Vector>} The temporary drawing layer to show total
     *     length.
     */
    layerLength: null,

    /**
     * Property: layerArea
     * {<OpenLayers.Layer.Vector>} The temporary drawing layer to show total
     *     area.
     */
    layerArea: null,

    /**
     * Property: dynamicObj
     * {Object} Internal use.
     */
    dynamicObj: null,

    /**
     * Property: isArea
     * {Boolean} Internal use.
     */
    isArea: null,

    /**
     * Constructor: OpenLayers.Control.Measure
     *
     * Parameters:
     * handler - {<OpenLayers.Handler>}
     * options - {Object}
     *
     * Valid options:
     * accuracy - {Integer} Digits measurement accuracy, default is 5.
     * styles - {Object} Alterations of the default styles of the points lines
     *     poligons and labels text, could use keys: "Point",
     *     "Line", "Polygon", "labelSegments", "labelLength", "labelArea".
     * positions - {Object} Alterations of the default position of the labels.
     * handlerOptions - {Object} Used to set non-default properties on the
     *     control's handler. If `layerOptions["styleMap"]` is set then the
     *     keys: "Point", "Line" and "Polygon" of the `styles` option
     *     are ignored.
     * layerSegmentsOptions - {Object} Any optional properties to be set on the
     *     layer of <layerSegments> of the lengths of the segments. If
     *     `styleMap` is set then the key "labelSegments" of the `styles` option
     *     is ignored. If set to null the layer does not act.
     * layerLengthOptions - {Object} Any optional properties to be set on the
     *     layer of <layerLength> of the total length. If
     *     `styleMap` is set then the key "labelLength" of the `styles` option
     *     is ignored. If set to null the layer does not act.
     * layerAreaOptions - {Object} Any optional properties to be set on the
     *     layer of <layerArea> of the total area. If
     *     `styleMap` is set then the key "labelArea" of the `styles` option
     *     is ignored. If set to null the layer does not act.
     * layerHeadingOptions - {Object} Any optional properties to be set on the
     *     layer of <layerHeading> of the angle of the segments. If
     *     `styleMap` is set then the key "labelHeading" of the `styles` option
     *     is ignored. If set to null the layer does not act.
     * drawingLayer - {<OpenLayers.Layer.Vector>} Optional drawing layer to
     *     store the drawing when finished.
     * multi - {Boolean} Cast features to multi-part geometries before passing
     *     to the drawing layer
     */
    initialize: function(handler, options) {

        // Manage options
        options = options || {};

        // handlerOptions: persist & multi
        options.handlerOptions = OpenLayers.Util.extend(
            {persist: !options.drawingLayer}, options.handlerOptions
        );
        if (options.drawingLayer && !('multi' in options.handlerOptions)) {
            options.handlerOptions.multi = options.multi;
        }

        // * styles option
        if (options.drawingLayer) {
            var sketchStyle = options.drawingLayer.styleMap &&
                                 options.drawingLayer.styleMap.styles.temporary;
            if (sketchStyle) {
                options.handlerOptions
                                  .layerOptions = OpenLayers.Util.applyDefaults(
                    options.handlerOptions.layerOptions, {
                        styleMap: new OpenLayers.StyleMap({
                            'default': sketchStyle
                        })
                    }
                );
            }
        }
        var optionsStyles = options.styles || {};
        options.styles = optionsStyles;
        var defaultStyles = OpenLayers.Control.DynamicMeasure.styles;
        // * * styles for handler layer.
        if (!options.handlerOptions.layerOptions ||
            !options.handlerOptions.layerOptions.styleMap) {
            // use the style option for layerOptions of the handler.
            var style = new OpenLayers.Style(null, {rules: [
                new OpenLayers.Rule({symbolizer: {
                    'Point': OpenLayers.Util.applyDefaults(
                                optionsStyles.Point, defaultStyles.Point),
                    'Line': OpenLayers.Util.applyDefaults(
                                optionsStyles.Line, defaultStyles.Line),
                    'Polygon': OpenLayers.Util.applyDefaults(
                                optionsStyles.Polygon, defaultStyles.Polygon)
                }})
            ]});
            options.handlerOptions = options.handlerOptions || {};
            options.handlerOptions.layerOptions =
                                      options.handlerOptions.layerOptions || {};
            options.handlerOptions.layerOptions.styleMap =
                                    new OpenLayers.StyleMap({'default': style});
        }

        // * positions option
        options.positions = OpenLayers.Util.applyDefaults(
            options.positions,
            OpenLayers.Control.DynamicMeasure.positions
        );

        // force some handler options
        options.callbacks = options.callbacks || {};
        if (options.drawingLayer) {
            OpenLayers.Util.applyDefaults(options.callbacks, {
                create: function(vertex, feature) {
                    this.callbackCreate(vertex, feature);
                    this.drawingLayer.events.triggerEvent(
                        'sketchstarted', {vertex: vertex, feature: feature}
                    );
                },
                modify: function(vertex, feature) {
                    this.callbackModify(vertex, feature);
                    this.drawingLayer.events.triggerEvent(
                        'sketchmodified', {vertex: vertex, feature: feature}
                    );
                },
                done: function(geometry) {
                    this.callbackDone(geometry);
                    this.drawFeature(geometry);
                }
            });
        }
        OpenLayers.Util.applyDefaults(options.callbacks, {
            create: this.callbackCreate,
            point: this.callbackPoint,
            cancel: this.callbackCancel,
            done: this.callbackDone,
            modify: this.callbackModify,
            redo: this.callbackRedo,
            undo: this.callbackUndo
        });

        // do a trick with the handler to avoid blue background in freehand.
        var _self = this;
        var oldOnselectstart = document.onselectstart ?
                              document.onselectstart : OpenLayers.Function.True;
        var handlerTuned = OpenLayers.Class(handler, {
            down: function(evt) {
                document.onselectstart = OpenLayers.Function.False;
                return handler.prototype.down.apply(this, arguments);
            },
            up: function(evt) {
                document.onselectstart = oldOnselectstart;
                return handler.prototype.up.apply(this, arguments);
            },
            move: function(evt) {
                if (!this.mouseDown) {
                    document.onselectstart = oldOnselectstart;
                }
                return handler.prototype.move.apply(this, arguments);
            },
            mouseout: function(evt) {
                if (OpenLayers.Util.mouseLeft(evt, this.map.viewPortDiv)) {
                    if (this.mouseDown) {
                        document.onselectstart = oldOnselectstart;
                    }
                }
                return handler.prototype.mouseout.apply(this, arguments);
            },
            finalize: function() {
                document.onselectstart = oldOnselectstart;
                handler.prototype.finalize.apply(this, arguments);
            }
        }, {
            undo: function() {
                var undone = handler.prototype.undo.call(this);
                if (undone) {
                    this.callback('undo',
                                 [this.point.geometry, this.getSketch(), true]);
                }
                return undone;
            },
            redo: function() {
                var redone = handler.prototype.redo.call(this);
                if (redone) {
                    this.callback('redo',
                                 [this.point.geometry, this.getSketch(), true]);
                }
                return redone;
            }
        });
        // ... and call the constructor
        OpenLayers.Control.Measure.prototype.initialize.call(
                                                   this, handlerTuned, options);

        this.isArea = handler.prototype.polygon !== undefined; // duck typing
    },

    /**
     * APIMethod: destroy
     */
    destroy: function() {
        this.deactivate();
        OpenLayers.Control.Measure.prototype.destroy.apply(this, arguments);
    },

    /**
     * Method: draw
     * This control does not have HTML component, so this method should
     *     be empty.
     */
    draw: function() {},

    /**
     * APIMethod: activate
     */
    activate: function() {
        var response = OpenLayers.Control.Measure.prototype.activate.apply(
                                                               this, arguments);
        if (response) {
            // Create dynamicObj
            this.dynamicObj = {};
            // Create layers
            var _optionsStyles = this.styles || {},
                _defaultStyles = OpenLayers.Control.DynamicMeasure.styles,
                _self = this;
            var _create = function(styleName, initialOptions) {
                if (initialOptions === null) {
                    return null;
                }
                var options = OpenLayers.Util.extend({
                    displayInLayerSwitcher: false,
                    calculateInRange: OpenLayers.Function.True
                    // ?? ,wrapDateLine: this.citeCompliant
                }, initialOptions);
                if (!options.styleMap) {
                    var style = _optionsStyles[styleName];

                    options.styleMap = new OpenLayers.StyleMap({
                        'default': OpenLayers.Util.applyDefaults(style,
                                                      _defaultStyles[styleName])
                    });
                }
                var layer = new OpenLayers.Layer.Vector(
                                   _self.CLASS_NAME + ' ' + styleName, options);
                _self.map.addLayer(layer);
                return layer;
            };
            this.layerSegments =
                            _create('labelSegments', this.layerSegmentsOptions);
            this.layerHeading =
                            _create('labelHeading', this.layerHeadingOptions);
            this.layerLength = _create('labelLength', this.layerLengthOptions);
            if (this.isArea) {
                this.layerArea = _create('labelArea', this.layerAreaOptions);
            }
        }
        return response;
    },

    /**
     * APIMethod: deactivate
     */
    deactivate: function() {
        var response = OpenLayers.Control.Measure.prototype.deactivate.apply(
                                                               this, arguments);
        if (response) {
            this.layerSegments && this.layerSegments.destroy();
            this.layerLength && this.layerLength.destroy();
            this.layerHeading && this.layerHeading.destroy();
            this.layerArea && this.layerArea.destroy();
            this.dynamicObj = null;
            this.layerSegments = null;
            this.layerLength = null;
            this.layerHeading = null;
            this.layerArea = null;
        }
        return response;
    },

    /**
     * APIMethod: setImmediate
     * Sets the <immediate> property. Changes the activity of immediate
     * measurement.
     */
    setImmediate: function(immediate) {
        this.immediate = immediate;
    },

    /**
     * Method: callbackCreate
     */
    callbackCreate: function() {
        var dynamicObj = this.dynamicObj;
        dynamicObj.drawing = false;
        dynamicObj.freehand = false;
        dynamicObj.fromIndex = 0;
        dynamicObj.countSegments = 0;
    },

    /**
     * Method: callbackCancel
     */
    callbackCancel: function() {
        this.destroyLabels();
    },

    /**
     * Method: callbackDone
     * Called when the measurement sketch is done.
     *
     * Parameters:
     * geometry - {<OpenLayers.Geometry>}
     */
    callbackDone: function(geometry) {
        this.measureComplete(geometry);
        if (!this.persist) {
            this.destroyLabels();
        }
    },

    /**
     * Method: drawFeature
     */
    drawFeature: function(geometry) {
        var feature = new OpenLayers.Feature.Vector(geometry);
        var proceed = this.drawingLayer.events.triggerEvent(
            'sketchcomplete', {feature: feature}
        );
        if (proceed !== false) {
            feature.state = OpenLayers.State.INSERT;
            this.drawingLayer.addFeatures([feature]);
            this.featureAdded && this.featureAdded(feature);// for compatibility
            this.events.triggerEvent('featureadded', {feature: feature});
        }
    },

    /**
     * Method: callbackCancel
     */
    destroyLabels: function() {
        this.layerSegments && this.layerSegments.destroyFeatures(
                                                          null, {silent: true});
        this.layerLength && this.layerLength.destroyFeatures(
                                                          null, {silent: true});
        this.layerHeading && this.layerHeading.destroyFeatures(
                                                          null, {silent: true});
        this.layerArea && this.layerArea.destroyFeatures(null, {silent: true});
    },

    /**
     * Method: callbackPoint
     */
    callbackPoint: function(point, geometry) {
        var dynamicObj = this.dynamicObj;
        if (!dynamicObj.drawing) {
            this.destroyLabels();
        }
        if (!this.handler.freehandMode(this.handler.evt)) {
            dynamicObj.fromIndex = this.handler.getCurrentPointIndex() - 1;
            dynamicObj.freehand = false;
            dynamicObj.countSegments++;
        }else if (!dynamicObj.freehand) {
            // freehand has started
            dynamicObj.fromIndex = this.handler.getCurrentPointIndex() - 1;
            dynamicObj.freehand = true;
            dynamicObj.countSegments++;
        }

        this.measurePartial(point, geometry);
        dynamicObj.drawing = true;
    },

    /**
     * Method: callbackUndo
     */
    callbackUndo: function(point, feature) {
        var _self = this,
            undoLabel = function(layer) {
                if (layer) {
                    var features = layer.features,
                        lastSegmentIndex = features.length - 1,
                        lastSegment = features[lastSegmentIndex],
                        lastSegmentFromIndex = lastSegment.attributes.from,
                        lastPointIndex = _self.handler.getCurrentPointIndex();
                    if (lastSegmentFromIndex >= lastPointIndex) {
                        var dynamicObj = _self.dynamicObj;
                        layer.destroyFeatures(lastSegment);
                        lastSegment = features[lastSegmentIndex - 1];
                        dynamicObj.fromIndex = lastSegment.attributes.from;
                        dynamicObj.countSegments = features.length;
                    }
                }
            };
        undoLabel(this.layerSegments);
        undoLabel(this.layerHeading);
        this.callbackModify(point, feature, true);
    },

    /**
     * Method: callbackRedo
     */
    callbackRedo: function(point, feature) {
        var line = this.handler.line.geometry,
            currIndex = this.handler.getCurrentPointIndex();
        var dynamicObj = this.dynamicObj;
        this.showLabelSegment(
            dynamicObj.countSegments,
            dynamicObj.fromIndex,
            line.components.slice(dynamicObj.fromIndex, currIndex)
        );
        dynamicObj.fromIndex = this.handler.getCurrentPointIndex() - 1;
        dynamicObj.countSegments++;
        this.callbackModify(point, feature, true);
    },

    /**
     * Method: callbackModify
     */
    callbackModify: function(point, feature, drawing) {
        if (this.immediate) {
            this.measureImmediate(point, feature, drawing);
        }

        var dynamicObj = this.dynamicObj;
        if (dynamicObj.drawing === false) {
           return;
        }

        var line = this.handler.line.geometry,
            currIndex = this.handler.getCurrentPointIndex();
        if (!this.handler.freehandMode(this.handler.evt) &&
                                                          dynamicObj.freehand) {
            // freehand has stopped
            dynamicObj.fromIndex = currIndex - 1;
            dynamicObj.freehand = false;
            dynamicObj.countSegments++;
        }

        // total measure
        var totalLength = this.getBestLength(line);
        if (!totalLength[0]) {
           return;
        }
        var positions = this.positions,
            positionGet = {
            center: function() {
                var center = feature.geometry.getBounds().clone();
                center.extend(point);
                center = center.getCenterLonLat();
                return [center.lon, center.lat];
            },
            initial: function() {
                var initial = line.components[0];
                return [initial.x, initial.y];
            },
            start: function() {
                var start = line.components[dynamicObj.fromIndex];
                return [start.x, start.y];
            },
            middle: function() {
                var start = line.components[dynamicObj.fromIndex];
                return [(start.x + point.x) / 2, (start.y + point.y) / 2];
            },
            end: function() {
                return [point.x, point.y];
            }
        };
        if (this.layerLength) {
            this.showLabel(
                        this.layerLength, 1, 0, totalLength,
                        positionGet[positions['labelLength']](), 1);
        }
        if (this.isArea) {
            if (this.layerArea) {
                this.showLabel(this.layerArea, 1, 0,
                     this.getBestArea(feature.geometry),
                     positionGet[positions['labelArea']](), 1);
            }
            if (this.showLabelSegment(
                      1, 0, [line.components[0], line.components[currIndex]])) {
                dynamicObj.countSegments++;
            }
        }
        this.showLabelSegment(
            dynamicObj.countSegments,
            dynamicObj.fromIndex,
            line.components.slice(dynamicObj.fromIndex, currIndex + 1)
        );
    },

    /**
     * Function: showLabelSegment
     *
     * Parameters:
     * labelsNumber- {Integer} Number of the labels to be on the label layer.
     * fromIndex - {Integer} Index of the last point on the measured feature.
     * points - Array({<OpenLayers.Geometry.Point>})
     *
     * Returns:
     * {Boolean}
     */
    showLabelSegment: function(labelsNumber, fromIndex, _points) {
        var layerSegments = this.layerSegments,
            layerHeading = this.layerHeading;
        if (!layerSegments && !layerHeading) {
            return false;
        }
        // clone points
        var points = [],
            pointsLen = _points.length;
        for (var i = 0; i < pointsLen; i++) {
            points.push(_points[i].clone());
        }
        var from = points[0],
            to = points[pointsLen - 1],
            segmentLength =
                 this.getBestLength(new OpenLayers.Geometry.LineString(points)),
            positions = this.positions,
            positionGet = {
                start: function() {
                    return [from.x, from.y];
                },
                middle: function() {
                    return [(from.x + to.x) / 2, (from.y + to.y) / 2];
                },
                end: function() {
                    return [to.x, to.y];
                }
            },
            created = false;
        if (layerSegments) {
            created = this.showLabel(layerSegments, labelsNumber, fromIndex,
                            segmentLength,
                            positionGet[positions['labelSegments']](),
                            this.maxSegments);
        }
        if (layerHeading && segmentLength[0] > 0) {
            var heading = Math.atan2(to.y - from.y, to.x - from.x),
                bearing = 90 - heading * 180 / Math.PI;
            if (bearing < 0) {
                bearing += 360;
            }
            created = created || this.showLabel(layerHeading,
                            labelsNumber, fromIndex,
                            [bearing, '°'],
                            positionGet[positions['labelHeading']](),
                            this.maxHeadings);
        }
        return created;
    },

    /**
     * Function: showLabel
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.Vector>} Layer of the labels.
     * labelsNumber- {Integer} Number of the labels to be on the label layer.
     * fromIndex - {Integer} Index of the last point on the measured feature.
     * measure - Array({Float|String}) Measure provided by OL Measure control.
     * points - Array({Fload}) Array of x and y of the point to draw the label.
     * maxSegments - {Integer|Null} Maximum number of visible segments measures
     *
     * Returns:
     * {Boolean}
     */
    showLabel: function(
                     layer, labelsNumber, fromIndex, measure, xy, maxSegments) {
        var featureLabel, featureAux,
            features = layer.features;
        if (features.length < labelsNumber) {
        // add a label
            if (measure[0] === 0) {
                return false;
            }
            featureLabel = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Point(xy[0], xy[1]),
                {from: fromIndex}
            );
            this.setMesureAttributes(featureLabel.attributes, measure);
            layer.addFeatures([featureLabel]);
            if (maxSegments !== null) {
                var hide = (features.length - maxSegments) - 1;
                if (hide >= 0) {
                    featureAux = features[hide];
                    featureAux.style = {display: 'none'};
                    layer.drawFeature(featureAux);
                }
            }
            return true;
        } else {
        // update a label
            featureLabel = features[labelsNumber - 1];
            var geometry = featureLabel.geometry;
            geometry.x = xy[0];
            geometry.y = xy[1];
            geometry.clearBounds();
            this.setMesureAttributes(featureLabel.attributes, measure);
            layer.drawFeature(featureLabel);
            if (maxSegments !== null) {
                var show = (features.length - maxSegments);
                if (show >= 0) {
                    featureAux = features[show];
                    if (featureAux.style) {
                        delete featureAux.style;
                        layer.drawFeature(featureAux);
                    }
                }
            }
            return false;
        }
    },

    /**
     * Method: setMesureAttributes
     * Format measure[0] with digits of <accuracy>. Could internationalize the
     *     format customizing <OpenLayers.Number.thousandsSeparator> and
     *     <OpenLayers.Number.decimalSeparator>
     *
     * Parameters:
     * attributes - {object} Target attributes.
     * measure - Array({*})
     */
    setMesureAttributes: function(attributes, measure) {
        attributes.measure = OpenLayers.Number.format(
                           Number(measure[0].toPrecision(this.accuracy)), null);
        attributes.units = measure[1];
    },

    CLASS_NAME: 'OpenLayers.Control.DynamicMeasure'
});

/**
 * Constant: OpenLayers.Control.DynamicMeasure.styles
 * Contains the keys: "Point", "Line", "Polygon",
 *     "labelSegments", "labelHeading", "labelLength" and
 *     "labelArea" as a objects with style keys.
 */
OpenLayers.Control.DynamicMeasure.styles = {
    'Point': {
        pointRadius: 4,
        graphicName: 'square',
        fillColor: 'white',
        fillOpacity: 1,
        strokeWidth: 1,
        strokeOpacity: 1,
        strokeColor: '#333333'
    },
    'Line': {
        strokeWidth: 2,
        strokeOpacity: 1,
        strokeColor: '#666666',
        strokeDashstyle: 'dash'
    },
    'Polygon': {
        strokeWidth: 2,
        strokeOpacity: 1,
        strokeColor: '#666666',
        strokeDashstyle: 'solid',
        fillColor: 'white',
        fillOpacity: 0.3
    },
    labelSegments: {
        label: '${measure} ${units}',
        fontSize: '11px',
        fontColor: '#800517',
        fontFamily: 'Verdana',
        labelOutlineColor: '#dddddd',
        labelAlign: 'cm',
        labelOutlineWidth: 2
    },
    labelLength: {
        label: '${measure} ${units}\n',
        fontSize: '11px',
        fontWeight: 'bold',
        fontColor: '#800517',
        fontFamily: 'Verdana',
        labelOutlineColor: '#dddddd',
        labelAlign: 'lb',
        labelOutlineWidth: 3
    },
    labelArea: {
        label: '${measure}\n${units}2\n',
        fontSize: '11px',
        fontWeight: 'bold',
        fontColor: '#800517',
        fontFamily: 'Verdana',
        labelOutlineColor: '#dddddd',
        labelAlign: 'cm',
        labelOutlineWidth: 3
    },
    labelHeading: {
        label: '${measure} ${units}',
        fontSize: '11px',
        fontColor: '#800517',
        fontFamily: 'Verdana',
        labelOutlineColor: '#dddddd',
        labelAlign: 'cm',
        labelOutlineWidth: 3
    }
};

/**
 * Constant: OpenLayers.Control.DynamicMeasure.positions
 * Contains the keys: "labelSegments", "labelHeading",
 *     "labelLength" and "labelArea" as a strings with values 'start',
 *     'middle' and 'end' allowed for all keys (refered of last segment) and
 *     'center' and 'initial' (refered of the measured feature and only allowed
 *     for "labelLength" and "labelArea" keys)
 */
OpenLayers.Control.DynamicMeasure.positions = {
    labelSegments: 'middle',
    labelLength: 'end',
    labelArea: 'center',
    labelHeading: 'start'
};


/**
 * @requires OpenLayers/Control.js
 *
 * Class: OpenLayers.Control.LoadingPanel
 * In some applications, it makes sense to alert the user that something is
 * happening while tiles are loading. This control displays a div across the
 * map when this is going on.
 *
 * Inherits from:
 *  - <OpenLayers.Control>
 */
OpenLayers.Control.LoadingPanel = OpenLayers.Class(OpenLayers.Control, {

  /**
     * Property: counter
     * {Integer} A counter for the number of layers loading
     */
  counter: 0,

  /**
     * Property: maximized
     * {Boolean} A boolean indicating whether or not the control is maximized
    */
  maximized: false,

  /**
     * Property: visible
     * {Boolean} A boolean indicating whether or not the control is visible
    */
  visible: true,

  /**
     * Constructor: OpenLayers.Control.LoadingPanel
     * Display a panel across the map that says 'loading'.
     *
     * Parameters:
     * options - {Object} additional options.
     */
  initialize: function(options) {
    OpenLayers.Control.prototype.initialize.apply(this, [options]);
  },

  /**
     * Function: setVisible
     * Set the visibility of this control
     *
     * Parameters:
     * visible - {Boolean} should the control be visible or not?
    */
  setVisible: function(visible) {
    this.visible = visible;
    if (visible) {
      OpenLayers.Element.show(this.div);
    } else {
      OpenLayers.Element.hide(this.div);
    }
  },

  /**
     * Function: getVisible
     * Get the visibility of this control
     *
     * Returns:
     * {Boolean} the current visibility of this control
    */
  getVisible: function() {
    return this.visible;
  },

  /**
     * APIMethod: hide
     * Hide the loading panel control
    */
  hide: function() {
    this.setVisible(false);
  },

  /**
     * APIMethod: show
     * Show the loading panel control
    */
  show: function() {
    this.setVisible(true);
  },

  /**
     * APIMethod: toggle
     * Toggle the visibility of the loading panel control
    */
  toggle: function() {
    this.setVisible(!this.getVisible());
  },

  /**
     * Method: addLayer
     * Attach event handlers when new layer gets added to the map
     *
     * Parameters:
     * evt - {Event}
    */
  addLayer: function(evt) {
    if (evt.layer) {
      evt.layer.events.register('loadstart', this, this.increaseCounter);
      evt.layer.events.register('loadend', this, this.decreaseCounter);
    }
  },

  /**
     * Method: setMap
     * Set the map property for the control and all handlers.
     *
     * Parameters:
     * map - {<OpenLayers.Map>} The control's map.
     */
  setMap: function(map) {
    OpenLayers.Control.prototype.setMap.apply(this, arguments);
    this.map.events.register('preaddlayer', this, this.addLayer);
    for (var i = 0; i < this.map.layers.length; i++) {
      var layer = this.map.layers[i];
      layer.events.register('loadstart', this, this.increaseCounter);
      layer.events.register('loadend', this, this.decreaseCounter);
    }
  },

  /**
     * Method: increaseCounter
     * Increase the counter and show control
    */
  increaseCounter: function() {
    this.counter++;
    if (this.counter > 0) {
      if (!this.maximized && this.visible) {
        this.maximizeControl();
      }
    }
  },

  /**
     * Method: decreaseCounter
     * Decrease the counter and hide the control if finished
    */
  decreaseCounter: function() {
    if (this.counter > 0) {
      this.counter--;
    }
    if (this.counter == 0) {
      if (this.maximized && this.visible) {
        this.minimizeControl();
      }
    }
  },

  /**
     * Method: draw
     * Create and return the element to be splashed over the map.
     */
  draw: function () {
    OpenLayers.Control.prototype.draw.apply(this, arguments);
    return this.div;
  },

  /**
     * Method: minimizeControl
     * Set the display properties of the control to make it disappear.
     *
     * Parameters:
     * evt - {Event}
     */
  minimizeControl: function(evt) {
    this.div.style.display = "none";
    this.maximized = false;

    if (evt != null) {
      OpenLayers.Event.stop(evt);
    }
  },

  /**
     * Method: maximizeControl
     * Make the control visible.
     *
     * Parameters:
     * evt - {Event}
     */
  maximizeControl: function(evt) {
    this.div.style.display = "block";
    this.maximized = true;

    if (evt != null) {
      OpenLayers.Event.stop(evt);
    }
  },

  /**
     * Method: destroy
     * Destroy control.
     */
  destroy: function() {
    if (this.map) {
      this.map.events.unregister('preaddlayer', this, this.addLayer);
      if (this.map.layers) {
        for (var i = 0; i < this.map.layers.length; i++) {
          var layer = this.map.layers[i];
          layer.events.unregister('loadstart', this,
            this.increaseCounter);
          layer.events.unregister('loadend', this,
            this.decreaseCounter);
        }
      }
    }
    OpenLayers.Control.prototype.destroy.apply(this, arguments);
  },
  CLASS_NAME: "OpenLayers.Control.LoadingPanel"
});

OpenLayers.Strategy.EBBOX = OpenLayers.Class(OpenLayers.Strategy.BBOX, {
  merge: function(resp)
  {
    var changedFeatures = {};
    var newFeatures = [];
    var feature;
    for (var j = 0; j < this.layer.features.length; j++)
    {
      feature = this.layer.features[j];
      if (feature.state)
      {
        if (feature.state == 'Insert') newFeatures.push(feature);
        else changedFeatures[feature.fid] = feature;
      }
    }
    this.layer.removeAllFeatures();
    if (resp.success())
    {
      var features = resp.features;
      if (features)
      {
        var remote = this.layer.projection;
        var local = this.layer.map.getProjectionObject();
        var changeProjection = (!local.equals(remote));
        var geom;
        for (var i = 0, len = features.length; i < len; ++i)
        {
          feature = features[i];
          var previousFeature = changedFeatures[feature.fid];
          if (previousFeature)
          {
            features[i] = previousFeature;
            delete changedFeatures[feature.fid];
          }
          else
          {
            geom = feature.geometry;
            if (geom && changeProjection)
            {
              geom.transform(remote, local);
            }
          }
        }
        for (var fid in changedFeatures)
        {
          features.push(changedFeatures[fid]);
        }
        features = features.concat(newFeatures);
        this.layer.addFeatures(features);
      }
    }
    else
    {
      this.bounds = null;
    }
    this.response = null;
    this.layer.events.triggerEvent("loadend", {
      response: resp
    });
  },

  CLASS_NAME: "OpenLayers.Strategy.EBBOX"
});

/* FeatureLocator */
OpenLayers.FeatureLocator = {
  map: null,
  markersLayer: null,
  searchLayer: null,
  elemId: null,
  language: null,
  baseUrl: null,
  loadingUrl: null,
  maxFeatures: 2000,
  features: null,
  locatorSelector: 'list', // or select
  locators: new Array(),
  activeLocatorIndex: 0,
  minExtent: 200,
  searchSelectorLabel: "Select search:",
  searchLabel: "Search",
  clearSearchLabel: "Clear previous search",
  resultsMessage: "results",
  noResultsMessage: "No results found.",

  addLocator: function(locator)
  {
    this.locators.push(locator);
  },

  getActiveLocator: function()
  {
    if (this.activeLocatorIndex >= this.locators.length) return null;
    return this.locators[this.activeLocatorIndex];
  },

  init: function()
  {
    for (var i = 0; i < this.locators.length; i++)
    {
      var locator = this.locators[i];
      if (locator.init)
      {
        locator.init(this.map);
      }
    }
    var elem = document.getElementById(this.elemId);
    var html = "<div id=\"formSelectors\">";
    html += "<div>" + this.searchSelectorLabel +"</div>";
    html += this.renderLocatorSelector();
    html += "</div>"
    html += "<div id=\"activeForm\"></div>";
    html += "<div id=\"searchResults\"></div>";
    elem.innerHTML = html;
  },

  activateLocatorByName: function(locatorName)
  {
    var found = false;
    var i = 0;
    while (!found && i < this.locators.length)
    {
      if (this.locators[i].name == locatorName) found = true;
      else i++;
    }
    if (found) this.activateLocator(i);
    return found;
  },

  activateLocator: function(activeLocatorIndex)
  {
    this.activeLocatorIndex = activeLocatorIndex;
    var activeLocator = this.locators[activeLocatorIndex];
    var elem;
    elem = document.getElementById("activeForm");
    elem.innerHTML = activeLocator.createFormFields() +
      '<input id="_dummy_" type="text" style="display:none" aria-hidden="true" aria-label="dummy input control"><br>' +
      '<div role="group" aria-labelledby="clearSearchLabel">' +
      '<input type="checkbox" id="clearSearch" checked>' +
      '<label id="clearSearchLabel" for="clearSearch">' +
      this.clearSearchLabel + '</label></div><br>' +
      '<input id="searchButton" type="submit" class="button" value="' +
        this.searchLabel + '" onclick="OpenLayers.FeatureLocator.search();return false">';

    elem = document.getElementById("searchResults");
    elem.innerHTML = "";
    var parameters = activeLocator.parameters;
    if (parameters)
    {
      var scope = this;
      var keyHandler = function(event)
      {
        if (!event) event = window.event;
        if (event.keyCode == 13) // search on ENTER
        {
          document.getElementById("searchButton").focus();
          scope.search();
        }
        else if (event.keyCode == 27)
        {
          document.getElementById("searchButton").focus();
        }
      };
      for (var i = 0; i < parameters.length; i++)
      {
        var parameter = parameters[i];
        elem = document.getElementById(parameter);
        if (elem)
        {
          if (elem.tagName.toUpperCase() != 'TEXTAREA')
          {
            elem.onkeyup = keyHandler;
          }
        }
      }
    }
    this.updateLocatorSelector();
    this.formLoaded(activeLocator);
  },
  
  search: function() // asynchronous
  {
    var searchButton = document.getElementById("searchButton");
    searchButton.disabled = true;
    var clearElem = document.getElementById("clearSearch");
    if (clearElem.checked)
    {
      this.clear();
    }
    var resultsElem = document.getElementById("searchResults");
    resultsElem.innerHTML = '<img src="' + this.loadingUrl + '">';
    var activeLocator = this.getActiveLocator();
    this.searchStarted(activeLocator);
    activeLocator.search(this.baseUrl, this.maxFeatures);
  },

  setFeatures: function(features, options)
  {
    options = options || {};
    var activeLocator = this.getActiveLocator();    
    if (options.sort)
    {
      features.sort(function(f1, f2){
        var v1 = activeLocator.getFeatureLabel(f1).latinize();
        var v2 = activeLocator.getFeatureLabel(f2).latinize();
        if (v1 == v2) return 0;
        return v1 < v2 ? -1 : 1;
      });
    }
    this.features = features;
    var resultsElem = document.getElementById("searchResults");
    if (this.features.length > 0)
    {
      var allBounds = new OpenLayers.Bounds();
      var results = "";
      if (options.message)
      {
        results += '<p>' + options.message + '</p>';
      }
      else if (features.length > 3)
      {
        results += '<p class="counter">' +
          features.length  + " " + this.resultsMessage + ':</p>';
      }
      results += "<ul>";
      for (var i = 0; i < this.features.length; i++)
      {
        var feature = this.features[i];
        if (feature.geometry)
        {
          var bounds = feature.geometry.getBounds();
          allBounds.extend(bounds);

          var label = activeLocator.getFeatureLabel ?
            activeLocator.getFeatureLabel(feature) : feature.fid;

          var markerIcon;
          if (activeLocator.getMarkerIcon)
          {
            markerIcon = activeLocator.getMarkerIcon(feature);
          }
          else
          {
            var markerUrl = this.baseUrl + "/plugins/mapviewer/img/marker.png";
            var size = new OpenLayers.Size(21, 25);
            var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
            markerIcon = new OpenLayers.Icon(markerUrl, size, offset);          
          }

          if (markerIcon !== null) // paint Marker
          {
            markerIcon.imageDiv.title = label;
            var marker = new OpenLayers.Marker(bounds.getCenterLonLat(), 
              markerIcon);
            marker.feature = feature;
            marker.events.register("mousedown", marker, function(evt) {
              OpenLayers.Event.stop(evt);
            });
            marker.events.register("mouseup", marker, function(evt) {
              OpenLayers.Event.stop(evt);
            });
            marker.events.register("touchend", marker, function(evt) {
              OpenLayers.FeatureLocator.markerSelected(
                OpenLayers.FeatureLocator.getActiveLocator(), this.feature);
              OpenLayers.Event.stop(evt);
            });
            marker.events.register("click", marker, function(evt) {
              OpenLayers.FeatureLocator.markerSelected(
                OpenLayers.FeatureLocator.getActiveLocator(), this.feature);
              OpenLayers.Event.stop(evt);
            });
            var markersLayer = activeLocator.markersLayer || this.markersLayer;
            markersLayer.addMarker(marker);
          }
          else // paint vector feature
          {
            var searchLayer = activeLocator.searchLayer || this.searchLayer;
            searchLayer.addFeatures([feature.clone()]);
          }
          var listIconUrl;
          if (activeLocator.getListIconUrl)
          {
            listIconUrl = activeLocator.getListIconUrl(feature);
          }
          else
          {
            listIconUrl = this.baseUrl + "/plugins/mapviewer/img/mini-marker.png";
          }
          results +=
            '<li><a href="javascript:OpenLayers.FeatureLocator.centerOn(' + i + ')" ' +
            ' style="background-image:url(' + listIconUrl + ')">' + label + '</a></li>';
        }
      }
      results += "</ul>";
      resultsElem.innerHTML = results;
      if (options.center || options.center === undefined)
      {
        this.map.zoomToExtent(this.getAdjustedBounds(allBounds), false);
        if (this.features.length == 1)
        {
          OpenLayers.FeatureLocator.markerSelected(
            activeLocator, this.features[0]);
        }
      }
    }
    else
    {
      if (options.message)
      {
        resultsElem.innerHTML = options.message;
      }
      else
      {
        resultsElem.innerHTML = this.noResultsMessage;
      }
    }
    var searchButton = document.getElementById("searchButton");
    searchButton.disabled = false;
    this.searchCompleted(activeLocator, this.features);

    // refresh result at refreshTime
    if (activeLocator.refreshTime)
    {
      if (activeLocator.timer)
      {
        clearTimeout(activeLocator.timer);
      }

      activeLocator.timer = setTimeout(function()
      {
        if (OpenLayers.FeatureLocator.getActiveLocator() === activeLocator)
        {
          OpenLayers.FeatureLocator.search();            
        }
      }, activeLocator.refreshTime);
    }
  },

  centerOn: function(index)
  {
    if (this.map)
    {
      var activeLocator = this.getActiveLocator();
      var feature = this.features[index];      
      var bounds = feature.geometry.getBounds();
      this.map.zoomToExtent(this.getAdjustedBounds(bounds), false);
      OpenLayers.FeatureLocator.markerSelected(activeLocator, feature);
    }
  },

  clear: function()
  {
    this.markersLayer.clearMarkers();
    this.searchLayer.removeAllFeatures();

    var activeLocator = this.getActiveLocator();
    if (activeLocator.markersLayer)
      activeLocator.markersLayer.clearMarkers();
    if (activeLocator.searchLayer)
      activeLocator.searchLayer.removeAllFeatures();

    var elem = document.getElementById("searchResults");
    if (elem) elem.innerHTML = "";
    this.features = null;
    this.resultsCleared(activeLocator);
  },

  renderLocatorSelector: function()
  {
    var html;
    var i, locator, title;
    if (this.locatorSelector == 'select')
    {
      html = '<select id="formSelector" style="margin-top:4px" class="selectBox" ' +
        'onchange="OpenLayers.FeatureLocator.activateLocator(this.selectedIndex);">';
      for (i = 0; i < this.locators.length; i++)
      {
        locator = this.locators[i];
        title = locator.getTitle ? locator.getTitle() : locator.name;
        html += "<option value=\"" + i + "\">" + title + "</option>";
      }
      html += "</select>";
    }
    else
    {
      html = '<ul id="formSelector">';
      for (i = 0; i < this.locators.length; i++)
      {
        locator = this.locators[i];
        title = locator.getTitle ? locator.getTitle() : locator.name;
        html += '<li>';
        html += '<a href="javascript:OpenLayers.FeatureLocator.activateLocator('
          + i + ');">';
        html += title;
        html += '</a></li>';
      }
      html += "</ul>";
    }
    return html;
  },

  updateLocatorSelector: function()
  {
    var index = this.activeLocatorIndex;
    var elem;
    if (this.locatorSelector == 'select') // select
    {
      elem = document.getElementById("formSelector");
      elem.selectedIndex = index;
    }
    else // list
    {
      elem = document.getElementById("formSelector");
      var children = elem.children;
      for (var t = 0; t < children.length; t++)
      {
        if (t == index) children[t].className = "selected";
        else children[t].className = "";
      }
    }
  },

  formLoaded: function(activeLocator)
  {
    // override
  },

  searchStarted: function(activeLocator)
  {
    // override
  },

  searchCompleted: function(activeLocator, features)
  {
    // override
  },

  markerSelected: function(activeLocator, feature)
  {
    // override
  },

  resultsCleared: function(activeLocator)
  {
    // override
  },
  
  getAdjustedBounds: function(bounds)
  {
    var bw = bounds.getWidth();
    var bh = bounds.getHeight();
    if (bw < this.minExtent)
    {
      bw = this.minExtent;
    }  
    if (bh < this.minExtent)
    {
      bh = this.minExtent;
    }
    var center = bounds.getCenterLonLat();
    var x = center.lon;
    var y = center.lat;
    
    var offsetX = 0.8 * bw;
    var offsetY = 0.8 * bh; 
    bounds = new OpenLayers.Bounds(x - offsetX, y - offsetY, 
      x + offsetX, y + offsetY);
    
    return bounds;
  }
};

/* FeatureTypeInfo */
OpenLayers.FeatureTypeInfo =
{
  cache: {},
  getInfo: function(serviceUrl, typeName)
  {
    var key = serviceUrl + " {" + typeName + "}";
    var featureInfo = this.cache[key];
    if (!featureInfo)
    {
      try
      {
        var layerUrl = serviceUrl +
          "&service=wfs&version=1.1.0&request=DescribeFeatureType&typeName=" +
          typeName;
        var request = OpenLayers.Request.GET({url: layerUrl, async: false});
        var parser = new OpenLayers.Format.WFSDescribeFeatureType();
        var response = parser.read(request.responseXML);
        featureInfo = response.featureTypes[0];
        featureInfo.geometryName = null;
        var properties = featureInfo.properties;
        var p = 0;
        while (p < properties.length && featureInfo.geometryName === null)
        {
          var property = properties[p];
          if (property.localType === 'GeometryPropertyType')
          {
            featureInfo.geometryName = property.name;
          }
          p++;
        }
        this.cache[key] = featureInfo;
      }
      catch (ex)
      {
        if (console)
        {
          console.warn("Can't get feature info from " + typeName + ": " + ex);
        }
      }
    }
    return featureInfo;
  }
};

OpenLayers.CQLAssistant =
{
  panelElement: null,

  operatorList: [
    ["+", "Add numbers: A + B."],
    ["-", "Subtract numbers: A - B."],
    ["*", "Multiply numbers: A * B."],
    ["/", "Divide numbers: A / B."],
    ["=", "Equals comparision: A = B."],
    ["<>", "Not equals comparision: A <> B."],
    [">", "Greater than: A > B."],
    ["<", "Less than: A < B."],
    [">=", "Greater than or equals to: A >= B."],
    ["<=", "Less than or equals to: A <= B."],
    ["AND", "Logic intersection: A AND B."],
    ["OR", "Logic conjuntion: A OR B."],
    ["NOT", "Logic negation: NOT A."],
    ["BETWEEN", "Range comparison: A BETWEEN C AND D."],
    ["LIKE", "String compare with pattern: A LIKE P. Accepted widcards: '%' and '_'."],
    ["IN", "Inclusion operator: A IN (E1, E2, ...)."],
    ["IS NULL", "NULL comparision: A IS NULL."],
    ["IS NOT NULL", "Not NULL comparision: A IS NOT NULL."]],

  functionList: [
    ["String functions",
      ["Concatenate(s1, s2, ...)", "Concatenates any number of strings. Non-string arguments are allowed."],
      ["strCapitalize(string)", "Fully capitalizes the string s."],
      ["strConcat(s1, s2)", "Concatenates the two strings into one."],
      ["strEndsWith(string, suffix)", "Returns true if string ends with suffix."],
      ["strEqualsIgnoreCase(s1, s2)", "Returns true if the two strings are equal ignoring case considerations."],
      ["strIndexOf(string, substring)", "Returns the index within this string of the first occurrence of the specified substring, or -1 if not found."],
      ["strLastIndexOf(string, substring)", "Returns the index within this string of the last occurrence of the specified substring, or -1 if not found."],
      ["strLength(string)", "Returns the string length."],
      ["strMatches(string, pattern)", "Returns true if the string matches the specified regular expression."],
      ["strReplace(string, pattern, replacement, global)", "Returns the string with the pattern replaced with the given replacement text. If the global argument is true then all occurrences of the pattern will be replaced, otherwise only the first."],
      ["strStartsWith(string, prefix)", "Returns true if string starts with prefix."],
      ["strSubstring(string, begin, end)", "Returns a new string that is a substring of this string. The substring begins at the specified begin and extends to the character at index endIndex - 1 (indexes are zero-based)."],
      ["strSubstringStart(string, begin)", "Returns a new string that is a substring of this string. The substring begins at the specified begin and extends to the last character of the string."],
      ["strToLowerCase(string)", "Returns the lower case version of the string"],
      ["strToUpperCase(string)", "Returns the upper case version of the string"],
      ["strTrim(string)", "Returns a copy of the string, with leading and trailing white space omitted."]],
    ["Math functions",
      ["abs(x)", "The absolute value of the specified Integer value."],
      ["acos(x)", "Returns the arc cosine of an angle in radians, in the range of 0.0 through PI."],
      ["asin(x)", "Returns the arc sine of an angle in radians, in the range of -PI / 2 through PI / 2."],
      ["atan(x)", "Returns the arc tangent of an angle in radians, in the range of -PI/2 through PI/2."],
      ["atan2(x, y)", "Converts a rectangular coordinate (x, y) to polar (r, theta) and returns theta."],
      ["ceil(x)", "Returns the smallest (closest to negative infinity) double value that is greater than or equal to x and is equal to a mathematical integer."],
      ["cos(angle)", "Returns the cosine of an angle expressed in radians"],
      ["double2bool(x)", "Returns true if x is zero, false otherwise"],
      ["exp(x)", "Returns Euler's number e raised to the power of x."],
      ["floor(x)", "Returns the largest (closest to positive infinity) value that is less than or equal to x and is equal to a mathematical integer."],
      ["IEEERemainder(x, y)", "Computes the remainder of x divided by y as prescribed by the IEEE 754 standard."],
      ["int2bbool(x)", "Returns true if x is zero, false otherwise."],
      ["int2ddouble(x)", "Converts x to a Double."],
      ["log(x)", "Returns the natural logarithm (base e) of x."],
      ["max(x1, x2)", "Returns the maximum between x1, x2."],
      ["min(x1, x2)", "Returns the minimum between x1, x2."],
      ["pi()", "Returns an approximation of pi, the ratio of the circumference of a circle to its diameter."],
      ["pow(base, exponent)", "Returns the value of base raised to the power of exponent."],
      ["random()", "Returns a Double value with a positive sign, greater than or equal to 0.0 and less than 1.0. Returned values are chosen pseudo-randomly with (approximately) uniform distribution from that range."],
      ["rint(x)", "Returns the Double value that is closest in value to the argument and is equal to a mathematical integer. If two double values that are mathematical integers are equally close, the result is the integer value that is even."],
      ["round(x)", "Returns the closest Integer to x. The result is rounded to an integer by adding 1/2, taking the floor of the result, and casting the result to type Integer. In other words, the result is equal to the value of the expression (int)floor(a + 0.5)."],
      ["roundDouble(x)", "Returns the closest Long to x."],
      ["sin(angle)", "Returns the trigonometric sinus of angle."],
      ["tan(angle)", "Returns the trigonometric tangent of angle."],
      ["toDegrees(angle)", "Converts an angle expressed in radians into degrees."],
      ["toRadians(angle)", "Converts an angle expressed in radians into degrees."]],
    ["Geometric functions",
      ["area(geometry)", "The area of the specified geometry. Works in a Cartesian plane, the result will be in the same unit of measure as the geometry coordinates (which also means the results won't make any sense for geographic data)."],
      ["boundary(geometry)", "Returns the boundary of a geometry."],
      ["boundaryDimension(geometry)", "Returns the number of dimensions of the geometry boundary."],
      ["buffer(geometry, distance)", "Returns the buffered area around the geometry using the specified distance."],
      ["bufferWithSegments(geometry, distance, segments)", "Returns the buffered area around the geometry using the specified distance and using the specified number of segments to represent a quadrant of a circle."],
      ["centroid(geometry)", "Returns the centroid of the geometry. Can be often used as a label point for polygons, though there is no guarantee it will actually lie inside the geometry."],
      ["convexHull(geometry)", "Returns the convex hull of the specified geometry."],
      ["difference(geometry_a, geometry_b)", "Returns all the points that sit in a but not in b."],
      ["dimension(geometry)", "Returns the dimension of the specified geometry."],
      ["distance(geometry_a, geometry_b)", "Returns the euclidean distance between the two geometries."],
      ["endAngle(line)", "Returns the angle of the end segment of the linestring."],
      ["endPoint(line)", "Returns the end point of the linestring."],
      ["envelope(geometry)", "Returns the polygon representing the envelope of the geometry, that is, the minimum rectangle with sides parallels to the axis containing it."],
      ["exteriorRing(polygon)", "Returns the exterior ring of the specified polygon."],
      ["geometryType(geometry)", "Returns the type of the geometry as a string. May be Point, MultiPoint, LineString, LinearRing, MultiLineString, Polygon, MultiPolygon, GeometryCollection."],
      ["geomFromWKT(gkt)", "Returns the Geometry represented in the Well Known Text format contained in the wkt parameter."],
      ["geomLength(geometry)", "Returns the length/perimeter of this geometry (computed in Cartesian space)."],
      ["getGeometryN(geometry, n)", "Returns the n-th geometry inside the collection."],
      ["getX(point)", "Returns the x ordinate of point."],
      ["getY(point)", "Returns the y ordinate of point."],
      ["getZ(point)", "Returns the z ordinate of point."],
      ["interiorPoint(geometry)", "Returns a point that is either interior to the geometry, when possible, or sitting on its boundary, otherwise."],
      ["interiorRingN(geometry, n)", "Returns the n-th interior ring of the polygon."],
      ["intersection(geometry_a, geometry_b)", "Returns the intersection between a and b. The intersection result can be anything including a geometry collection of heterogeneous, if the result is empty, it will be represented by an empty collection."],
      ["isClosed(line)", "Returns true if line forms a closed ring, that is, if the first and last coordinates are equal."],
      ["isEmpty(geometry)", "Returns true if the geometry does not contain any point (typical case, an empty geometry collection)."],
      ["isometric(geometry, extrusion)", "Returns a MultiPolygon containing the isometric extrusions of all components of the input geometry. The extrusion distance is extrusion, expressed in the same unit as the geometry coordinates. Can be used to get a pseudo-3d effect in a map."],
      ["isRing(line)", "Returns true if the line is actually a closed ring (equivalent to isRing(line) and isSimple(line))."],
      ["isSimple(geometry)", "Returns true if the geometry self intersects only at boundary points."],
      ["isValid(geometry)", "Returns true if the geometry is topologically valid (rings are closed, holes are inside the hull, and so on)."],
      ["numGeometries(collection)", "Returns the number of geometries contained in the geometry collection."],
      ["numInteriorRing(polygon)", "Returns the number of interior rings (holes) inside the specified polygon."],
      ["numPoint(geometry)", "Returns the number of points (vertexes) contained in geometry."],
      ["offset(geometry, offset_x, offset_y)", "Offsets all points in a geometry by the specified x and y offsets. Offsets are working in the same coordinate system as the geometry own coordinates."],
      ["pointN(geometry, n)", "Returns the n-th point inside the specified geometry."],
      ["startAngle(line)", "Returns the angle of the starting segment of the input linestring."],
      ["startPoint(line)", "Returns the starting point of the input linestring."],
      ["symDifference(geometry_a, geometry_b)", "Returns the symmetrical difference between a and b (all points that are inside a or b, but not both)."],
      ["union(geoemtry_a, geometry_b)", "Returns the union of a and b (the result may be a geometry collection)."],
      ["startPoint(line)", "Returns the starting point of the input linestring."],
      ["vertices(geometry)", "Returns a multi-point made with all the vertices of geometry."]]
  ],

  show: function(serviceUrl, typeName, element)
  {
    if (typeName.indexOf(",") !== -1) return;

    if (this.panelElement === null)
    {
      this.panelElement = document.createElement("div");
      this.panelElement.className = "cqlAssistant";
      this.panelElement.style.visibility = "hidden";
      this.panelElement.style.position = "absolute";
      this.panelElement.style.zIndex = "1000";
      this.panelElement.style.width = "400px";
      this.panelElement.style.height = "300px";
      this.panelElement.style.overflow = "auto";
      this.panelElement.style.backgroundColor = "white";
      this.panelElement.style.border = "2px solid #404040";
      this.panelElement.setAttribute("unselectable", "on");      

      document.body.appendChild(this.panelElement);

      var listener = function(event)
      {
        if (event.preventDefault) event.preventDefault();
      }
      if (this.panelElement.addEventListener)
      {
        this.panelElement.addEventListener("mousedown", listener, false);
      }
      else
      {
        this.panelElement.attachEvent("onmousedown", listener);
        this.panelElement.attachEvent("onmouseup", listener);
      }
    }

    var featureInfo = OpenLayers.FeatureTypeInfo.getInfo(serviceUrl, typeName);
    var html = '<div><div class="cqlHeader">Layer attributes:</div><ul class="cqlBlock">';
    var i;
    for (i = 0; i < featureInfo.properties.length; i++)
    {
      var property = featureInfo.properties[i];
      var className = (property.minOccurs == 0) ? "optional" : "mandatory";
      html += '<li unselectable="on">';
      html += '<a href="javascript:_insertText(\'' + property.name +
        '\')" unselectable="on" class="' + className + '">' +
        property.name + '</a>';
      var localType = property.localType;
      if (localType === 'GeometryPropertyType') localType = "geometry";
      html += ' : ' + localType;
      html += '</li>';
    }
    html += "</ul>";
    html += '<div><div class="cqlHeader">Operators:</div><ul class="cqlBlock">';
    for (i = 0; i < this.operatorList.length; i++)
    {
      var cqlOp = this.operatorList[i];
      html += '<li unselectable="on">';
      html += '<a href="javascript:_insertText(\'' + cqlOp[0] +
        '\')" unselectable="on">' + cqlOp[0] + '</a>';
      html += ' : ' + cqlOp[1];
      html += '</li>';
    }
    html += "</ul>";
    for (var g = 0; g < this.functionList.length; g++)
    {
      var group = this.functionList[g];
      html += '<div><div class="cqlHeader">' + group[0] +
        ':</div><ul class="cqlBlock">';
      for (i = 1; i < group.length; i++)
      {
        var cqlFn = group[i];
        html += '<li unselectable="on">';
        html += '<a href="javascript:_insertText(\'' + cqlFn[0] +
          '\')" unselectable="on">' + cqlFn[0] + '</a>';
        html += ' : ' + cqlFn[1];
         html += '</li>';
      }
      html += "</ul>";
    }
    this.panelElement.innerHTML = html;

    if (element)
    {
      this.panelElement.scrollTop = 0;
      _insertText = function(text)
      {
        if (document.selection)
        {
          element.focus();
          var sel = document.selection.createRange();
          sel.text = text;
        }
        else if (element.selectionStart || element.selectionStart == '0')
        {
          var startPos = element.selectionStart;
          var endPos = element.selectionEnd;
          element.value = element.value.substring(0, startPos) +
            text + element.value.substring(endPos, element.value.length);
          var pos = startPos + text.length;
          element.setSelectionRange(pos, pos);
        }
        else
        {
          element.value += text;
        }
      };

      element.setAttribute("autocomplete", "off");

      var rect = element.getBoundingClientRect();
      if (rect)
      {
        var xoffset = window.pageXOffset || document.documentElement.scrollLeft;
        var yoffset = window.pageYOffset || document.documentElement.scrollTop;
        this.panelElement.style.top = (yoffset + rect.bottom) + "px";
        this.panelElement.style.left = (xoffset + rect.left) + "px";
        this.panelElement.style.visibility = "visible";
      }
    }
  },

  hide: function()
  {
    if (this.panelElement)
    {
      this.panelElement.style.visibility = "hidden";
    }
  }
};

/* Utility functions */

function joinFilter(filterArray)
{
  var newFilterArray = [];
  var fieldHash = {};
  for (var i = 0; i < filterArray.length; i++)
  {
    var filter = filterArray[i];
    var tokens = filter.split("=");
    if (tokens.length == 2) // VAR = VALUE ?
    {
      var t1 = tokens[0].trim().toUpperCase();
      var t2 = tokens[1].trim();
      if (t1.match(/^[$A-Z_][0-9A-Z_$]*$/i))
      {
        if (!fieldHash[t1])
        {
          fieldHash[t1] = [];
        }
        fieldHash[t1].push(t2);
      }
      else newFilterArray.push(filter);
    }
    else
    {
      newFilterArray.push(filter);
    }
  }
  for (var fn in fieldHash)
  {
    var fv = fieldHash[fn];
    newFilterArray.push(fn + " in (" + fv.join(",") + ")");
  }
  return newFilterArray.join(" or ");
}

var Latinise = {};
Latinise.latin_map = {
  "\u00C0" : "A", "\u00C1" : "A", "\u00C8": "E", "\u00C9": "E",
  "\u00CC": "I", "\u00CD": "I", "\u00D2": "O", "\u00D3": "O", "\u00D9": "U", "\u00DA": "U",

  "\u00E0" : "a", "\u00E1" : "a", "\u00E8": "E", "\u00E9": "E",
  "\u00EC": "I", "\u00ED": "I", "\u00F2": "O", "\u00F3": "O", "\u00F9": "U", "\u00FA": "U",
  
  "\u00CF" : "I", "\u00EF" : "i",
  "\u00DC" : "U", "\u00FC" : "u"
};
if (!String.prototype.latinize)
{
  String.prototype.latinize = function()
  {
    return this.replace(/[^A-Za-z0-9\[\] ]/g,
      function(a){return Latinise.latin_map[a] || a})
  };
}
if (!String.prototype.trim)
{
  String.prototype.trim = function()
  {
    return this.replace(/^\s+|\s+$/g,'');
  };
}

function parseUrlParameters()
{
  var urlParameters = {};
  var paramString = window.location.search.substr(1);
  var paramArray = paramString.split("&");

  for (var i = 0; i < paramArray.length; i++)
  {
    var param = paramArray[i].split("=");
    var paramName = param[0];
    var paramValue = decodeURIComponent(param[1]);
    urlParameters[paramName] = paramValue;
  }  
  return urlParameters;
}