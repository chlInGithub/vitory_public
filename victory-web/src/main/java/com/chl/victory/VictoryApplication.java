package com.chl.victory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
/*@MapperScan(value = {
		"com.chl.victory.dao.mapper"
})*/
//@tk.mybatis.spring.annotation.MapperScan(value = {"com.chl.victory.dao.mapper"})
@ComponentScan(value = {"com.chl.victory"})
@EnableScheduling
public class VictoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(VictoryApplication.class, args);
    }
}
