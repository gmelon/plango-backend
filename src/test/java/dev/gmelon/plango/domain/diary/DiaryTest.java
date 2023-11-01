package dev.gmelon.plango.domain.diary;


import static org.assertj.core.api.Assertions.assertThat;

import dev.gmelon.plango.domain.member.Member;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiaryTest {
    @Test
    void Diary_작성_시_첨부한_이미지링크는_DiaryImage로_변환되어_저장된다() {
        // given
        List<String> imageUrls = List.of("image.com/a", "image.com/b", "image.com/c");

        // when
        Diary diary = Diary.builder()
                .imageUrls(imageUrls)
                .build();

        // then
        assertThat(diary.getDiaryImages())
                .extracting(DiaryImage::getImageUrl)
                .containsExactlyInAnyOrderElementsOf(imageUrls);
    }

    @Test
    void Diary를_수정한다() {
        // given
        Diary diary = Diary.builder()
                .content("기존 내용")
                .build();
        DiaryEditor editor = DiaryEditor.builder()
                .content("새로운 내용")
                .build();

        // when
        diary.edit(editor);

        // then
        assertThat(diary.getContent()).isEqualTo(editor.getContent());
    }

    @Test
    void Diary_수정_시_기존_DiaryImage는_삭제되고_수정_값으로_대체된다() {
        // given
        List<String> oldImageUrls = List.of("image.com/a", "image.com/b", "image.com/c");
        Diary diary = Diary.builder()
                .imageUrls(oldImageUrls)
                .build();

        List<String> newImageUrls = List.of("image.com/d", "image.com/e");
        DiaryEditor editor = DiaryEditor.builder()
                .imageUrls(newImageUrls)
                .build();

        // when
        diary.edit(editor);

        // then
        assertThat(diary.getDiaryImages()).hasSize(2);
        assertThat(diary.getDiaryImages())
                .extracting(DiaryImage::getImageUrl)
                .containsExactlyInAnyOrderElementsOf(newImageUrls);
    }

    @Test
    void Diary가_가진_DiaryImage의_실제_url를_반환한다() {
        // given
        List<String> imageUrls = List.of("image.com/a", "image.com/b", "image.com/c");
        Diary diary = Diary.builder()
                .imageUrls(imageUrls)
                .build();

        // when, then
        assertThat(diary.getDiaryImageUrls())
                .containsExactlyInAnyOrderElementsOf(imageUrls);
    }

    @Test
    void Diary를_작성한_회원의_id를_반환한다() {
        // given
        Member member = Member.builder()
                .id(1L)
                .build();
        Diary diary = Diary.builder()
                .member(member)
                .build();

        // when, then
        assertThat(diary.memberId()).isEqualTo(member.getId());
    }
}
