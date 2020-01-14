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
package org.santfeliu.elections;

import java.util.Collections;
import java.awt.Color;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.matrix.elections.District;
import org.matrix.elections.ElectionsManagerPort;
import org.matrix.elections.ElectionsResult;
import org.matrix.elections.PoliticalParty;

import org.santfeliu.ws.WSPortFactory;


/**
 *
 * @author unknown
 */
public class Results
{
  private String wsdlLocation;
  private Calendar date;
  private String callId;
  private String currentDistrict;
  private String currentSection;
  private String currentBoardName;
  private final TreeMap<String, String> districts = new TreeMap<String, String>();
  private final TreeMap<String, Board> boards = new TreeMap();
  private final TreeMap<String, Party> parties = new TreeMap<String, Party>();
  private int councillorsCount;
  private double minPercentage = 5.0;
  private ArrayList<Councillor> electedCouncillors;
  private HashMap<String, PartyStats> partyStats;


  public void loadData(Calendar date, String callId) throws Exception
  {
    this.date = date;
    this.callId = callId;

    ElectionsManagerPort port =
      WSPortFactory.getPort(ElectionsManagerPort.class, wsdlLocation);

    districts.clear();

    DatatypeFactory factory = DatatypeFactory.newInstance();
    XMLGregorianCalendar xmlDate =
      factory.newXMLGregorianCalendar((GregorianCalendar)date);

    //Districts
    List<District> districtList = port.listDistricts(xmlDate);
    for (District district : districtList)
    {
      districts.put(district.getDistrictId(), district.getDescription());
    }

    //Parties
    List<PoliticalParty> partyList =
      port.listPoliticalParties(xmlDate, callId);
    int i = 0;
    for (PoliticalParty wsParty : partyList)
    {
      Party appParty = new Party();
      appParty.setId(wsParty.getPartyId());
      appParty.setAbbreviation(wsParty.getAbbreviation().trim());
      appParty.setDescription(wsParty.getDescription().trim());
      String rgbString = wsParty.getColor();
      if (rgbString != null)
      {
        int rgb = Integer.parseInt(rgbString, 16);
        Color color = new Color(rgb & 0x00FFFFFF);
        appParty.setColor(color);
      }
      String partyImage = wsParty.getImage();
      if (partyImage != null)
      {
        appParty.setLogo(partyImage.trim());
      }
      appParty.setOrder(i + 1);
      parties.put(wsParty.getPartyId(), appParty);
      i++;
    }

    //Councillors
    List<org.matrix.elections.Councillor> councillorList =
      port.listCouncillors(xmlDate, callId);
    if (councillorList.size() > 0)
    {
      for (org.matrix.elections.Councillor councillor : councillorList)
      {
        Party appParty = getParty(councillor.getPartyId());
        appParty.addCouncillor(councillor.getName(), councillor.getImageURL());
      }
    }
    else // unknow councillors, create fakes
    {
      for (Party party : parties.values())
      {
        for (int c = 1; c <= councillorsCount; c++)
        {
          party.addCouncillor("R" + c, null);
        }
      }
    }

    //Boards
    List<org.matrix.elections.Board> boardList =
      port.listBoards(xmlDate, callId);
    for (org.matrix.elections.Board wsBoard : boardList)
    {
      Board appBoard = new Board();
      appBoard.setDescription(wsBoard.getDescription());
      appBoard.setDistrict(wsBoard.getDistrictId());
      appBoard.setSection(wsBoard.getSectionId().trim());
      appBoard.setBoardName(wsBoard.getBoardId().trim());
      appBoard.setVotes(wsBoard.getElectors(), wsBoard.getTotalVotes(),
        wsBoard.getBlankVotes(), wsBoard.getNullVotes());
      boards.put(appBoard.getId(), appBoard);
    }

    //Results
    List<ElectionsResult> resultList = port.listResults(xmlDate, callId);
    for (ElectionsResult wsResult : resultList)
    {
      String partyId = wsResult.getPartyId();
      String district = wsResult.getDistrictId();
      String section = wsResult.getSectionId().trim();
      String boardName = wsResult.getBoardId().trim();
      int result = wsResult.getVotes();

      Board board = getBoard(district, section, boardName);
      board.setVotes(partyId, result);
    }

    if ("1".equals(callId))
    {
      calculateCouncillors();
    }
  }

