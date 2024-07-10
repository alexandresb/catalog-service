package com.polarbookshop.catalogservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

//spécifie un Configuration data bean
//avantage : s'abstraire dans le code de la manipulation des noms de clés des propriétés
@ConfigurationProperties(prefix = "polar")
public class PolarProperties {
    /**
     * A message to welcome user
     */
    private String greeting;

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
}
