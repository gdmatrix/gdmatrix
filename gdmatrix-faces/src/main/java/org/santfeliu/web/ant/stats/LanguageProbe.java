package org.santfeliu.web.ant.stats;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ruizsj
 */
public class LanguageProbe extends CounterProbe
{
  Map<String, String> languageMap;

  @Override
  public void init()
  {
    languageMap = new HashMap();
    languageMap.put("ar", "Àrab");
    languageMap.put("bg", "búlgar");
    languageMap.put("ca", "català ");
    languageMap.put("de", "alemany");
    languageMap.put("en", "anglès");
    languageMap.put("es", "espanyol");
    languageMap.put("fr", "francés");
    languageMap.put("it", "italià ");
    languageMap.put("pt", "portugués");
    languageMap.put("ro", "romanès");
    languageMap.put("ru", "rus");
    languageMap.put("zh", "xinès");
  }

  @Override
  public void processLine(Line line)
  {
    increment(line.getLanguage(), languageMap.get(line.getLanguage()));
  }
}