  public void randomData(Calendar date, String callid,
    int partyCount, int councillorsCount, int districtCount,
    int sectionsPerDistrict, int boardsPerSection)
  {
    districts.clear();
    for (int d = 1; d < districtCount; d++)
    {
      districts.put(String.valueOf(d), "D-" + d);
    }
    this.councillorsCount = councillorsCount;
    for (int p = 1; p <= partyCount; p++)
    {
      Party party = new Party();
      party.setId(String.valueOf(p));
      party.setAbbreviation("P-" + p);
      party.setDescription("Party number " + p);
      party.setOrder(p);
      parties.put(party.getId(), party);
      for (int i = 1; i <= councillorsCount; i++)
      {
        String name = String.valueOf("Councillor-" + i);
        String image = String.valueOf("Photo-" + i);
        party.addCouncillor(name, image);
      }
    }

    int j = 0;
    for (int d = 1; d <= districtCount; d++)
    {
      for (int s = 1; s <= sectionsPerDistrict; s++)
      {
        for (int b = 1; b <= boardsPerSection; b++)
        {
          j++;
          Board board = new Board();
          board.setDescription("Board-" + j);
          board.setDistrict(String.valueOf(String.valueOf(d)));
          board.setSection(String.valueOf(String.valueOf(s)));
          board.setBoardName(String.valueOf(String.valueOf(b)));

          int electors = (int)(Math.random() * 1000);
          int totalVotes = (int)(Math.random() * (1 + electors));
          int blankVotes = (int)(Math.random() * (1 + totalVotes / 50));
          int invalidVotes = (int)(Math.random() *
            (1 + (totalVotes - blankVotes) / 100));

          board.setVotes(electors, totalVotes, blankVotes, invalidVotes);
          boards.put(board.getId(), board);

          int validVotes = totalVotes - blankVotes - invalidVotes;
          for (int p = 1; p < partyCount; p++)
          {
            String partyId = String.valueOf(p);
            int result = (int)(Math.random() * (validVotes + 1));
            validVotes -= result;
            board.setVotes(partyId, result);
          }
          board.setVotes(String.valueOf(partyCount), validVotes);
        }
      }
    }
    calculateCouncillors();
  }

  public Calendar getDate()
  {
    return date;
  }

  public String getCallId()
  {
    return callId;
  }

  public String getCurrentDistrict()
  {
    return currentDistrict;
  }

  public String getCurrentSection()
  {
    return currentSection;
  }

  public String getCurrentBoardName()
  {
    return currentBoardName;
  }

  public void setScope(String district,
                       String section,
                       String boardName)
  {
    this.currentDistrict = district;
    this.currentSection = section;
    this.currentBoardName = boardName;
  }

  public int getTotalBoardsCount()
  {
    return boards.size();
  }

  public int getScrutinizedBoardsCount()
  {
    Collection collection = boards.values();
    Iterator iter = collection.iterator();
    int count = 0;
    while (iter.hasNext())
    {
      Board board = (Board)iter.next();
      if (board.isScrutinized()) count++;
    }
    return count;
  }

  public Board getBoard(String district, String section, String boardName)
  {
    return (Board)boards.get(district + ":" + section + ":" + boardName);
  }

  public void setCouncillorsCount(int councillorsCount)
  {
    this.councillorsCount = councillorsCount;
  }

  public int getCouncillorsCount()
  {
    return councillorsCount;
  }

  public Collection<Board> getBoards()
  {
    return boards.values();
  }

  public Party getParty(String partyId)
  {
    return (Party)parties.get(partyId);
  }

  public String getDistrict(String districtId)
  {
    return (String)districts.get(districtId);
  }

  public Collection<String> getDistricts()
  {
    return districts.values();
  }

  public Collection<Party> getParties()
  {
    return parties.values();
  }

  public Collection<Party> getParties(Comparator comparator)
  {
    List<Party> list = new ArrayList<Party>(getParties());
    Collections.sort(list, comparator);
    return list;
  }

