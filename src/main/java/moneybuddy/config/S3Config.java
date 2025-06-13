package moneybuddy.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class S3Config {
    @Value("${cloud.aws.credential.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credential.secret-key}")
    private String secretKey;

    @Bean
    public AmazonS3 amazonS3() {

        String region = "us-east-1";

        if (accessKey == null || secretKey == null) {
            throw new IllegalArgumentException("Access key or secret key is missing!");
        }

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}
