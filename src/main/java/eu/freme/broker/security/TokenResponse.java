package eu.freme.broker.security;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class TokenResponse {
    @JsonProperty
    private String token;

    public TokenResponse() {
    }

    public TokenResponse(String token) {
        this.token = token;
    }
}
