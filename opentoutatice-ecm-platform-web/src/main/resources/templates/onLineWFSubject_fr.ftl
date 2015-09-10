<#if eventId == "workflowOnlineTaskAssigned">
Demande de mise en ligne du document ${docTitle}
<#elseif eventId == "workflowOnlineTaskApproved">
Document ${docTitle} mis en ligne 
<#elseif eventId == "workflowOnlineTaskRejected">
Rejet de la demande de mise en ligne du document ${docTitle}
<#elseif eventId == "workflowOnlineCanceled">
Annulation de la demande de mise en ligne du document ${docTitle}
</#if>