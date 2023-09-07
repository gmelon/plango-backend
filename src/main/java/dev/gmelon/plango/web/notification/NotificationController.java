package dev.gmelon.plango.web.notification;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.notification.NotificationService;
import dev.gmelon.plango.service.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
