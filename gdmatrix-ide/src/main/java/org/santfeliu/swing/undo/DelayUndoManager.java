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
package org.santfeliu.swing.undo;

import javax.swing.*;
import javax.swing.undo.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author realor
 */
public class DelayUndoManager extends UndoManager
{
  private static final long serialVersionUID = -2564589027391767863L;

  private int delay = 300;
  private CompoundEdit buffer;
  protected Timer timer;

  public DelayUndoManager()
  {
  }

  public DelayUndoManager(int delay)
  {
    this.delay = delay;
  }

  public synchronized void flushBuffer()
  {
    if (buffer != null)
    {
      buffer.end();
      DelayUndoManager.super.addEdit(buffer);
      buffer = null;
      onNewEdit();
    }
  }

  public synchronized void discardBuffer()
  {
    buffer = null;
    if (timer != null)
    {
      timer.stop();
      timer = null;
    }
  }

  public void onNewEdit()
  {
  }

  @Override
  public synchronized boolean addEdit(UndoableEdit anEdit)
  {
    if (buffer == null)
    {
      buffer = new CompoundEdit();
    }
    boolean inProgress = buffer.addEdit(anEdit);
    if (timer == null)
    {
      timer = new Timer(delay, (ActionEvent e) ->
      {
        flushBuffer();
      });
      timer.setRepeats(false);
      timer.start();
    }
    else
    {
      timer.restart();
    }
    return inProgress;
  }

  @Override
  public synchronized boolean canUndo()
  {
    flushBuffer();
    return super.canUndo();
  }

  @Override
  public synchronized boolean canRedo()
  {
    flushBuffer();
    return super.canRedo();
  }

  @Override
  public synchronized void undo() throws CannotUndoException
  {
    flushBuffer();
    super.undo();
  }

  @Override
  public synchronized void redo() throws CannotRedoException
  {
    flushBuffer();
    super.redo();
  }

  @Override
  public synchronized void discardAllEdits()
  {
    super.discardAllEdits();
    discardBuffer();
  }
}
