package easyon.dating.app.data;

import easyon.dating.app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;
    private final String table = "users";

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> selectUsers(){
        return jdbcTemplate.query(
                "SELECT * FROM " + table,
                new UserMapper()
        );
    }

    public User getUser(int id){
        return jdbcTemplate.query("SELECT * FROM " + table + " WHERE user_id =  ?",
               new UserMapper(), id
        ).get(0);
    }

    public void createUser(User user){
        jdbcTemplate.update(
                "INSERT into users(first_name, last_name, email, password, username, date_of_birth) VALUES(?, ?, ?, ?, ?, ?)",
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getUsername(),
                user.getDateOfBirth()
        );
    }
}