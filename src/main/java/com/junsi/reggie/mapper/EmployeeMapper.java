package com.junsi.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.junsi.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 连接数据库SQL
public interface EmployeeMapper extends BaseMapper<Employee> {

}
