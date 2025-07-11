package Website.EventRentals.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Service
public class GmailApiService {

    private static final String APPLICATION_NAME = "Event Rentals Email Service";
    
    @Value("${app.backendUrl}")
    private String backendUrl;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_SEND);

    @Value("${gmail.user.email:finaltouchco.info@gmail.com}")
    private String userEmail;

    /**
     * Creates an authorized Credential object using environment variables.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        String refreshToken = System.getenv("GOOGLE_REFRESH_TOKEN");
        
        if (clientId == null || clientSecret == null) {
            throw new IOException("Google OAuth credentials not found. Please set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables.");
        }

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IOException(
                "No refresh token found. Please set GOOGLE_REFRESH_TOKEN environment variable.\n" +
                "You need to complete the OAuth setup process first by calling /api/email/setup-oauth"
            );
        }

        try {
            // Create credential with refresh token
            GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(refreshToken);

            // Refresh the access token
            credential.refreshToken();
            
            System.out.println("Gmail credentials loaded successfully from environment variables");
            return credential;
            
        } catch (Exception e) {
            throw new IOException("Failed to create credentials from environment variables: " + e.getMessage(), e);
        }
    }

    /**
     * Create a MimeMessage using the parameters provided.
     */
    public MimeMessage createEmail(String to, String from, String subject, String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    /**
     * Create a message from an email.
     */
    public Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = java.util.Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * Send an email using Gmail API with OAuth 2.0.
     */
    public boolean sendEmail(String recipientEmail, String subject, String bodyText) {
        try {
            String clientId = System.getenv("GOOGLE_CLIENT_ID");
            String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
            
            if (clientId == null || clientSecret == null || 
                "your_actual_client_id_from_google_cloud".equals(clientId)) {
                
                // Fallback to development mode
                System.out.println("=== GMAIL API DEVELOPMENT MODE ===");
                System.out.println("TO: " + recipientEmail);
                System.out.println("FROM: " + userEmail);
                System.out.println("SUBJECT: " + subject);
                System.out.println("MESSAGE:");
                System.out.println(bodyText);
                System.out.println("STATUS: OAuth not configured, using development mode");
                System.out.println("=== END DEVELOPMENT MODE ===");
                return true;
            }

            // Build a new authorized API client service
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Create the email content
            MimeMessage emailContent = createEmail(recipientEmail, userEmail, subject, bodyText);
            Message message = createMessageWithEmail(emailContent);

            // Send the message
            message = service.users().messages().send("me", message).execute();
            System.out.println("Gmail API: Email sent successfully. Message ID: " + message.getId());
            return true;
            
        } catch (Exception e) {
            System.err.println("Gmail API error: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to console logging if Gmail API fails
            System.out.println("=== GMAIL API FALLBACK ===");
            System.out.println("TO: " + recipientEmail);
            System.out.println("FROM: " + userEmail);
            System.out.println("SUBJECT: " + subject);
            System.out.println("MESSAGE:");
            System.out.println(bodyText);
            System.out.println("=== END FALLBACK ===");
            return true; // Return true so verification process continues
        }
    }

    /**
     * One-time OAuth setup method. Call this once to authorize the application.
     * This will provide instructions for manual OAuth setup.
     */
    public void authorizeGmailAccess() throws IOException, GeneralSecurityException {
        System.out.println("\n=== GMAIL OAUTH SETUP INSTRUCTIONS ===");
        
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        
        if (clientId == null || clientSecret == null) {
            throw new IOException("Google OAuth credentials not found. Please set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables.");
        }

        System.out.println("Client ID: " + clientId);
        System.out.println("Client Secret: " + (clientSecret != null ? "***configured***" : "NOT SET"));
        
        // Build the authorization URL
        // Use the actual redirect URIs configured in Google Cloud Console
        String[] configuredRedirectUris = {
            backendUrl + "/api/email/complete-oauth"
        };
        
        String scope = "https://www.googleapis.com/auth/gmail.send";
        
        System.out.println("\nUsing your configured redirect URIs from Google Cloud Console:");
        
        for (int i = 0; i < configuredRedirectUris.length; i++) {
            String redirectUri = configuredRedirectUris[i];
            String authUrl = String.format(
                "https://accounts.google.com/o/oauth2/auth?client_id=%s&redirect_uri=%s&scope=%s&response_type=code&access_type=offline&prompt=consent",
                clientId, redirectUri, scope
            );
            
            System.out.println((i + 1) + ". URL with redirect URI: " + redirectUri);
            System.out.println("   " + authUrl);
            System.out.println();
        }
        
        System.out.println("Choose one of the URLs above (both should work now)");
        System.out.println("After authorization, you'll get redirected with a 'code' parameter in the URL");
        System.out.println("Copy that authorization code and call: POST /api/email/complete-oauth");
        System.out.println("Body: {\"code\": \"your_authorization_code_here\", \"redirectUri\": \"the_redirect_uri_you_used\"}");
        System.out.println("\nNote: Use the same redirect URI in the completion call as you used for authorization");
        System.out.println("=== END SETUP INSTRUCTIONS ===\n");
    }

    /**
     * Complete the OAuth flow with the authorization code
     */
    public boolean completeOAuthFlow(String authorizationCode) throws IOException, GeneralSecurityException {
        return completeOAuthFlow(authorizationCode, backendUrl + "/api/email/complete-oauth");
    }
    
    /**
     * Complete the OAuth flow with the authorization code and specific redirect URI
     */
    public boolean completeOAuthFlow(String authorizationCode, String redirectUri) throws IOException, GeneralSecurityException {
        System.out.println("Completing OAuth flow with authorization code...");
        System.out.println("Using redirect URI: " + redirectUri);
        
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        
        if (clientId == null || clientSecret == null) {
            throw new IOException("Google OAuth credentials not found.");
        }

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        // Create client secrets
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(details);

        // Build flow (without file storage)
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .build();
        
        try {
            // Exchange authorization code for tokens
            var tokenRequest = flow.newTokenRequest(authorizationCode)
                .setRedirectUri(redirectUri);
            
            var tokenResponse = tokenRequest.execute();
            
            System.out.println("=== OAUTH SETUP SUCCESSFUL ===");
            System.out.println("Access token received: " + (tokenResponse.getAccessToken() != null ? "YES" : "NO"));
            System.out.println("Refresh token received: " + (tokenResponse.getRefreshToken() != null ? "YES" : "NO"));
            
            if (tokenResponse.getRefreshToken() != null) {
                System.out.println("\n*** IMPORTANT: Set this as your GOOGLE_REFRESH_TOKEN environment variable ***");
                System.out.println("REFRESH TOKEN (save as GOOGLE_REFRESH_TOKEN): " + tokenResponse.getRefreshToken());
                System.out.println("*** Copy the token above and set it in your environment ***");
            } else {
                System.out.println("WARNING: No refresh token received. You may need to revoke access and try again with prompt=consent");
            }
            
            System.out.println("=== NEXT STEPS ===");
            System.out.println("1. Copy the refresh token above");
            System.out.println("2. Set environment variable: export GOOGLE_REFRESH_TOKEN=\"your_refresh_token_here\"");
            System.out.println("3. Restart your application");
            System.out.println("4. Test email verification - it should now work for all users!");
            System.out.println("=== END OAUTH SETUP ===");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error completing OAuth flow: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
