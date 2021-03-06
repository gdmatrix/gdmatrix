CREATE TABLE WFW_INSTANCE
(
  INSTANCEID NUMBER(10, 0) NOT NULL,
  WORKFLOW VARCHAR2(50) NOT NULL,
  EVENTCOUNT NUMBER(10, 0) NOT NULL,
  PROCESSABLE CHAR(1),
  STARTDT CHAR(14),
  CONSTRAINT WFW_INSTANCE_PK PRIMARY KEY (INSTANCEID) ENABLE
);

CREATE TABLE WFW_VARIABLE
(
  INSTANCEID NUMBER NOT NULL,
  NAME VARCHAR2(64) NOT NULL,
  TYPE CHAR(1) NOT NULL,
  VALUE VARCHAR2(4000),
  CONSTRAINT WFW_VARIABLE_PK PRIMARY KEY (INSTANCEID,NAME) ENABLE,
  CONSTRAINT WFW_VARIABLE_INSTANCE_FK FOREIGN KEY (INSTANCEID)
    REFERENCES WFW_INSTANCE (INSTANCEID) ENABLE
);

CREATE TABLE WFW_EVENT
(
  INSTANCEID NUMBER(10, 0) NOT NULL,
  EVENTNUM NUMBER(10, 0) NOT NULL,
  DISPATCHED CHAR(1),
  EVENTDATE CHAR(8) NOT NULL,
  EVENTHOUR CHAR(6) NOT NULL,
  ACTOR VARCHAR2(50),
  CONSTRAINT WFW_EVENT_PK PRIMARY KEY (INSTANCEID,EVENTNUM) ENABLE,
  CONSTRAINT WFW_EVENT_INSTANCE_FK FOREIGN KEY (INSTANCEID)
    REFERENCES WFW_INSTANCE (INSTANCEID) ENABLE
);

CREATE TABLE WFW_EVENTVAR
(
  INSTANCEID NUMBER(10, 0) NOT NULL,
  EVENTNUM NUMBER(10, 0) NOT NULL,
  NAME VARCHAR2(64) NOT NULL,
  TYPE CHAR(1) NOT NULL,
  OLDVALUE VARCHAR2(4000),
  NEWVALUE VARCHAR2(4000),
  CONSTRAINT WFW_EVENTVAR_PK PRIMARY KEY (INSTANCEID,EVENTNUM,NAME) ENABLE,
  CONSTRAINT WFW_EVENTVAR_EVENT_FK FOREIGN KEY (INSTANCEID,EVENTNUM)
    REFERENCES WFW_EVENT (INSTANCEID,EVENTNUM) ENABLE
);

CREATE TABLE WFW_TIMER
(
  INSTANCEID NUMBER(10, 0) NOT NULL,
  DATETIME CHAR(14) NOT NULL,
  CONSTRAINT "WFW_TIMER_PK" PRIMARY KEY(INSTANCEID, DATETIME) ENABLE
);

CREATE TABLE TABLESEQ 
(
  "COUNTER" VARCHAR2(50), 
	"VALUE" NUMBER(10, 0),
  CONSTRAINT TABLESEQ_PK PRIMARY KEY ("COUNTER") ENABLE
);

CREATE INDEX WFW_EVENTDATE_IDX ON WFW_EVENT ("EVENTDATE");
CREATE INDEX WFW_VARIABLE_VALUE_IDX ON WFW_VARIABLE ("VALUE");


