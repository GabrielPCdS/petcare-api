package com.petcareconnect.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.usuario.Usuario;
import com.petcareconnect.api.usuario.UsuarioRepository;

// Cria o primeiro Admin em producao, somente se o banco estiver sem usuarios
@Component
@Profile("prod")
public class AdminInicialSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialSeeder.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String login;
    private final String senha;
    private final String nome;
    private final String email;

    public AdminInicialSeeder(UsuarioRepository usuarios, PasswordEncoder encoder,
                              @Value("${ADMIN_LOGIN:admin}") String login,
                              @Value("${ADMIN_SENHA:}") String senha,
                              @Value("${ADMIN_NOME:Administrador PetCare}") String nome,
                              @Value("${ADMIN_EMAIL:admin@petcare.com}") String email) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }
        if (senha.length() < 8) {
            log.warn("Banco sem usuarios e ADMIN_SENHA ausente ou com menos de 8 caracteres: "
                    + "nenhum Admin foi criado.");
            return;
        }
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setLogin(login.trim());
        u.setSenhaHash(encoder.encode(senha));
        u.setPerfil(Usuario.ADMIN);
        u.setEmail(email);
        u.setTelefone("-");
        u.setAtivo(true);
        usuarios.save(u);
        log.info("Admin inicial '{}' criado.", login.trim());
    }
}