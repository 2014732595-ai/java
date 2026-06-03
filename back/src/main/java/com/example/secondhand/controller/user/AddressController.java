package com.example.secondhand.controller.user;

import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Address;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.AddressService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/address")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<Address>> listAddresses() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        List<Address> list = addressService.getList(user.getId());
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> createAddress(@RequestBody Address address) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        addressService.create(user.getId(), address);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        address.setId(id);
        addressService.update(user.getId(), address);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        addressService.delete(id, user.getId());
        return Result.success();
    }

    @GetMapping("/default")
    public Result<Address> getDefaultAddress() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        Address address = addressService.getDefault(user.getId());
        return Result.success(address);
    }
}
