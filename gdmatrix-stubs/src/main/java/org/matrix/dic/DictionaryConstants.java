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
package org.matrix.dic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventDocument;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventTheme;
import org.matrix.agenda.Theme;
import org.matrix.cases.Case;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CasePerson;
import org.matrix.cases.Demand;
import org.matrix.cases.Intervention;
import org.matrix.cases.Problem;
import org.matrix.cms.Node;
import org.matrix.cms.Workspace;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.edu.Course;
import org.matrix.edu.Inscription;
import org.matrix.edu.School;
import org.matrix.elections.Board;
import org.matrix.elections.Call;
import org.matrix.elections.Councillor;
import org.matrix.elections.District;
import org.matrix.elections.PoliticalParty;
import org.matrix.forum.Answer;
import org.matrix.forum.Forum;
import org.matrix.forum.Question;
import org.matrix.kernel.Address;
import org.matrix.kernel.City;
import org.matrix.kernel.Contact;
import org.matrix.kernel.Country;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonRepresentant;
import org.matrix.kernel.PersonDocument;
import org.matrix.kernel.PersonPerson;
import org.matrix.kernel.Province;
import org.matrix.kernel.Room;
import org.matrix.kernel.Street;
import org.matrix.policy.*;
import org.matrix.news.New;
import org.matrix.news.NewDocument;
import org.matrix.news.NewSection;
import org.matrix.news.Source;
import org.matrix.report.Report;
import org.matrix.security.Role;
import org.matrix.security.RoleInRole;
import org.matrix.security.User;
import org.matrix.security.UserInRole;
import org.matrix.signature.SignedDocument;
import org.matrix.translation.Translation;
import org.matrix.workflow.Variable;

/**
 *
 * @author realor
 */
public class DictionaryConstants
{
  public static final String DIC_ADMIN_ROLE = "DIC_ADMIN";
  public static final String TYPE_PATH_SEPARATOR = "/";
  public static final String CREATE_ACTION = "Create";
  public static final String READ_ACTION = "Read";
  public static final String WRITE_ACTION = "Write";
  public static final String DELETE_ACTION = "Delete";
  public static final String PRINT_ACTION = "Print";
  public static final String DERIVE_DEFINITION_ACTION = "DeriveDefinition";
  public static final String MODIFY_DEFINITION_ACTION = "ModifyDefinition";
  public static final String EXECUTE_ACTION = "Execute";

  /* agenda module */
  public static final String EVENT_TYPE =
    Event.class.getSimpleName();
  public static final String ATTENDANT_TYPE =
    Attendant.class.getSimpleName();
  public static final String EVENT_PLACE_TYPE =
    EventPlace.class.getSimpleName();
  public static final String THEME_TYPE =
    Theme.class.getSimpleName();
  public static final String EVENT_THEME_TYPE =
    EventTheme.class.getSimpleName();
  public static final String EVENT_DOCUMENT_TYPE =
    EventDocument.class.getSimpleName();

  /* cases module */
  public static final String CASE_TYPE =
    Case.class.getSimpleName();
  public static final String CASE_PERSON_TYPE =
    CasePerson.class.getSimpleName();
  public static final String CASE_ADDRESS_TYPE =
    CaseAddress.class.getSimpleName();
  public static final String CASE_DOCUMENT_TYPE =
    CaseDocument.class.getSimpleName();
  public static final String CASE_EVENT_TYPE =
    CaseEvent.class.getSimpleName();
  public static final String CASE_CASE_TYPE =
    CaseCase.class.getSimpleName();
  public static final String DEMAND_TYPE =
    Demand.class.getSimpleName();
  public static final String PROBLEM_TYPE =
    Problem.class.getSimpleName();
  public static final String INTERVENTION_TYPE =
    Intervention.class.getSimpleName();

  /* classif module */
  public static final String CLASS_TYPE =
    org.matrix.classif.Class.class.getSimpleName();

  /* dic module */
  public static final String TYPE_TYPE =
    Type.class.getSimpleName();
  public static final String ENUM_TYPE_TYPE =
    EnumType.class.getSimpleName();
  public static final String ENUM_TYPE_ITEM_TYPE =
    EnumTypeItem.class.getSimpleName();

  /* policy module */
  public static final String POLICY_TYPE =
    Policy.class.getSimpleName();
  public static final String CLASS_POLICY_TYPE =
    ClassPolicy.class.getSimpleName();
  public static final String CASE_POLICY_TYPE =
    CasePolicy.class.getSimpleName();
  public static final String DOCUMENT_POLICY_TYPE =
    DocumentPolicy.class.getSimpleName();
  public static final String DISPOSAL_HOLD_TYPE =
    DisposalHold.class.getSimpleName();

  /* doc module */
  public static final String DOCUMENT_TYPE =
    Document.class.getSimpleName();
  public static final String CONTENT_TYPE =
    Content.class.getSimpleName();

  /* edu module */
  public static final String SCHOOL_TYPE =
    School.class.getSimpleName();
  public static final String COURSE_TYPE =
    Course.class.getSimpleName();
  public static final String INSCRIPTION_TYPE =
    Inscription.class.getSimpleName();

