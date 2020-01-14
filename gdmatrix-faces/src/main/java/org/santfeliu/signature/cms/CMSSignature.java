package org.santfeliu.signature.cms;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

@Deprecated
public class CMSSignature implements Comparable
{  
  private X509Certificate certificate;
  private Map certificateProperties;
  private String signature;
  private Date signingDate;
  private Date timeStampDate;
  private String filename;
  private String decretNumber;
  private boolean valid;

  public String getCertificateName()
  {
    return certificate.getSubjectDN().getName();
  }

  public String getCertificateSerialNumber()
  {
    return certificate.getSerialNumber().toString(16).toUpperCase();
  }

  public Map getCertificateProperties()
  {
    return certificateProperties;
  }

  public X509Certificate getCertificate()
  {
    return certificate;
  }

  public String getSignerSN()
  {
    return (String)certificateProperties.get("SERIALNUMBER");
  }

  public String getSignerCN()
  {
    return (String)certificateProperties.get("CN");
  }

  public String getSignerOU()
  {
    return (String)certificateProperties.get("OU");
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
      String csn = getCertificateSerialNumber();
      if (csn != null)
      {
        builder.append(" ID:");
        builder.append(csn);
      }
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
  public int compareTo(Object o)
  {
    if (!(o instanceof CMSSignature))
      return 0;
    else
      return signingDate.compareTo(((CMSSignature) o).signingDate);
  }

  protected void loadProperties(String subjectDN)
  {
    certificateProperties = new HashMap();
    for (StringTokenizer tokenizer = new StringTokenizer(subjectDN, ","); 
         tokenizer.hasMoreTokens(); )
    {
      String token = tokenizer.nextToken();
      int index = token.indexOf("=");
      if (index != -1)
      {
        String key = token.substring(0, index);
        String value = token.substring(index + 1);
        Object oldValue = certificateProperties.get(key);
        if (oldValue == null)
          certificateProperties.put(key, value);
        else
          certificateProperties.put(key, 
                                    String.valueOf(oldValue) + ", " + 
                                    value);
      }
    }
  }

  public void setCertificate(X509Certificate certificate)
  {
    this.certificate = certificate;
  }

  public void setCertificateProperties(Map certificateProperties)
  {
    this.certificateProperties = certificateProperties;
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
