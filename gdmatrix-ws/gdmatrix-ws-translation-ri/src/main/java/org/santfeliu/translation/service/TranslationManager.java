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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang.StringUtils;

import org.matrix.translation.Translation;
import org.matrix.translation.TranslationFilter;
import org.matrix.translation.TranslationState;
import org.matrix.translation.TranslationManagerPort;

import org.matrix.translation.TranslationMetaData;
import org.santfeliu.jpa.JPA;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.translation.StringTranslator;
import org.santfeliu.translation.TranslatorFactory;
import org.santfeliu.translation.util.TranslationUtils;
import org.santfeliu.util.enc.Unicode;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author unknown
 */
@WebService(endpointInterface = "org.matrix.translation.TranslationManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class TranslationManager implements TranslationManagerPort
{  
  @Resource
  WebServiceContext wsContext;

  @PersistenceContext
  public EntityManager entityManager;

  private static StringTranslator stringTranslator;
  
  private static final int TEXT_FIELD_MAX_SIZE = 4000;
  private static final int GROUP_FIELD_MAX_SIZE = 64;
  private static final int TRANSLATION_MAX_SIZE = 4000;
  private static final int MAX_GROUP_SIZE = 500;
  
  protected static final Logger logger = Logger.getLogger("Translation");

  static
  {
    try
    {
      String value = MatrixConfig.getClassProperty(TranslationManager.class,
        "stringTranslator");
      if (value != null)
      {
        stringTranslator = TranslatorFactory.getStringTranslator(value);
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "init failed", ex);
    }
  }

  public TranslationMetaData getTranslationMetaData()
  {
    TranslationMetaData metaData = new TranslationMetaData();
    metaData.setTranslationMaxSize(TRANSLATION_MAX_SIZE);
    return metaData;
  }

  public Translation translate(String language, String text, String group)
  {
    logger.log(Level.INFO, "translate language:{0} text:{1} group:{2}",
      new String[]{language, text, group});

    if (text == null || !isValidLanguage(language))
      throw new WebServiceException("INVALID_PARAMETERS");

    group = normalizeGroup(group);
    
    String translatedText = TranslationUtils.directTranslate(language, text);
    if (translatedText != null)
    {
      Translation t = new Translation();
      t.setLanguage(language);
      t.setText(text);
      t.setTranslation(translatedText);
      t.setState(TranslationState.COMPLETED);
      t.setGroup(group);
      return t;
    }
    text = truncate(text, TEXT_FIELD_MAX_SIZE);
    return translateText(language, text, group);
  }

  public List<Translation> translateGroup(String language, String text,
     String group)
  {
    List<Translation> result = new ArrayList<Translation>();
    logger.log(Level.INFO, "translateGroup language:{0} text:{1} group:{2}",
      new String[]{language, text, group});

    if (text == null || !isValidLanguage(language) || group == null)
      throw new WebServiceException("INVALID_PARAMETERS");

    group = normalizeGroup(group);
    
    String translatedText = TranslationUtils.directTranslate(language, text);
    if (translatedText != null)
    {
      Translation t = new Translation();
      t.setLanguage(language);
      t.setText(text);
      t.setTranslation(translatedText);
      t.setState(TranslationState.COMPLETED);
      t.setGroup(group);
      result.add(t);
    }
    text = truncate(text, TEXT_FIELD_MAX_SIZE);

    if (group != null)
    {
      readEntireGroup(language, group, result);
    }
    
    if (!isTranslationInList(text, result))
    {
      result.add(translateText(language, text, group));
    }
    return result;
  }

  public Translation loadTranslation(String transId)
  {
    DBTranslation dbTranslation = 
      entityManager.find(DBTranslation.class, transId);
    if (dbTranslation == null || 
        dbTranslation.getStrState().equals(DBTranslation.REMOVED))
      throw new WebServiceException("translation:TRANSLATION_NOT_FOUND");
    return dbTranslation;
  }

  public Translation storeTranslation(Translation translation)
  {
    User user = UserCache.getUser(wsContext);
    String dateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");

    validateTranslation(translation);
    DBTranslation dbTranslation = null;
    String transId = translation.getTransId();

    if (transId == null) // new translation
    {
      dbTranslation = new DBTranslation(translation);
      dbTranslation.setTransId(UUID.randomUUID().toString());
      dbTranslation.setReadDateTime(dateTime);
      dbTranslation.setCreationDateTime(dateTime);
      dbTranslation.setModifyDateTime(dateTime);
      dbTranslation.setModifyUserId(user.getUserId());
      entityManager.persist(dbTranslation);
    }
    else // translation found
    {
      dbTranslation = entityManager.getReference(DBTranslation.class, transId);
      dbTranslation.copyFrom(translation);
      dbTranslation.setReadDateTime(dateTime);
      dbTranslation.setModifyDateTime(dateTime);
      dbTranslation.setModifyUserId(user.getUserId());
      entityManager.merge(dbTranslation);
    }
    dbTranslation.copyTo(translation);
    return translation;
  }
  
  public boolean removeTranslation(String transId)
  {
    try
    {
      User user = UserCache.getUser(wsContext);
      String dateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");

      DBTranslation dbTranslation =
        entityManager.getReference(DBTranslation.class, transId);
      dbTranslation.setModifyDateTime(dateTime);
      dbTranslation.setModifyUserId(user.getUserId());
      dbTranslation.setStrState(DBTranslation.REMOVED);
      entityManager.merge(dbTranslation);
      return true;
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
  }

  public int countTranslations(TranslationFilter filter)
  {
    Query query = entityManager.createQuery(createQueryString(filter, true));
    setFilterParameters(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  public List<Translation> findTranslations(TranslationFilter filter)
  {
    Query query = entityManager.createQuery(createQueryString(filter, false));
    setFilterParameters(query, filter);
    if (filter.getFirstResult() > 0)
    {
      query.setFirstResult(filter.getFirstResult());
    }
    if (filter.getMaxResults() > 0)
    {
      query.setMaxResults(filter.getMaxResults());
    }
    List<DBTranslation> dbTranslations = query.getResultList();
    List<Translation> result = new ArrayList<Translation>();
    for (DBTranslation dbTranslation : dbTranslations)
    {
      result.add(dbTranslation);
    }
    return result;
  }

  public List<String> listModifiedTranslations(
    String language, String dateTime1, String dateTime2)
  {
    try
    {
      Query query = entityManager.createNamedQuery("listModifiedTranslations");
      query.setParameter("language", language);
      query.setParameter("dateTime1", dateTime1);
      query.setParameter("dateTime2", dateTime2);
      return query.getResultList();
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "listModifiedTranslations", ex);
      throw new WebServiceException(ex);
    }
  }

  public int setActiveTranslations(List<Translation> translations)
  {
    int numUpdated = 0;
    try
    {
      Query query = entityManager.createNamedQuery("setActiveTranslations");
      for (Translation translation : translations)
      {
        query.setParameter("transId", translation.getTransId());
        query.setParameter("readDateTime", translation.getReadDateTime());
        numUpdated += query.executeUpdate();
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "setActiveTranslations", ex);
      throw new WebServiceException(ex);
    }
    return numUpdated;
  }

  // ********** private methods **********

  private String createQueryString(TranslationFilter filter, boolean count)
  {
    boolean byGroup = !StringUtils.isBlank(filter.getGroup());
    StringBuilder queryBuffer = new StringBuilder();
    if (count)
    {
      queryBuffer.append("SELECT count(t) ");      
    }
    else
    {
      queryBuffer.append("SELECT t ");
    }
    if (byGroup)
    {
      queryBuffer.append(" FROM Translation t, TranslationGroup tg ");
    }
    else
    {
      queryBuffer.append(" FROM Translation t ");
    }
    queryBuffer.append(
      "WHERE (t.language = :language OR :language is null) " + 
      "AND (t.strState = :strState OR :strState IS NULL) " +
      "AND (lower(t.encodedText) LIKE :encodedText OR :encodedText IS NULL) " +
      "AND (lower(t.encodedTranslation) LIKE :encodedTranslation OR :encodedTranslation IS NULL) " +
      "AND t.strState <> 'R' ");
    if (byGroup)
    {
      queryBuffer.append(
        "AND t.transId = tg.transId " + 
        "AND tg.group LIKE :group ");
    }
    if (!count)
    {
      queryBuffer.append("ORDER BY t.encodedText");
    }
    return queryBuffer.toString();
  }
  
  private DBTranslation translateText(String language, String text, String group)
  {
    DBTranslation dbTranslation;
    String dateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");

    Query query = entityManager.createNamedQuery("findTranslation");
    query.setParameter("language", language);
    query.setParameter("encodedText", Unicode.encode(text));
    query.setFirstResult(0);
    query.setMaxResults(1);
    List<DBTranslation> dbTranslations =
      (List<DBTranslation>)query.getResultList();
    if (dbTranslations.size() > 0)
    {
      dbTranslation = dbTranslations.get(0);
    }
    else // new translation
    {
      String automaticTranslation =
        getAutomaticTranslation(language, text, group);
      automaticTranslation =
        truncate(automaticTranslation, TRANSLATION_MAX_SIZE);
      dbTranslation = new DBTranslation();
      dbTranslation.setTransId(UUID.randomUUID().toString());
      dbTranslation.setLanguage(language);
      dbTranslation.setTranslation(automaticTranslation);
      dbTranslation.setText(text);
      dbTranslation.setGroup(group);
      dbTranslation.setState(TranslationState.DRAFT);
      dbTranslation.setReadDateTime(dateTime);
      dbTranslation.setCreationDateTime(dateTime);
      dbTranslation.setModifyDateTime(dateTime);
      dbTranslation.setModifyUserId(null);
      entityManager.persist(dbTranslation);
    }
    if (group != null)
    {
      addTranslationToGroup(dbTranslation.getTransId(), group);
    }
    return dbTranslation;
  }

  private DBTranslationGroup addTranslationToGroup(String transId, String group)
  {
    DBTranslationGroupPK pk = new DBTranslationGroupPK(transId, group);
    DBTranslationGroup dbTranslationGroup = 
      entityManager.find(DBTranslationGroup.class, pk);
    if (dbTranslationGroup == null)
    {
      dbTranslationGroup = new DBTranslationGroup();
      dbTranslationGroup.setTransId(transId);
      dbTranslationGroup.setGroup(group);
      entityManager.persist(dbTranslationGroup);
    }
    return dbTranslationGroup;
  }
  
  private String getAutomaticTranslation(
    String language, String text, String group)
  {
    String translation = null;
    if (stringTranslator != null)
    {
      translation = stringTranslator.translate(text, language, group);
    }
    if (translation == null)
    {
      translation = text;
    }
    return translation;
  }

  private void setFilterParameters(Query query, TranslationFilter filter)
  {
    query.setParameter("language", filter.getLanguage());
    if (TranslationState.COMPLETED.equals(filter.getState()))
    {
      query.setParameter("strState", DBTranslation.COMPLETED);
    }
    else if (TranslationState.DRAFT.equals(filter.getState()))
    {
      query.setParameter("strState", DBTranslation.DRAFT);
    }
    else
    {
      query.setParameter("strState", null);
    }
    query.setParameter("encodedText", "%" + (filter.getText() == null ? "" :
      Unicode.encode(filter.getText().toLowerCase())) + "%");

    query.setParameter("encodedTranslation",
      "%" + (filter.getTranslation() == null ? "" :
      Unicode.encode(filter.getTranslation().toLowerCase())) + "%");

    String group = normalizeGroup(filter.getGroup());
    if (group != null)
    {
      query.setParameter("group", group);
    }
  }

  private void readEntireGroup(String language, String group,
    List<Translation> result)
  {
    Query query = entityManager.createNamedQuery("readEntireGroup");
    query.setParameter("language", language);
    query.setParameter("group", group);
    query.setMaxResults(MAX_GROUP_SIZE);
    List<DBTranslation> queryResult = query.getResultList();
    for (DBTranslation dbTranslation : queryResult)
    {
      result.add(dbTranslation);
    }
  }

  private boolean isTranslationInList(String text,
    List<Translation> translations)
  {
    boolean found = false;
    Iterator<Translation> iter = translations.iterator();
    while (iter.hasNext() && !found)
    {
      Translation translation = iter.next();
      if (translation.getText().equals(text))
      {
        found = true;
      }
    }
    return found;
  }

  private String normalizeGroup(String group)
  {
    if (StringUtils.isBlank(group)) return null;
    return truncate(group, GROUP_FIELD_MAX_SIZE).toLowerCase();
  }
  
  private String truncate(String text, int size)
  {
    // TODO: truncate a HTML code
    if (text == null) return null;
    if (text.length() > size) return text.substring(0, size);
    return text;
  }

  private void validateTranslation(Translation translation)
  {
    TranslationMetaData metaData = getTranslationMetaData();
    if (translation.getTranslation() != null &&
      translation.getTranslation().length() > metaData.getTranslationMaxSize())
    {
      throw new WebServiceException("VALUE_TOO_LARGE");
    }
    if (!isValidLanguage(translation.getLanguage()))
      throw new WebServiceException("INVALID_PARAMETERS");
  }

  private boolean isValidLanguage(String language)
  {
    if (language == null) return false;
    return language.length() == 2;
  }
}
