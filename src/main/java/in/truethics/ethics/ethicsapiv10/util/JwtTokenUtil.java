package in.truethics.ethics.ethicsapiv10.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.service.user_service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenUtil {
    @Autowired
    private UserService userService;
    private String SECRET_KEY = "SECRET_KEY";

    public String getUsernameFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();
        return username;
    }

    public Users getUserDataFromToken(String jwtToken){
        String userName = this.getUsernameFromToken(jwtToken);
        if(userName != null) {
            Users user = (Users) userService.findUser(userName);
            return user;
        }
        return null;
    }

}
