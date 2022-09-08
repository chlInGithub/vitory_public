package com.chl.victory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
/*@MapperScan(value = {
		"com.chl.victory.dao.mapper"
})*/
//@tk.mybatis.spring.annotation.MapperScan(value = {"com.chl.victory.dao.mapper"})
@ComponentScan(value = {"com.chl.victory"})
@EnableSwagger2
public class WMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(WMallApplication.class, args);
    }
}
