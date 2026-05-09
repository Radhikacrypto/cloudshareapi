package in.radhika.cloudshareapi.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

@Component
@RequiredArgsConstructor

public class ClerkJwtAuthFilter extends OncePerRequestFilter {
    
    @Value("${clerk.issuer}")
    private String clerkIssuer;


    private final ClerkJwksProvider jwksProvider;

    @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{

    if (request.getRequestURI().contains("/webhooks")|| 
               request.getRequestURI().contains("/public")||
                request.getRequestURI().contains("/download")) {
        filterChain.doFilter(request, response);
        return;
    }


    String authHeader = request.getHeader("Authorization");

     System.out.println("URI = " + request.getRequestURI());
     System.out.println("Authorization Header = " + authHeader);

       if (authHeader == null || !authHeader.startsWith("Bearer ")) {
           response.setStatus(HttpServletResponse.SC_FORBIDDEN);
           response.getWriter().write("Authorization header missing/invalid");
           response.getWriter().flush();
           return;
        }

        try {
            String token = authHeader.substring(7);
            System.out.println("Token = " + token.substring(0, 20) + "...");

            String[] chunks = token.split("\\.");
            if (chunks.length < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT token format");
                return;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);

            if(!headerNode.has("kid")){
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token header is missing kid");
                return;
            }

             String kid = headerNode.get("kid").asText();

            PublicKey publicKey = jwksProvider.getPublicKey(kid);

            //verify the token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(60)
                    .requireIssuer(clerkIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String clerkId = claims.getSubject();
             System.out.println("JWT verified successfully");
              System.out.println("Authenticated clerkId = " + clerkId);


            UsernamePasswordAuthenticationToken authenticationToken =  new UsernamePasswordAuthenticationToken(
                clerkId,
                null,
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_USER")
                )
        );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            System.out.println("Authorities = " +
        SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            filterChain.doFilter(request, response);

        } 
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("JWT ERROR = " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Invalid JWT token: " + e.getMessage());
            response.getWriter().flush();
            return;
        }
    
   }
}
