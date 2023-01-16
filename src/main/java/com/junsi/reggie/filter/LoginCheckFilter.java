package com.junsi.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.junsi.reggie.common.BaseContext;
import com.junsi.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录--过滤器
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")  // 过滤来自所有路径的请求
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest; // 向下转型
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. 获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}", requestURI);

        String[] urls = new String[]{  // 直接放行的请求
                "/employee/login",
                "/employee/logout",
                "/backend/**",   // 放行静态页面（不包含动态数据，动态的会有controller处理）
                "/front/**",
                "/common/**",
                "/user/sendMsg", // 移动端发送短信
                "/user/login" // 移动端登录
        };

        // 2. 判断本次请求是否需要处理
        boolean check = check(requestURI, urls);

        // 3. 若不需处理，直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4-1. 判断登录状态，若已登录，直接放行
        if (request.getSession().getAttribute("employee") !=null) {
            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("employee"));

            Long empId = (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
//            long id = Thread.currentThread().getId();
//            log.info("线程id为：{}", id);

            filterChain.doFilter(request, response);
            return;
        }

        // 4-2. 判断移动端登录状态，若已登录，直接放行
        if (request.getSession().getAttribute("user") !=null) {
            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("user"));

            Long userId = (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
//            long id = Thread.currentThread().getId();
//            log.info("线程id为：{}", id);

            filterChain.doFilter(request, response);
            return;
        }

        // 5. 未登录，返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("用户未登录");
    }

    /**
     * 路径匹配，检查本次请求是否放行
     * @param requestURI
     * @param urls
     * @return
     */
    public boolean check(String requestURI, String urls[]) {
        for (String url: urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true; // 放行
            }
        }
        return false;
    }
}
