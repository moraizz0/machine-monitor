package main.java.com.cloudtech.machine_monitor.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cloudtech.machinemonitor.models.Producao;

import tools.jackson.core.type.TypeReference;

@Repository
public class ProducaoRepository {

    private final ArquivoJson<Producao> arquivo;
    private final List<Producao> registros;
    private final AtomicLong sequencia;

    public ProducaoRepository(@Value("${app.dados.pasta:data}") String pasta) {
        this.arquivo = new ArquivoJson<>(Path.of(pasta, "producao.json"), new TypeReference<List<Producao>>() {});
        this.registros = arquivo.ler();
        long maiorId = registros.stream().mapToLong(Producao::getId).max().orElse(0);
        this.sequencia = new AtomicLong(maiorId);
    }

    public synchronized List<Producao> listar() {
        return List.copyOf(registros);
    }

    public synchronized List<Producao> listarPorMaquina(Long maquinaId) {
        return registros.stream().filter(p -> p.getMaquinaId().equals(maquinaId)).toList();
    }

    public synchronized Optional<Producao> buscarPorId(Long id) {
        return registros.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public synchronized Producao salvar(Producao producao) {
        producao.setId(sequencia.incrementAndGet());
        registros.add(producao);
        arquivo.gravar(registros);
        return producao;
    }

    public synchronized Optional<Producao> atualizar(Long id, Producao dados) {
        Optional<Producao> existente = buscarPorId(id);
        existente.ifPresent(p -> {
            p.setMaquinaId(dados.getMaquinaId());
            p.setConsumoKw(dados.getConsumoKw());
            p.setHorarioAcionamento(dados.getHorarioAcionamento());
            p.setPecasProduzidas(dados.getPecasProduzidas());
            arquivo.gravar(registros);
        });
        return existente;
    }

    public synchronized boolean remover(Long id) {
        boolean removeu = registros.removeIf(p -> p.getId().equals(id));
        if (removeu) {
            arquivo.gravar(registros);
        }
        return removeu;
    }

    public synchronized void removerPorMaquina(Long maquinaId) {
        if (registros.removeIf(p -> p.getMaquinaId().equals(maquinaId))) {
            arquivo.gravar(registros);
        }
    }
}
