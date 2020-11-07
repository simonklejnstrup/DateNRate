package easyon.dating.app.data;

import easyon.dating.app.models.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;




public class UserMapper implements RowMapper<User>{


    @Override
    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setTownId(resultSet.getInt("town_id"));
        user.setPassword(resultSet.getString("password"));
        user.setUsername(resultSet.getString("username"));
        user.setProfilePicture(resultSet.getString("profile_picture"));
        user.setDateOfBirth(resultSet.getDate("date_of_birth"));
        user.setCreated(resultSet.getDate("created"));
        user.setUserDescription(resultSet.getString("user_description"));

        return user;
    }





}
