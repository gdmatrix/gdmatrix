INSERT INTO ORG_USUARI (usrcod, usrdesc, usrpass, perscod) values ('admin', 'Administrador', '?w/SYf[]', 0);


INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('KERNEL_ADMIN', 'Administració de nucli (proves)', NULL, to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('DIC_ADMIN', 'Administrador del diccionari', 'Permet crear, modificar i esborrar qualsevol tipus del diccionari.', to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('DOC_ADMIN', 'Administrador del gestor documental de MATRIX2', 'Permet fer totes les operacions sobre el gestor documetal.', to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('WF_ADMIN', 'Administrador del workflow', NULL, to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('GRX_ADMIN', 'Administrador gis', NULL, to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('SECURITY_ADMIN', 'Rol d''administrador de la seguretat', 'Rol per administrar usuaris i rols', to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('CASE_ADMIN', 'Administrador d''expedients de Matrix2', NULL, to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('CLASSIF_ADMIN', 'Administrador del mòdul de classificació', NULL, to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('CMS_ADMIN', 'Administrador de CMS', NULL, to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));

INSERT INTO APL_ROL
(ROLCOD, ROLDESC, OBSERV, STDDMOD, STDHMOD)
VALUES
('WEBMASTER', 'Administrador de la web', NULL, to_char(sysdate, 'YYYYMMDD'), to_char(sysdate, 'HH24MISS'));








INSERT INTO APL_ROLUSR
(USRCOD, ROLCOD)
VALUES
('admin               ', 'CASE_ADMIN');

INSERT INTO APL_ROLUSR
(USRCOD, ROLCOD)
VALUES
('admin               ', 'CLASSIF_ADMIN');

INSERT INTO APL_ROLUSR
(USRCOD, ROLCOD)
VALUES
('admin               ', 'CMS_ADMIN');

INSERT INTO APL_ROLUSR
(USRCOD, ROLCOD)
VALUES
('admin               ', 'DIC_ADMIN');

INSERT INTO APL_ROLUSR
(USRCOD, ROLCOD)
VALUES
('admin               ', 'DOC_ADMIN');

INSERT INTO APL_ROLUSR
(USRCOD, ROLCOD)
VALUES
('admin               ', 'SECURITY_ADMIN');

INSERT INTO APL_ROLUSR
(USRCOD, ROLCOD)
VALUES
('admin               ', 'WEBMASTER');

INSERT INTO APL_ROLUSR
(USRCOD, ROLCOD)
VALUES
('admin               ', 'WF_ADMIN');

INSERT INTO APL_ROLROL
(ROLCOD, INROLCOD)
VALUES
('DOC_ADMIN', 'FILE_ADMIN');
