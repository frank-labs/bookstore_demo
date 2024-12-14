package ca.uwo.bookstore.repository;




import ca.uwo.bookstore.models.BookList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookListRepository extends JpaRepository<BookList, Long> {
    Optional<BookList> findByName(String name);

    Boolean existsByName(String name);

    Page<BookList> findByNameContaining(String name, Pageable pageable);

    List<BookList> findAll();

}
