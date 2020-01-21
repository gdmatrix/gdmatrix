---CAS_PERSON-------------------------------------------------------------------
-- 1. Afegir columna temporal
ALTER TABLE CAS_PERSON ADD
(
  TEMPCOLUMN VARCHAR2(32)
);
-- 2. Copia els valors de la columna a modificar a la columna temporal
UPDATE CAS_PERSON SET TEMPCOLUMN = PERSCOD;
-- 3. Drop de la PK
ALTER TABLE CAS_PERSON DROP CONSTRAINT CAS_PERSON_PK;
--ALTER TABLE CAS_PERSON MODIFY (PERSCOD NULL);

-- 4. Anul·la els valors de la columna a modificar
UPDATE CAS_PERSON SET PERSCOD = null;
-- 5. Modifica el tipus de la columna
ALTER TABLE CAS_PERSON MODIFY
(
   PERSCOD VARCHAR2(32)
);
-- 6. Copia els valors de la columna temporal a la original
UPDATE CAS_PERSON SET PERSCOD = TEMPCOLUMN;
-- 7. Afegeix de nou la PK
ALTER TABLE CAS_PERSON ADD
  CONSTRAINT CAS_PERSON_PK PRIMARY KEY (CASEID, PERSCOD);
-- 8. Drop de la columna temporal
ALTER TABLE CAS_PERSON DROP COLUMN TEMPCOLUMN;

COMMIT;
--CAS_ADDRESS-------------------------------------------------------------------
-- 1. Afegir columna temporal
ALTER TABLE CAS_ADDRESS ADD
(
  TEMPCOLUMN VARCHAR2(32)
);
-- 2. Copia els valors de la columna a modificar a la columna temporal
UPDATE CAS_ADDRESS SET TEMPCOLUMN = ADDRESSID;
-- 3. Drop de la PK
ALTER TABLE CAS_ADDRESS DROP CONSTRAINT CAS_ADDRESS_PK;
--ALTER TABLE CAS_ADDRESS MODIFY (ADDRESSID NULL);

-- 4. Anul·la els valors de la columna a modificar
UPDATE CAS_ADDRESS SET ADDRESSID = null;
-- 5. Modifica el tipus de la columna
ALTER TABLE CAS_ADDRESS MODIFY
(
   ADDRESSID VARCHAR2(32)
);
-- 6. Copia els valors de la columna temporal a la original
UPDATE CAS_ADDRESS SET ADDRESSID = TEMPCOLUMN;
-- 7. Afegeix de nou la PK
ALTER TABLE CAS_ADDRESS
  ADD CONSTRAINT CAS_ADDRESS_PK PRIMARY KEY (CASEID, ADDRESSID);
-- 8. Drop de la columna temporal
ALTER TABLE CAS_ADDRESS DROP COLUMN TEMPCOLUMN;

COMMIT;
--CAS_DOCUMENT------------------------------------------------------------------
-- 1. Afegir columna temporal
ALTER TABLE CAS_DOCUMENT ADD
(
  TEMPCOLUMN VARCHAR2(32)
);
-- 2. Copia els valors de la columna a modificar a la columna temporal
UPDATE CAS_DOCUMENT SET TEMPCOLUMN = DOCID;
-- 3. Drop de la PK
ALTER TABLE CAS_DOCUMENT DROP CONSTRAINT PK_CAS_DOCUMENT;
--ALTER TABLE CAS_DOCUMENT MODIFY (DOCID NULL);
-- 4. Anul·la els valors de la columna a modificar
UPDATE CAS_DOCUMENT SET DOCID = null;
-- 5. Modifica el tipus de la columna
ALTER TABLE CAS_DOCUMENT MODIFY
(
   DOCID VARCHAR2(32)
);
-- 6. Copia els valors de la columna temporal a la original
UPDATE CAS_DOCUMENT SET DOCID = TEMPCOLUMN;
-- 7. Afegeix de nou la PK
ALTER TABLE CAS_DOCUMENT 
  ADD CONSTRAINT CAS_DOCUMENT_PK PRIMARY KEY (CASEID, DOCID);
-- 8. Drop de la columna temporal
ALTER TABLE CAS_DOCUMENT DROP COLUMN TEMPCOLUMN;

