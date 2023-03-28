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
package org.santfeliu.webapp.util;

import java.util.Map;
import java.util.logging.Logger;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.ViewMapListener;
import javax.servlet.http.HttpSession;

/**
 *
 * @author realor
 */
public class PreDestroyViewMapListener implements ViewMapListener
{
  private final static Logger log = Logger.getLogger("PreDestroyViewMapListener");

  @Override
  public boolean isListenerForSource(Object source)
  {
    return (source instanceof UIViewRoot);
  }

  @Override
  public void processEvent(SystemEvent event)
  {
    if (event instanceof PreDestroyViewMapEvent)
    {
      FacesContext context = FacesContext.getCurrentInstance();

      if (context == null)
      {
        return;
      }

      HttpSession session = (HttpSession)context.getExternalContext().getSession(false);
      if (session == null) return;

      Map activeViewMap = (Map)session.getAttribute("com.sun.faces.application.view.activeViewMaps");
      if (activeViewMap != null)
      {
        String viewMapId = (String)context.getViewRoot()
          .getTransientStateHelper().getTransient("com.sun.faces.application.view.viewMapId");
        activeViewMap.remove(viewMapId);
      }
    }
  }
}
