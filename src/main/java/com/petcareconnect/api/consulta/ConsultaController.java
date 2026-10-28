package com.petcareconnect.api.consulta;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcareconnect.api.common.ApiResponse;
import com.petcareconnect.api.security.UsuarioLogado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/Consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ConsultaResponse>> listar() {
        return ApiResponse.ok(service.listar(UsuarioLogado.atual()));
    }

    @GetMapping("/horarios-disponiveis")
    public ApiResponse<List<String>> horariosDisponiveis(@RequestParam Long veterinarioId,
                                                         @RequestParam String data) {
        return ApiResponse.ok(service.horariosDisponiveis(veterinarioId, data));
    }

    @PostMapping
    public ApiResponse<Long> criar(@Valid @RequestBody ConsultaCriarRequest req) {
        return ApiResponse.okComMensagem("Consulta agendada com sucesso.",
                service.criar(req, UsuarioLogado.atual()));
    }

    @PostMapping("/{id}/cancelar")
    public ApiResponse<Void> cancelar(@PathVariable Long id) {
        String status = service.cancelar(id, UsuarioLogado.atual());
        String mensagem = "NaoCompareceu".equals(status)
                ? "Cancelamento com menos de 24h de antecedência: a consulta foi registrada como NaoCompareceu (Regra RN-005)."
                : "Consulta cancelada com sucesso.";
        return ApiResponse.<Void>okComMensagem(mensagem, null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> atualizarStatus(@PathVariable Long id,
                                             @RequestParam String status,
                                             @RequestParam(required = false) String obs) {
        service.atualizarStatus(id, status, obs, UsuarioLogado.atual());
        return ApiResponse.<Void>okComMensagem("Status atualizado com sucesso.", null);
    }
}