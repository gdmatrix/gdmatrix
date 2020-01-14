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
package org.santfeliu.translation.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author realor
 */
public class TranslationUtils
{
  private static final Set<String> ISO_LANGUAGES = new HashSet<String>
   (Arrays.asList(Locale.getISOLanguages()));  
  
  public static boolean isValidLanguage(String language)
  {
    return ISO_LANGUAGES.contains(language);
  }
  
  /* return translation of text to language or null if cannot translate it */
  public static String directTranslate(String language, String text)
  {
    if (text == null) return "";
    else if (!hasAnyLetter(text)) return text;
    return null;
  }

  private static boolean hasAnyLetter(String text)
  {
    boolean hasAnyLetter = false;
    int i = 0;
    while (!hasAnyLetter && i < text.length())
    {
      hasAnyLetter = Character.isLetter(text.charAt(i));
      i++;
    }
    return hasAnyLetter;
  }
}
