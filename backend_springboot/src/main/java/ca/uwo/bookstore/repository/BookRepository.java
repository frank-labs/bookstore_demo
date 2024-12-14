package ca.uwo.bookstore.repository;



import ca.uwo.bookstore.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitle(String title);

    Boolean existsByTitle(String title);

    Page<Book> findByTitleContaining(String title, Pageable pageable);

    List<Book> findAll();

//    @Query("SELECT t FROM Book t WHERE t.published=?1")
//    List<Book> findByPublished(boolean isPublished);

    @Query("SELECT t FROM Book t WHERE t.title LIKE %?1%")
    List<Book> findByTitleLike(String title);

    @Query("SELECT t FROM Book t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', ?1,'%'))")
    List<Book> findByTitleLikeCaseInsensitive(String title);

//    @Query("SELECT b FROM Book b JOIN b.users a WHERE a.id = :userId")
    Page<Book> findBooksByUsersId(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT b FROM Book b JOIN b.users a WHERE a.id = :userId AND b.title LIKE %:title%")
    Page<Book> findBooksByUserIdAndTitleContaining(@Param("title") String title, @Param("userId") Long userId, Pageable pageable);

//    @Transactional
//    @Modifying
//    @Query("UPDATE Book t SET t.published=true WHERE t.id=?1")
//    int publishBook(Long id);

//    @Query("SELECT b FROM Book b join User u b.level >= ?1")
//    List<Book> findByLevelGreaterThanEqual(int level);

//    @Query("SELECT b FROM Book b JOIN b.courses c WHERE c.id = :username")
//    List<Book> findByCourseId(@Param("username") String username);
//
//    @Query("SELECT t FROM Book t WHERE t.createdAt >= ?1")
//    List<Book> findByDateGreaterThanEqual(Date date);
//
//    @Query("SELECT t FROM Book t WHERE t.level BETWEEN ?1 AND ?2")
//    List<Book> findByLevelBetween(int start, int end);
//
//    @Query("SELECT t FROM Book t WHERE t.published=:isPublished AND t.level BETWEEN :start AND :end")
//    List<Book> findByLevelBetween(@Param("start") int start, @Param("end") int end, @Param("isPublished") boolean isPublished);
//
//    @Query("SELECT t FROM Book t WHERE t.createdAt BETWEEN ?1 AND ?2")
//    List<Book> findByDateBetween(Date start, Date end);

    @Query("SELECT t FROM Book t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword,'%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword,'%'))")
    List<Book> findByTitleContainingOrDescriptionContainingCaseInsensitive(String keyword);

//    @Query("SELECT t FROM Book t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title,'%')) AND t.published=:isPublished")
//    List<Book> findByTitleContainingCaseInsensitiveAndPublished(String title, boolean isPublished);
//
//    @Query("SELECT t FROM Book t ORDER BY t.level DESC")
//    List<Book> findAllOrderByLevelDesc();

//    @Query("SELECT t FROM Book t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', ?1,'%')) ORDER BY t.level ASC")
//    List<Book> findByTitleOrderByLevelAsc(String title);

//    @Query("SELECT t FROM Book t WHERE t.published=true ORDER BY t.createdAt DESC")
//    List<Book> findAllPublishedOrderByCreatedDesc();

    @Query("SELECT t FROM Book t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', ?1,'%'))")
    List<Book> findByTitleAndSort(String title, Sort sort);
//
//    @Query("SELECT t FROM Book t WHERE t.published=?1")
//    List<Book> findByPublishedAndSort(boolean isPublished, Sort sort);

    // Pagination and Sorting with Pageable
    @Query("SELECT t FROM Book t")
    Page<Book> findAllWithPagination(Pageable pageable);

//    @Query("SELECT t FROM Book t WHERE t.published=?1")
//    Page<Book> findByPublishedWithPagination(boolean isPublished, Pageable pageable);

    @Query("SELECT t FROM Book t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', ?1,'%'))")
    Page<Book> findByTitleWithPagination(String title, Pageable pageable);
}
