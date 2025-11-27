/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.util.jwt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * Utily to work with JWT, compatible with 0.13.0
 *
 * @author granadogj
 */
public class JWTUtils
{

  private final SecretKey secretKey;
  private final long expirationTime; //ms
  private final Gson gson;

  /**
   * Constructor
   *
   * @param secretKey Secret key to sign the JWT
   * @param expirationTimeHours Expiration time in hours
   */
  public JWTUtils(String secretKey, long expirationTimeHours)
  {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32)
    { //HS256 requires minimum 32 bytes.
      byte[] pKey = new byte[32];
      System.arraycopy(keyBytes, 0, pKey, 0, keyBytes.length);
      this.secretKey = Keys.hmacShaKeyFor(pKey);
    } 
    else
    {
      this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    this.expirationTime = expirationTimeHours * 60 * 60 * 1000;
    this.gson = new GsonBuilder().setPrettyPrinting().create();
  }

  /**
   * Constructor with default expiration time (8h)
   *
   * @param secretKey Secret key to sign the JWT
   */
  public JWTUtils(String secretKey)
  {
    this(secretKey, 8);
  }

  /**
   * Generate a JWT token with the data
   *
   * @param payload Map with the data
   * @return JWT Token
   */
  public String generateToken(Map<String, Object> payload)
  {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationTime);

    return Jwts.builder()
      .claims(payload) //data
      .issuedAt(now)
      .expiration(expiryDate)
      .signWith(secretKey)
      .compact();
  }

  /**
   * Generate a JWT token from a JSON string
   *
   * @param jsonPayload JSON string with the data
   * @return JWT Tooken
   */
  public String generateTokenFromJson(String jsonPayload)
  {
    @SuppressWarnings("unchecked")
    Map<String, Object> payload = gson.fromJson(jsonPayload, Map.class);
    return generateToken(payload);
  }

  /**
   * Verify and decode a JWT token
   *
   * @param token JWT Token to verify
   * @return Claims (payload) of the JWT Token if valid
   * @throws io.jsonwebtoken.JwtException If the token is no valid
   */
  public Claims verifyToken(String token)
  {
    return Jwts.parser()
      .verifyWith(secretKey)
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  /**
   * Verify it a JWT Token is valid
   *
   * @param token JWT Token to verify
   * @return True if valid, otherwise False
   */
  public boolean isTokenValid(String token)
  {
    try
    {
      verifyToken(token);
      return true;
    } catch (Exception e)
    {
      return false;
    }
  }

  /**
   * Convierte un Map a JSON string
   *
   * @param map Map a convertir
   * @return JSON string
   */
  public String toJson(Map<String, Object> map)
  {
    return gson.toJson(map);
  }

  /**
   * Convierte un JSON string a Map
   *
   * @param json JSON string
   * @return Map con los datos
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> fromJson(String json)
  {
    return gson.fromJson(json, Map.class);
  }
}
