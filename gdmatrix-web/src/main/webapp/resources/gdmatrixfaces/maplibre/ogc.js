/* ogc */

class FeatureTypeInspector
{
  static cache = {};

  static getInfo(serviceUrl, typeName)
  {
    const key = serviceUrl + " {" + typeName + "}";
    let featureInfo = this.cache[key];
    if (!featureInfo)
    {
      const layerUrl = serviceUrl +
        "&service=wfs&version=1.1.0&request=DescribeFeatureType&typeName=" +
        typeName;

      fetch(layerUrl)
        .then(response => response.text())
        .then(text => this.parseFeatureInfo(text))
        .catch(console.error);
    }
  }

  static parseFeatureInfo(responseText)
  {
    const parser = new DOMParser();
    const xml = parser.parseFromString(responseText, "application/xml");
    console.info(xml);
  }
}

