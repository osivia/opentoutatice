<#if isOnLineWF && eventId == "workflowTaskAssigned">
Demande de mise en ligne du document: ${docTitle}
<#elseif !isOnLineWF && eventId == "workflowTaskAssigned">
T&eagrave;che assign&eacute;e pour le document ${docTitle}
</#if>