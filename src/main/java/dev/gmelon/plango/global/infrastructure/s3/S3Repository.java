package dev.gmelon.plango.global.infrastructure.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import dev.gmelon.plango.domain.s3.exception.FileUploadFailureException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Repository
public class S3Repository {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<String> uploadAll(List<MultipartFile> files) {
        List<String> uploadedFileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try (InputStream inputStream = file.getInputStream()) {
                String uploadedFileUrl = this.upload(Objects.requireNonNull(file.getOriginalFilename()), inputStream, file.getContentType(), file.getSize());
                uploadedFileUrls.add(uploadedFileUrl);
            } catch (IOException e) {
                throw new FileUploadFailureException();
            }
        }

        return uploadedFileUrls;
    }

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
        // TODO substring 실패 시 400 오류
        return originalFileName.substring(originalFileName.lastIndexOf('.'));
    }

    private ObjectMetadata createMetadata(String contentType, Long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(contentLength);
        return metadata;
    }

    public void deleteAll(List<String> savedFileUrls) {
        savedFileUrls.forEach(this::delete);
    }

    public void delete(String savedFileUrl) {
        String savedFileName = extractSavedFileName(savedFileUrl);
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, savedFileName));
    }

    private String extractSavedFileName(String savedFileUrl) {
        return savedFileUrl.substring(savedFileUrl.lastIndexOf('/') + 1);
    }

}
