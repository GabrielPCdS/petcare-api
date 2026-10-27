package com.petcareconnect.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.common.NegocioException;
import com.petcareconnect.api.security.JwtService;
import com.petcareconnect.api.usuario.Usuario;
import com.petcareconnect.api.usuario.UsuarioRepository;

@Service
public class AuthService {

    private static final String MSG_LOGIN_INVALIDO = "Login ou senha inválidos.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwtService = jwtService;
        // Usado quando o login nao existe, para o tempo de resposta nao revelar isso
        this.hashFalso = encoder.encode("senha-falsa");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarios.findByLogin(req.login().trim()).orElse(null);

        String hash = usuario != null ? usuario.getSenhaHash() : hashFalso;
        boolean senhaOk = encoder.matches(req.senha(), hash);

        // O perfil enviado no corpo e ignorado de proposito: devolvemos sempre o perfil real
        if (usuario == null || !usuario.isAtivo() || !senhaOk) {
            throw new NegocioException(MSG_LOGIN_INVALIDO, HttpStatus.BAD_REQUEST);
        }

        String token = jwtService.gerar(usuario);
        return new LoginResponse(token, usuario.getId(), usuario.getNome(),
                usuario.getLogin(), usuario.getPerfil());
    }
}