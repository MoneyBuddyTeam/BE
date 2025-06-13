package moneybuddy.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 이미지 업로드 실패: " + e.getMessage());
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public void deleteFile(String fileUrl) {
        // fileUrl: https://{bucket}.s3.{region}.amazonaws.com/{dirName}/{fileName}
        // → "dirName/fileName" 형식으로 추출해서 삭제해야 함

        String fileKey = extractFileKey(fileUrl);
        if (amazonS3.doesObjectExist(bucket, fileKey)) {
            amazonS3.deleteObject(bucket, fileKey);
        } else {
            throw new RuntimeException("삭제할 파일을 찾을 수 없습니다: " + fileUrl);
        }
    }

    private String extractFileKey(String fileUrl) {
        // URL에서 버킷 도메인을 제거한 상대 경로를 추출
        return fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
    }

}
