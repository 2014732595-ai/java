package com.example.secondhand.controller.user;

import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Category;
import com.example.secondhand.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> listCategories() {
        List<Category> list = categoryService.list();
        return Result.success(list);
    }
}
