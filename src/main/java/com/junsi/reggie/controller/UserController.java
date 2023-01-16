package com.junsi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.junsi.reggie.common.R;
import com.junsi.reggie.entity.User;
import com.junsi.reggie.service.UserService;
import com.junsi.reggie.utils.SMSUtils;
import com.junsi.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;


    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(HttpSession httpSession, @RequestBody User user) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            // 生成随机4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);

            // 使用阿里云的短信服务API发送短信
//            SMSUtils.sendMessage("瑞吉外卖", "SMS_267900530", phone, code);

            // 存验证码到session中，方便后续比对
            httpSession.setAttribute(phone, code);

            return R.success("手机验证码发送成功");
        }

        return R.error("手机验证码发送失败");
    }

    /**
     * 移动端用户登录
     * @param user
     * @return
     */
    @PostMapping("/login")
    public R<User> login(HttpSession httpSession, @RequestBody Map user) { // 可以用map来接收json (或者DTO)
        // 获取手机号及验证码
        String phone = user.get("phone").toString();
        String code = user.get("code").toString();

        // 与session中验证码的比对
        String codeInSession = httpSession.getAttribute(phone).toString();

        // 如果比对成功，则登录成功
        if (codeInSession != null && codeInSession.equals(code)) {
            // 判断当前用户是否为新用户，若新则自动完成注册（存到user表中）
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user1 = userService.getOne(queryWrapper);
            if (user1 == null) { // 新用户
                user1 = new User();
                user1.setPhone(phone);
                user1.setStatus(1);
                userService.save(user1);
            }
            httpSession.setAttribute("user", user1.getId());
            return R.success(user1);
        }

        return R.error("登录失败");
    }

    /**
     * 移动端用户登出
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
