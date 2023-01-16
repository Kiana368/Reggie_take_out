package com.junsi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junsi.reggie.dto.DishDto;
import com.junsi.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    // 新增菜品，同时插入口味数据到口味表中，需操作两张表：dish, dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    // 根据id来查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    // 更新菜品，同时更新对应口味
    public void updateWithFlavor(DishDto dishDto);

    // 根据Id来删除菜品信息和对应口味信息
    public void deleteByIdWithFlavor(Long id);
}
