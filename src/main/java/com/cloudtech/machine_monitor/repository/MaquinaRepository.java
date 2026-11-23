package main.java.com.cloudtech.machine_monitor.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cloudtech.machinemonitor.models.Maquina;

import tools.jackson.core.type.TypeReference;

@Repository
public class MaquinaRepository {

    private final ArquivoJson<Maquina> arquivo;
    private final List<Maquina> maquinas;
    private final AtomicLong sequencia;

    public MaquinaRepository(@Value("${app.dados.pasta:data}") String pasta) {
        this.arquivo = new ArquivoJson<>(Path.of(pasta, "maquinas.json"), new TypeReference<List<Maquina>>() {});
        this.maquinas = arquivo.ler();
        long maiorId = maquinas.stream().mapToLong(Maquina::getId).max().orElse(0);
        this.sequencia = new AtomicLong(maiorId);

        if (maquinas.isEmpty()) {
            salvar(new Maquina(null, "Máquina CNC 01", "CNC", "Usinagem", "em operação"));
        }
    }

    public synchronized List<Maquina> listar() {
        return List.copyOf(maquinas);
    }

    public synchronized Optional<Maquina> buscarPorId(Long id) {
        return maquinas.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public synchronized Maquina salvar(Maquina maquina) {
        maquina.setId(sequencia.incrementAndGet());
        maquinas.add(maquina);
        arquivo.gravar(maquinas);
        return maquina;
    }

    public synchronized Optional<Maquina> atualizar(Long id, Maquina dados) {
        Optional<Maquina> existente = buscarPorId(id);
        existente.ifPresent(m -> {
            m.setNome(dados.getNome());
            m.setTipo(dados.getTipo());
            m.setSetor(dados.getSetor());
            m.setStatus(dados.getStatus());
            arquivo.gravar(maquinas);
        });
        return existente;
    }

    public synchronized boolean remover(Long id) {
        boolean removeu = maquinas.removeIf(m -> m.getId().equals(id));
        if (removeu) {
            arquivo.gravar(maquinas);
        }
        return removeu;
    }
}
