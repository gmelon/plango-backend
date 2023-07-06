package dev.gmelon.plango.infrastructure.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class S3Repository {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(String originalFileName, InputStream inputStream, String contentType, Long contentLength) {
        String saveFileName = createSaveFileName(originalFileName);
        ObjectMetadata metadata = createMetadata(contentType, contentLength);

        amazonS3.putObject(new PutObjectRequest(bucket, saveFileName, inputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3.getUrl(bucket, saveFileName).toString();
    }

    private String createSaveFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        return uuid.concat(extractFileExtension(originalFileName));
    }

    private String extractFileExtension(String originalFileName) {
        return originalFileName.substring(originalFileName.lastIndexOf('.'));
    }

    private ObjectMetadata createMetadata(String contentType, Long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(contentLength);
        return metadata;
    }

    public void delete(String savedFileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, savedFileName));
    }

}
