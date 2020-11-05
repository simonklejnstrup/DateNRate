package easyon.dating.app.controllers;

import easyon.dating.app.models.Message;
import easyon.dating.app.models.Rating;
import easyon.dating.app.models.User;
import easyon.dating.app.models.UserRating;
import easyon.dating.app.services.MessageService;
import easyon.dating.app.services.RatingService;
import easyon.dating.app.services.UserRatingService;
import easyon.dating.app.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DemoController {
    private final UserService userService;
    private final MessageService messageService;
    private  final RatingService ratingService;
    private final UserRatingService userRatingService;

    public DemoController(UserService userService, RatingService ratingService, MessageService messageService, UserRatingService userRatingService) {
        this.userService = userService;
        this.ratingService = ratingService;
        this.messageService = messageService;
        this.userRatingService = userRatingService;
    }

    @GetMapping("/")
    public String index(Model model){
        List<User> user = userService.getListOfUsers();
        model.addAttribute("user", user.get(0));
        return "index";
    }

    @GetMapping("/createUser")
    public String createUser(Model model, User user){
        return "createUser";
    }

    @PostMapping("/createUser/submit")
    public String createUserSubmit(User user){
        userService.createUser(user);
        return "redirect:/";
    }


    @GetMapping("/createMessage")
    public String createMessage(@RequestParam int from, @RequestParam int to, Message message){
        message.setRecieverId(to);
        message.setSenderId(from);
        return "createMessage";
    }

    @PostMapping("/createMessage/submit")
    public String createMessageSubmit(Message message){
        messageService.createMessage(message);
        return "redirect:/messages?id=4";
    }

    @GetMapping("/messages")
    public String messages(@RequestParam Integer userId, Model model){
        model.addAttribute("messages", messageService.getUserMessages(userId));
        return "messages";
    }

    @GetMapping("/userProfile")
    public String userProfile(@RequestParam int userId, Model model){
        List<Rating> ratings = ratingService.getRatings();
        model.addAttribute("ratings", ratings);
        model.addAttribute("user", userService.getUser(userId));
        model.addAttribute("userRatings",userRatingService.getEmptyUserRatingArray(ratings.size()));
        return "userProfile";
    }

    @PostMapping("/userProfile/createUserRating")
    public String createUserRating(UserRating userRating) {
       userRatingService.createUserRating(userRating);
        return "redirect:/userProfile?userId=4";
    }



}