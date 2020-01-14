-- This file helps to migrate agenda data from version 1 to version 2

CREATE OR REPLACE VIEW "AJUNTAMENT"."AGD_DICTYPE" AS 
select replace(initcap(translate(lower(tipesdevnom),'аийнтуъ/,''().·','aeeioou_')),' ','')||'Event' as typeid,
'Event' as supertypeid,
tipesdevnom as description,
'T' as instantiable,
to_char(sysdate,'yyyyMMddHH24miss') as creationdt,
'blanquepa' as creationuserid,
to_char(sysdate,'yyyyMMddHH24miss') as modifydt,
'blanquepa' as modifyuserid,
decode(actiu, 'S', 'F', 'T') as removed,
'/Event/'||replace(initcap(translate(lower(tipesdevnom),'аийнтуъ/,''().·','aeeioou_')),' ','')||'Event/' as typepath,
'F' as restricted,
tipesdevcod, rolviscod, rolmodcod
from AGD_TIPUSESDEVENIMENT
where tipesdevnom <> '{GENERIC}'

insert into dic_type (typeid, supertypeid, description, instantiable, creationdt,
creationuserid, modifydt,modifyuserid,removed,typepath,restricted)
select typeid, supertypeid, description, instantiable, creationdt,
creationuserid, modifydt,modifyuserid,removed,typepath,restricted
from agd_dictype a where not exists (select 1 from dic_type d where a.typeid = d.typeid)

insert into dic_propdef (typeid, propname, description, proptype, propsize, minoccurs, maxoccurs, defaultvalue, hidden, readonly)
select typeid, 'tipesdevcod', 'tipesdevcod', 'T', '0', '1', '1', tipesdevcod, 'T', 'T'
from agd_dictype a where not exists (select 1 from dic_propdef p where a.typeid = p.typeid and p.PROPNAME = 'tipesdevcod')

insert into dic_acl (typeid, roleid, action)
select distinct typeid, decode(rolviscod, null, 'EVERYONE', rolviscod), 'Read'
from agd_dictype a where not exists (select 1 from dic_acl c where a.typeid = c.typeid and c.action = 'Read')

insert into dic_acl (typeid, roleid, action)
select distinct typeid, decode(rolmodcod, null, 'EVERYONE', rolmodcod), 'Write'
from agd_dictype a where not exists (select 1 from dic_acl c where a.typeid = c.typeid and c.action = 'Write')

insert into dic_acl (typeid, roleid, action)
select distinct typeid, decode(rolmodcod, null, 'EVERYONE', rolmodcod), 'Create'
from agd_dictype a where not exists (select 1 from dic_acl c where a.typeid = c.typeid and c.action = 'Create')

insert into dic_acl (typeid, roleid, action)
select 'Event', decode(rolviscod, null, 'EVERYONE', rolviscod), 'Read'
from agd_tipusesdeveniment t where t.tipesdevnom = '{GENERIC}'
and not exists (select 1 from dic_acl where typeid = 'Event' and action = 'Read')

insert into dic_acl (typeid, roleid, action)
select 'Event', decode(rolmodcod, null, 'EVERYONE', rolmodcod), 'Write'
from agd_tipusesdeveniment t where t.tipesdevnom = '{GENERIC}'
and not exists (select 1 from dic_acl where typeid = 'Event' and action = 'Write')

insert into dic_acl (typeid, roleid, action)
select 'Event', decode(rolmodcod, null, 'EVERYONE', rolmodcod), 'Create'
from agd_tipusesdeveniment t where t.tipesdevnom = '{GENERIC}'
and not exists (select 1 from dic_acl where typeid = 'Event' and action = 'Create')

update agd_esdeveniment e set eventtypeid =
(select typeid from agd_dictype d where e.tipesdevcod = d.tipesdevcod)

update agd_esdeveniment set eventtypeid = 'Event'
where eventtypeid is null

insert into dic_propdef
select types.typeid, 'themeId', 'Tema', 'T', 0, 0, 1, themes.themeid, 'T','T'
from agd_dictype types,
(select tipesdevcod,
       substr(max(sys_connect_by_path(temacod, ',' )),2) themeid
from (select tipesdevcod, temacod, row_number() over (partition by tipesdevcod order by temacod) rn
      from AGD_TIPESDEVTEMA)
start with rn = 1
connect by prior rn = rn-1 and prior tipesdevcod = tipesdevcod
group by tipesdevcod
order by tipesdevcod) themes
where types.tipesdevcod = themes.tipesdevcod

DROP VIEW "AJUNTAMENT"."AGD_DICTYPE"
