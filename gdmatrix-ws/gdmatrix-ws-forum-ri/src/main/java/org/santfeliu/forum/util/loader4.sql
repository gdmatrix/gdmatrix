select f.forumcod "forumId",
f.descripcio "forumName",
f.descripcio "forumDescription",
f.dataalta||f.horaalta "forumStartDateTime",
f.databaixa||f.horabaixa "forumEndDateTime",
0 "forumMaxQuestions",
'N' "forumType",
f.emailde "forumEmailFrom",
f.emailpera "forumEmailTo",
f.adminrol "forumAdminRoleId",
'OAC' "forumGroup",
'20110928120000' "forumCreationDateTime",
'blanquepa' "forumCreationUserId",
'20110928120000' "forumChangeDateTime",
'blanquepa' "forumChangeUserId",

p.pregcod "questionId",
p.titol "questionTitle",
p.texte "questionText",
'Y' "questionVisible",
p.dataalta||p.horaalta "questionCreationDateTime",
p.usercod "questionCreationUserId",
p.dataalta||p.horaalta "questionChangeDateTime",
p.usercod "questionChangeUserId",
decode(p.dataultresp, null, p.dataalta||p.horaalta, p.dataultresp||p.horaultresp) "questionActivityDateTime",
p.numlect "questionReadCount",

r.texte "answerText",
r.dataalta||r.horaalta "answerCreationDateTime",
r.usercod "answerCreationUserId",
r.dataalta||r.horaalta "answerChangeDateTime",
r.usercod "answerChangeUserId"

from for_forum f, for_pregunta p, for_resposta r
where f.forumcod = p.forumcod and p.pregcod = r.pregcod (+)
and f.forumcod in (276,277)
order by "forumId", "questionId"