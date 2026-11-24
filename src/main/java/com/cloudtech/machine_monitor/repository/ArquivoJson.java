package com.cloudtech.machine_monitor.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** Lê e grava uma lista de objetos em um arquivo JSON. */
public class ArquivoJson<T> {

    private final Path caminho;
    private final TypeReference<List<T>> tipo;
    private final JsonMapper mapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public ArquivoJson(Path caminho, TypeReference<List<T>> tipo) {
        this.caminho = caminho;
        this.tipo = tipo;
    }

    public List<T> ler() {
        if (!Files.exists(caminho)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mapper.readValue(caminho.toFile(), tipo));
    }

    public void gravar(List<T> dados) {
        try {
            Path pasta = caminho.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            mapper.writeValue(caminho.toFile(), dados);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gravar " + caminho, e);
        }
    }
}