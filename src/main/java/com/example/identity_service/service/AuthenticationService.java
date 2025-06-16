package com.example.identity_service.service;

import com.example.identity_service.dto.request.AuthenticationRequest;
import com.example.identity_service.dto.request.IntrospectRequest;
import com.example.identity_service.dto.request.LogoutRequest;
import com.example.identity_service.dto.request.RefreshRequest;
import com.example.identity_service.dto.response.AuthenticationResponse;
import com.example.identity_service.dto.response.IntrospectResponse;
import com.example.identity_service.entity.InvalidatedToken;
import com.example.identity_service.entity.User;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.repository.InvalidatedTokenRepository;
import com.example.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;


@Service
@RequiredArgsConstructor //Annotation này dùng để Autowire các Bean
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableMethodSecurity //Phân quyền trên method. Nên khi muốn phân quyền trên method nào thì chỉ cần đặt annotation trên method đó
@Slf4j
//Class này có chức năng xác thực người dùng(đăng nhập bằng username, password). Tạo và xác minh JWT token. Quản lý scope người dùng
public class AuthenticationService {
    UserRepository userRepository; //Lấy thông tin của user
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;


    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESH_DURATION;

    //Method này sẽ được thực hiện cuối cùng sau khi token đã được khởi tạo
    public IntrospectResponse introspect(IntrospectRequest request) //IntrospectRequest nhận vào 1 token
            throws JOSEException, ParseException {
        var token = request.getToken(); //Lấy token ra
        //JWSVerifier kiểm tra tính toàn vẹn: Xác nhận token chưa bị sửa đổi, xác thực nguồn gốc: Đảm bảo token được kí bởi 1 bên tin cậy
        //JWSVerifier có nhiệm vụ kiểm tra chữ kí trong JWT có khớp dữ liệu(Header + Payload) không
        //MACVerifier dùng để kiểm tra xem JWT được kí bằng thuật toán HMAC
        //Ví dụ: JWT có header {"alg": "HS256"} sẽ được kiểm tra bằng MACVerifier
        //SIGNER_KEY.getBytes(): chuyển chuỗi chữ kí thàh mảng byte phục vụ cho việc tạo MACVerifier (dùng để xác minh chữ ký JWT với thuật toán HMAC).

        boolean isValid = true;
        try{
            verifyToken(token, false);
        } catch(AppException e){
           isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();

    }
    //Thứ tự sẽ thực hiện method này trước. Method này sẽ xác thực đăng nhập.
    //Đầu tiên sẽ tìm tên user nếu không tồn tại trong database. Sẽ throw ra AppException. Tiếp theo sẽ so sánh password nhập vào và password đã được mã hóa trong database. Néu sai thì throw UNAUTHENTICATED. Cuối cùng là gọi xuống method generateToken để khởi tạo 1 token.
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        log.info("SignKey: ", SIGNER_KEY);

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST)); //Để xác thực (authentication) thì đầu tiên chúng ta cần get thông tin của 1 user
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword()); //Match password của user đã nhập vào và password ta đã persist xuống DBMS
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(user); //Khởi tạo 1 token dựa vào thông tin user

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try{
            var signToken = verifyToken(request.getToken(), true);
            String jti = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch(AppException exception){
            log.info("Token already expired");
        }

    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        //Đầu tiên kiểm tra hiệu lực của token xem token còn hiệu lực hay không
        var signJWT = verifyToken(request.getToken(), true);
        var jti = signJWT.getJWTClaimsSet().getJWTID(); //Lấy Id của token cũ (token đã hết hiệu lực)
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime(); //Lấy ra thời gian hiệu lực của token
        InvalidatedToken invalidatedToken = InvalidatedToken.builder() //Thực hiện log-out token này bằng cách đưa vào table invalidated token
                .id(jti)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signJWT.getJWTClaimsSet().getSubject(); //Lấy ra thông tin của username nằm trong subject

        var user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHENTICATED)
        );

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    //Method verifyToken sẽ có 1 cái cờ truyền vào isRefresh nếu là true thì verify dùng để refresh 1 cái token. Nếu false thì sẽ verify nó cho cái authenticate() hoặc introspec() cái token
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        //SignedJWT phần tích(parse) 1 String token thành đối tượng SignedJWT
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date (signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier); //Xác minh chữ kí để kiểm tra tọken có bị giả mạo không
        if(!(verified && expiryTime.after(new Date()))){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        return signedJWT;
    }

    //Method này sẽ được thực hiện sau method authenticate
    //Method này có chức năng khởi tạo 1 token
    private String generateToken(User user) {
        //Để tạo 1 token thì ta cần dầu tiên là header
        //Header sẽ bao gồm thuật toán mà ta sẽ sử dụng
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        //Tiếp theo ta cần body (nội dung sẽ gửi đi trong token)
        //Để build được payload thì ta cần 1 khái niệm gọi là claim. Các data trong body đươc gọi là claim
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) //Đại diện cho user đăng nhập
                .issuer("example.com") //Xác định token này được issue từ ai
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                )) //Thời gian tồn tại của Token này kéo dài được bao lâu
                .jwtID(UUID.randomUUID().toString()) //Token ID. UUID laf chuỗi có 32 kí tự random ngẫu nhiên có tính chất không trùng.
                .claim("scope", buildScope(user))
                .build();
        //Payload sẽ gồm có constructor lấy từ jwtClaimsSet trên
        Payload payload = new Payload(jwtClaimsSet.toJSONObject()); //Converter ClaimsSet về JSONObject

        //Tạo 1 token bằng thư viện nimbus
        //JWSObject() yêu cầu 2 param 1 là structure tương ứng với token đó bao gồm: header và payload
        //Từ header và payload ta sẽ tạo được JSON Web signature
        JWSObject jwsObject = new JWSObject(header, payload);

        //Tiếp theo là kí token (Signature)
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes())); //Dùng thuật toán để kí token
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.warn("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }
    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner("");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if(CollectionUtils.isEmpty(role.getPermission())){
                    role.getPermission().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }
}
