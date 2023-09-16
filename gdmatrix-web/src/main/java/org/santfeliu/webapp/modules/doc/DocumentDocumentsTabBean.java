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
package org.santfeliu.webapp.modules.doc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import static org.santfeliu.webapp.modules.doc.DocumentDocumentsTabBean.RelatedDocumentEdit.CHANGED;
import static org.santfeliu.webapp.modules.doc.DocumentDocumentsTabBean.RelatedDocumentEdit.REMOVED;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class DocumentDocumentsTabBean extends TabBean
{
  private int firstRow;
  private RelatedDocumentEdit editing;
  private List<RelatedDocumentEdit> rows = new ArrayList<>();

  @Inject
  DocumentObjectBean documentObjectBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return documentObjectBean;
  }

  public List<RelatedDocumentEdit> getRows()
  {
    return rows;
  }

  public void setRows(List<RelatedDocumentEdit> rows)
  {
    this.rows = rows;
  }

  public RelatedDocumentEdit getEditing()
  {
    return editing;
  }

  public void setEditing(RelatedDocumentEdit edit)
  {
    this.editing = edit;
  }

  public String getDocId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.relatedDocument.getDocId();
  }

  public void setDocId(String docId)
  {
    if (editing != null)
    {
      editing.relatedDocument.setDocId(docId);
    }
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  @Override
  public boolean isModified()
  {
    return true;
  }

  @Override
  public void load()
  {
    System.out.println("load relatedDocuments:" + getObjectId());
    rows.clear();

    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      List<RelatedDocument> relatedDocuments =
        documentObjectBean.getDocument().getRelatedDocument();
      for (RelatedDocument relatedDocument : relatedDocuments)
      {
        rows.add(new RelatedDocumentEdit(relatedDocument));
      }
    }
  }

  @Override
  public void store()
  {
    load();
  }

  public void accept()
  {
    if (editing == null) return;

    int index = rows.indexOf(editing);
    if (index == -1) // new edit
    {
      index = indexOfRelatedDocument(editing.getRelatedDocument());
      if (index == -1)
      {
        rows.add(editing);
      }
      else
      {
        rows.set(index, editing);
      }
    }
    editing.setState(CHANGED);
    editing = null;

    syncRows();
  }

  public void cancel()
  {
    editing = null;
  }

  public void create()
  {
    editing = new RelatedDocumentEdit(new RelatedDocument());
  }

  public void edit(RelatedDocumentEdit edit)
  {
    if (edit != null)
    {
      editing = edit;
    }
    else
    {
      create();
    }
  }

  public void remove(RelatedDocumentEdit edit)
  {
    if (edit != null)
    {
      edit.setState(REMOVED);
      syncRows();
    }
  }

  public RelationType[] getRelationTypes()
  {
    return RelationType.class.getEnumConstants();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (RelatedDocumentEdit)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  protected int indexOfRelatedDocument(RelatedDocument relatedDocument)
  {
    for (int i = 0; i < rows.size(); i++)
    {
      RelatedDocument other = rows.get(i).getRelatedDocument();
      if (other.getRelationType().equals(relatedDocument.getRelationType()) &&
          other.getName().equals(relatedDocument.getName()))
        return i;
    }
    return -1;
  }

  protected void syncRows()
  {
    List<RelatedDocument> relatedDocuments =
      documentObjectBean.getDocument().getRelatedDocument();
    relatedDocuments.clear();

    for (RelatedDocumentEdit edit : rows)
    {
      if (edit.getState() != REMOVED)
      {
        relatedDocuments.add(edit.getRelatedDocument());
      }
    }
  }

  public class RelatedDocumentEdit implements Serializable
  {
    public static final int UNCHANGED = 0;
    public static final int CHANGED = 1;
    public static final int REMOVED = 2;

    int state = UNCHANGED;
    RelatedDocument relatedDocument;

    public RelatedDocumentEdit(RelatedDocument relatedDocument)
    {
      this.relatedDocument = relatedDocument;
    }

    public int getState()
    {
      return state;
    }

    public void setState(int state)
    {
      this.state = state;
    }

    public RelatedDocument getRelatedDocument()
    {
      return relatedDocument;
    }

    public void setRelatedDocument(RelatedDocument relatedDocument)
    {
      this.relatedDocument = relatedDocument;
    }
  }

}
