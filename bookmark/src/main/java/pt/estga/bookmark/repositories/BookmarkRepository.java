package pt.estga.bookmark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.bookmark.entities.Bookmark;
import pt.estga.bookmark.enums.BookmarkTargetType;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findAllByUserId(Long userId);

    Optional<Bookmark> findByUserIdAndTargetTypeAndTargetId(Long userId, BookmarkTargetType targetType, Long targetId);

    Optional<Bookmark> findByIdAndUserId(Long id, Long userId);
}
