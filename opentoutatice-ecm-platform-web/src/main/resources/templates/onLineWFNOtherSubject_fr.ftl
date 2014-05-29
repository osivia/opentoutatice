<#if isOnLineWF && eventId == "workflowTaskAssigned">
Demande de mise en ligne du document: ${docTitle}
<#elseif !isOnLineWF && eventId == "workflowTaskAssigned">
Task Assigned for ${docTitle}
<#elseif isOnLineWF && eventId == "workflowTaskCompleted">
Document mis en ligne: ${docTitle}
<#elseif isOnLineWF && eventId == "workflowTaskRejected">
Rejet de la demande de mise en ligne de: ${docTitle}
<#elseif isOnLineWF &&  eventId == "workflowProcessCanceled">
Annulation de la demande de mise en ligne de: ${docTitle}
</#if>