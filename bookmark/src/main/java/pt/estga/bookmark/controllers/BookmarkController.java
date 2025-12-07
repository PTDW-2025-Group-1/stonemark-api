package pt.estga.bookmark.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.bookmark.dtos.BookmarkDto;
import pt.estga.bookmark.services.BookmarkService;
import pt.estga.file.enums.TargetType;
import pt.estga.user.entities.User;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmarks", description = "Endpoints for user bookmarks.")
public class BookmarkController {

    private final BookmarkService service;

    @GetMapping
    public List<BookmarkDto> getUserBookmarks(@AuthenticationPrincipal User user) {
        return service.getUserBookmarks(user);
    }

    @PostMapping("/{type}/{targetId}")
    public BookmarkDto create(
            @AuthenticationPrincipal User user,
            @PathVariable TargetType type,
            @PathVariable Long targetId
    ) {
        return service.createBookmark(user, type, targetId);
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long bookmarkId
    ) {
        service.deleteBookmark(user, bookmarkId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{type}/{targetId}")
    public boolean isBookmarked(
            @AuthenticationPrincipal User user,
            @PathVariable TargetType type,
            @PathVariable Long targetId
    ) {
        return service.isBookmarked(user, type, targetId);
    }
}
