package kr.ac.knue.facultyevaluation.config;

import kr.ac.knue.facultyevaluation.auth.SessionAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionAuthenticationInterceptor sessionAuthenticationInterceptor;

    public WebConfig(SessionAuthenticationInterceptor sessionAuthenticationInterceptor) {
        this.sessionAuthenticationInterceptor = sessionAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionAuthenticationInterceptor)
            .addPathPatterns("/api/**");
    }
}