COMMIT;
--CAS_INTERVENTION------------------------------------------------------------------
-- 1. Afegir columna temporal
ALTER TABLE CAS_INTERVENTION ADD
(
  TEMPCOLUMN VARCHAR2(32)
);
-- 2. Copia els valors de la columna a modificar a la columna temporal
UPDATE CAS_INTERVENTION SET TEMPCOLUMN = PERSCOD;

UPDATE CAS_INTERVENTION SET PERSCOD = null;
-- 4. Modifica el tipus de la columna
ALTER TABLE CAS_INTERVENTION MODIFY
(
   PERSCOD VARCHAR2(32)
);
-- 5. Copia els valors de la columna temporal a la original
UPDATE CAS_INTERVENTION SET PERSCOD = TEMPCOLUMN;

-- 6. Drop de la columna temporal
ALTER TABLE CAS_INTERVENTION DROP COLUMN TEMPCOLUMN;

COMMIT;
---DOM_PERSON-------------------------------------------------------------------
-- 1. Afegir columna temporal
ALTER TABLE DOM_PERSON ADD
(
  TEMPCOLUMN VARCHAR2(32)
);
-- 2. Copia els valors de la columna a modificar a la columna temporal
UPDATE DOM_PERSON SET TEMPCOLUMN = PERSONID;
-- 3. Drop de la PK
ALTER TABLE DOM_PERSON DROP CONSTRAINT DOM_PERSON_PK;
ALTER TABLE DOM_PERSON MODIFY (PERSONID NULL);
-- 4. Anul·la els valors de la columna a modificar
UPDATE DOM_PERSON SET PERSONID = null;
-- 5. Modifica el tipus de la columna
ALTER TABLE DOM_PERSON MODIFY
(
   PERSONID VARCHAR2(32)
);
-- 6. Copia els valors de la columna temporal a la original
UPDATE DOM_PERSON SET PERSONID = TEMPCOLUMN;
-- 7. Afegeix de nou la PK
ALTER TABLE DOM_PERSON ADD
  CONSTRAINT DOM_PERSON_PK PRIMARY KEY (DOCID, VERSION, PERSONID, PERSFUNCTION);
-- 8. Drop de la columna temporal
ALTER TABLE DOM_PERSON DROP COLUMN TEMPCOLUMN;

COMMIT;
---INF_NEWDOC-------------------------------------------------------------------
-- 1. Afegir columna temporal
ALTER TABLE INF_NEWDOC ADD
(
  TEMPCOLUMN VARCHAR2(32)
);
-- 2. Copia els valors de la columna a modificar a la columna temporal
UPDATE INF_NEWDOC SET TEMPCOLUMN = DOCID;
-- 3. Drop de la PK
ALTER TABLE INF_NEWDOC DROP CONSTRAINT INF_NEWDOC_PK;
ALTER TABLE INF_NEWDOC MODIFY (DOCID NULL);
-- 4. Drop unique index
DROP INDEX AJUNTAMENT.INF_NEWDOC_PK;

-- 5. Anul·la els valors de la columna a modificar
UPDATE INF_NEWDOC SET DOCID = null;
-- 6. Modifica el tipus de la columna
ALTER TABLE INF_NEWDOC MODIFY
(
   DOCID VARCHAR2(32)
);
-- 7. Copia els valors de la columna temporal a la original
UPDATE INF_NEWDOC SET DOCID = TEMPCOLUMN;
-- 8. Afegeix de nou la PK
ALTER TABLE INF_NEWDOC
  ADD CONSTRAINT INF_NEWDOC_PK PRIMARY KEY (NEWID, DOCID);
-- 9. Crea índex
CREATE UNIQUE INDEX "AJUNTAMENT"."INF_NEWDOC_PK" ON "AJUNTAMENT"."INF_NEWDOC" ("NEWID", "DOCID")
TABLESPACE "USER_DATA" PCTFREE 10 INITRANS 2 MAXTRANS 255 STORAGE ( INITIAL 64K BUFFER_POOL DEFAULT)
LOGGING LOCAL
-- 10. Drop de la columna temporal
ALTER TABLE INF_NEWDOC DROP COLUMN TEMPCOLUMN;

COMMIT;
