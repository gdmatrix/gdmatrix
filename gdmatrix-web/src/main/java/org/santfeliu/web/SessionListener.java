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

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.logging.Logger;
import org.matrix.security.SecurityConstants;
import static org.matrix.security.SecurityConstants.ANONYMOUS;

/**
 *
 * @author realor
 */
public class SessionListener implements HttpSessionListener
{
  public static final String SESSION_PURGE_PERIOD = "SESSION_PURGE_PERIOD";
  public static final String ANONYMOUS_SESSION_TIMEOUT = "ANONYMOUS_SESSION_TIMEOUT";
  public static final String ANONYMOUS_SESSION_INTERVAL = "ANONYMOUS_SESSION_INTERVAL";

  private static final Logger LOGGER = Logger.getLogger("SessionListener");

  private static final Set<HttpSession> SESSIONS =
    Collections.synchronizedSet(new HashSet<>());

  private boolean initialized;
  private int sessionPurgePeriod = 1; // minutes
  private int anonymousSessionTimeout = 3; // minutes
  private int anonymousSessionInterval = 10000; // millis
  private long lastPurgeMillis;

  @Override
  public void sessionCreated(HttpSessionEvent hse)
  {
    HttpSession session = hse.getSession();
    SESSIONS.add(session);

    if (!initialized)
    {
      initialiaze(session.getServletContext());
      initialized = true;
    }

    synchronized (SESSIONS)
    {
      long now = System.currentTimeMillis();
      if (now - lastPurgeMillis > sessionPurgePeriod)
      {
        purgeSessions();
        lastPurgeMillis = now;
      }
    }
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent hse)
  {
    HttpSession session = hse.getSession();
    SESSIONS.remove(session);
  }

  public static int getActiveSessionCount()
  {
    return SESSIONS.size();
  }

  private void initialiaze(ServletContext context)
  {
    String value = context.getInitParameter(SESSION_PURGE_PERIOD);
    try
    {
      if (value != null)
      {
        sessionPurgePeriod = Integer.parseInt(value);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.WARNING, "Invalid SESSION_PURGE_PERIOD: {0}", value);
    }

    value = context.getInitParameter(ANONYMOUS_SESSION_TIMEOUT);
    try
    {
      if (value != null)
      {
        anonymousSessionTimeout = Integer.parseInt(value);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.WARNING, "Invalid ANONYMOUS_SESSION_TIMEOUT: {0}", value);
    }

    value = context.getInitParameter(ANONYMOUS_SESSION_INTERVAL);
    try
    {
      if (value != null)
      {
        anonymousSessionInterval = Integer.parseInt(value);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.WARNING, "Invalid ANONYMOUS_SESSION_INTERVAL: {0}", value);
    }
  }

  private void purgeSessions()
  {
    long now = System.currentTimeMillis();
    for (HttpSession session : SESSIONS)
    {
      UserSessionBean userSessionBean = UserSessionBean.getInstance(session);
      String userId = userSessionBean == null ?
        ANONYMOUS : userSessionBean.getUserId();
      if (ANONYMOUS.equals(userId))
      {
        long ellapsedMinutes = (now - session.getLastAccessedTime()) / 60000;
        if (ellapsedMinutes >= anonymousSessionTimeout)
        {
          long interval = session.getLastAccessedTime() - session.getCreationTime();
          if (interval < anonymousSessionInterval)
          {
            session.invalidate();
            LOGGER.log(Level.INFO, "Invalidate session: {0}", session.getId());
          }
        }
      }
    }
  }

  public static List<HttpSession> getActiveSessions()
  {
    synchronized (SESSIONS)
    {
      return new ArrayList<>(SESSIONS);
    }
  }

}
