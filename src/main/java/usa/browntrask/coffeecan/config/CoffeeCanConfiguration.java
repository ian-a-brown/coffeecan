package usa.browntrask.coffeecan.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import usa.browntrask.coffeecan.CoffeeCanInterceptor;

@Configuration
@ComponentScan("usa.browntrask.coffeecan")
@EnableWebMvc
public class CoffeeCanConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new CoffeeCanInterceptor());
    }
}
