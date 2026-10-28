package com.petcareconnect.api.usuario;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcareconnect.api.common.NegocioException;
import com.petcareconnect.api.common.RecursoNaoEncontradoException;
import com.petcareconnect.api.security.UsuarioLogado;

@Service
public class UsuarioService {

    private static final Set<String> PERFIS_FILTRO =
            Set.of(Usuario.TUTOR, Usuario.VETERINARIO, Usuario.ADMIN);
    private static final Set<String> PERFIS_CRIACAO =
            Set.of(Usuario.TUTOR, Usuario.VETERINARIO);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(String perfil) {
        List<Usuario> lista;
        if (perfil == null || perfil.isBlank()) {
            lista = usuarios.findAll();
        } else {
            String p = perfil.trim();
            if (!PERFIS_FILTRO.contains(p)) {
                throw new NegocioException("Perfil inválido. Use Tutor, Veterinario ou Admin.");
            }
            lista = usuarios.findByPerfil(p);
        }
        return lista.stream()
                .sorted(Comparator.comparing(Usuario::getId))
                .map(UsuarioResponse::de)
                .toList();
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCriarRequest req) {
        if (!PERFIS_CRIACAO.contains(req.perfil())) {
            throw new NegocioException("O perfil deve ser Tutor ou Veterinario.");
        }
        String login = req.login().trim();
        if (usuarios.existsByLogin(login)) {
            throw new NegocioException("Este login já está em uso.");
        }

        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setLogin(login);
        u.setSenhaHash(encoder.encode(req.senha()));
        u.setPerfil(req.perfil());
        u.setEmail(req.email().trim());
        u.setTelefone(req.telefone().trim());
        u.setEndereco(vazioParaNulo(req.endereco()));
        u.setConvenioStatus(vazioParaNulo(req.convenioStatus()));
        u.setRegistro(vazioParaNulo(req.registro()));
        u.setAtivo(true);
        return UsuarioResponse.de(usuarios.save(u));
    }

    // Nao altera login, perfil nem senha
    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioAtualizarRequest req) {
        Usuario u = buscar(id);
        u.setNome(req.nome().trim());
        u.setEmail(req.email().trim());
        u.setTelefone(req.telefone().trim());
        u.setEndereco(vazioParaNulo(req.endereco()));
        u.setConvenioStatus(vazioParaNulo(req.convenioStatus()));
        u.setRegistro(vazioParaNulo(req.registro()));
        return UsuarioResponse.de(usuarios.save(u));
    }

    @Transactional
    public void alterarAtivo(Long id, Boolean ativo, UsuarioLogado atual) {
        if (ativo == null) {
            throw new NegocioException("Informe true ou false.");
        }
        Usuario u = buscar(id);
        if (!ativo && u.getId().equals(atual.id())) {
            throw new NegocioException("Você não pode desativar a própria conta.");
        }
        u.setAtivo(ativo);
        usuarios.save(u);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}