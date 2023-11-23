package expresstalk.dev.backend.authentication;

import expresstalk.dev.backend.authentication.dto.EmailVerificationDto;
import expresstalk.dev.backend.authentication.dto.SignInUserDto;
import expresstalk.dev.backend.authentication.dto.SignUpUserDto;
import expresstalk.dev.backend.exception.EmailNotVerifiedException;
import expresstalk.dev.backend.user.User;
import expresstalk.dev.backend.user.UserRepository;
import expresstalk.dev.backend.utils.Generator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signUp(SignUpUserDto signUpUserDto) {
        User existedUser = userRepository.findUserByLoginOrEmail(signUpUserDto.login(), signUpUserDto.email());

        if(existedUser != null) {
            if(existedUser.getEmailCode() != null) {
                throw new EmailNotVerifiedException(HttpStatus.FORBIDDEN, "User's email needs verification");
            }

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User already exists");
        }

        String passwordHash = passwordEncoder.encode(signUpUserDto.password());
        String emailCode = Generator.generateCode();

        User newUser = new User(
                signUpUserDto.name(),
                signUpUserDto.login(),
                signUpUserDto.email(),
                passwordHash
        );
        newUser.setEmailCode(emailCode);

        userRepository.save(newUser);

        return newUser;
    };

    public void signIn(SignInUserDto signInUserDto) {
        // signInUserDto.login() can be login or email
        User existedUser = userRepository.findUserByLoginOrEmail(signInUserDto.login(), signInUserDto.login());

        if(existedUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist.");
        }

        if(existedUser.getEmailCode() != null) {
            throw new EmailNotVerifiedException(HttpStatus.FORBIDDEN, "User's email needs verification");
        }

        Boolean isPasswordValid = passwordEncoder.matches(signInUserDto.password(), existedUser.getPasswordHash());

        if(!isPasswordValid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Password or email is not correct.");
        }
    };

    public void makeEmailVerification(EmailVerificationDto emailVerificationDto) {
        User existedUser = userRepository.findUserByLoginOrEmail(emailVerificationDto.email(), emailVerificationDto.email());

        if(existedUser.getEmailCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User's email has already verified.");
        }

        if(!emailVerificationDto.code().equals(existedUser.getEmailCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect code.");
        }

        existedUser.setEmailCode(null);
        userRepository.save(existedUser);
    };
}
