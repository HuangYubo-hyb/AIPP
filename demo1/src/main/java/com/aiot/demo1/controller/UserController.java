package com.aiot.demo1.controller;

import com.aiot.demo1.entity.User;
import com.aiot.demo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    /**
     * 获取非管理员用户列表，用于任务分配
     * @return 非管理员用户列表
     */
    @GetMapping("/users/non-admin")
    public List<User> getNonAdminUsers() {
        List<User> allUsers = userService.getAllUsers();
        return allUsers.stream()
                .filter(user -> !"ADMIN".equals(user.getRole().getCode()))
                .collect(Collectors.toList());
    }
    
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(userService.encodePassword(user.getPassword()));
        }
        boolean updated = userService.updateById(user);
        if (updated) {
            return ResponseEntity.ok(userService.getById(id));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.removeById(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> payload, HttpSession session) {
        String username = payload.get("username");
        String password = payload.get("password");
        String email = payload.get("email");
        String captcha = payload.get("captcha");
        
        // 验证验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "验证码错误");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // 检查用户名是否已存在
        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "用户名已存在");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(userService.encodePassword(password));
        newUser.setEmail(email);
        newUser.setRole(com.aiot.demo1.enums.UserRole.EMPLOYEE);
        
        // 保存用户
        userService.save(newUser);
        
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("message", "注册成功");
        successResponse.put("user", newUser);
        return ResponseEntity.ok(successResponse);
    }
}