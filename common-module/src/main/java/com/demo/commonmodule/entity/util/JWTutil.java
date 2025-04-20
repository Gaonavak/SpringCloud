/**
 * @Author :Novak
 * @Description : JWT 鉴权
 * @Date: 2025/4/18 8:19
 */
package com.demo.commonmodule.entity.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTutil {

    private static final String UserName = "admin";

    private static final String key = "token_key";

    public static final int token_time_out = 1000 * 60 ;

    public static String getToken(String userId) {
        JWTCreator.Builder jwtBuilder = JWT.create();

        Map<String, Object> headers = new HashMap<>();

        headers.put("type", "jwt");
        headers.put("alg", "hs256");

        String token = jwtBuilder.withHeader(headers).withClaim("userId", userId).withExpiresAt(new Date(System.currentTimeMillis() + token_time_out)).withIssuedAt(new Date(System.currentTimeMillis())).withIssuer(UserName).sign(Algorithm.HMAC256(key));

        System.out.println(token);
        return token;
    }

//    public static boolen verify(String token) {
//        if (token == null) return false;
//
//        JWTVerifier require = JWT.require(Algorithm.HMAC256(key)).build();
//
//        DecodedJWT decodedJWT;
//        try {
//            decodedJWT = require.verify(token);
//        } catch (Exception e) {
//            return false;
//        }
//
//        Map<String, Claim> claims = decodedJWT.getClaims();
//        if (claims == null) return false;
//        claims.forEach((k, v) -> System.out.println(k + " " + v.asString()));
//
//        if (decodedJWT.getClaim("userId") == null) return false;
//        System.out.println(decodedJWT.getClaim("userId").toString());
//
//        System.out.println(decodedJWT.getIssuer());
//
//        System.out.println((decodedJWT.getExpiresAt()));
//
//        System.out.println(decodedJWT.getSubject());
//        return true;
//    }
}