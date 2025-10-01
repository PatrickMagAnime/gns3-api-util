package model;

/**
 * Represents a GNS3 authentication token
 */
public class Token {
    private String access_token;
    private String token_type;

    public Token() {}

    public Token(String accessToken, String tokenType) {
        this.access_token = accessToken;
        this.token_type = tokenType;
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String tokenType) {
        this.token_type = tokenType;
    }

    @Override
    public String toString() {
        return "Token{" +
                "access_token='" + access_token + '\'' +
                ", token_type='" + token_type + '\'' +
                '}';
    }
}
