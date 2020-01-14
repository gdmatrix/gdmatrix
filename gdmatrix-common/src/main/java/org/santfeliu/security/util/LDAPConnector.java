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
package org.santfeliu.security.util;

/**
 *
 * @author realor
 */
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LDAPConnector
{
  public static final String URL_SEPARATOR = ";";
  private String domain;
  private String ldapUrl;
  private String searchBase;
  private String userId;
  private String password;

  public LDAPConnector()
  {
  }

  public LDAPConnector(String ldapURL, String domain, String searchBase,
    String userId, String password)
  {
    this.ldapUrl = ldapURL;
    this.domain = domain;
    this.searchBase = searchBase;
    this.userId = userId;
    this.password = password;
  }

  public String getDomain()
  {
    return domain;
  }

  public void setDomain(String domain)
  {
    this.domain = domain;
  }

  public String getLdapUrl()
  {
    return ldapUrl;
  }

  public void setLdapUrl(String ldapUrl)
  {
    this.ldapUrl = ldapUrl;
  }

  public String getSearchBase()
  {
    return searchBase;
  }

  public void setSearchBase(String searchBase)
  {
    this.searchBase = searchBase;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public Object getAttribute(String dn, String attribute)
    throws Exception
  {
    LdapContext ctxGC = createLdapContext();
    
    Attributes attrs = ctxGC.getAttributes(dn, new String[]{attribute});
    Attribute att = attrs.get(attribute);
    return att.get();
  }

  public void setAttribute(String dn, String attribute, Object value)
    throws Exception
  {
    LdapContext ctxGC = createLdapContext();
    
    ModificationItem[] mods = new ModificationItem[1];
    Attribute mod0 = new BasicAttribute(attribute, value);
    mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
    ctxGC.modifyAttributes(dn, mods);
  }

  public String find(String attribute, String value) throws Exception
  {
    String dn = null;

    LdapContext ctxGC = createLdapContext();

    String searchFilter ="(" + attribute + "=" + value + ")";

    // Create the search controls
    SearchControls searchCtls = new SearchControls();
    searchCtls.setReturningAttributes(new String[]{"distinguishedName"});

    // Specify the search scope
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    // Search objects in GC using filters
    NamingEnumeration answer =
      ctxGC.search(searchBase, searchFilter, searchCtls);
    if (answer.hasMoreElements())
    {
      SearchResult sr = (SearchResult)answer.next();
      Attributes attrs = sr.getAttributes();
      if (attrs != null)
      {
        NamingEnumeration ne = attrs.getAll();
        if (ne.hasMore())
        {
          Attribute attr = (Attribute)ne.next();
          dn = (String)attr.get();
        }
        ne.close();
      }
    }
    return dn;
  }

  public Map authenticate() throws Exception
  {
    return getUserInfo(userId, "distinguishedName", "sn", "givenName", "mail");
  }

  public Map getUserInfo(String userId, String ... returnedAtts)
    throws Exception
  {
    LdapContext ctxGC = createLdapContext();

    String searchFilter =
      "(&(objectClass=user)(sAMAccountName=" + userId + "))";

    // Create the search controls
    SearchControls searchCtls = new SearchControls();
    searchCtls.setReturningAttributes(returnedAtts);

    // Specify the search scope
    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    // Search objects in GC using filters
    NamingEnumeration answer =
      ctxGC.search(searchBase, searchFilter, searchCtls);
    if (answer.hasMoreElements())
    {
      SearchResult sr = (SearchResult) answer.next();
      Attributes attrs = sr.getAttributes();
      Map amap = null;
      if (attrs != null)
      {
        amap = new HashMap();
        NamingEnumeration ne = attrs.getAll();
        while (ne.hasMore())
        {
          Attribute attr = (Attribute) ne.next();
          amap.put(attr.getID(), attr.get());
        }
        ne.close();
      }
      return amap;
    }
    return null;
  }

  private LdapContext createLdapContext() throws Exception
  {
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, this.userId + "@" + domain);
    env.put(Context.SECURITY_CREDENTIALS, this.password);
    env.put("com.sun.jndi.ldap.connect.timeout", "3000"); // 3 seconds

    String[] urls = ldapUrl.split(URL_SEPARATOR);
    LdapContext context = null;
    Exception exception = null;
    for (String url : urls)
    {
      env.put(Context.PROVIDER_URL, url);
      try
      {
        context = new InitialLdapContext(env, null);
      }
      catch (Exception ex)
      {
        exception = ex;
      }
    }
    if (context == null) throw exception;
    
    return context;
  }

  public static void main(String[] args)
  {
    try
    {
      LDAPConnector conn = new LDAPConnector(
        "ldap://xxxxx;ldap://yyyyy", "santfeliu.local", "DC=santfeliu,DC=local",
        "ldapadmin", "******");

      Map map = conn.getUserInfo("realor",
        "distinguishedName", "sn", "givenName", "mail", "name", "language");
      System.out.println(map);

      String dn = conn.find("division", "99999");
      System.out.println(dn);
      String userId = (String)conn.getAttribute(dn, "sAMAccountName");

      System.out.println("userId: " + userId);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
