package sc.whorl.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;

import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(value = "sc.whorl.web.dao",annotationClass = Repository.class)
public class MyProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyProjectApplication.class,args);
    }
}