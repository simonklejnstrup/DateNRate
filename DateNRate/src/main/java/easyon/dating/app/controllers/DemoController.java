package easyon.dating.app.controllers;

import easyon.dating.app.models.*;
import easyon.dating.app.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Controller
public class DemoController {
    private final UserService userService;
    private final MessageService messageService;
    private final UserRatingService userRatingService;
    private final FavoriteService favoriteService;
    private final UserTagService userTagService;

    public DemoController(
            UserService userService,
            MessageService messageService,
            UserRatingService userRatingService,
            FavoriteService favoriteService,
            UserTagService userTagService
    ) {
        this.userService = userService;
        this.messageService = messageService;
        this.userRatingService = userRatingService;
        this.favoriteService = favoriteService;
        this.userTagService = userTagService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/login")
    public String loginUser(WebRequest request, Model model) {
        //Retrieve values from HTML form via WebRequest
        String email = request.getParameter("username");
        String pwd = request.getParameter("password");

        User user = userService.login(email, pwd);
        if(user == null){
            model.addAttribute("loginError", true);
            return "index";
        }
        setSessionInfo(request, user);

        return "redirect:/userProfile?userId=" + user.getUserId();
    }

    @GetMapping("/logout")
    public String logout(WebRequest request){
        setSessionInfo(request, null);
        return "redirect:/";
    }



    @PostMapping("/createMessage")
    public String createMessageSubmit(Message message) {
        messageService.createMessage(message);
        return "redirect:/messages?active=" + message.getRecieverId();
    }

    @GetMapping("/messages")
    public String messages(@RequestParam(required = false, name = "active") Integer activeUserId, WebRequest request, Model model) {
        User currentUser = (User) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (currentUser == null) { // If your aren't logged in, redirect to index.html
            return "redirect:/";
        }
        int recieverId = currentUser.getUserId();
        model.addAttribute("currentUser", currentUser);

        List<User> conversationUsers = messageService.getConversationUsers(recieverId);
        if (conversationUsers.size() == 0 && activeUserId == null) { // If you don't have any messages, and not send any, display No messges page.
            return "noMessages";
        }
        if (activeUserId == null) { // Set activeUserId as the first user, as default.
            activeUserId = conversationUsers.get(0).getUserId();
        }

        List<Message> activeConversation = messageService.getConversation(recieverId, activeUserId);
        User activeUser = userService.getUser(activeUserId);
        model.addAttribute("conversationUsers", conversationUsers);
        model.addAttribute("activeConversation", activeConversation);
        model.addAttribute("activeUser", activeUser);

        Message newMessage = new Message();
        newMessage.setRecieverId(activeUserId);
        newMessage.setSenderId(recieverId);
        model.addAttribute("newMessage", newMessage);

        return "messages";
    }


    @GetMapping("/userProfile")
    public String userProfile(@RequestParam int userId, UserTag userTag, WebRequest request, Model model, Favorite favorite, UserRating userRating) {
        User loggedInUser = (User) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (loggedInUser == null) { // If your aren't logged in, redirect to index.html
            return "redirect:/";
        }
        List<Rating> ratings = userRatingService.getRatings();
        favorite.setFavoriteUserId(userId);
        favorite.setUserId(loggedInUser.getUserId());
        model.addAttribute("isMyProfile", loggedInUser.getUserId() == userId);
        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("favorite", favorite);
        model.addAttribute("user", userService.getUser(userId));
        model.addAttribute("userRatings", userRatingService.getEmptyUserRatingArray(ratings.size()));
        model.addAttribute("userRating", userRating);
        List<Rating> ratingList = userRatingService.getRatings();
        model.addAttribute("ratingList", ratingList);
        model.addAttribute("userTag", userTag);
        model.addAttribute("activeTags", userTagService.getActiveTagList(userId));
        model.addAttribute("inactiveTags", userTagService.getInactiveTagList(userId));
        model.addAttribute("ratings", userRatingService.getUserRatings(userId));

        return "userProfile";
    }

    @PostMapping("/userProfile/createUserRating")
    public String createUserRating(UserRating userRating) {
        userRatingService.createUserRating(userRating);
        return "redirect:/userProfile?userId=4";
    }

    @GetMapping("/createUser")
    public String createUser(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("errors", new UserFormError());
        return "createUser";
    }

    @PostMapping("/createUser/submit")
    public String createUserSubmit(User user, Model model, WebRequest request) {
        UserFormError userFormError = userService.getUserFormError(user);
        if(userFormError.containsErrors()){
            model.addAttribute("errors", userFormError);
            model.addAttribute("user", user);
            return "/createUser";
        }
        try {
            User newUser = userService.createUser(user);
            setSessionInfo(request, newUser);
            return "redirect:/userProfile?userId=" + newUser.getUserId();
        }
        catch (SQLException e){
            userFormError = userService.getUserFormError(user);
            model.addAttribute("errors", userFormError);
            model.addAttribute("user", user);
            return "/createUser";
        }
    }

    @GetMapping("/updateUser")
    public String updateUser(WebRequest request, Model model){
        User loggedInUser = (User) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (loggedInUser == null) { // If your aren't logged in, redirect to index.html
            return "redirect:/";
        }
        model.addAttribute("errors", new UserFormError());
        model.addAttribute("user", loggedInUser);
        return "/updateUser";
    }

    @PostMapping("/updateUser/submit")
    public String updateUserSubmit(User user, WebRequest request, Model model){
        User loggedInUser = (User) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (loggedInUser == null) { // If your aren't logged in, redirect to index.html
            return "redirect:/";
        }
        if(loggedInUser.getUserId() != user.getUserId()){
            return "redirect:/";
        }
        UserFormError userFormError = userService.getUserFormError(user, loggedInUser);
        if(userFormError.containsErrors()){
            model.addAttribute("errors", userFormError);
            model.addAttribute("user", user);
            return "/updateUser";
        }
        try {
            User updatedUser = userService.updateUser(user);
            setSessionInfo(request, updatedUser);
            return "redirect:/userProfile?userId=" + user.getUserId();
        }
        catch (SQLException e){
            userFormError = userService.getUserFormError(user, loggedInUser);
            model.addAttribute("errors", userFormError);
            model.addAttribute("user", user);
            return "/updateUser";
        }
    }


    private void setSessionInfo(WebRequest request, User user) {
        // Place user info on session
        request.setAttribute("user", user, WebRequest.SCOPE_SESSION);
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String search, Model model, WebRequest request) {
        User currentUser = (User) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (currentUser == null) { // If your aren't logged in, redirect to index.html
            return "redirect:/";
        }
        if(search != null){
            List<User> searchList = userService.searchUser(search);
            model.addAttribute("searchList", searchList);
        }
        model.addAttribute("currentUser", currentUser);
        return "/search";
    }

    @GetMapping("/favorite")
    public String favorite(Model model, WebRequest request, Favorite favorite) {
        User currentUser = (User) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (currentUser == null) { // If your aren't logged in, redirect to index.html
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        int currentUserId = currentUser.getUserId();
        List<User> favoriteAsUserList = favoriteService.getFavoritesAsUsers(currentUserId);
        if(favoriteAsUserList.size() == 0){
            return "noFavorites";
        }
        model.addAttribute("favoritesAsUsersList", favoriteAsUserList);
        return "favorite";
    }

    @PostMapping("/favorite/submit")
    public String postFavorite(Favorite favorite) {
        favoriteService.addToFavorites(favorite);
        return "redirect:/userProfile?userId=" + favorite.getFavoriteUserId();
    }

    @PostMapping("/favorite/remove")
    public String removeFavorite(Favorite favorite){
        favoriteService.removeFavorite(favorite);
        return "redirect:/favorite";
    }

    @PostMapping("/userTagPost")
    public String addTagToUser(UserTag userTag) {
        userTagService.addTagToUser(userTag);
        return "redirect:/userProfile?userId=" + userTag.getUserId();
    }

    @PostMapping("/postRating")
    public String postRating(UserRating userRating, Model model) {
        model.addAttribute("userRating", userRating);
        userRatingService.createUserRating(userRating);
        return "redirect:/userProfile?userId=" + userRating.getTargetUserId();
    }

    @PostMapping("/uploadProfilePicture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file, WebRequest request) throws IOException {
        User loggedInUser = (User) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (loggedInUser == null) { // If your aren't logged in, redirect to index.html
            return "redirect:/";
        }
        User user = userService.uploadProfilePicture(file, loggedInUser);
        setSessionInfo(request, user);
        return "redirect:/userProfile?userId=" + user.getUserId();
    }

    @GetMapping("/userFrontpage")
    public String userFrontpage(WebRequest request, Model model){
        User loggedInUser = (User) request.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (loggedInUser == null) { // If your aren't logged in, redirect to index.html
            return "redirect:/";
        }
        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("newUsers", userService.getTheFiveNewestProfiles());
        model.addAttribute("topRated", userService.getTopRatedProfiles());
        return "/userFrontpage";
    }


}