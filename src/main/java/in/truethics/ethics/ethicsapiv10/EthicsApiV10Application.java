package in.truethics.ethics.ethicsapiv10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class EthicsApiV10Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(EthicsApiV10Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(EthicsApiV10Application.class, args);
        System.out.println("Successfully Executed.......!");
    }

}