  /* elections module */
  public static final String CALL_TYPE =
    Call.class.getSimpleName();
  public static final String DISTRICT_TYPE =
    District.class.getSimpleName();
  public static final String BOARD_TYPE =
    Board.class.getSimpleName();
  public static final String COUNCILLOR_TYPE =
    Councillor.class.getSimpleName();
  public static final String POLITICAL_PARTY_TYPE =
    PoliticalParty.class.getSimpleName();

  /* cms module */
  public static final String NODE_TYPE =
    Node.class.getSimpleName();
  public static final String WORKSPACE_TYPE =
    Workspace.class.getSimpleName();

  /* forum module */
  public static final String FORUM_TYPE =
    Forum.class.getSimpleName();
  public static final String QUESTION_TYPE =
    Question.class.getSimpleName();
  public static final String FORUM_ANSWER_TYPE =
    Answer.class.getSimpleName();

  /* kernel module */
  public static final String PERSON_TYPE =
    Person.class.getSimpleName();
  public static final String COUNTRY_TYPE =
    Country.class.getSimpleName();
  public static final String PROVINCE_TYPE =
    Province.class.getSimpleName();
  public static final String CITY_TYPE =
    City.class.getSimpleName();
  public static final String STREET_TYPE =
    Street.class.getSimpleName();
  public static final String ADDRESS_TYPE =
    Address.class.getSimpleName();
  public static final String ROOM_TYPE =
    Room.class.getSimpleName();
  public static final String CONTACT_TYPE =
    Contact.class.getSimpleName();
  public static final String PERSON_ADDRESS_TYPE =
    PersonAddress.class.getSimpleName();
  public static final String PERSON_REPRESENTANT_TYPE =
    PersonRepresentant.class.getSimpleName();
  public static final String PERSON_DOCUMENT_TYPE =
    PersonDocument.class.getSimpleName();
  public static final String PERSON_PERSON_TYPE =
    PersonPerson.class.getSimpleName();


  /* news module */
  public static final String NEW_TYPE =
    New.class.getSimpleName();
  public static final String NEW_SECTION =
    NewSection.class.getSimpleName();
  public static final String NEW_DOCUMENT_TYPE =
    NewDocument.class.getSimpleName();
  public static final String SOURCE_TYPE =
    Source.class.getSimpleName();

  /* report module */
  public static final String REPORT_TYPE =
    Report.class.getSimpleName();

  /* request module */

  /* search module */

  /* security module */
  public static final String USER_TYPE =
    User.class.getSimpleName();
  public static final String ROLE_TYPE =
    Role.class.getSimpleName();
  public static final String USER_IN_ROLE_TYPE =
    UserInRole.class.getSimpleName();
  public static final String ROLE_IN_ROLE_TYPE =
    RoleInRole.class.getSimpleName();

  /* signature module */
  public static final String SIGNED_DOCUMENT_TYPE =
    SignedDocument.class.getSimpleName();

  /* sql module */

  /* survey module */
  // (duplicated with forum)
  //public static final String SURVEY_ANSWER_TYPE =
  //  org.matrix.survey.Answer.class.getSimpleName();

  /* task module */

  /* translation module */
  public static final String TRANSLATION_TYPE =
    Translation.class.getSimpleName();

  /* translation module */
  public static final String VARIABLE_TYPE =
    Variable.class.getSimpleName();

  public static final HashMap<String, Class> rootTypeClasses = new HashMap();
  public static final HashSet<String> rootTypeIds = new HashSet();
  public static final HashSet<String> derivableTypeIds = new HashSet();
  public static final ArrayList<String> standardActions = new ArrayList();

