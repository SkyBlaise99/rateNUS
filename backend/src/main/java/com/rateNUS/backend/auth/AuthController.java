package com.rateNUS.backend.auth;

import com.rateNUS.backend.exception.InvalidInputException;
import com.rateNUS.backend.security.UserDetailsImpl;
import com.rateNUS.backend.security.jwt.JwtUtils;
import com.rateNUS.backend.user.User;
import com.rateNUS.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serves as the API for authentication.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class AuthController {
    @Autowired
    AuthenticationManager authenticateManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticateManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwt);

        return new ResponseEntity<>(
                new JwtResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles),
                headers,
                HttpStatus.OK);
    }

    @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        String username = signupRequest.getUsername();
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email has been registered."));
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username has been registered."));
        }

        User user = new User();
        try {
            user = new User(username, email, password);
        } catch (InvalidInputException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registration is successful."));
    }
}
