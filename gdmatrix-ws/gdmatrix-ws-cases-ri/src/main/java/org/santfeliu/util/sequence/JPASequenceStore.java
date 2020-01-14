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
package org.santfeliu.util.sequence;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * This class at the moment depends on the orm.xml file, so it is dependent on 
 * the ws module that contains the file. The namedQueries it should be changed 
 * by normal queries to make it a really common class for other services.
 * 
 * @author blanquepa
 */
public class JPASequenceStore implements SequenceStore
{
  protected EntityManager em;

  public JPASequenceStore(EntityManager em)
  {
    this.em = em;
  }

  @Override
  public Sequence loadSequence(String counter)
  {
    Sequence seq = incrementSequence(counter);

    return seq;
  }

  protected Sequence incrementSequence(String counter)
  {
    Sequence seq = null;

    Query query = em.createNamedQuery("incrementSequence");
    query.setParameter("counter", counter);
    int updated = query.executeUpdate();

    if (updated == 1)
    {
      query = em.createNamedQuery("readSequence");
      query.setParameter("counter", counter);
      seq = (Sequence)query.getSingleResult();
    }

    return seq;
  }

  @Override
  public Sequence createSequence(String counter, String value)
  {
    Sequence seq = new Sequence(counter, value);
    em.persist(seq);

    return seq;
  }

  @Override
  public Sequence changeSequence(String counter, String value)
  {
    Sequence seq = new Sequence(counter, value);
    return em.merge(seq);
  }
  
}
