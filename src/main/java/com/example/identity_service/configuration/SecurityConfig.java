package com.example.identity_service.configuration;

import com.example.identity_service.enums.Role;
import com.example.identity_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.crypto.spec.SecretKeySpec;

//Class này dùng để cấu hình cho Security. Tập trung vào việc xác thực JWT và phân quyền endpoint
//Khi spring IoC container được khởi chạy sẽ quét qua tất cả class có annotation @Configuration và khởi tạo các Bean IoC container
@Configuration //Chú thích annotation này để biết class này sẽ cấu hình cho project và cấu hình các Bean
@EnableWebSecurity //Kích hoạt Spring security, cho phép config các security tùy ý
public class SecurityConfig {
    private static final String[] PUBLIC_ENDPOINT = {"/users", "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh"};

    protected static final String SIGNER_KEY = "kgVcXjhib3Tvrb+k5SYM9taZzeGxup6VkTM23IHCHC+MI/xH5t7xvdk4aoThe7Qy"; //Khóa bí mật dùng để kí và xác nhận Token

    private final CustomJwtDecoder customJwtDecoder;

    public SecurityConfig(CustomJwtDecoder customJwtDecoder){
        this.customJwtDecoder = customJwtDecoder;
    }

    @Bean
    //Spring IoC sẽ quản lý SecurityFilterChain.
    //SecurityFilterChain có chức năng:Lọc và xử lý các request. Cấu hình Security: xác định các endpoint được public/authorize.
    /*Khi một HTTP request được gửi đén. Nó sẽ đi qua bộ lọc, cụ thể là sẽ đi qua các chuỗi filters(các bộ lọc) trước khi đến Controller.
    * Filters là các class đứng giữa client và application có nhiệm vụ kiểm tra các chức năng: Authentication, authorization, chống tấn công (csrf)
    * Ví dụ thực tế: Kiểm tra thẻ(token) của 1 nhân viên trước khi cho vào cổng
    * Vậy SecurityFilterChain sẽ quản lý các filters và các filters sẽ xử lý từng request*/

    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        //Filters được dùng ở đây là AuthorizationFilter được sử dụng thông qua method authorizeHttpRequests
        httpSecurity.authorizeHttpRequests(request -> //config những request bằng cách dùng requestMatchers để xác định endpoint nào và sẽ cầu hình ra sao
                request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINT).permitAll() //Xác định endpoint users, auth/token, auth/introspect sẽ được public cho phép truy cập mà không cần security
                        .requestMatchers(HttpMethod.GET, "/users").hasRole(Role.ADMIN.name()) //Những endpoint users với method là get có quyền ADMIN thì mới truy cập vào được
                         .anyRequest().authenticated()
        );
        /*Khi config resource server ta muốn đăng kí provider manager, 1 authentication provider để support JWT token
          nghĩa là khi thực hiện 1 request mà chúng ta cung cấp 1 token vào Auth: Type Bearer Token thì JWT authentication provider sẽ in
          chạc và sẽ thực hiện authentication. Lúc này sẽ cấu hình thêm cho jwt khi thực hiện validate jwt đó thì chúng ta cần jwtDecoder
          thì authentication provider sẽ sử dụng decoder này để thực hiện decode token để biết token hợp lệ hay không
        */

        //Filters được dùng là BearerTokenAuthenticationFilter được sử dụng thông qua method oauth2ResourceServer
        //Có chức năng xác thực JWT token trong header. Sử dụng jwtDecoder() để giải mã token
        //jwtAuthenticationConverter() có chức năng chuyển đổi claims thành role
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        //SecurityConfig của Spring se cung cấp cho chúng ta 1 cái config để chúng ta config được cái điểm mà error xảy ra thì chúng ta sẽ xử lý như sau:
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()) //Khi mà authentication failed thì chúng ta sẽ điều hướng user đi đâu trong trường hợp này chỉ cần trả về 1 error message
        );

        //Spring security mặc định config sẽ bật cấu hình của csrf để bảo vệ endpoint khỏi những attack cross high
        //Trong trường hợp này không cần config này nên sẽ bỏ đi
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

//    @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.addAllowedOrigin("http://localhost:3000"); //Cho phép truy cập cái API này từ những origin nào. Tức là đứng từ trang web nào để truy cập API đó
//        corsConfiguration.addAllowedMethod("*"); //Cho phép method nào được gọi từ origin này
//        corsConfiguration.addAllowedHeader("*"); //Cho phép tất cả các header truy cập có thể đi qua cái filter này không giới hạn header nào cả
//
//        //UrlBasedCorsConfigurationSource là để khai báo Cors theo từng endpoint
//        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
//        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration); //Apply cors cho toàn bộ endpoint của chúng ta. Config được sử dụng ở đây là corsConfiguration được khai báo ở trên
//        return new CorsFilter(urlBasedCorsConfigurationSource);
//    }

    @Bean
    //Class này dùng để converter các scope về authority và sẽ tự động gán prefix là role vào tất cả các scope nhưng vì method buildScope của class AuthenticationService đã chủ động gắn prefix "ROLE_" vào nên method này không cần dùng nữa
    //Tóm lại là class này chuyển đổi prefix từ "SCOPE_" thành "ROLE_"
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); //Bỏ tiền tố mặc định "SCOPE_"
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter); //
        return jwtAuthenticationConverter;
    }


    @Bean
    //Config passwordEncoder() mã hóa mật khẩu với độ mạnh của password là 10
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

}
