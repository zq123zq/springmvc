package com.zq.demo.xservice.impl;

import com.zq.demo.xservice.ITestXService;
import com.zq.xspring.annotation.XService;

/**
 * @author
 */
@XService
public class TestXServiceImpl  implements ITestXService {
    @Override
    public String listClassName() {
        // 假装来自数据库
        return "123456TestXServiceImpl";
    }
}
