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
package org.santfeliu.translation.service;

import java.util.Collection;
import org.matrix.translation.Translation;
import org.matrix.translation.TranslationState;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.util.enc.Unicode;

/**
 *
 * @author unknown
 */
public class DBTranslation extends Translation
{
  public static final String DRAFT  = "D";
  public static final String COMPLETED  = "C";
  public static final String REMOVED  = "R";

  private String encodedText;
  private String encodedTranslation;
  private String strState;
  
  private Collection<DBTranslationGroup> groups;  

  public DBTranslation()
  {
  }

  public DBTranslation(Translation translation)
  {
    copyFrom(translation);
  }

  @Override
  public void setText(String text)
  {
    this.text = text;
    this.encodedText = Unicode.encode(text);
  }

  @Override
  public void setTranslation(String translation)
  {
    this.translation = translation;
    this.encodedTranslation = Unicode.encode(translation);
  }

  @Override
  public void setState(TranslationState state)
  {
    this.state = state;
    if (TranslationState.DRAFT.equals(state))
    {
      strState = DRAFT;
    }
    else
    {
      strState = COMPLETED;
    }
  }

  public String getEncodedText()
  {
    return encodedText;
  }

  public void setEncodedText(String encodedText)
  {
    this.encodedText = encodedText;
    this.text = Unicode.decode(encodedText);
  }

  public String getEncodedTranslation()
  {
    return encodedTranslation;
  }

  public void setEncodedTranslation(String encodedTranslation)
  {
    this.encodedTranslation = encodedTranslation;
    this.translation = Unicode.decode(encodedTranslation);
  }

  public String getStrState()
  {
    return strState;
  }

  public void setStrState(String strState)
  {
    this.strState = strState;
    this.state = COMPLETED.equals(strState) ?
      TranslationState.COMPLETED : TranslationState.DRAFT;
  }

  public Collection<DBTranslationGroup> getGroups() 
  {
    return groups;
  }

  public void setGroups(Collection<DBTranslationGroup> groups) 
  {
    this.groups = groups;
  }

  public void copyTo(Translation translation)
  {
    JPAUtils.copy(this, translation);
  }

  public void copyFrom(Translation translation)
  {
    setTransId(translation.getTransId());
    setLanguage(translation.getLanguage());
    setText(translation.getText());
    setTranslation(translation.getTranslation());
    setGroup(translation.getGroup());
    setState(translation.getState());
    setReadDateTime(translation.getReadDateTime());
  }
}
