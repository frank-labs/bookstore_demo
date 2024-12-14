package ca.uwo.bookstore.controller;

import ca.uwo.bookstore.models.BookList;
import ca.uwo.bookstore.repository.BookListMapper;
import ca.uwo.bookstore.repository.BookListRepository;
import ca.uwo.bookstore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/")
public class BookListController {
    @Autowired
    AuthenticationManager authenticationManager;
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    BookListRepository booklistRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BookListMapper bookListMapper;

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

    @GetMapping("/booklists")
    public ResponseEntity<Map<String, Object>> getAllBookListsPage(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        try {
            List<Sort.Order> orders = new ArrayList<Sort.Order>();

            if (sort[0].contains(",")) {
                // will sort more than 2 fields
                // sortOrder="field, direction"
                for (String sortOrder : sort) {
                    String[] _sort = sortOrder.split(",");
                    orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
                }
            } else {
                // sort=[field, direction]
                orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
            }

            List<BookList> booklists = new ArrayList<BookList>();
            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

            Page<BookList> pageTuts;
            if (title == null)
                pageTuts = booklistRepository.findAll(pagingSort);
            else
                pageTuts = booklistRepository.findByNameContaining(title, pagingSort);

            booklists = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("booklists", booklists);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/booklists/{id}")
    public ResponseEntity<BookList> getBookListById(@PathVariable("id") long id) {
        Optional<BookList> booklistData = booklistRepository.findById(id);

        if (booklistData.isPresent()) {
            return new ResponseEntity<>(booklistData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/booklists")
    public ResponseEntity<BookList> createBookList(@RequestBody BookList booklist) {
        try {
            BookList _booklist = booklistRepository
                    .save(new BookList(booklist.getVisibility(), booklist.getName(), booklist.getBooks()));
            return new ResponseEntity<>(_booklist, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/booklists/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookList> updateBookList(@PathVariable("id") long id, @RequestBody BookList booklist) {
        Optional<BookList> booklistData = booklistRepository.findById(id);

        if (booklistData.isPresent()) {
            BookList _booklist = booklistData.get();
            _booklist.setBooks(booklist.getBooks());
            _booklist.setName(booklist.getName());
            _booklist.setVisibility(booklist.getVisibility());
            return new ResponseEntity<>(booklistRepository.save(_booklist), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/booklists/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteBookList(@PathVariable("id") long id) {
        try {
            booklistRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
