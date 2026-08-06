/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author granadogj
 */
@Named
@ApplicationScoped
public class SeoBean
{
  private static final Map<String, String> OG_LOCALE_MAP = new HashMap<>();
  static
  {
    OG_LOCALE_MAP.put("ca", "ca_ES");
    OG_LOCALE_MAP.put("es", "es_ES");
    OG_LOCALE_MAP.put("en", "en_US"); // or en_GB
    OG_LOCALE_MAP.put("fr", "fr_FR");
    OG_LOCALE_MAP.put("de", "de_DE");
    OG_LOCALE_MAP.put("it", "it_IT");
    OG_LOCALE_MAP.put("pt", "pt_PT");
    OG_LOCALE_MAP.put("ru", "ru_RU");
    OG_LOCALE_MAP.put("ar", "ar_AR");
    OG_LOCALE_MAP.put("zh", "zh_CN");
    OG_LOCALE_MAP.put("ro", "ro_RO");
    OG_LOCALE_MAP.put("bg", "bg_BG");
    OG_LOCALE_MAP.put("ja", "ja_JP");
  }
  
  private static final String DEFAULT_OG_LOCALE = "ca_ES";

  public String getOgLocale()
  {
    String locale = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
    return OG_LOCALE_MAP.getOrDefault(locale, DEFAULT_OG_LOCALE);
  }
  
  public List<String> getOgLocaleAlternates()
  {
    String current = getOgLocale();
    return OG_LOCALE_MAP.values().stream()
      .filter(v -> !v.equals(current))
      .collect(Collectors.toList());
  }  
}
