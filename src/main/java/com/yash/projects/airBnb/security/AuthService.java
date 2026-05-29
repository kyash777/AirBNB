package com.yash.projects.airBnb.security;

import com.yash.projects.airBnb.dto.LoginDTO;
import com.yash.projects.airBnb.dto.SignupRequestDTO;
import com.yash.projects.airBnb.dto.UserDTO;
import com.yash.projects.airBnb.entity.User;
import com.yash.projects.airBnb.entity.enums.Role;
import com.yash.projects.airBnb.exception.ResourceNotFoundException;
import com.yash.projects.airBnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDTO signUp(SignupRequestDTO signupRequestDTO) {

        User user = userRepository.findByEmail(signupRequestDTO.getEmail()).orElse(null);
        if(user!=null){
            throw new RuntimeException("User is already present with same email");
        }

        User newUser = modelMapper.map(signupRequestDTO,User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser,UserDTO.class);

    }

    public String[] login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(), loginDTO.getPassword()
        ));

        User user = (User) authentication.getPrincipal();
        String arr[] = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);
        return arr;

    }

    public String refreshToken(String refreshToken) {
        Long id = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found with ID: " + id));
        return jwtService.generateAccessToken(user);
    }
}
