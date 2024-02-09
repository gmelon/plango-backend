package dev.gmelon.plango.domain.scheduleplace.dto;

import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlaceEditor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@Getter
public class SchedulePlaceEditRequestDto {

    private String memo;

    @Length(max = 10)
    private String category;

    @Builder
    public SchedulePlaceEditRequestDto(String memo, String category) {
        this.memo = memo;
        this.category = category;
    }

    public SchedulePlaceEditor toEditor() {
        return SchedulePlaceEditor.builder()
                .memo(memo)
                .category(category)
                .build();
    }
}
