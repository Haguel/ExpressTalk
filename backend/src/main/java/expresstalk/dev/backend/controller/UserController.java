package expresstalk.dev.backend.controller;

import expresstalk.dev.backend.dto.request.EditUserDto;
import expresstalk.dev.backend.dto.response.GetUserChatsDto;
import expresstalk.dev.backend.dto.response.ImageId;
import expresstalk.dev.backend.entity.User;
import expresstalk.dev.backend.enums.UserStatus;
import expresstalk.dev.backend.service.ChatService;
import expresstalk.dev.backend.service.SessionService;
import expresstalk.dev.backend.service.UserService;
import expresstalk.dev.backend.utils.Converter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final SessionService sessionService;
    private final ChatService chatService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description =  "User with provided login doesn't exist"),
            @ApiResponse(responseCode = "404", description =  "User with provided id doesn't exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{loginOrId}")
    public User getUserByLoginOrId(@PathVariable String loginOrId) {
        UUID id = UUID.randomUUID();
        String login = loginOrId;
        try {
            id = UUID.fromString(loginOrId);
        } catch (Exception ex) {
            return userService.findByLogin(login);
        }

        return userService.findById(id);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/self")
    public User getSelf(HttpServletRequest request) {
        UUID userId = sessionService.getUserIdFromSession(request);

        return userService.findById(userId);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/chats")
    @ResponseBody
    public GetUserChatsDto getChatsPage(HttpServletRequest request) {
        UUID userId = sessionService.getUserIdFromSession(request);
        User user = userService.findById(userId);

        try {
            userService.handleStatusTo(userId, UserStatus.ONLINE);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }

        return chatService.getChats(user);
    }

    @PostMapping("/avatar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "Invalid file type provided. Only image acceptable"),
            @ApiResponse(responseCode = "404", description = "User is not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void setAvatarImage(@RequestParam("avatarImage") MultipartFile avatarImage, HttpServletRequest request) {
        UUID userId = sessionService.getUserIdFromSession(request);
        User user = userService.findById(userId);

        userService.setAvatarImage(user, avatarImage);
    }

    @GetMapping("/avatar/{userStrId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Unable to convert provided UUID from path"),
            @ApiResponse(responseCode = "403", description = "Invalid file type provided. Only image acceptable"),
            @ApiResponse(responseCode = "403", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "User is not found"),
            @ApiResponse(responseCode = "404", description = "User does not have avatar image"),
            @ApiResponse(responseCode = "500", description = "Error downloading an image"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity getAvatarImage(@PathVariable String userStrId, HttpServletRequest request) {
        sessionService.ensureSessionExistense(request);

        UUID userId;
        try {
            userId = Converter.convertStringToUUID(userStrId);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to convert provided UUID from path");
        }

        User user = userService.findById(userId);
        byte[] imageData = userService.getAvatarImage(user);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageData);
    }

    @PostMapping("/edit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "User is not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public User editUser(@RequestBody @Valid EditUserDto editUserDto, HttpServletRequest request) {
        UUID userId = sessionService.getUserIdFromSession(request);
        User user = userService.findById(userId);

        return userService.editUser(user, editUserDto);
    }
}
