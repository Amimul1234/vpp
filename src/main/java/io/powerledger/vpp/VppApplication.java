package io.powerledger.vpp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class VppApplication {

    public static void main(String[] args) {
        SpringApplication.run(VppApplication.class, args);
    }

}
