package easyon.dating.app.services;

import easyon.dating.app.models.Tag;
import easyon.dating.app.models.User;
import easyon.dating.app.repository.UserTagDAO;
import easyon.dating.app.models.UserTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTagService {
    private final UserTagDAO userTagDAO;

    @Autowired
    public UserTagService(UserTagDAO userTagDAO) {
        this.userTagDAO = userTagDAO;
    }

    public List<UserTag> getListOfUserTags() {
        return userTagDAO.getUserTagList();
    }

    public UserTag getUserTag(int userId) {
        return userTagDAO.getUserTag(userId);
    }

    public void addTagToUser(UserTag userTag, int userId) {

        try {
            userTagDAO.addTagToUser(userTag, userId);

        } catch (Exception e) {
            userTagDAO.removeTagFromUser(userTag, userId);

        }
    }




}
