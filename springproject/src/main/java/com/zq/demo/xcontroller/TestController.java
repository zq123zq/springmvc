package com.zq.demo.xcontroller;

import com.zq.demo.xservice.ITestXService;
import com.zq.xspring.annotation.XAutowired;
import com.zq.xspring.annotation.XController;
import com.zq.xspring.annotation.XRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author  zq
 */
@XController
@XRequestMapping("/test")
public class TestController {

    @XAutowired
    ITestXService testXService;

    /**
     * 测试方法 /test/query
     *
     * @param req  请求体
     * @param resp 响应体
     */
    @XRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp) {

        if (req.getParameter("username") == null) {
            try {
                resp.getWriter().write("param username is null");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            String paramName = req.getParameter("username");
            try {
                resp.getWriter().write("param username is " + paramName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[INFO-req] New request param username-->" + paramName);
        }
    }

    /**
     * 测试方法 /test/listClassName
     *
     * @param req  请求体
     * @param resp 响应体
     */
    @XRequestMapping("/listClassName")
    public void listClassName(HttpServletRequest req, HttpServletResponse resp) {
        String str = testXService.listClassName();
        System.out.println("testXService----------=-=-=>" + str);
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
