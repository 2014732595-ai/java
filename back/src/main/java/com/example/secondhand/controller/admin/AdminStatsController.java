package com.example.secondhand.controller.admin;

import com.example.secondhand.common.Result;
import com.example.secondhand.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public Result<Map<String, Object>> getStats() {
        return Result.success(adminService.getStats());
    }
}
