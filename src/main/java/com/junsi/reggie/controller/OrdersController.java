package com.junsi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.junsi.reggie.common.BaseContext;
import com.junsi.reggie.common.R;
import com.junsi.reggie.dto.OrdersDto;
import com.junsi.reggie.entity.AddressBook;
import com.junsi.reggie.entity.OrderDetail;
import com.junsi.reggie.entity.Orders;
import com.junsi.reggie.entity.User;
import com.junsi.reggie.service.AddressBookService;
import com.junsi.reggie.service.OrderDetailService;
import com.junsi.reggie.service.OrdersService;
import com.junsi.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据为：{}", orders);

        ordersService.submit(orders);

        return R.success("提交成功");
    }

    /**
     * 移动端查看订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize, HttpServletRequest request) {
        // 创建分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> dtoPageInfo = new Page<>(page, pageSize);

        // 创建条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, request.getSession().getAttribute("user"));
        queryWrapper.orderByDesc(Orders::getOrderTime);

        // 查询
        ordersService.page(pageInfo, queryWrapper);

        // 转移到dto中
        BeanUtils.copyProperties(pageInfo, dtoPageInfo, "records");

        List<Orders> orders = pageInfo.getRecords();
        List<OrdersDto> ordersDtos = orders.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);

            // 查找订单明细对象
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId, item.getNumber());
            List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper1);

//            ordersDto.setOrderDetails(orderDetails);
            int totalCount = 0;
            for (OrderDetail od : orderDetails) {
                totalCount += od.getNumber();
            }
            ordersDto.setSumNum(totalCount);
            return ordersDto;
        }).collect(Collectors.toList());

        dtoPageInfo.setRecords(ordersDtos);

        return R.success(dtoPageInfo);
    }

    /**
     * 管理端查看订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> adminPage(int page, int pageSize, String number, String beginTime, String endTime) {
        // 创建分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> dtoPageInfo = new Page<>(page, pageSize);

        // 创建条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 过滤条件
        queryWrapper.like(number != null, Orders::getNumber, number); // 找订单号相同的
        queryWrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
        queryWrapper.le(endTime != null, Orders::getOrderTime, endTime);
        // 排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        // 查询
        ordersService.page(pageInfo, queryWrapper);

        // 转移到dto中
        BeanUtils.copyProperties(pageInfo, dtoPageInfo, "records");
        List<Orders> orders = pageInfo.getRecords();
        List<OrdersDto> ordersDtos = orders.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);

            ordersDto.setUserName(String.valueOf(item.getUserId()));

            return ordersDto;
        }).collect(Collectors.toList());

        dtoPageInfo.setRecords(ordersDtos);

        return R.success(dtoPageInfo);
    }

    /**
     * 更改订单状态是否派送
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> edit(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return R.success("派送成功");
    }

    /**
     * 再来一单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {
        ordersService.again(orders);
        return R.success("再来一单成功");
    }
}
