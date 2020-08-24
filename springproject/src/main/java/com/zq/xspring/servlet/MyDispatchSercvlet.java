package com.zq.xspring.servlet;

import com.zq.xspring.annotation.XAutowired;
import com.zq.xspring.annotation.XController;
import com.zq.xspring.annotation.XRequestMapping;
import com.zq.xspring.annotation.XService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author zq
 */
public class MyDispatchSercvlet extends HttpServlet {
    /**
     * 配置文件
     */
    private Properties contextConfig = new Properties();
    /**
     * 扫描的包下所有类文件
     */
    private List<String> classNameList = new ArrayList<>();
    /**
     * IOC 容器     类似于bean   首字母小写 ，反射的对象
     */
    Map<String, Object> iocMap = new HashMap<String, Object>();
    /**
     * 路径和方法映射
     */
    Map<String, Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    /**
     * 7、运行阶段，进行拦截，匹配
     *
     * @param req  请求
     * @param resp 响应
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException {
            String url=req.getRequestURI();
            String contextBase=req.getContextPath();
            url=url.replaceAll(contextBase,"").replaceAll("/+","/");
            Method method=handlerMapping.get(url);
            String beanName=toLowerFirstCase(method.getDeclaringClass().getSimpleName());
            method.invoke(iocMap.get(beanName),req,resp);
    }
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        //加载配置文件
        doLoadConfig(servletConfig.getInitParameter("contextConfigLocation"));
        //扫描包路径
        doScanner(contextConfig.getProperty("scan-package"));
        //初始化ioc容器（类似于bean ，存入)
        initIocInstance();
        //注入（类似 controller注入service）     这里只做类似Autoired注解
        doAutowired();
        //创建handmapping
        initHandlerMapping();
        super.init();
    }

    /**
     * 1、加载配置文件
     *
     * @param contextConfigLocation web.xml --> servlet/init-param     application.properties
     */
    private void doLoadConfig(String contextConfigLocation) {

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            // 保存在内存   加载流
            contextConfig.load(inputStream);

            System.out.println("[INFO-1] property file has been saved in contextConfig.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫描相关的类
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        URL resourcePath = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        if (resourcePath == null) {
            return;
        }
        File classPath = new File(resourcePath.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                System.out.println("[INFO-2] {" + file.getName() + "} is a directory.");
                // 子目录递归
                doScanner(scanPackage + "." + file.getName());

            } else {

                if (!file.getName().endsWith(".class")) {
                    System.out.println("[INFO-2] {" + file.getName() + "} is not a class file.");
                    continue;
                }

                String className = (scanPackage + "." + file.getName()).replace(".class", "");

                // 保存在内容
                classNameList.add(className);

                System.out.println("[INFO-2] {" + className + "} has been saved in classNameList.");
            }
        }
    }

    /**
     * 3、初始化 IOC 容器，将所有相关的类实例保存到 IOC 容器中
     */
    private void initIocInstance() {
        if (classNameList.isEmpty()) {
            return;
        }
        try {
            for (String className : classNameList) {
                Class clazz = Class.forName(className);
                String beanName = toLowerFirstCase(clazz.getSimpleName());
                //这里注解不做赋值
                if (clazz.isAnnotationPresent(XController.class)) {
                    //后续使用反射
                    Object instance = clazz.newInstance();
                    iocMap.put(beanName, instance);
                }
                if (clazz.isAnnotationPresent(XService.class)) {
                    //后续使用反射
                    Object instance = clazz.newInstance();
                    iocMap.put(beanName, instance);
                    Class<?>[] classes = instance.getClass().getInterfaces();
                    for (Class c : classes) {
                        if (iocMap.containsKey(c.getName())) {
                            throw new Exception("404");
                        }
                        iocMap.put(c.getName(), instance);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取类的首字母小写的名称
     *
     * @param className ClassName
     * @return java.lang.String
     */
    private String toLowerFirstCase(String className) {
        char[] charArray = className.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

    /**
     * 4、依赖注入
     */
    private void doAutowired() {
        if (iocMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(XAutowired.class)) {
                    continue;
                }
                XAutowired x = field.getAnnotation(XAutowired.class);
                //非首字母小写     这里默认注解不带参数
                //Xautoired括号里面的值
                String beanName = x.value().trim();
                if ("".endsWith(beanName) || beanName == null) {
                    //获取字段的值
                    beanName = field.getType().getName();
                }
                // 只要加了注解，都要加载，不管是 private 还是 protect   设置 public
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), iocMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 5、初始化 HandlerMapping
     */
    private void initHandlerMapping() {
        if(iocMap.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Class<?>  clazz=entry.getValue().getClass();
            String uri="";
            if (clazz.isAnnotationPresent(XRequestMapping.class)) {
                XRequestMapping xRequestMapping=clazz.getAnnotation(XRequestMapping.class);
                uri=xRequestMapping.value();
            }
            Method[] methods=clazz.getMethods();
            for(Method method:methods){
                if (!method.isAnnotationPresent(XRequestMapping.class)) {
                    continue;
                }
                XRequestMapping xRequestMapping=method.getAnnotation(XRequestMapping.class);
                uri=("/"+uri+"/"+xRequestMapping.value()).replaceAll("/+", "/");;
                handlerMapping.put(uri,method);
            }
        }
    }
}
