package com.junsi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junsi.reggie.common.CustomException;
import com.junsi.reggie.dto.SetmealDto;
import com.junsi.reggie.entity.Setmeal;
import com.junsi.reggie.entity.SetmealDish;
import com.junsi.reggie.mapper.SetmealMapper;
import com.junsi.reggie.service.SetmealDishService;
import com.junsi.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时保存相应的菜品关系
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐基本信息
        this.save(setmealDto);

        // 保存和套餐内菜品的关联信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 给每一个菜品都加上套餐的Id
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存菜品信息
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐基本信息以及对应的菜品关系信息
     * 若套餐在售卖中则不能删除
     * @param ids
     */
    @Transactional
    @Override
    public void deleteWithDish(List<Long> ids) {
        // 查询套餐状态
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int cnt = this.count(queryWrapper);
        if (cnt > 0) { // 有在售卖中的套餐
            throw new CustomException("套餐在售卖中，不可删除");
        }

        // 删除套餐基本信息
        this.removeByIds(ids);

        // 删除套餐内菜品关系信息
        LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishQueryWrapper);
    }
}
