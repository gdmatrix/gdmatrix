select e.sesiocod "forumId",
e.temadesc "forumName",
e.temadesc "forumDescription",
e.datainicial||e.horainicial "forumStartDateTime",
e.datafinal||e.horafinal "forumEndDateTime",
e.maxpreg "forumMaxQuestions",
'I' "forumType",
'realor@santfeliu.cat' "forumEmailFrom",
'realor@santfeliu.cat' "forumEmailTo",
'ALCALDIA' "forumAdminRoleId",
'ENTR_VAZQUEZ' "forumGroup",
e.datainicial||e.horainicial "forumCreationDateTime",
'realor' "forumCreationUserId",
e.datainicial||e.horainicial "forumChangeDateTime",
'realor' "forumChangeUserId",

c.pregcod "questionId",
'' "questionTitle",
c.pregunta "questionText",
c.visible "questionVisible",
c.data||c.hora "questionCreationDateTime",
'anonymous' "questionCreationUserId",
c.data||c.hora "questionChangeDateTime",
'anonymous' "questionChangeUserId",
c.data||c.hora "questionActivityDateTime",
0 "questionReadCount",

c.resposta "answerText",
c.data||c.hora "answerCreationDateTime",
'vazquezcj' "answerCreationUserId",
c.data||c.hora "answerChangeDateTime",
'vazquezcj' "answerChangeUserId"

from cns_entrevsesio e, cns_entrevista c
where e.sesiocod = c.sesiocod
and e.sesiocod >= 15 and e.sesiocod <= 94
order by "forumId", "questionId"