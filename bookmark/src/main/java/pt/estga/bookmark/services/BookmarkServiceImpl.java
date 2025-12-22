package pt.estga.bookmark.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.bookmark.dtos.BookmarkDto;
import pt.estga.bookmark.entities.Bookmark;
import pt.estga.bookmark.mappers.BookmarkMapper;
import pt.estga.bookmark.repositories.BookmarkRepository;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.content.repositories.MonumentRepository;
import pt.estga.shared.enums.TargetType;
import pt.estga.user.entities.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MonumentRepository monumentRepository;
    private final MarkRepository markRepository;
    private final MonumentMapper monumentMapper;
    private final MarkMapper markMapper;
    private final BookmarkMapper mapper;

    @Override
    @Transactional
    public BookmarkDto createBookmark(User user, TargetType type, Long targetId) {

        bookmarkRepository.findByUserIdAndTargetTypeAndTargetId(user.getId(), type, targetId)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Bookmark already exists");
                });

        Object content = switch (type) {
            case MONUMENT -> monumentRepository.findById(targetId)
                    .map(monumentMapper::toResponseDto)
                    .orElseThrow(() -> new IllegalArgumentException("Monument not found"));

            case MARK -> markRepository.findById(targetId)
                    .map(markMapper::markToMarkDto)
                    .orElseThrow(() -> new IllegalArgumentException("Mark not found"));

            default -> null;
        };

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .targetType(type)
                .targetId(targetId)
                .build();

        bookmarkRepository.save(bookmark);

        BookmarkDto dto = mapper.toDto(bookmark);
        return new BookmarkDto(dto.id(), dto.type(), dto.targetId(), content);
    }

    @Override
    public List<BookmarkDto> getUserBookmarks(User user) {
        return bookmarkRepository.findAllByUserId(user.getId())
                .stream()
                .map(b -> {
                    Object content = switch (b.getTargetType()) {
                        case MONUMENT -> monumentRepository.findById(b.getTargetId())
                                .map(monumentMapper::toResponseDto)
                                .orElse(null);

                        case MARK -> markRepository.findById(b.getTargetId())
                                .map(markMapper::markToMarkDto)
                                .orElse(null);

                        default -> null;
                    };
                    BookmarkDto dto = mapper.toDto(b);
                    return new BookmarkDto(dto.id(), dto.type(), dto.targetId(), content);
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteBookmark(User user, Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found"));
        bookmarkRepository.delete(bookmark);
    }

    @Override
    public boolean isBookmarked(User user, TargetType type, Long targetId) {
        return bookmarkRepository
                .findByUserIdAndTargetTypeAndTargetId(user.getId(), type, targetId)
                .isPresent();
    }
}