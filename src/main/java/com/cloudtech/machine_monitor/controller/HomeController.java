package com.cloudtech.machine_monitor.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final String versao;

    public HomeController(@Value("${app.versao}") String versao) {
        this.versao = versao;
    }

    @GetMapping("/")
    public Map<String, Object> inicio() {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Bem-vindo à API de Monitoramento de Máquinas Industriais - CloudTech Solutions");
        resposta.put("endpoints", List.of("/", "/status", "/versao", "/maquinas", "/maquinas/cadastro",
                "/maquinas/{id}", "/maquinas/{id}/historico", "/producao", "/producao/{id}"));
        return resposta;
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        Map<String, String> resposta = new LinkedHashMap<>();
        resposta.put("status", "online");
        resposta.put("mensagem", "Aplicação de monitoramento em execução");
        return resposta;
    }

    @GetMapping("/versao")
    public Map<String, String> versao() {
        Map<String, String> resposta = new LinkedHashMap<>();
        resposta.put("aplicacao", "machine-monitor");
        resposta.put("versao", versao);
        resposta.put("java", System.getProperty("java.version"));
        return resposta;
    }
}