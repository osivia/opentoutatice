<HTML>
<BODY>
<#if isOnLineWF && eventId == "workflowTaskAssigned">
Une demande de mise en ligne du document '${htmlEscape(docTitle)}' a &eacute;t&eacute; &eacute;mise par ${initiator} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowOnlineTaskApproved">
Le document '${htmlEscape(docTitle)}' a &eacute;t&eacute; mis en ligne par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowOnlineTaskRejected">
La demande de mise en ligne du document '${htmlEscape(docTitle)}' a &eacute;t&eacute; rejet&eacute;e par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
<#elseif eventId == "workflowOnlineCanceled">
La demande de mise en ligne du document '${htmlEscape(docTitle)}' a &eacute;t&eacute; annul&eacute;e par ${author} le ${dateTime?datetime?string("dd/MM/yyyy - HH:mm")}.
</#if>
<BR>
<#if eventId == "workflowOnlineTaskApproved">
Vous &ecirc;tes invit&eacute;(e) &agrave; consulter ce document
<#if docPermalink!=""> dans toutatice : <a href="${docPermalink}">${htmlEscape(docTitle)}</a>.
<br/><i>
( vous pouvez y accéder directement 
</#if>
dans nuxeo en cliquant <a href="${docUrl}">ici</a>.
<#if docPermalink!=""> ) </i> </#if>
<#else>
Vous &ecirc;tes invit&eacute;(e) &agrave; le consulter &agrave; l'adresse suivante: <a href="${docUrl}">${htmlEscape(docTitle)}</a></P>
</#if>
</BODY>
<HTML>