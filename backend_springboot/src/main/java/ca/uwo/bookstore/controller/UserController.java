package ca.uwo.bookstore.controller;

import ca.uwo.bookstore.controller.payload.request.UserBookPair;
import ca.uwo.bookstore.models.User;
import ca.uwo.bookstore.repository.UserMapper;
import ca.uwo.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/")
public class UserController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserMapper userMapper;


    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

    @GetMapping("/userlist")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllBooksPagebyAdmin(
            @RequestParam(required = false) String username,
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

            List<User> userlist = new ArrayList<User>();
            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

            Page<User> pageTuts;

            pageTuts = userRepository.findAll(pagingSort);


            userlist = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("userlist", userlist);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/makeadmin")
    public int makeadmin(@RequestParam(required = true) String userid) {
        int user=Integer.parseInt(userid);
        int success_state = userMapper.makeAdminByUserID(user);
        return success_state;
    }
}
