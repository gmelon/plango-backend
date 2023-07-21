package dev.gmelon.plango.web.diary.validator;

import dev.gmelon.plango.service.diary.dto.DiaryCreateRequestDto;
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
        if (!StringUtils.hasText(requestDto.getTitle()) && !StringUtils.hasText(requestDto.getImageUrl())) {
            errors.reject("NotNull.titleOrImageUrl", "제목과 사진 중 한 가지는 필수입니다.");
        }
    }

}
