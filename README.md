# LearnSpringSecurity

## Project Setup

- jjwt api
- jjwt impl
- jjwt jackson


## Setting Username and Password in config file

```
spring.security.user.name=amol
spring.security.user.password=amol
```

## Create handler Function which will return homepage
```
@RestController
public class HomepageController {

    @GetMapping("/")
    public String homePath(){
        return "Welcome to Spring Security";
    }
}
```

## Test Application
![image](https://github.com/user-attachments/assets/c63df08a-30e3-4a49-b963-5aae6d573df1)


## Create method for Csrf Token

```
    @GetMapping("/csrfToken")
    public CsrfToken getToken(HttpServletRequest http){
        return (CsrfToken) http.getAttribute("_csrf");
    }
```

## Test it..
![image](https://github.com/user-attachments/assets/9ee6a30e-f8db-4f0e-8e2a-bb749af5c3a9)


## Create List of Employee and Add Employee
```
@RestController
public class CsrfDemoController {

    ArrayList<Employee> list=new ArrayList<>(List.of(
            new Employee(1, "A"),
            new Employee(2,"B"),
            new Employee(3,"C")
    ));
    @GetMapping("/csrfToken")
    public CsrfToken getToken(HttpServletRequest http){
        return (CsrfToken) http.getAttribute("_csrf");
    }

    @PostMapping("/addEmployee")
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee emp){
        list.add(emp);
        return ResponseEntity.status(HttpStatus.OK).body(emp);
    }
}
```
## Add Employee without csrf token
![image](https://github.com/user-attachments/assets/7050099e-b446-4d5a-b4b1-2545142570ac)



![image](https://github.com/user-attachments/assets/b63313ed-3348-4ff3-8536-e6d54eccffc7)


Even though we have passed username and password still this request is failed with the 401 status code. Any modification in the date requires csrf token if its enabled.

## Get CSRF Token 
![image](https://github.com/user-attachments/assets/c6572434-0e55-4e53-9951-4dd0e14f0202)





## Now Add Employee using this token
![image](https://github.com/user-attachments/assets/ff64e13a-43a0-400f-a01b-14b56ab3d4c9)


## Disable Spring Security Autocofiguration:
This can be achieved by adding annotation 

```
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.build();
    }
}
```

- Above SecurityFilterChain doesn't have any applied filters, everyone can access the application without credentials.

```
 @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.authorizeHttpRequests(requests -> requests.anyRequest().authenticated());
        return httpSecurity.build();
    }
```
- Above FIlter chain have disabled csrf and made all the requests authenticated.
- But it doesn't provided any utility to the user to provide the username and password to access the resource.

```
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.authorizeHttpRequests(requests -> requests.anyRequest().authenticated());
        httpSecurity.formLogin(Customizer.withDefaults());
        return httpSecurity.build();
    }
```
- Above filter chain will provide the login form to the user, but this will return login page content from the postman.

![image](https://github.com/user-attachments/assets/6c030194-66e0-4adb-9f30-1c2f234a16c6)

- to see the actual page add below filter
  ```
  httpSecurity.httpBasic(Customizer.withDefaults());
  ```

  ![image](https://github.com/user-attachments/assets/823f760f-a777-46a6-a09b-10ab17395e35)


## Customize UserDetailsService

- Create bean inside SecurityConfig file for UserDetailsService:
  ```
  @Bean
    public UserDetailsService userDetailsService(){
        return new InMemoryUserDetailsManager();
    }
  ```
- Try accessing the application using the default user and password which we have set in application.properties file
![image](https://github.com/user-attachments/assets/9df43983-f4d7-413a-b616-59fcee777e08)

- It won't work as we havent provided the proper implementation. We need to user the UserDetails. modify bean like below
```
@Bean
    public UserDetailsService userDetailsService(){
        UserDetails userDetails= User
                                  .withDefaultPasswordEncoder()//User.withDefaultPasswordEncoder() is considered unsafe for production and is only intended for sample applications
                                  .username("amolm") 
                                  .password("amol123")
                                  .roles("ADMIN")
                                  .build();
        return new InMemoryUserDetailsManager();
    }
```
- Test application
  ![image](https://github.com/user-attachments/assets/ab9ef0f0-9860-4266-afd5-7076572f5bba)



  ## Let's work with AuthenticationProvider(load users from database)
  - AuthenticationProvider provide Authentication object
  - create bean for AuthenticationProvider
```
   @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider= new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }
```
- this bean does not have any password encoder. and it does have dependency on the UserDetailsService.
- create a class which will provide the implementation for UserDetailsService and implement the method `loadUserByUsername` which returns UserDetails(interface) object.
```
package com.abm.SpringSecurityDemo.service;

import com.abm.SpringSecurityDemo.entity.Users;
import com.abm.SpringSecurityDemo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    public UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user=userRepository.getUserByUsername(username);
        if(user==null){
            System.out.println("User not found");
            throw new UsernameNotFoundException("UsernameNotFoundException");
        }
        return new UserDetailsImpl(user);
    }
}
```

- create UserRepo which will load the users from database based on the username.
```
package com.abm.SpringSecurityDemo.repositories;

import com.abm.SpringSecurityDemo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    @Query(nativeQuery = true,value = "SELECT * FROM spring_security_demo.users where username=:username")
    Users getUserByUsername(String username);
}

```
-  User model:
```
package com.abm.SpringSecurityDemo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name="users")
public class Users {

    @Id
    private int id;
    private String username;
    private String password;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

- create class which will implement UserDetails, implement all the methods.
```
package com.abm.SpringSecurityDemo.service;

import com.abm.SpringSecurityDemo.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;


public class UserDetailsImpl implements UserDetails {
    private Users user;

    public UserDetailsImpl(Users user){
        this.user=user;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

```
- Test application




## UserRegistration with encrypted pwd
```
package com.abm.SpringSecurityDemo.controllers;

import com.abm.SpringSecurityDemo.entity.Users;
import com.abm.SpringSecurityDemo.service.UserRegitrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRegistrationController {

    @Autowired
    private UserRegitrationService service;


    private final BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder(10);

    @PostMapping("/register")
    public ResponseEntity<Users> registration(@RequestBody Users user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Users user1= service.register(user);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}

```
- Modify bean AuthenticationProvider, allow verification of password with BcryptPasswordEncoder


 ```
@Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider= new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder(10));
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }
```

## Login Functionality and Verification:
- Create Bean of AuthenticationManager in SecurityConfig class
```
@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
```
- Create login controller
```
package com.abm.SpringSecurityDemo.controllers;

import com.abm.SpringSecurityDemo.entity.Users;
import com.abm.SpringSecurityDemo.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserLoginController {

    @Autowired
    UserLoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<String> doLogin(@RequestBody  Users user){
        String resp= loginService.verifyUserDetails(user);
        return  ResponseEntity.status(HttpStatus.OK).body(resp);
    }
}
```
- Create UserLoginService class and implement verifyUserDetails method
```
package com.abm.SpringSecurityDemo.service;

import com.abm.SpringSecurityDemo.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserLoginService {

    @Autowired
    private AuthenticationManager authManager;


    public String verifyUserDetails(Users user){
        Authentication authentication= authManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword()));
        if (authentication.isAuthenticated()) {
            return "User Successfully Authenticated";
        }
        return "User Failed to Authenticate";
    }
}
```
- Test
![image](https://github.com/user-attachments/assets/0327ed24-be82-4e29-bb6a-3ec975d067ae)


## JWT
- Modified SecurityFilterChain bean which will allow user to access the /register and /login url without username and password.
  ```
 @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                 .csrf(customizer-> customizer.disable())
                 .authorizeHttpRequests(requests->
                         requests.requestMatchers("register", "login")
                                 .permitAll()
                                 .anyRequest().authenticated())
                 .httpBasic(Customizer.withDefaults())
                 .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .build();
    }

```

  


  


  









