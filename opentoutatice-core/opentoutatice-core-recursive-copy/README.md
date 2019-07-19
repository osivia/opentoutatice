# opentoutatice-core-recursive-copy

## Copie partielle

#### Présentation
La copie partielle de document permet de ne copier un document qu'avec une partie des ses méta-données, les autres étant réinitialisées.

Les méta-données à réinitialisées sont identifiées par schéma (autrement dit, on ne peut réinitialiser une méta-donnéee individuelle mais seulement un groupe de méta-données contenues dans un schéma). 

#### Utilisation
Les schémas à réinitialiser sont à indiquer dans une contribution sur le service ResetableSchemasRegister. 
Un exemple de contribution est ici: `/opentoutatice-core-recursive-copy/src/main/resources/OSGI-INF/extensions/partial-copy/resetable-schemas-contrib.xml.sample`.
Les schémas à réinitialiser lors de la copie sont à indiquer sous la forme suivante:

```xml
<schemas>
	<schema>schema_1</schema>
		...
	<schema>schema_N</schema>
</schemas>
```

Votre propre contribution est à placer dans l'addon voulant utilser le mécanisme et son identifiant (component@name) doit être unique.
Chaque schéma est à valoriser comme la valeur d'un noeud <schema> (cf exemple):



Cette contribution est alors à référencer dans le fichier `/<adddon-nx>/src/main/resources/META-INF/MANIFEST.MF`:

    Nuxeo-Component: ...
     OSGI-INF/extensions/<restable-schemas-contrib-name>.xml
 
(l'espace en début de ligne devant `OSGI-INF` est nécessaire)

