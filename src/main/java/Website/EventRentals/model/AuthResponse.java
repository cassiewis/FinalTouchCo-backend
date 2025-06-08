package Website.EventRentals.model;

public class AuthResponse {
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

    // Getter
    public String getToken() {
        System.out.println("CASSIE getToken: " + token);
        return token;
    }

    // Setter
    public void setToken(String token) {
        System.out.println("CASSIE token set to: " + token);
        this.token = token;
    }
}