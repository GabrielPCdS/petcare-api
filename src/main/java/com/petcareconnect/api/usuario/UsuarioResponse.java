package com.petcareconnect.api.usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String login,
        String perfil,
        String email,
        String telefone,
        String endereco,
        String convenioStatus,
        String registro,
        boolean ativo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getLogin(), u.getPerfil(),
                u.getEmail(), u.getTelefone(), u.getEndereco(), u.getConvenioStatus(),
                u.getRegistro(), u.isAtivo());
    }
}