package com.chl.victory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ChenHailong
 * @date 2020/9/1 10:27
 **/
@SpringBootApplication
@tk.mybatis.spring.annotation.MapperScan(value = {"com.chl.victory.dao.mapper"})
@ComponentScan(value = {"com.chl.victory"})
public class BYDubboProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(BYDubboProviderApplication.class, args);
    }
}
