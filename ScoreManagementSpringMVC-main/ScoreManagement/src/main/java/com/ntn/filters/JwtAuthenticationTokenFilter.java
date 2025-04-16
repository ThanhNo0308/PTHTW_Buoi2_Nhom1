//package com.ntn.filters;
//
//import com.ntn.components.JwtService;
//import com.ntn.pojo.User;
//import com.ntn.service.UserService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//
//public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
//
//    private static final String TOKEN_HEADER = "Authorization";
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Autowired
//    private UserService userService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain) throws ServletException, IOException {
//        String authToken = request.getHeader(TOKEN_HEADER);
//
//        if (authToken != null && jwtService.validateTokenLogin(authToken)) {
//            String username = jwtService.getUsernameFromToken(authToken);
//            User user = userService.getUserByUn(username);
//            if (user != null) {
//                Set<GrantedAuthority> authorities = new HashSet<>();
//                authorities.add(new SimpleGrantedAuthority(user.getRoleID().getRoleName()));
//
//                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
//                        user.getUsername(), user.getPassword(), authorities);
//
//                UsernamePasswordAuthenticationToken authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities());
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
