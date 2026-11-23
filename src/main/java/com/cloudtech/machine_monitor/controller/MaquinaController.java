package main.java.com.cloudtech.machine_monitor.controller;

import java.util.LinkedHashMap;
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
import org.springframework.web.bind.annotation.RestController;

import com.cloudtech.machinemonitor.models.Maquina;
import com.cloudtech.machinemonitor.models.Producao;
import com.cloudtech.machinemonitor.repository.MaquinaRepository;
import com.cloudtech.machinemonitor.repository.ProducaoRepository;

@RestController
@RequestMapping("/maquinas")
public class MaquinaController {

    private final MaquinaRepository maquinaRepository;
    private final ProducaoRepository producaoRepository;

    public MaquinaController(MaquinaRepository maquinaRepository, ProducaoRepository producaoRepository) {
        this.maquinaRepository = maquinaRepository;
        this.producaoRepository = producaoRepository;
    }

    @GetMapping
    public List<Maquina> listar() {
        return maquinaRepository.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return maquinaRepository.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> naoEncontrada(id));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody Maquina maquina) {
        String erro = validar(maquina);
        if (erro != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", erro));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(maquinaRepository.salvar(maquina));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Maquina dados) {
        String erro = validar(dados);
        if (erro != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", erro));
        }
        return maquinaRepository.atualizar(id, dados)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> naoEncontrada(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(@PathVariable Long id) {
        if (!maquinaRepository.remover(id)) {
            return naoEncontrada(id);
        }
        producaoRepository.removerPorMaquina(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<?> historico(@PathVariable Long id) {
        var maquina = maquinaRepository.buscarPorId(id);
        if (maquina.isEmpty()) {
            return naoEncontrada(id);
        }
        List<Producao> registros = producaoRepository.listarPorMaquina(id);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("maquina", maquina.get());
        resposta.put("totalRegistros", registros.size());
        resposta.put("consumoTotalKw", registros.stream().mapToDouble(Producao::getConsumoKw).sum());
        resposta.put("pecasProduzidasTotal", registros.stream().mapToInt(Producao::getPecasProduzidas).sum());
        resposta.put("registros", registros);
        return ResponseEntity.ok(resposta);
    }

    private String validar(Maquina m) {
        if (vazio(m.getNome()) || vazio(m.getTipo()) || vazio(m.getSetor()) || vazio(m.getStatus())) {
            return "Os campos nome, tipo, setor e status são obrigatórios";
        }
        return null;
    }

    private static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    private static ResponseEntity<?> naoEncontrada(Long id) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Máquina com id " + id + " não encontrada"));
    }
}
