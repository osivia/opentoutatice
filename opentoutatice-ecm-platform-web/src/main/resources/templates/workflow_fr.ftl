<HTML>
<BODY>
<P>
<#if eventId == "workflowNewProcessStarted">
Une t&acirc;che a &eacute;t&eacute; d&eacute;marr&eacute;e pour le document (UID: ${docId}) par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowProcessEnded">
Fin d'une t&acirc;che pour le document (UID: ${docId}) par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowAbandoned">
Une t&acirc;che pour le document ${htmlEscape(docTitle)} est annul&eacute;e par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowProcessCanceled">
Une t&acirc;che pour le document ${htmlEscape(docTitle)} est annul&eacute;e par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowTaskUnassigned">
Une t&acirc;che est retir&eacute;e sur le document ${htmlEscape(docTitle)} par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowTaskCompleted">
Une t&acirc;che pour le document ${htmlEscape(docTitle)} est accept&eacute;e par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
Avec pour commentaire: ${comment}.
<#elseif eventId == "workflowTaskRemoved">
Suppression de la t&acirc;che pour le document (UID: ${docId}) par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowTaskSuspended">
Une t&acirc;che a &eacute;t&eacute; suspendue pour le document (UID: ${docId}) par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowTaskRejected">
Une t&acirc;che pour le document ${htmlEscape(docTitle)} est rejet&eacute;e par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.<BR>
Avec pour commentaire: ${comment}.
<#elseif eventId == "ForumCommentPublication">
Une t&acirc;che de mod&eacute;ration pour le fil de discussion "${htmlEscape(docTitle)}" vous est assign&eacute;e. ${author} a ajout&eacute; un commentaire le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.<BR>
</#if>
<BR>
Vous &ecirc;tes invit&eacute;(e) &agrave; consulter ce document &agrave; l'adresse suivante: <a href="${docUrl}">${htmlEscape(docTitle)}</a></P>
</BODY>
<HTML>
