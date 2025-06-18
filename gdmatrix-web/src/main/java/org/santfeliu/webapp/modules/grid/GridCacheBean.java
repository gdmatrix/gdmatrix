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
package org.santfeliu.webapp.modules.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import org.matrix.news.NewDocument;
import org.matrix.news.NewView;
import org.matrix.news.SectionFilter;
import org.matrix.news.SectionView;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.modules.geo.io.MapFilter;
import org.santfeliu.webapp.modules.geo.io.MapGroup;
import org.santfeliu.webapp.modules.geo.io.MapStore;
import org.santfeliu.webapp.modules.geo.io.MapView;
import org.santfeliu.webapp.modules.news.NewsModuleBean;

/**
 *
 * @author realor
 */
@ApplicationScoped
public class GridCacheBean
{
  final List<Card> cards = Collections.synchronizedList(new ArrayList<>());

  public List<Card> getCards() throws Exception
  {
    if (cards.isEmpty())
    {
      loadCards();
    }
    return cards;
  }

  public void loadCards() throws Exception
  {
    List<Card> newCards = loadNewsCards();
    List<Card> mapCards = loadMapCards();
    synchronized (cards)
    {
      cards.addAll(newCards);
      cards.addAll(mapCards);
      cards.sort((card1, card2) -> card1.getPriority() - card2.getPriority());
    }
  }

  private List<Card> loadNewsCards() throws Exception
  {
    List<Card> newCards = new ArrayList<>();
    SectionFilter filter = new SectionFilter();
    filter.setStartDateTime(TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));
    filter.getSectionId().add("1467");
    filter.getExcludeDrafts().add(Boolean.TRUE);
    filter.setMaxResults(100);

    List<SectionView> sectionViews =
      NewsModuleBean.getPort(true).findNewsBySection(filter);
    int priority = 0;
    for (SectionView sectionView : sectionViews)
    {
      List<NewView> newViews = sectionView.getNewView();
      for (NewView newView : newViews)
      {
        NewDocument newDocument = getNewImage(newView.getNewDocument());
        if (newDocument != null)
        {
          Card card = new Card();
          card.setTitle(newView.getHeadline());
          card.setType("New");
          card.setId(newView.getNewId());
          card.setPriority(priority++);
          card.setImageId(newDocument.getContentId());
          card.setAspect(getNewAspect(newDocument));
          newCards.add(card);
        }
      }
    }
    return newCards;
  }

  private NewDocument getNewImage(List<NewDocument> documents)
  {
    for (NewDocument document : documents)
    {
      if (document.getMimeType().startsWith("image/"))
      {
        return document;
      }
    }
    return null;
  }

  private String getNewAspect(NewDocument newDocument)
  {
    int aspect;
    String newDocTypeId = newDocument.getNewDocTypeId();
    if ("sf:NewDocumentCarouselAndDetailsImage".equals(newDocTypeId))
    {
      aspect = (int)(Math.random() * 6 + 5);
    }
    else if ("sf:NewDocumentListImage".equals(newDocTypeId))
    {
      aspect = (int)(Math.random() * 8 + 3);
    }
    else
    {
      aspect = (int)(Math.random() * 10 + 1);
    }
    return String.valueOf(aspect);
  }

  private List<Card> loadMapCards() throws Exception
  {
    String adminUserId = MatrixConfig.getProperty("adminCredentials.userId");
    String adminPassword = MatrixConfig.getProperty("adminCredentials.password");

    MapStore mapStore = CDI.current().select(MapStore.class).get();
    mapStore.setCredentials(adminUserId, adminPassword);
    MapFilter mapFilter = new MapFilter();
    mapFilter.setMapClass(1); // featured
    MapGroup mapGroup = mapStore.findMaps(mapFilter, 10, 10);
    List<Card> mapCards = new ArrayList<>();
    exploreMapGroup(mapGroup, mapCards);
    return mapCards;
  }

  private void exploreMapGroup(MapGroup mapGroup, List<Card> mapCards)
  {
    List<MapView> mapViews = mapGroup.getMapViews();
    for (MapView mapView : mapViews)
    {
      Card card = new Card();
      card.setId(mapView.getMapName());
      card.setTitle(mapView.getTitle());
      card.setPriority((int)(Math.random() * 10));
      card.setType("Map");
      card.setAspect("8");
      card.setImageId(mapView.getSnapshotContentId());
      mapCards.add(card);
    }
    for (MapGroup childMapGroup : mapGroup.getMapGroups())
    {
      exploreMapGroup(childMapGroup, mapCards);
    }
  }
}
