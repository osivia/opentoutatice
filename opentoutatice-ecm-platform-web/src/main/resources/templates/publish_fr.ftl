<HTML>
<BODY>
<P>
<#if eventId =="documentWaitingPublication">
Une demande de publication du document '${htmlEscape(docTitle)}' vous a &eacute;t&eacute; assign&eacute;e par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "documentPublished">
Un nouveau document (UID: ${docId}) a été publié par ${author} à ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.<BR>
Vous pouvez consulter ce document à l'adresse suivante: <a href="${docUrl}">${htmlEscape(docTitle)}</a>.
<#elseif eventId == "documentPublicationApproved">
Un document (UID: ${docId}) a été approuvé par ${author} à ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.<BR>
Commentaire: ${comment}.<BR>
Vous pouvez consulter ce document à l'adresse suivante: <a href="${docUrl}">${htmlEscape(docTitle)}</a>.
<#elseif eventId == "documentPublicationRejected">
Un document (UID: ${docId}) a été rejeté par ${author} à ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.<BR>
Commentaire: ${comment}.<BR>
</#if>
</P>
</BODY>
<HTML>