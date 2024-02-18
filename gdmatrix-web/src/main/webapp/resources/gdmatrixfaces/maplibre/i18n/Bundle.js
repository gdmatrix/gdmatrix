/* Bundle.js */

class Bundle
{
  static bundles = {};
  static userLanguage;

  constructor(name, translations)
  {
    this.name = name;
    this.languages = {};

    if (translations)
    {
      for (let language in translations)
      {
        this.languages[language] = translations[language];
      }
    }
  }

  static getBundle(name)
  {
    let bundle = this.bundles[name];
    if (bundle === undefined)
    {
      bundle = new Bundle(name);
      this.bundles[name] = bundle;
    }
    return bundle;
  }

  static localizeBundles()
  {
    const language = this.userLanguage;
    const modules = [];
    for (let name in this.bundles)
    {
      let bundle = this.bundles[name];
      if (bundle[""] === undefined)
      {
        let path = MAPLIBRE_BASE_PATH + "i18n/" + name + ".js";
        modules.push(import(path).catch(error => {}));
      }
      if (bundle[language] === undefined)
      {
        let path = MAPLIBRE_BASE_PATH + "i18n/" + name + "_" + language + ".js";
        modules.push(import(path).catch(error => {}));
      }
    }
    return Promise.all(modules);
  }

  getTransations(language)
  {
    return this.languages[language];
  }

  setTranslations(language, translations)
  {
    let bundleFile = this.name;
    if (language.length > 0) bundleFile += "_" + language;
    console.info(bundleFile + " bundle loaded.");
    this.languages[language] = translations;
  }

  get(key, ...args)
  {
    let translation;

    let translations = this.languages[Bundle.userLanguage];    
    if (translations)
    {
      translation = translations[key];
    }

    if (translation === undefined)
    {
      let defaultTranslations = this.languages[""];
      if (defaultTranslations)
      {
        translation = defaultTranslations[key];
      }
    }

    if (translation === undefined)
    {
      translation = key;
    }
    
    if (typeof translation === "function")
    {
      return translation(...args);
    }

    return translation;
  }

}

export { Bundle };

