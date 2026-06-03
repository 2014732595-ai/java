package com.example.secondhand.controller.admin;

import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Comment;
import com.example.secondhand.service.AdminService;
import com.example.secondhand.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AdminService adminService;

    @GetMapping
    public Result<List<Comment>> listComments(
            @RequestParam(required = false) Long productId) {
        if (productId != null) {
            return Result.success(commentService.getComments(productId));
        }
        return Result.success(commentService.list());
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return Result.success();
    }
}
