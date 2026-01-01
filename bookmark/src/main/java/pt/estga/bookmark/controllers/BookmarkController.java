package pt.estga.bookmark.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.bookmark.dtos.BookmarkDto;
import pt.estga.bookmark.services.BookmarkService;
import pt.estga.shared.enums.TargetType;
import pt.estga.shared.models.AppPrincipal;
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
    public List<BookmarkDto> getUserBookmarks(@AuthenticationPrincipal AppPrincipal principal) {
        User user = userService.findById(principal.getId()).orElseThrow();
        return service.getUserBookmarks(user);
    }

    @PostMapping("/{type}/{targetId}")
    public BookmarkDto create(
            @AuthenticationPrincipal AppPrincipal principal,
            @PathVariable TargetType type,
            @PathVariable Long targetId
    ) {
        User user = userService.findById(principal.getId()).orElseThrow();
        return service.createBookmark(user, type, targetId);
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AppPrincipal principal,
            @PathVariable Long bookmarkId
    ) {
        User user = userService.findById(principal.getId()).orElseThrow();
        service.deleteBookmark(user, bookmarkId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{type}/{targetId}")
    public boolean isBookmarked(
            @AuthenticationPrincipal AppPrincipal principal,
            @PathVariable TargetType type,
            @PathVariable Long targetId
    ) {
        User user = userService.findById(principal.getId()).orElseThrow();
        return service.isBookmarked(user, type, targetId);
    }
}
