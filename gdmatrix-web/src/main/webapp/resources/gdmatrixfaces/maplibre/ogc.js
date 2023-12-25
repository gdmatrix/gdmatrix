/* ogc */

if (window.ogcLoaded === undefined)
{

  class FeatureTypeInspector
  {
    static cache = {};

    static getInfo(serviceUrl, typeName)
    {
      const key = serviceUrl + " {" + typeName + "}";
      let featureInfo = this.cache[key];
      if (!featureInfo)
      {
        const layerUrl = "/proxy?url=" + serviceUrl +
          "&service=wfs&version=1.1.0&request=DescribeFeatureType&typeName=" +
          typeName;

        return fetch(layerUrl)
          .then(response => response.text())
          .then(text => this.parseFeatureInfo(typeName, text))
          .catch(console.error);
      }
    }

    static parseFeatureInfo(typeName, responseText)
    {
      const properties = [];
      const parser = new DOMParser();
      const xml = parser.parseFromString(responseText, "application/xml");
      
      let geometryColumn = null;
      let geometryType = null;
      let complexType = xml.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "complexType")[0];
      if (complexType)
      {
        let elements = complexType.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");
        for (let element of elements)
        {
          let name = element.getAttribute("name");
          let type = element.getAttribute("type");
          let index = type.indexOf(":");
          if (index !== -1) type = type.substring(index + 1);
          if (type.endsWith("PropertyType"))
          {
            type = type.substring(0, type.length - 12);
          }
          if (type === "Point" || 
              type === "LineString" ||
              type === "Surface" ||
              type === "MultiPoint" || 
              type === "MultiLineString" ||
              type === "MultiSurface" ||              
              type === "Geometry")
          {
            geometryColumn = name;
            geometryType = type;
          }

          let property = {
            name: name,
            type: type,
            minOccurs: element.getAttribute("minOccurs"),
            maxOccurs: element.getAttribute("maxOccurs")
          };
          properties.push(property);      
        }
      }
      return { 
        name: typeName, 
        geometryColumn: geometryColumn,
        geometryType: geometryType,
        properties: properties 
      };
    }
  }

  class CQLAssistant
  {
    static panelElement = null;

    static serviceUrl = null;

    static operatorList = [
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
      ["IS NOT NULL", "Not NULL comparision: A IS NOT NULL."]];

    static functionList = [
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
      ["Transformation functions",
        ["Recode(value, in1, out1, in2, out2,...)", "Transforms value according to the in/out table."],
        ["Categorize(value, out1, in1, out2, in2,...)", "Transforms value into categories defined by the in/out table."],
        ["Interpolate(value, in1, out1, in2, out2,..., method, mode)", "Interpolates value according to in/out table. Method can be 'color'|'numeric'. Mode can be 'linear'|'cubic'|'cosine'."]      
      ],
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
    ];

    static show(typeName, element)
    {
      if (typeof typeName !== "string") return;
      if (typeName.indexOf(",") !== -1) return;

      if (this.serviceUrl)
      {
        const infoPromise = FeatureTypeInspector.getInfo(this.serviceUrl, typeName);
        infoPromise.then(info => this.showPanel(element, info));
      }
      else
      {
        this.showPanel(element);
      }
    }

    static showPanel(element, info)
    {
      let panelElement = this.panelElement;
      if (panelElement === null)
      {
        panelElement = document.createElement("div");
        this.panelElement = panelElement;
        panelElement.style.display = "none";
        panelElement.className = "cql_assistant";
        panelElement.style.position = "absolute";
        panelElement.style.zIndex = "10000";
        panelElement.style.width = "400px";
        panelElement.style.height = "200px";
        panelElement.style.overflow = "auto";
        panelElement.setAttribute("unselectable", "on");      

        document.body.appendChild(panelElement);

        const listener = function(event)
        {
          if (event.preventDefault) event.preventDefault();
        };
        panelElement.addEventListener("pointerdown", listener, false);
      }

      window._insertText = text =>
      {
        if (document.selection)
        {
          element.focus();
          let sel = document.selection.createRange();
          sel.text = text;
        }
        else if (element.selectionStart || element.selectionStart === '0')
        {
          let startPos = element.selectionStart;
          let endPos = element.selectionEnd;
          element.value = element.value.substring(0, startPos) +
            text + element.value.substring(endPos, element.value.length);
          let pos = startPos + text.length;
          element.setSelectionRange(pos, pos);
        }
        else
        {
          element.value += text;
        }
      };

      let html = '<div>';
      if (info)
      {
        html += '<div class="cql_header">Layer attributes:</div>';
        html += '<ul>';
        for (let property of info.properties)
        {
          let className = (property.minOccurs === 0) ? "optional" : "mandatory";

          html += '<li unselectable="on">';
          html += '<a href="javascript:_insertText(\'' + property.name +
            '\')" unselectable="on" class="' + className + '">' +
            property.name + '</a>';

          html += ' : ' + property.type;
          html += '</li>';
        }
        html += "</ul>";
      }

      html += '<div class="cql_header">Operators:</div>';
      html += '<ul>';
      for (let cqlOp of this.operatorList)
      {
        html += '<li unselectable="on">';
        html += '<a href="javascript:_insertText(\'' + cqlOp[0] +
          '\')" unselectable="on">' + cqlOp[0] + '</a>';
        html += ' : ' + cqlOp[1];
        html += '</li>';
      }
      html += "</ul>";

      for (let group of this.functionList)
      {
        html += '<div class="cql_header">' + group[0] + ':</div>';
        html += '<ul>';
        for (let i = 1; i < group.length; i++)
        {
          let cqlFn = group[i];
          html += '<li unselectable="on">';
          html += '<a href="javascript:_insertText(\'' + cqlFn[0] +
            '\')" unselectable="on">' + cqlFn[0] + '</a>';
          html += ' : ' + cqlFn[1];
           html += '</li>';
        }
        html += "</ul>";
      }
      panelElement.innerHTML = html;

      if (element)
      {
        panelElement.scrollTop = 0;
        element.setAttribute("autocomplete", "off");

        let rect = element.getBoundingClientRect();
        if (rect)
        {
          let xoffset = window.pageXOffset || document.documentElement.scrollLeft;
          let yoffset = window.pageYOffset || document.documentElement.scrollTop;
          panelElement.style.top = (yoffset + rect.bottom) + "px";
          panelElement.style.left = (xoffset + rect.left) + "px";
          panelElement.style.display = "block";
        }
      }
    }

    static hide()
    {
      if (this.panelElement)
      {
        this.panelElement.style.display = "none";
      }
    }
  }
  
  window.FeatureTypeInspector = FeatureTypeInspector;
  window.CQLAssistant = CQLAssistant;
  window.ogcLoaded = true;
}
