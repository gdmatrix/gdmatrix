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
package org.santfeliu.webapp.modules.ide.visualEditor;

import java.util.ArrayList;
import java.util.List;
import org.santfeliu.form.type.html.HtmlViewWrapper;

/**
 * 
 * DTO that groups a form element with its associated label for visual rendering 
 * in the editor canvas.
 * 
 * A block holds the element and (optionally) its label, remebers whether label
 * comes first, and for fieldsets the list of childs blocks inside of it.
 */
  public class VisualCanvasBlock
  {
    
    private HtmlViewWrapper label;
    private HtmlViewWrapper element;
    private boolean labelFirst = true;
    private List<VisualCanvasBlock> childrenBlocks = new ArrayList();

    public VisualCanvasBlock(HtmlViewWrapper label, HtmlViewWrapper element)
    {
      this.label = label;
      this.element = element;
    }

    public HtmlViewWrapper getLabel()
    {
      return this.label;
    }

    public HtmlViewWrapper getElement()
    {
      return this.element;
    }

    public List<VisualCanvasBlock> getChildrenBlocks()
    {
      return childrenBlocks;
    }

    public void setChildrenBlocks(List<VisualCanvasBlock> childrenBlocks)
    {
      this.childrenBlocks = childrenBlocks;
    }

    public boolean isLabelFirst()
    {
      return labelFirst;
    }

    public void setLabelFirst(boolean labelFirst)
    {
      this.labelFirst = labelFirst;
    }
  }
