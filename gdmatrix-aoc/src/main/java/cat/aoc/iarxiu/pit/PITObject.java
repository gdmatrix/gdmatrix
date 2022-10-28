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
package cat.aoc.iarxiu.pit;

import java.util.List;
import cat.aoc.iarxiu.mets.Div;
import cat.aoc.iarxiu.mets.Metadata;

/**
 *
 * @author blanquepa
 */
public abstract class PITObject
{
  protected PIT pit;
  protected Div div;

  protected Div getDiv()
  {
    return div;
  }

  protected void setDiv(Div div)
  {
    this.div = div;
  }

  protected void setPIT(PIT pit)
  {
    this.pit = pit;
  }

  protected PIT getPIT()
  {
    return pit;
  }

  protected Metadata newDmdMetadata(String type, String urn) throws Exception
  {
    int count = getDmdMetadatas().size();
    String id = div.getLabel() + "_DMD_" + String.valueOf(count);
    Metadata metadata = pit.getMets().newDmdMetadata(id, type, urn);
    div.getDmdMetadatas().add(metadata);

    return metadata;
  }

  protected List<Metadata> getDmdMetadatas()
  {
    return div.getDmdMetadatas();
  }

  protected Metadata newAmdMetadata(String type, String urn) throws Exception
  {
    int count = getAmdMetadatas().size();
    String id = div.getLabel() + "_AMD_" + String.valueOf(count);
    Metadata metadata = pit.getMets().newAmdMetadata(id, type, urn);
    div.getAmdMetadatas().add(metadata);

    return metadata;
  }

  protected List<Metadata> getAmdMetadatas()
  {
    return div.getAmdMetadatas();
  }
}
