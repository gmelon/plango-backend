package dev.gmelon.plango.web.diary.validator;

import dev.gmelon.plango.service.diary.dto.DiaryEditRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class DiaryEditRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(DiaryEditRequestDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DiaryEditRequestDto requestDto = (DiaryEditRequestDto) target;
        if (!StringUtils.hasText(requestDto.getContent()) && requestDto.getImageUrls().isEmpty()) {
            errors.reject("NotNull.contentOrImageUrl", "내용과 사진 중 한 가지는 필수입니다.");
        }
    }

}
