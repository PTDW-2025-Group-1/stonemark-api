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

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmarks", description = "Endpoints for user bookmarks.")
public class BookmarkController {

    private final BookmarkService service;

    @GetMapping
    public List<BookmarkDto> getUserBookmarks(@AuthenticationPrincipal AppPrincipal principal) {
        return service.getUserBookmarks(principal.getId());
    }

    @PostMapping("/{type}/{targetId}")
    public BookmarkDto create(
            @AuthenticationPrincipal AppPrincipal principal,
            @PathVariable TargetType type,
            @PathVariable Long targetId
    ) {
        return service.createBookmark(principal.getId(), type, targetId);
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AppPrincipal principal,
            @PathVariable Long bookmarkId
    ) {
        service.deleteBookmark(principal.getId(), bookmarkId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{type}/{targetId}")
    public boolean isBookmarked(
            @AuthenticationPrincipal AppPrincipal principal,
            @PathVariable TargetType type,
            @PathVariable Long targetId
    ) {
        return service.isBookmarked(principal.getId(), type, targetId);
    }
}
