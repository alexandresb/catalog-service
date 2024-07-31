package com.polarbookshop.catalogservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@Configuration //source de configurations
/* activation de l'audit des données lors de leur insertion, modification ou suppression d'enregistrement en base
Lors d'une op de persistance, modif ou suppr un event est émis.Spring Data peut capturer cet event et en extraire les métadonnées.
Ces métadonnées seront intégrées dans les requêtes (cas d'insert ou update) au niveau des champs de l'enregistrement correspondant
aux propriétés annotées spécifiquement pour l'audit de l'objet Entité manipulé.
Si une entité de type record est retournée par la méthode de persistance, le record Java sera mis à jour avec ces valeurs (cela est aussi valable pour delete)
*/
@EnableJdbcAuditing
public class DataConfig {
}
