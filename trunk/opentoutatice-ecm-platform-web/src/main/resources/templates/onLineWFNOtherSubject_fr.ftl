<#if isOnLineWF && eventId == "workflowTaskAssigned">
Demande de mise en ligne du document: ${docTitle}
<#elseif !isOnLineWF && eventId == "workflowTaskAssigned">
Tâche assignée pour le document ${docTitle}
</#if>