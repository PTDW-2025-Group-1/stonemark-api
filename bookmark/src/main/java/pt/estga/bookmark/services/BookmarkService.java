package pt.estga.bookmark.services;

import pt.estga.bookmark.dtos.BookmarkDto;
import pt.estga.bookmark.enums.BookmarkTargetType;
import pt.estga.user.entities.User;

import java.util.List;

public interface BookmarkService {

    BookmarkDto createBookmark(User user, BookmarkTargetType type, Long targetId);

    List<BookmarkDto> getUserBookmarks(User user);

    void deleteBookmark(User user, Long bookmarkId);

    boolean isBookmarked(User user, BookmarkTargetType type, Long targetId);
}
