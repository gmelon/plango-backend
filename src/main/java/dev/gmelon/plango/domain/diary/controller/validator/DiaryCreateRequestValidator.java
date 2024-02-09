package dev.gmelon.plango.domain.diary.controller.validator;

import dev.gmelon.plango.domain.diary.dto.DiaryCreateRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class DiaryCreateRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(DiaryCreateRequestDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DiaryCreateRequestDto requestDto = (DiaryCreateRequestDto) target;
        if (!StringUtils.hasText(requestDto.getContent()) && requestDto.getImageUrls().isEmpty()) {
            errors.reject("NotNull.contentOrImageUrl", "내용과 사진 중 한 가지는 필수입니다.");
        }
    }

}
