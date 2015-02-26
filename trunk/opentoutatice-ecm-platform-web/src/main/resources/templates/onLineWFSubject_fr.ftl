<#if eventId == "workflowOnlineTaskApproved">
Document mis en ligne: ${docTitle}
<#elseif eventId == "workflowOnlineTaskRejected">
Rejet de la demande de mise en ligne de: ${docTitle}
<#elseif eventId == "workflowOnlineCanceled">
Annulation de la demande de mise en ligne de: ${docTitle}
</#if>