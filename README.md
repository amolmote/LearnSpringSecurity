# LearnSpringSecurity

## Project Setup


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








