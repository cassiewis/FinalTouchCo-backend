package Website.EventRentals.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.general.accessKeyId}")
    private String generalAccessKeyId;

    @Value("${aws.general.secretAccessKey}")
    private String generalSecretAccessKey;

    @Value("${aws.admin.accessKeyId}")
    private String adminAccessKeyId;

    @Value("${aws.admin.secretAccessKey}")
    private String adminSecretAccessKey;

    @Bean
    public S3Client generalS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(generalAccessKeyId, generalSecretAccessKey)
                        )
                )
                .build();
    }

    @Bean
    public DynamoDbClient generalDynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(generalAccessKeyId, generalSecretAccessKey)
                        )
                )
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient generalDynamoDbEnhancedClient(DynamoDbClient generalDynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(generalDynamoDbClient)
                .build();
    }

    @Bean
    public S3Client adminS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(adminAccessKeyId, adminSecretAccessKey)
                        )
                )
                .build();
    }

    @Bean
    public DynamoDbClient adminDynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(adminAccessKeyId, adminSecretAccessKey)
                        )
                )
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient adminDynamoDbEnhancedClient(DynamoDbClient adminDynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(adminDynamoDbClient)
                .build();
    }
}