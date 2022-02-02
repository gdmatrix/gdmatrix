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
package org.matrix.pf.web;

import java.util.List;
import java.util.Map;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.util.SerializableFunction;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;

/**
 *
 * @author blanquepa
 */
public class MatrixMenuItem extends DefaultMenuItem
{
  private final MenuModel menuModel;
  private final String mid;

  public MatrixMenuItem(MenuItemCursor mic)
  {
    menuModel = mic.getMenuModel();
    mid = mic.getMid();
  }

  public MenuItemCursor getMenuItem()
  {
    return menuModel.getMenuItem(mid);
  }

  public String getProperty(String propName)
  {
    return getMenuItem().getProperty(propName);
  }

  public static Builder builder(MenuItemCursor mic)
  {
    return new Builder(mic);
  }

  public static final class Builder
  {
    private final MatrixMenuItem menuItem;

    private Builder(MenuItemCursor mic)
    {
      menuItem = new MatrixMenuItem(mic);
    }

    public Builder id(String id)
    {
      menuItem.setId(id);
      return this;
    }

    public Builder icon(String icon)
    {
      menuItem.setIcon(icon);
      return this;
    }

    public Builder iconPos(String iconPos)
    {
      menuItem.setIconPos(iconPos);
      return this;
    }

    public Builder title(String title)
    {
      menuItem.setTitle(title);
      return this;
    }

    public Builder disabled(boolean disabled)
    {
      menuItem.setDisabled(disabled);
      return this;
    }

    public Builder onclick(String onclick)
    {
      menuItem.setOnclick(onclick);
      return this;
    }

    public Builder style(String style)
    {
      menuItem.setStyle(style);
      return this;
    }

    public Builder styleClass(String styleClass)
    {
      menuItem.setStyleClass(styleClass);
      return this;
    }

    public Builder url(String url)
    {
      menuItem.setUrl(url);
      return this;
    }

    public Builder target(String target)
    {
      menuItem.setTarget(target);
      return this;
    }

    public Builder ajax(boolean ajax)
    {
      menuItem.setAjax(ajax);
      return this;
    }

    public Builder value(Object value)
    {
      menuItem.setValue(value);
      return this;
    }

    public Builder outcome(String outcome)
    {
      menuItem.setOutcome(outcome);
      return this;
    }

    public Builder includeViewParams(boolean includeViewParams)
    {
      menuItem.setIncludeViewParams(includeViewParams);
      return this;
    }

    public Builder fragment(String fragment)
    {
      menuItem.setFragment(fragment);
      return this;
    }

    public Builder params(Map<String, List<String>> params)
    {
      menuItem.setParams(params);
      return this;
    }

    public Builder command(String command)
    {
      menuItem.setCommand(command);
      return this;
    }

    public Builder function(SerializableFunction<MenuItem, String> function)
    {
      menuItem.setFunction(function);
      return this;
    }

    public Builder rendered(boolean rendered)
    {
      menuItem.setRendered(rendered);
      return this;
    }

    public Builder onstart(String onstart)
    {
      menuItem.setOnstart(onstart);
      return this;
    }

    public Builder onerror(String onerror)
    {
      menuItem.setOnerror(onerror);
      return this;
    }

    public Builder onsuccess(String onsuccess)
    {
      menuItem.setOnsuccess(onsuccess);
      return this;
    }

    public Builder oncomplete(String oncomplete)
    {
      menuItem.setOncomplete(oncomplete);
      return this;
    }

    public Builder update(String update)
    {
      menuItem.setUpdate(update);
      return this;
    }

    public Builder process(String process)
    {
      menuItem.setProcess(process);
      return this;
    }

    public Builder partialSubmit(boolean partialSubmit)
    {
      menuItem.setPartialSubmit(partialSubmit);
      return this;
    }

    public Builder global(boolean global)
    {
      menuItem.setGlobal(global);
      return this;
    }

    public Builder async(boolean async)
    {
      menuItem.setAsync(async);
      return this;
    }

    public Builder resetValues(boolean resetValues)
    {
      menuItem.setResetValues(resetValues);
      return this;
    }

    public Builder ignoreAutoUpdate(boolean ignoreAutoUpdate)
    {
      menuItem.setIgnoreAutoUpdate(ignoreAutoUpdate);
      return this;
    }

    public Builder immediate(boolean immediate)
    {
      menuItem.setImmediate(immediate);
      return this;
    }

    public Builder delay(String delay)
    {
      menuItem.setDelay(delay);
      return this;
    }

    public Builder timeout(int timeout)
    {
      menuItem.setTimeout(timeout);
      return this;
    }

    public Builder disableClientWindow(boolean disableClientWindow)
    {
      menuItem.setDisableClientWindow(disableClientWindow);
      return this;
    }

    public Builder containerStyle(String containerStyle)
    {
      menuItem.setContainerStyle(containerStyle);
      return this;
    }

    public Builder containerStyleClass(String containerStyleClass)
    {
      menuItem.setContainerStyleClass(containerStyleClass);
      return this;
    }

    public Builder partialSubmitFilter(String partialSubmitFilter)
    {
      menuItem.setPartialSubmitFilter(partialSubmitFilter);
      return this;
    }

    public Builder confirmationScript(String confirmationScript)
    {
      menuItem.setConfirmationScript(confirmationScript);
      return this;
    }

    public Builder form(String form)
    {
      menuItem.setForm(form);
      return this;
    }

    public Builder escape(boolean escape)
    {
      menuItem.setEscape(escape);
      return this;
    }

    public Builder rel(String rel)
    {
      menuItem.setRel(rel);
      return this;
    }

    public Builder ignoreComponentNotFound(boolean ignoreComponentNotFound)
    {
      menuItem.setIgnoreComponentNotFound(ignoreComponentNotFound);
      return this;
    }

    public MatrixMenuItem build()
    {
      return menuItem;
    }
  }
}
