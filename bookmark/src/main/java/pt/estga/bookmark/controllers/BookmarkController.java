package pt.estga.bookmark.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.bookmark.dtos.BookmarkDto;
import pt.estga.bookmark.services.BookmarkService;
import pt.estga.shared.utils.SecurityUtils;
import pt.estga.shared.enums.TargetType;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmarks", description = "Endpoints for user bookmarks.")
public class BookmarkController {

    private final BookmarkService service;
    private final UserService userService;

    @GetMapping
    public List<BookmarkDto> getUserBookmarks() {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        User user = userService.findById(userId).orElseThrow();
        return service.getUserBookmarks(user);
    }

    @PostMapping("/{type}/{targetId}")
    public BookmarkDto create(
            @PathVariable TargetType type,
            @PathVariable Long targetId
    ) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        User user = userService.findById(userId).orElseThrow();
        return service.createBookmark(user, type, targetId);
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long bookmarkId
    ) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        User user = userService.findById(userId).orElseThrow();
        service.deleteBookmark(user, bookmarkId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{type}/{targetId}")
    public boolean isBookmarked(
            @PathVariable TargetType type,
            @PathVariable Long targetId
    ) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        User user = userService.findById(userId).orElseThrow();
        return service.isBookmarked(user, type, targetId);
    }
}
