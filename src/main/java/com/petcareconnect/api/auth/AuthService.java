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
    private static final String MSG_RECUPERACAO_INVALIDA = "Login ou e-mail não conferem.";
    private static final String MSG_TOKEN_INVALIDO = "Token inválido ou expirado.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final ResetTokenService resetTokens;
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwtService,
                       ResetTokenService resetTokens) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.resetTokens = resetTokens;
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

    // Sempre cria perfil Tutor. Retorna o id do novo usuario.
    @Transactional
    public Long cadastrar(CadastroRequest req) {
        String login = req.login().trim();
        if (usuarios.existsByLogin(login)) {
            throw new NegocioException("Este login já está em uso.");
        }

        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setLogin(login);
        u.setEmail(req.email().trim());
        u.setSenhaHash(encoder.encode(req.senha()));
        u.setPerfil(Usuario.TUTOR);
        u.setAtivo(true);
        return usuarios.save(u).getId();
    }

    // NAO altera a senha: apenas gera um token de uso unico
    @Transactional(readOnly = true)
    public RecuperarSenhaResponse recuperarSenha(RecuperarSenhaRequest req) {
        Usuario u = usuarios.findByLogin(req.login().trim()).orElse(null);

        boolean confere = u != null
                && u.isAtivo()
                && u.getEmail().trim().equalsIgnoreCase(req.email().trim());
        if (!confere) {
            throw new NegocioException(MSG_RECUPERACAO_INVALIDA);
        }

        return new RecuperarSenhaResponse(resetTokens.gerar(u.getLogin()));
    }

    @Transactional
    public void novaSenha(NovaSenhaRequest req) {
        String login = req.login().trim();

        // Consome o token antes de qualquer outra coisa: uso unico, preso ao login
        if (!resetTokens.consumir(req.token().trim(), login)) {
            throw new NegocioException(MSG_TOKEN_INVALIDO);
        }

        Usuario u = usuarios.findByLogin(login)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new NegocioException(MSG_TOKEN_INVALIDO));

        u.setSenhaHash(encoder.encode(req.novaSenha()));
        usuarios.save(u);
    }
}