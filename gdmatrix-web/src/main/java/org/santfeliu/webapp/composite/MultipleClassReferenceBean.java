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
package org.santfeliu.webapp.composite;

import java.util.ArrayList;
import java.util.List;
import javax.el.ValueExpression;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.el.CompositeComponentExpressionHolder;
import javax.inject.Named;
import static org.matrix.dic.DictionaryConstants.CLASS_TYPE;
import org.primefaces.event.SelectEvent;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.modules.classif.ClassTypeBean;
import org.santfeliu.webapp.util.WebUtils;
import org.matrix.classif.Class;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class MultipleClassReferenceBean
{
  public List<String> complete(String query)
  {
    ClassTypeBean typeBean = (ClassTypeBean)TypeBean.getInstance(CLASS_TYPE);

    List<Class> classList = typeBean.findByQuery(query);
    List<String> classIdList = new ArrayList<>();
    for (Class classObject : classList)
    {
      classIdList.add(classObject.getClassId());
    }
    return classIdList;
  }

  public void setClassIdList(List<String> classIdList)
  {
    List<String> currentClassIdList = getClassIdList();
    currentClassIdList.clear();
    currentClassIdList.addAll(classIdList);
  }

  public List<String> getClassIdList()
  {
    return WebUtils.getValue("#{cc.attrs.value}");
  }

  public void onItemSelect(SelectEvent event)
  {
    String classId = (String)event.getObject();
    WebUtils.setValue("#{cc.attrs.newClassId}", String.class, classId);
  }

  public String find()
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.find(CLASS_TYPE,
      getNewClassIdExpression().getExpressionString());
  }

  public String show()
  {
    List<String> classIdList = getClassIdList();
    String classId = classIdList.isEmpty() ? NEW_OBJECT_ID : classIdList.get(0);

    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.show(CLASS_TYPE, classId);
  }

  public ValueExpression getNewClassIdExpression()
  {
    CompositeComponentExpressionHolder exprHolder =
      (CompositeComponentExpressionHolder)WebUtils.getValue("#{cc.attrs}");

    return exprHolder.getExpression("newClassId");
  }

}