  public Collection<Party> getPartiesSortByVotes()
  {
    Comparator comparator = new Comparator()
    {
      @Override
      public int compare(Object o1, Object o2)
      {
        Party p1 = (Party)o1;
        Party p2 = (Party)o2;
        PartyStats stats1 = partyStats.get(p1.getId());
        PartyStats stats2 = partyStats.get(p2.getId());
        if (stats1 == null || stats2 == null) return 0;
        return stats2.totalVotes - stats1.totalVotes;
      }
    };
    return getParties(comparator);
  }

  public Collection<Party> getPartiesMatchedWith(Collection<Party> parties)
  {
    // match parties by color first, by votes second.
    final Map<Integer, Integer> colors = new HashMap<Integer, Integer>();
    int index = 0;
    for (Party party : parties)
    {
      Color color = party.getColor();
      if (color != null)
      {
        colors.put(color.getRGB(), index++);
      }
    }
    Comparator comparator = new Comparator()
    {
      @Override
      public int compare(Object o1, Object o2)
      {
        Party p1 = (Party)o1;
        Party p2 = (Party)o2;
        Color c1 = p1.getColor();
        Color c2 = p2.getColor();
        Integer w1 = c1 == null ? null : colors.get(c1.getRGB());
        Integer w2 = c2 == null ? null : colors.get(c2.getRGB());
        if (w1 == null && w2 != null)
        {
          return 1;
        }
        else if (w1 != null && w2 == null)
        {
          return -1;
        }
        else if (w1 != null && w2 != null && !w1.equals(w2))
        {
          return w1 - w2;
        }
        else // compare by votes
        {
          PartyStats stats1 = partyStats.get(p1.getId());
          PartyStats stats2 = partyStats.get(p2.getId());
          if (stats1 == null || stats2 == null) return 0;
          return stats2.totalVotes - stats1.totalVotes;
        }
      }
    };
    return getParties(comparator);
  }

  public int getElectors()
  {
    return sum(Board.ELECTORS, false, false);
  }

  public int getScrutinizedElectors()
  {
    return sum(Board.ELECTORS, false, true);
  }

  public int getVotes()
  {
    return sum(Board.TOTAL_VOTES, false);
  }

  public int getBlankVotes()
  {
    return sum(Board.BLANK_VOTES, false);
  }

  public int getInvalidVotes()
  {
    return sum(Board.INVALID_VOTES, false);
  }

  public int getValidVotes()
  {
    return getVotes() - getInvalidVotes();
  }

  public int getVotes(String party)
  {
    return sum(party, false);
  }

  public int getVotesPercentage(String party)
  {
    int validVotes = getValidVotes();
    return validVotes == 0 ? 0 : getVotes(party) / validVotes;
  }

  public int getBlankVotesPercentage()
  {
    int validVotes = getValidVotes();
    return validVotes == 0 ? 0 : getBlankVotes() / validVotes;
  }

  public double getParticipation()
  {
    int scrutinizedElectors = getScrutinizedElectors();
    return scrutinizedElectors == 0 ? -1 :
      (100.0 * getVotes()) / scrutinizedElectors;
  }

  public double getAbstention()
  {
    double participation = getParticipation();
    return participation == -1.0 ? -1.0 : 100.0 - participation;
  }

  // total methods

  public int getTotalElectors()
  {
    return sum(Board.ELECTORS, true, false);
  }

  public int getTotalScrutinizedElectors()
  {
    return sum(Board.ELECTORS, true, true);
  }

  public int getTotalVotes()
  {
    return sum(Board.TOTAL_VOTES, true);
  }

  public int getTotalBlankVotes()
  {
    return sum(Board.BLANK_VOTES, true);
  }

  public int getTotalInvalidVotes()
  {
    return sum(Board.INVALID_VOTES, true);
  }

  public int getTotalValidVotes()
  {
    return getTotalVotes() - getTotalInvalidVotes();
  }

  public int getTotalVotes(String party)
  {
    return sum(party, true);
  }

  public int getTotalVotesPercentage(String partyId)
  {
    return getTotalVotes(partyId) / getTotalValidVotes();
  }

  public Collection getElectedCouncillors()
  {
    return electedCouncillors;
  }

  public HashMap getPartyStats()
  {
    return partyStats;
  }

  // private methods

