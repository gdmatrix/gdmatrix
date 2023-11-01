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
package org.santfeliu.faces;

import java.util.Iterator;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class MatrixExceptionHandlerFactory extends ExceptionHandlerFactory
{
  public static final String UNHANDLED_ERROR_PAGE =
    "/common/util/error.xhtml";
  private final ExceptionHandlerFactory parent;

  public MatrixExceptionHandlerFactory(ExceptionHandlerFactory parent)
  {
    this.parent = parent;
  }

  @Override
  public ExceptionHandler getExceptionHandler()
  {
    return new Handler(parent.getExceptionHandler());
  }


  public class Handler extends ExceptionHandlerWrapper
  {
    private final ExceptionHandler wrapped;

    public Handler(ExceptionHandler wrapped)
    {
      this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped()
    {
      return wrapped;
    }

    @Override
    public void handle() throws FacesException
    {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Iterable<ExceptionQueuedEvent> queue = getUnhandledExceptionQueuedEvents();
      Iterator<ExceptionQueuedEvent> iter = queue.iterator();
      while (iter.hasNext())
      {
        try
        {
          UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
          ExceptionQueuedEvent event = iter.next();
          ExceptionQueuedEventContext context =
            (ExceptionQueuedEventContext) event.getSource();

          Throwable throwable = context.getException();
          userSessionBean.setUnhandledError(throwable);

          throwable.printStackTrace();

          facesContext.getExternalContext().redirect(UNHANDLED_ERROR_PAGE);
        }
        catch (Exception ex)
        {
        }
        finally
        {
          iter.remove();
        }
      }
    }
  }
}