  static
  {
    // all root types + module
    rootTypeClasses.put(EVENT_TYPE, Event.class);
    rootTypeClasses.put(ATTENDANT_TYPE, Attendant.class);
    rootTypeClasses.put(EVENT_PLACE_TYPE, EventPlace.class);
    rootTypeClasses.put(EVENT_DOCUMENT_TYPE, EventDocument.class);
    rootTypeClasses.put(THEME_TYPE, Theme.class);
    rootTypeClasses.put(EVENT_THEME_TYPE, EventTheme.class);
    rootTypeClasses.put(CASE_TYPE, Case.class);
    rootTypeClasses.put(CASE_PERSON_TYPE, CasePerson.class);
    rootTypeClasses.put(CASE_ADDRESS_TYPE, CaseAddress.class);
    rootTypeClasses.put(CASE_DOCUMENT_TYPE, CaseDocument.class);
    rootTypeClasses.put(CASE_EVENT_TYPE, CaseEvent.class);
    rootTypeClasses.put(CASE_CASE_TYPE, CaseCase.class);
    rootTypeClasses.put(DEMAND_TYPE, Demand.class);
    rootTypeClasses.put(PROBLEM_TYPE, Problem.class);
    rootTypeClasses.put(INTERVENTION_TYPE, Intervention.class);
    rootTypeClasses.put(CLASS_TYPE, org.matrix.classif.Class.class);
    rootTypeClasses.put(TYPE_TYPE, org.matrix.dic.Type.class);
    rootTypeClasses.put(ENUM_TYPE_TYPE, org.matrix.dic.EnumType.class);    
    rootTypeClasses.put(POLICY_TYPE, Policy.class);
    rootTypeClasses.put(CLASS_POLICY_TYPE, ClassPolicy.class);
    rootTypeClasses.put(CASE_POLICY_TYPE, CasePolicy.class);
    rootTypeClasses.put(DOCUMENT_POLICY_TYPE, DocumentPolicy.class);
    rootTypeClasses.put(DISPOSAL_HOLD_TYPE, DisposalHold.class);
    rootTypeClasses.put(NODE_TYPE, Node.class);
    rootTypeClasses.put(WORKSPACE_TYPE, Workspace.class);
    rootTypeClasses.put(DOCUMENT_TYPE, Document.class);
    rootTypeClasses.put(CONTENT_TYPE, Content.class);
    rootTypeClasses.put(SCHOOL_TYPE, School.class);
    rootTypeClasses.put(COURSE_TYPE, Course.class);
    rootTypeClasses.put(INSCRIPTION_TYPE, Inscription.class);
    rootTypeClasses.put(CALL_TYPE, Call.class);
    rootTypeClasses.put(DISTRICT_TYPE, District.class);
    rootTypeClasses.put(BOARD_TYPE, Board.class);
    rootTypeClasses.put(COUNCILLOR_TYPE, Councillor.class);
    rootTypeClasses.put(POLITICAL_PARTY_TYPE, PoliticalParty.class);
    rootTypeClasses.put(FORUM_TYPE, Forum.class);
    rootTypeClasses.put(QUESTION_TYPE, Question.class);
    rootTypeClasses.put(FORUM_ANSWER_TYPE, Answer.class);
    rootTypeClasses.put(PERSON_TYPE, Person.class);
    rootTypeClasses.put(COUNTRY_TYPE, Country.class);
    rootTypeClasses.put(PROVINCE_TYPE, Province.class);
    rootTypeClasses.put(CITY_TYPE, City.class);
    rootTypeClasses.put(STREET_TYPE, Street.class);
    rootTypeClasses.put(ADDRESS_TYPE, Address.class);
    rootTypeClasses.put(ROOM_TYPE, Room.class);
    rootTypeClasses.put(CONTACT_TYPE, Contact.class);
    rootTypeClasses.put(PERSON_ADDRESS_TYPE, PersonAddress.class);
    rootTypeClasses.put(PERSON_REPRESENTANT_TYPE, PersonRepresentant.class);
    rootTypeClasses.put(PERSON_DOCUMENT_TYPE, PersonDocument.class);
    rootTypeClasses.put(PERSON_PERSON_TYPE, PersonPerson.class);
    rootTypeClasses.put(NEW_TYPE, New.class);
    rootTypeClasses.put(NEW_SECTION, NewSection.class);
    rootTypeClasses.put(NEW_DOCUMENT_TYPE, NewDocument.class);
    rootTypeClasses.put(SOURCE_TYPE, Source.class);
    rootTypeClasses.put(REPORT_TYPE, Report.class);
    rootTypeClasses.put(USER_TYPE, User.class);
    rootTypeClasses.put(ROLE_TYPE, Role.class);
    rootTypeClasses.put(USER_IN_ROLE_TYPE, UserInRole.class);
    rootTypeClasses.put(ROLE_IN_ROLE_TYPE, RoleInRole.class);
    rootTypeClasses.put(SIGNED_DOCUMENT_TYPE, SignedDocument.class);
    rootTypeClasses.put(TRANSLATION_TYPE, Translation.class);
    rootTypeClasses.put(VARIABLE_TYPE, Variable.class);

    // add rootTypeIds
    rootTypeIds.addAll(rootTypeClasses.keySet());

    // derivable root types
    derivableTypeIds.add(CASE_TYPE);
    derivableTypeIds.add(DEMAND_TYPE);
    derivableTypeIds.add(PROBLEM_TYPE);
    derivableTypeIds.add(INTERVENTION_TYPE);
    derivableTypeIds.add(DOCUMENT_TYPE);
    derivableTypeIds.add(ADDRESS_TYPE);
    derivableTypeIds.add(CONTACT_TYPE);
    derivableTypeIds.add(PERSON_DOCUMENT_TYPE);
    derivableTypeIds.add(CASE_DOCUMENT_TYPE);
    derivableTypeIds.add(CASE_EVENT_TYPE);
    derivableTypeIds.add(CLASS_TYPE);
    derivableTypeIds.add(NEW_DOCUMENT_TYPE);

    // standardActions
    standardActions.add(CREATE_ACTION);
    standardActions.add(READ_ACTION);
    standardActions.add(WRITE_ACTION);
    standardActions.add(DELETE_ACTION);
    standardActions.add(PRINT_ACTION);
    standardActions.add(EXECUTE_ACTION);
  }
}
