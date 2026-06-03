package com.example.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {

    List<Address> getList(Long userId);

    Long create(Long userId, Address address);

    void update(Long userId, Address address);

    void delete(Long id, Long userId);

    Address getDefault(Long userId);
}
