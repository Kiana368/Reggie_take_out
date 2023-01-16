package com.junsi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junsi.reggie.dto.DishDto;
import com.junsi.reggie.entity.Dish;
import com.junsi.reggie.entity.DishFlavor;
import com.junsi.reggie.mapper.DishMapper;
import com.junsi.reggie.service.DishFlavorService;
import com.junsi.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>  implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional // 保证数据一致性
    public void saveWithFlavor(DishDto dishDto) {
        // 把基本信息保存到菜品表
        this.save(dishDto);

        // 保存菜品口味数据
        // 菜品ID
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 用流来处理 (也可以for循环处理)
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        // 批量保存
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();

        // 查询菜品基本信息
        Dish dish = this.getById(id);

        // 查询菜品口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        // 赋值
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 更新菜品，同时更新口味信息
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 更新菜品表
        this.updateById(dishDto);

        // 更新口味表
        // 先清理之前的口味数据，再重新提交新的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        // 删除掉当前的菜品记录中的口味
        dishFlavorService.remove(queryWrapper);
        // 提交新的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 批量保存
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品信息及对应的口味信息
     * @param id
     */
    @Transactional
    @Override
    public void deleteByIdWithFlavor(Long id) {
        // 删除菜品信息
        this.removeById(id);

        // 删除口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        dishFlavorService.remove(queryWrapper);
    }
}
