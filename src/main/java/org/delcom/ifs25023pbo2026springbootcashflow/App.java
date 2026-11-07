package org.delcom.ifs25023pbo2026springbootcashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point aplikasi Cashflow.
 * Memuat .env terlebih dahulu, baru menjalankan Spring Boot.
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