  private void calculateCouncillors()
  {
    electedCouncillors = new ArrayList<Councillor>();
    partyStats = new HashMap<String, PartyStats>();

    if (getScrutinizedBoardsCount() == 0) return;

    int validVotes = getTotalValidVotes();
    int allPartyVotes = validVotes - getBlankVotes();
    List<Councillor> councillors = new ArrayList<Councillor>();
    for (Party party : parties.values())
    {
      PartyStats stats = new PartyStats();
      partyStats.put(party.getId(), stats);

      int partyVotes = getTotalVotes(party.getId());
      stats.totalVotes = partyVotes;
      stats.votesPercentage = 100.0 * partyVotes / validVotes;
      if (stats.votesPercentage >= minPercentage)
      {
        for (int i = 1; i <= councillorsCount; i++)
        {
          Councillor councillor = party.getCouncillor(i);
          councillor.setScore((double)partyVotes / i);
          councillors.add(councillor);
        }
      }
    }
    
    if (councillors.isEmpty()) return;

    Comparator scoreComparator = new Comparator()
    {
      @Override
      public int compare(Object o1, Object o2)
      {
        Councillor c1 = (Councillor)o1;
        Councillor c2 = (Councillor)o2;
        Double s1 = c1.getScore();
        Double s2 = c2.getScore();
        if (s1.equals(s2))
        {
          Integer v1 = (partyStats.get(c1.getParty().getId())).getTotalVotes();
          Integer v2 = (partyStats.get(c2.getParty().getId())).getTotalVotes();
          return v2.compareTo(v1);
        }
        else
        {
          return s2.compareTo(s1);
        }
      }
    };

    // sort councillors by score
    Collections.sort(councillors, scoreComparator);

    // select elected councillors
    Councillor outCouncillor;
    if (councillors.size() == councillorsCount)
    {
      electedCouncillors.addAll(councillors);
      outCouncillor = null;
    }
    else // councillors.size() > councillorCount
    {
      electedCouncillors.addAll(councillors.subList(0, councillorsCount));
      outCouncillor = councillors.get(councillorsCount);
      for (int i = councillorsCount - 1; i >= 0; i--)
      {
        Councillor councillor = electedCouncillors.get(i);
        if (scoreComparator.compare(councillor, outCouncillor) == 0)
        {
          electedCouncillors.remove(i);
        }
        else break;
      }
    }

    // count elected councillors per party
    for (Councillor electedCouncillor : electedCouncillors)
    {
      PartyStats stats = partyStats.get(electedCouncillor.getParty().getId());
      stats.numElectedCouncillors++;
    }

    // set takesLastCouncillor in PartyStats
    Councillor lastCouncillor;
    int lastPartyVotes = 0;
    if (electedCouncillors.size() < councillorsCount && outCouncillor != null)
    {
      // draw
      lastCouncillor = outCouncillor;
    }
    else // normal case, not draw, electedCouncillors.size() == councillorsCount
    {
      lastCouncillor = electedCouncillors.get(councillorsCount - 1);
      String lastPartyId = lastCouncillor.getParty().getId();
      PartyStats lastParty = partyStats.get(lastPartyId);
      lastPartyVotes = lastParty.getTotalVotes();
      lastParty.takesLastCouncillor = true;
    }

    double lastCouncillorScore = lastCouncillor.getScore();
    
    for (Party party : parties.values())
    {
      PartyStats stats = partyStats.get(party.getId());
      int nc = stats.getNumElectedCouncillors();
      double partyVotes = stats.totalVotes;
      if (nc > 0)
      {
        stats.votesPerCouncillor = ((double)partyVotes) / nc;
      }
      if (allPartyVotes > 0)
      {
        stats.linearCouncillors =
          (double)(partyVotes * councillorsCount) / allPartyVotes;
      }

      int extraVotes;
      if (stats.getVotesPercentage() >= minPercentage)
      {
        extraVotes = (int)Math.ceil(lastCouncillorScore *
          (stats.getNumElectedCouncillors() + 1) - stats.getTotalVotes());
        if (extraVotes < 0) extraVotes = 0;
      }
      else
      {
        // minimalVotes to be over minPercentage
        int minimalVotes = (int)Math.ceil(validVotes * minPercentage / 100.0);
        int score = stats.getTotalVotes();
        if (minimalVotes < lastCouncillorScore)
        {
          extraVotes = (int)Math.ceil(lastCouncillorScore - score);
        }
        else // minimalScore >= lastCouncillorScore
        {
          extraVotes = (minimalVotes - score);
        }
      }
      
      double score = (double)(stats.getTotalVotes() + extraVotes) /
        (stats.getNumElectedCouncillors() + 1);
      if (score == lastCouncillorScore)
      {
        if (stats.getTotalVotes() + extraVotes < lastPartyVotes)
        {
          extraVotes++;
        }
      }
      stats.votesToNextCouncillor = extraVotes;
    }
  }

