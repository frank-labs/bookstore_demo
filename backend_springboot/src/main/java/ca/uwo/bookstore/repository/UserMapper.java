package ca.uwo.bookstore.repository;



import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Update("UPDATE user_roles set role_id=2 WHERE user_id= #{userid}")
    int makeAdminByUserID(int userid);
}