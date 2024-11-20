package com.example.admin_backend.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.admin_backend.Entity.AdminEntity;
import com.example.admin_backend.Service.AdminService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/print")
    public String itWorks() {
        return "It works";
    }

    // Create Admin
    @PostMapping("/insertAdmin")
    public AdminEntity insertAdmin(@RequestBody AdminEntity admin) {
        admin.setStatus(true); // Default status to active
        return adminService.insertAdmin(admin);
    }

    // Retrieve All Admins
    @GetMapping("/getAllAdmins")
    public List<AdminEntity> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    // Update Password
    @PutMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestParam Integer adminId,
                                            @RequestBody Map<String, String> requestBody) {
        String currentPassword = requestBody.get("currentPassword");
        String newPassword = requestBody.get("newPassword");

        try {
            AdminEntity updatedAdmin = adminService.updateAdmin(adminId, newPassword, currentPassword);
            return ResponseEntity.ok(updatedAdmin);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    // Sign-in
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody Map<String, String> loginData) {
        String idNumber = loginData.get("idNumber");
        String password = loginData.get("password");

        try {
            AdminEntity admin = adminService.getAdminByIdNumberAndPassword(idNumber, password);
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID Number or password.");
            }

            if (!admin.getStatus()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is disabled.");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", "dummyToken"); // Replace with actual JWT if applicable
            response.put("adminId", admin.getAdminId());
            response.put("adminname", admin.getAdminname());
            response.put("fullName", admin.getFullName());
            response.put("email", admin.getEmail());
            response.put("idNumber", admin.getIdNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during sign-in.");
        }
    }

    // Retrieve Admin by Username
    @GetMapping("/getByAdminname")
    public ResponseEntity<?> getAdminByAdminname(@RequestParam String adminname) {
        try {
            AdminEntity admin = adminService.getAdminByAdminname(adminname);
            return ResponseEntity.ok(admin);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found.");
        }
    }

    // Update Admin Status
    @PutMapping("/updateStatus")
    public ResponseEntity<?> updateAdminStatus(@RequestBody Map<String, Object> requestBody) {
        String idNumber = (String) requestBody.get("idNumber");
        Boolean status = (Boolean) requestBody.get("status");

        try {
            AdminEntity admin = adminService.getAdminByIdNumber(idNumber);
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found.");
            }

            admin.setStatus(status);
            adminService.saveAdmin(admin);

            return ResponseEntity.ok("Status updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating admin status.");
        }
    }

    // Request Password Reset
    @PostMapping("/requestPasswordReset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        try {
            String resetCode = adminService.generateResetCode(email);
            return ResponseEntity.ok("Reset code sent to " + email);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending reset code.");
        }
    }

    // Verify Reset Code
    @PostMapping("/verifyResetCode")
    public ResponseEntity<?> verifyResetCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String resetCode = requestBody.get("resetCode");

        try {
            adminService.validateResetCode(email, resetCode);
            return ResponseEntity.ok("Reset code verified successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    // Reset Password
    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String newPassword = requestBody.get("newPassword");

        try {
            adminService.resetPassword(email, newPassword);
            return ResponseEntity.ok("Password reset successfully.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error resetting password.");
        }
    }
}