  private int sum(int column, boolean total)
  {
    return sum(column, total, false);
  }

  private int sum(int column, boolean total, boolean scrutinized)
  {
    int sum = 0;
    for (Board board : boards.values())
    {
      if (total ||
        match(currentDistrict, board.getDistrict()) &&
        match(currentSection, board.getSection()) &&
        match(currentBoardName, board.getBoardName()))
      {
        if (!scrutinized || board.isScrutinized())
        {
          sum += board.getVotes(column);
        }
      }
    }
    return sum;
  }

  private int sum(String partyId, boolean total)
  {
    int sum = 0;
    for (Board board : boards.values())
    {
      if (total ||
        match(currentDistrict, board.getDistrict()) &&
        match(currentSection, board.getSection()) &&
        match(currentBoardName, board.getBoardName()))
      {
        if (board.isScrutinized())
        {
          sum += board.getVotes(partyId);
        }
      }
    }
    return sum;
  }

  private boolean match(String s1, String s2)
  {
    return s1 == null || s1.equals(s2);
  }

  public void setWsdlLocation(String wsdlLocation)
  {
    this.wsdlLocation = wsdlLocation;
  }

  public String getWsdlLocation()
  {
    return wsdlLocation;
  }

  public class PartyStats
  {
    private int totalVotes;
    private int numElectedCouncillors;
    private int votesToNextCouncillor;
    double votesPercentage;
    double votesPerCouncillor;
    double linearCouncillors;
    boolean takesLastCouncillor;

    public int getTotalVotes()
    {
      return totalVotes;
    }

    public int getNumElectedCouncillors()
    {
      return numElectedCouncillors;
    }

    public int getVotesToNextCouncillor()
    {
      return votesToNextCouncillor;
    }

    public double getVotesPerCouncillor()
    {
      return votesPerCouncillor;
    }

    public double getVotesPercentage()
    {
      return votesPercentage;
    }

    public double getLinearCouncillors()
    {
      return linearCouncillors;
    }

    public double getLastCouncillorScore()
    {
      if (numElectedCouncillors == 0) return 0;
      return (double)totalVotes / numElectedCouncillors;
    }

    public double getNextCouncillorScore()
    {
      return (double)totalVotes / (numElectedCouncillors + 1);
    }

    public boolean isTakesLastCouncillor()
    {
      return takesLastCouncillor;
    }

    @Override
    public String toString()
    {
      DecimalFormat df = new DecimalFormat("##0.00");
      return numElectedCouncillors + " / " + totalVotes + " / " +
        df.format(votesPercentage) + "% / " + votesPerCouncillor;
    }
  }

  public static void main(String[] args)
  {
    try
    {
      Calendar calendar = Calendar.getInstance();
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
      Date date = df.parse("20190526");
      calendar.setTime(date);
      Results results = new Results();
      results.setCouncillorsCount(21);
      results.setWsdlLocation("http://localhost/services/elections");
      results.loadData(calendar, "1");

      System.out.println("Electors: " + results.getElectors());
      System.out.println("Scrut. boards: " + results.getScrutinizedBoardsCount());
      System.out.println("Total votes: " + results.getTotalVotes());
      System.out.println("Participation: " + results.getParticipation());
      Iterator iter = results.getParties().iterator();
      while (iter.hasNext())
      {
        Party party = (Party)iter.next();
        int votes = results.getVotes(party.getId());
        System.out.println(party.getAbbreviation() + ": " +
          party.getDescription() + " : " + votes);
      }
      System.out.println(results.getPartyStats());
      System.out.println("Elected councillors: " + results.getElectedCouncillors());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
