package br.gov.sp.fatec.user.service;

import br.gov.sp.fatec.cadi.service.CadiService;
import br.gov.sp.fatec.representative.service.RepresentativeService;
import br.gov.sp.fatec.security.domain.Authorization;
import br.gov.sp.fatec.security.repository.AuthorizationRepository;
import br.gov.sp.fatec.student.service.StudentService;
import br.gov.sp.fatec.teacher.service.TeacherService;
import br.gov.sp.fatec.user.domain.User;
import br.gov.sp.fatec.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentService studentService;

    @Autowired
    private RepresentativeService representativeService;

    @Autowired
    private CadiService cadiService;

    @Autowired
    private TeacherService teacherService;

    @Transactional
    public User addUser (String login, String password, String authorizationName) {
        Authorization authorization = authorizationRepository.findByName(authorizationName);
        if(authorization == null) {
            // Cria nova
            authorization.setName(authorizationName);
            authorizationRepository.save(authorization);
        }
        User user = new User();
        user.setEmail(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setAuthorizations(new ArrayList<>());
        user.getAuthorizations().add(authorization);
        userRepository.save(user);
        return user;
    }

    public List<User> search(String login) {
        return userRepository.findByNameContainsIgnoreCase(login);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @PreAuthorize("isAuthenticated()")
    public User search(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @PreAuthorize("isAuthenticated()")
    public List<User> all() {
        List<User> retorno = new ArrayList<User>();
        for(User user: userRepository.findAll()) {
            retorno.add(user);
        }
        return retorno;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getLoggedInfo() {
        User user = getUserLoggedIn();

        if (user.isStudent()) {
            return studentService.findById(user.getId());
        } else if (user.isTeacher()) {
            return teacherService.findById(user.getId());
        } else if (user.isCadi()) {
            return cadiService.findById(user.getId());
        } else if (user.isRepresentative()) {
            return representativeService.findById(user.getId());
        }
        return null;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public User save(User user) {
        List<Authorization> authorizations = user.getAuthorizations();

        if(!authorizations.isEmpty()) {
            for(Authorization aut: authorizations) {
                // Se nao existe, cria
                if(aut.getId() == null && authorizationRepository.findByName(aut.getName()) == null) {
                    authorizationRepository.save(aut);
                }
            }
        }
        user.setPassword(user.getPassword());
        return userRepository.save(user);
    }

    public User getUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            User user = findByEmail(authentication.getName());
            return user;
        }
        return null;
    }
}

