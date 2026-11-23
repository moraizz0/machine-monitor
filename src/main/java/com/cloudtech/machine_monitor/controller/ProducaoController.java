package com.cloudtech.machine_monitor.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudtech.machine_monitor.models.Producao;
import com.cloudtech.machine_monitor.repository.MaquinaRepository;
import com.cloudtech.machine_monitor.repository.ProducaoRepository;

@RestController
@RequestMapping("/producao")
public class ProducaoController {

    private final ProducaoRepository producaoRepository;
    private final MaquinaRepository maquinaRepository;

    public ProducaoController(ProducaoRepository producaoRepository, MaquinaRepository maquinaRepository) {
        this.producaoRepository = producaoRepository;
        this.maquinaRepository = maquinaRepository;
    }

    @GetMapping
    public List<Producao> listar(@RequestParam(required = false) Long maquinaId) {
        return maquinaId == null ? producaoRepository.listar() : producaoRepository.listarPorMaquina(maquinaId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return producaoRepository.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> naoEncontrado(id));
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Producao producao) {
        ResponseEntity<?> erro = validar(producao);
        if (erro != null) {
            return erro;
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(producaoRepository.salvar(producao));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Producao dados) {
        ResponseEntity<?> erro = validar(dados);
        if (erro != null) {
            return erro;
        }
        return producaoRepository.atualizar(id, dados)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> naoEncontrado(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(@PathVariable Long id) {
        if (!producaoRepository.remover(id)) {
            return naoEncontrado(id);
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> validar(Producao p) {
        if (p.getMaquinaId() == null || p.getConsumoKw() == null
                || p.getHorarioAcionamento() == null || p.getPecasProduzidas() == null) {
            return ResponseEntity.badRequest().body(Map.of("erro",
                    "Os campos maquinaId, consumoKw, horarioAcionamento e pecasProduzidas são obrigatórios"));
        }
        if (!p.getHorarioAcionamento().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            return ResponseEntity.badRequest().body(Map.of("erro", "horarioAcionamento deve estar no formato HH:mm"));
        }
        if (p.getConsumoKw() < 0 || p.getPecasProduzidas() < 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Valores não podem ser negativos"));
        }
        if (maquinaRepository.buscarPorId(p.getMaquinaId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Máquina com id " + p.getMaquinaId() + " não encontrada"));
        }
        return null;
    }

    private static ResponseEntity<?> naoEncontrado(Long id) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Registro de produção com id " + id + " não encontrado"));
    }
}
