package ca.uwo.bookstore.controller;

//import com.github.pagehelper.PageHelper;


import ca.uwo.bookstore.controller.payload.request.UserBookPair;
import ca.uwo.bookstore.models.Book;
import ca.uwo.bookstore.repository.BookMapper;
import ca.uwo.bookstore.repository.BookRepository;
import ca.uwo.bookstore.repository.UserRepository;
import ca.uwo.bookstore.security.jwt.JwtUtils;
import ca.uwo.bookstore.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/")
public class BookController {
    @Autowired
    AuthenticationManager authenticationManager;
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    BookRepository bookRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BookMapper bookMapper;

    @Autowired
    JwtUtils jwtUtils;

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

    @GetMapping("/books")
    public ResponseEntity<Map<String, Object>> getAllBooksPage(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "36") int size,
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

            List<Book> books = new ArrayList<Book>();
            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

            Page<Book> pageTuts;
            if (title == null)
                pageTuts = bookRepository.findAll(pagingSort);
            else
                pageTuts = bookRepository.findByTitleContaining(title, pagingSort);

            books = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("books", books);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/mybook")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserBooksPage(@CookieValue("jwt") String jwt,
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
            if (jwt == null || !jwtUtils.validateJwtToken(jwt)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid or missing token");
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Extract user details from the Authentication object
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
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
            List<Book> books = new ArrayList<Book>();
            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
//            List<Book> books = bookRepository.findBooksByUserId(userId);
            Page<Book> pageTuts;

            if (title == null)
                pageTuts = bookRepository.findBooksByUsersId(userId,pagingSort);
            else
                pageTuts = bookRepository.findBooksByUserIdAndTitleContaining(title, userId,pagingSort);

            books = pageTuts.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("books", books);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/checkowner")
    public int checkookowner(@RequestBody UserBookPair userBookPair) {
        List<Integer> booksids = bookMapper.getBookIDByUserID(userBookPair.getUserid());
        if (booksids.contains(userBookPair.getBookid())) {
            return 1;
        } else {
            return 0;
        }
    }

    @PostMapping("/buybook")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> buyBook(@RequestBody UserBookPair userBookPair) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String username = userDetails.getUsername();

        List<Integer> booksids = bookMapper.getBookIdByUsername(username);
        if (booksids.contains(userBookPair.getBookid())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "You already bought this one");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        bookMapper.buybook(userBookPair.getUserid(), userBookPair.getBookid());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllBooksPagebyAdmin(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

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

            List<Book> books = new ArrayList<Book>();
            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

            Page<Book> pageTuts;
            if (title == null)
                pageTuts = bookRepository.findAll(pagingSort);
            else
                pageTuts = bookRepository.findByTitleContaining(title, pagingSort);

            books = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("books", books);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") long id) {
        Optional<Book> bookData = bookRepository.findById(id);

        if (bookData.isPresent()) {
            return new ResponseEntity<>(bookData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/books")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        try {
            Book _book = bookRepository
                    .save(new Book(book.getAuthor(), book.getBookformat(), book.getDescription(), book.getGenre(), book.getImg(), book.getIsbn(), book.getLink(), book.getPages(), book.getRating(), book.getReviews(), book.getTitle()));
            return new ResponseEntity<>(_book, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/books/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> updateBook(@PathVariable("id") long id, @RequestBody Book book) {
        Optional<Book> bookData = bookRepository.findById(id);

        if (bookData.isPresent()) {
            Book _book = bookData.get();
            _book.setTitle(book.getTitle());
            _book.setDescription(book.getDescription());
            return new ResponseEntity<>(bookRepository.save(_book), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/books/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteBook(@PathVariable("id") long id) {
        try {
            bookRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
