package dev.gmelon.plango.domain.notification.controller;

import dev.gmelon.plango.domain.notification.dto.NotificationResponseDto;
import dev.gmelon.plango.domain.notification.service.NotificationService;
import dev.gmelon.plango.global.config.auth.LoginMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponseDto> findAll(
            @LoginMember Long memberId,
            @RequestParam(defaultValue = "1") int page) {
        return notificationService.findAll(memberId, page);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{notificationId}")
    public void deleteOne(@LoginMember Long memberId, @PathVariable Long notificationId) {
        notificationService.deleteOne(memberId, notificationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteAll(@LoginMember Long memberId) {
        notificationService.deleteAll(memberId);
    }

}
