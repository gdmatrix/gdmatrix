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
package org.santfeliu.doc.fmt.p7m;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 *
 * @author unknown
 */
public class P7MSignature implements Comparable
{
  private X509Certificate certificate;
  private Map subjectProperties = new HashMap();
  private Map issuerProperties = new HashMap();
  private String signature;
  private Date signingDate;
  private Date timeStampDate;
  private X509Certificate timeStampCertificate;
  private String filename;
  private String decretNumber;
  private boolean valid;

  @Deprecated
  public String getCertificateName()
  {
    return getSubjectName();
  }

  public String getSubjectName()
  {
    return certificate.getSubjectX500Principal().getName();
  }

  public String getIssuerName()
  {
    return certificate.getIssuerX500Principal().getName();
  }

  public String getCertificateSerialNumber()
  {
    return certificate.getSerialNumber().toString(16).toUpperCase();
  }

  public Map getSubjectProperties()
  {
    return subjectProperties;
  }

  public Map getIssuerProperties()
  {
    return issuerProperties;
  }

  public String getSignerSN()
  {
    return (String)subjectProperties.get("SERIALNUMBER");
  }

  public String getSignerCN()
  {
    return (String)subjectProperties.get("CN");
  }

  public String getSignerOU()
  {
    return (String)subjectProperties.get("OU");
  }

  public String getSignerInfo()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getSignerCN());
    String signerSN = getSignerSN();
    if (signerSN != null)
    {
      builder.append(" (");
      builder.append(signerSN);
      builder.append(")");
    }
    return builder.toString();
  }

  public String getSignature()
  {
    return signature;
  }

  public String getSigningTime()
  {
    if (signingDate == null) return null;

    SimpleDateFormat df2 =
      new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm:ss 'GMT+'00:00",
                           new Locale("ca"));
    return df2.format(signingDate);
  }

  public String getSigningTime(String format)
  {
    return getSigningTime(format, Locale.getDefault());
  }

  public String getSigningTime(String format, Locale locale)
  {
    if (signingDate == null) return null;
    SimpleDateFormat df2 = new SimpleDateFormat(format, locale);
    return df2.format(signingDate);
  }

  public String getTimeStamp()
  {
    if (timeStampDate == null) return null;

    SimpleDateFormat df2 =
      new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm:ss 'GMT+'00:00",
                           new Locale("ca"));
    return df2.format(timeStampDate);
  }

  public String getTimeStamp(String format, Locale locale)
  {
    if (timeStampDate == null) return null;

    SimpleDateFormat df2 = new SimpleDateFormat(format, locale);
    return df2.format(timeStampDate);
  }

  // comparable
  @Override
  public int compareTo(Object o)
  {
    if (!(o instanceof P7MSignature))
      return 0;
    else
      return signingDate.compareTo(((P7MSignature) o).signingDate);
  }

  public void loadProperties()
  {
    String subjectName = certificate.getSubjectX500Principal().getName();
    loadProperties(subjectProperties, subjectName);
    String issuerName = certificate.getIssuerX500Principal().getName();
    loadProperties(issuerProperties, issuerName);
  }

  protected void loadProperties(Map properties, String subjectDN)
  {
    for (StringTokenizer tokenizer = new StringTokenizer(subjectDN, ",");
         tokenizer.hasMoreTokens(); )
    {
      String token = tokenizer.nextToken();
      int index = token.indexOf("=");
      if (index != -1)
      {
        String key = token.substring(0, index);
        String value = token.substring(index + 1);
        Object oldValue = properties.get(key);
        if (oldValue == null)
          properties.put(key, value);
        else
          properties.put(key, String.valueOf(oldValue) + ", " + value);
      }
    }
  }

  public void setCertificate(X509Certificate certificate)
  {
    this.certificate = certificate;
  }

  public X509Certificate getCertificate()
  {
    return certificate;
  }

  public void setCertificateProperties(Map certificateProperties)
  {
    this.subjectProperties = certificateProperties;
  }

  public void setTimeStampCertificate(X509Certificate certificate)
  {
    this.timeStampCertificate = certificate;
  }

  public X509Certificate getTimeStampCertificate()
  {
    return timeStampCertificate;
  }

  public void setSignature(String signature)
  {
    this.signature = signature;
  }

  public void setSigningDate(Date signingDate)
  {
    this.signingDate = signingDate;
  }

  public Date getSigningDate()
  {
    return signingDate;
  }

  public Date getTimeStampDate()
  {
    return timeStampDate;
  }

  public void setTimeStampDate(Date timeStampDate)
  {
    this.timeStampDate = timeStampDate;
  }

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public void setDecretNumber(String decretNumber)
  {
    this.decretNumber = decretNumber;
  }

  public String getDecretNumber()
  {
    return decretNumber;
  }

  public boolean isValid()
  {
    return valid;
  }

  protected void setValid(boolean valid)
  {
    this.valid = valid;
  }
}
