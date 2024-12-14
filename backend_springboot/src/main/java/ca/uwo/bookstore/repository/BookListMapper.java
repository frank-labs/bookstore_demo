package ca.uwo.bookstore.repository;



import ca.uwo.bookstore.models.BookList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookListMapper {
    @Select("SELECT * FROM booklists,users WHERE users.username=#{username} and users.id=booklists.user_id ")
    List<BookList> getBookListByUSername(String username);
}