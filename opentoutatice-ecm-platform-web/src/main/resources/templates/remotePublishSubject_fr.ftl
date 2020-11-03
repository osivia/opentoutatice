<#if eventId == "documentPublished" || eventId == "documentPublicationApproved" >
Le document '${docTitle}' a été publié
<#elseif eventId == "documentWaitingPublication">
Demande de publication du document '${docTitle}'
<#elseif eventId == "documentPublicationRejected">
Rejet de la demande de publication du document '${docTitle}'
</#if>