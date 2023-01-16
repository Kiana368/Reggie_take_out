package com.junsi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.junsi.reggie.common.R;
import com.junsi.reggie.dto.DishDto;
import com.junsi.reggie.entity.Category;
import com.junsi.reggie.entity.Dish;
import com.junsi.reggie.entity.DishFlavor;
import com.junsi.reggie.service.CategoryService;
import com.junsi.reggie.service.DishFlavorService;
import com.junsi.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 创建分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        // 由于dish对象不含有分类名称，需要用一个更广的类对象DishDto来存信息返回
        Page<DishDto> dishDtoPageInfo = new Page<>(page, pageSize);

        // 创建查询构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        // 排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        // 执行分页查询 (pageInfo被填满）
        dishService.page(pageInfo, queryWrapper);

        // 对象拷贝 (不拷贝records，也就是包含有Dish对象的列表）
        BeanUtils.copyProperties(pageInfo, dishDtoPageInfo, "records");
        // 处理，对于查到的所有Dish对象都加上对应的分类名称，用Dto装
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            // 最后需要返回一个DishDto对象
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            // 找分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();

                // 装进一个DishDto对象里
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        // 把得到的dishDto对象放进分页器里
        dishDtoPageInfo.setRecords(list);
        return R.success(dishDtoPageInfo);
    }

    /**
     * 修改菜品，回显菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) { // 从路径中获取的参数
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> edit(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品信息成功");
    }

    /**
     * 菜品发售状态更新
     * @param request
//     * @param dish
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> stop(HttpServletRequest request, String id, @PathVariable int status) {
        // 要修改的若干个菜品的id
        String[] ids = id.split(",");

        // 更新信息
        for (String s: ids){
            Dish dish = new Dish();
            dish.setId(Long.valueOf(s));
            dish.setStatus(status);
            dish.setUpdateTime(LocalDateTime.now());
            dish.setUpdateUser((Long) request.getSession().getAttribute("employee"));

            dishService.updateById(dish);
        }
        return R.success("菜品信息修改成功");
    }

    /**
     * 删除菜品及口味信息
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(String id) {
        // 要修改的若干个菜品的id
        String[] ids = id.split(",");
        for(String s: ids) {
            Long dishId = Long.valueOf(s);
            dishService.deleteByIdWithFlavor(dishId);
        }

        return R.success("删除菜品成功");
    }

    /**
     * 根据条件查询相应的菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) { // 传过来的Id封在dish里，更通用
//        // 创建条件构造器
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        // 添加查询条件
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus, 1); // 查询在售的菜品
//        // 添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        // 查询
//        List<Dish> dishes = dishService.list(queryWrapper);
//
//        return R.success(dishes);
//    }

    /**
     * 根据条件查询相应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) { // 传过来的Id封在dish里，更通用
        // 创建条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1); // 查询在售的菜品
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        // 查询
        List<Dish> dishes = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = dishes.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            // 设置分类名
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            // 设置口味
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }

}
