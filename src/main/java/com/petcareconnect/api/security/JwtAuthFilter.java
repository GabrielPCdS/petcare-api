package com.petcareconnect.api.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.petcareconnect.api.usuario.Usuario;
import com.petcareconnect.api.usuario.UsuarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                Claims claims = jwtService.validar(token);
                String perfil = claims.get("perfil", String.class);
                if (perfil != null) {
                    Long id = Long.valueOf(claims.getSubject());
                    Optional<Usuario> doBanco = usuarios.findById(id);
                    // Usuario removido ou desativado: nao autentica (rota protegida responde 401)
                    if (doBanco.isPresent() && doBanco.get().isAtivo()) {
                        UsuarioLogado usuario = new UsuarioLogado(
                                id,
                                claims.get("nome", String.class),
                                claims.get("login", String.class),
                                perfil);
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}