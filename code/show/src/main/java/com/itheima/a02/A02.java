package com.itheima.a02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
    常见 ApplicationContext 实现
 */
public class A02 {
    private static final Logger log = LoggerFactory.getLogger(A02.class);

    public static void main(String[] args) {
        // 不同测试方法
        testClassPathXmlApplicationContext();
//        testFileSystemXmlApplicationContext();
//        testAnnotationConfigApplicationContext();
//        testAnnotationConfigServletWebServerApplicationContext();

        // 以下代码解释：读取 xml 配置文件是怎么获取到 Bean 的
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        System.out.println("读取之前...");
        for (String name : beanFactory.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        System.out.println("读取之后...");
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(new ClassPathResource("a02.xml"));   // 通过类路径读取
        // reader.loadBeanDefinitions(new FileSystemResource("src\\main\\resources\\a02.xml"));   // 通过磁盘路径读取
        for (String name : beanFactory.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        /*
            学到了什么
                a. 常见的 ApplicationContext 容器实现
                b. 内嵌容器、DispatcherServlet 的创建方法、作用
         */
    }

    // ⬇️较为经典的容器, 基于 classpath 下 xml 格式的配置文件来创建
    // but xml配置现在用的很少了，了解即可
    private static void testClassPathXmlApplicationContext() {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("a02.xml");

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        // 看bean2是否成功依赖注入bean1
        System.out.println(context.getBean(Bean2.class).getBean1());
    }

    // ⬇️基于磁盘路径下 xml 格式的配置文件来创建
    // 和上一个不同的就在于路径写法
    private static void testFileSystemXmlApplicationContext() {
        FileSystemXmlApplicationContext context =
                new FileSystemXmlApplicationContext(
                        "src\\main\\resources\\a02.xml");
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        System.out.println(context.getBean(Bean2.class).getBean1());
    }

    // ⬇️较为经典的容器, 基于 java 配置类/注解配置来创建
    // 相比较于上两种方法，AnnotationConfigApplicationContext 帮我们多加了bean后置处理器，也就是那一堆可以处理@AutoWired、@Resource的东西
    private static void testAnnotationConfigApplicationContext() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(Config.class);

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        // 看bean2是否成功依赖注入bean1
        System.out.println(context.getBean(Bean2.class).getBean1());
    }

    // ⬇️较为经典的容器, 基于 java 配置类来创建, 用于 web 环境  ==》学会内嵌Tomcat怎么工作？
    private static void testAnnotationConfigServletWebServerApplicationContext() {
        AnnotationConfigServletWebServerApplicationContext context =
                new AnnotationConfigServletWebServerApplicationContext(WebConfig.class);
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
    }

    @Configuration
    static class WebConfig {
        // 1. 提供一个内嵌的 Servlet 容器，这样才可以运行 web 项目，这里是Tomcat
        @Bean
        public ServletWebServerFactory servletWebServerFactory() {
            return new TomcatServletWebServerFactory();
        }

        // 2. 提供一个 DispatcherServlet，这样才可以处理请求（前控制器）
        @Bean
        public DispatcherServlet dispatcherServlet() {
            return new DispatcherServlet();
        }

        // 3. 把 DispatcherServlet 注册到 Servlet 容器中
        @Bean
        public DispatcherServletRegistrationBean registrationBean(DispatcherServlet dispatcherServlet) {
            return new DispatcherServletRegistrationBean(dispatcherServlet, "/");
        }

        // 4. 写一个控制器用于测试
        @Bean("/hello")
        public Controller controller1() {
            //  匿名内部类写法
            return new Controller() {
                @Override
                public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
                    response.getWriter().print("hello");
                    return null;
                }
            };

            //     return (request, response) -> {
            //         response.getWriter().print("hello");
            //         return null;
            //     };
            // }
        }
    }

    @Configuration
    static class Config {
        @Bean
        public Bean1 bean1() {
            return new Bean1();
        }

        @Bean
        public Bean2 bean2(Bean1 bean1) {
            Bean2 bean2 = new Bean2();
            bean2.setBean1(bean1);   // 手动建立依赖关系
            return bean2;
        }
    }

    static class Bean1 {
    }

    static class Bean2 {

        private Bean1 bean1;

        public void setBean1(Bean1 bean1) {
            this.bean1 = bean1;
        }

        public Bean1 getBean1() {
            return bean1;
        }
    }
}
