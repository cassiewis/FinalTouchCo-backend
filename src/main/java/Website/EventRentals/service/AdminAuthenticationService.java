package Website.EventRentals.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class AdminAuthenticationService {

    private static final String SECRET_NAME = "application/login/credentials";

    @Value("${aws.general.accessKeyId}")
    private String generalAccessKeyId;

    @Value("${aws.general.secretAccessKey}")
    private String generalSecretAccessKey;

    @Value("${aws.region}")
    private String region;

    public boolean authenticate(String username, String password) {
        Map<String, String> adminCredentials = getAdminCredentials();

        String storedUsername = adminCredentials.get("username");
        String storedPassword = adminCredentials.get("password");

        // Validate username and password
        return username.equals(storedUsername) && password.equals(storedPassword);
    }

    public Map<String, String> getAdminCredentials() {
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(generalAccessKeyId, generalSecretAccessKey)
                    )
                )
                .region(Region.of(region))
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(SECRET_NAME)
                .build();

        GetSecretValueResponse getSecretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(getSecretValueResponse.secretString(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse secret", e);
        }
    }
}