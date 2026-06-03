package com.example.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.secondhand.common.BusinessException;
import com.example.secondhand.entity.Address;
import com.example.secondhand.mapper.AddressMapper;
import com.example.secondhand.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> getList(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        return list(wrapper);
    }

    @Override
    @Transactional
    public Long create(Long userId, Address address) {
        address.setUserId(userId);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, userId);
            wrapper.eq(Address::getIsDefault, 1);
            List<Address> defaults = list(wrapper);
            for (Address defaultAddr : defaults) {
                defaultAddr.setIsDefault(0);
                updateById(defaultAddr);
            }
        }
        save(address);
        return address.getId();
    }

    @Override
    @Transactional
    public void update(Long userId, Address address) {
        Address existing = getById(address.getId());
        if (existing == null) {
            throw new BusinessException("地址不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此地址");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, userId);
            wrapper.eq(Address::getIsDefault, 1);
            wrapper.ne(Address::getId, address.getId());
            List<Address> defaults = list(wrapper);
            for (Address defaultAddr : defaults) {
                defaultAddr.setIsDefault(0);
                updateById(defaultAddr);
            }
        }
        updateById(address);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Address address = getById(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此地址");
        }
        removeById(id);
    }

    @Override
    public Address getDefault(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getIsDefault, 1);
        return getOne(wrapper);
    }
}
