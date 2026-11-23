package com.cloudtech.machine_monitor;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = "app.dados.pasta=target/test-data")
@AutoConfigureMockMvc
class MaquinaControllerTest {

    private static final String MAQUINA_JSON = """
            {"nome": "Torno Teste", "tipo": "Torno", "setor": "Usinagem", "status": "em operação"}
            """;

    @Autowired
    private MockMvc mockMvc;

    private long cadastrarMaquina() throws Exception {
        String resposta = mockMvc.perform(post("/maquinas/cadastro")
                        .contentType(MediaType.APPLICATION_JSON).content(MAQUINA_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id");
        return id.longValue();
    }

    private void registrarProducao(long maquinaId) throws Exception {
        mockMvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"maquinaId": %d, "consumoKw": 10.0, "horarioAcionamento": "07:00", "pecasProduzidas": 100}
                                """.formatted(maquinaId)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("a) GET / responde HTTP 200")
    void paginaInicial() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    @DisplayName("b) GET /status retorna aplicação online")
    void statusOnline() throws Exception {
        mockMvc.perform(get("/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("online"));
    }

    @Test
    @DisplayName("GET /versao retorna a versão")
    void versao() throws Exception {
        mockMvc.perform(get("/versao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value("1.0.0"));
    }

    @Test
    @DisplayName("c) POST /maquinas/cadastro aceita nome, tipo, setor e status (201)")
    void cadastraMaquina() throws Exception {
        mockMvc.perform(post("/maquinas/cadastro")
                        .contentType(MediaType.APPLICATION_JSON).content(MAQUINA_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Torno Teste"))
                .andExpect(jsonPath("$.tipo").value("Torno"))
                .andExpect(jsonPath("$.setor").value("Usinagem"))
                .andExpect(jsonPath("$.status").value("em operação"));
    }

    @Test
    @DisplayName("f) Cadastro sem campos obrigatórios retorna 400")
    void cadastroInvalido() throws Exception {
        mockMvc.perform(post("/maquinas/cadastro")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"Sem tipo\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("d) GET /maquinas lista as máquinas (200)")
    void listaMaquinas() throws Exception {
        cadastrarMaquina();
        mockMvc.perform(get("/maquinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("PUT /maquinas/{id} atualiza a máquina (200)")
    void atualizaMaquina() throws Exception {
        long id = cadastrarMaquina();
        mockMvc.perform(put("/maquinas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Torno Teste", "tipo": "Torno", "setor": "Usinagem", "status": "em manutenção"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("em manutenção"));
    }

    @Test
    @DisplayName("DELETE /maquinas/{id} remove a máquina (204) e depois retorna 404")
    void removeMaquina() throws Exception {
        long id = cadastrarMaquina();
        mockMvc.perform(delete("/maquinas/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/maquinas/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("f) GET /maquinas/{id} inexistente retorna 404")
    void maquinaInexistente() throws Exception {
        mockMvc.perform(get("/maquinas/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("e) POST /producao registra consumo, horário e peças (201)")
    void registraProducao() throws Exception {
        long id = cadastrarMaquina();
        mockMvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"maquinaId": %d, "consumoKw": 12.5, "horarioAcionamento": "08:30", "pecasProduzidas": 150}
                                """.formatted(id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumoKw").value(12.5))
                .andExpect(jsonPath("$.horarioAcionamento").value("08:30"))
                .andExpect(jsonPath("$.pecasProduzidas").value(150));
    }

    @Test
    @DisplayName("GET /producao lista os registros (200)")
    void listaProducao() throws Exception {
        mockMvc.perform(get("/producao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("f) POST /producao para máquina inexistente retorna 404")
    void producaoMaquinaInexistente() throws Exception {
        mockMvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"maquinaId": 999999, "consumoKw": 10.0, "horarioAcionamento": "09:00", "pecasProduzidas": 10}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /maquinas/{id}/historico soma consumo e peças")
    void historico() throws Exception {
        long id = cadastrarMaquina();
        registrarProducao(id);
        registrarProducao(id);
        mockMvc.perform(get("/maquinas/" + id + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRegistros").value(2))
                .andExpect(jsonPath("$.consumoTotalKw").value(20.0))
                .andExpect(jsonPath("$.pecasProduzidasTotal").value(200));
    }
}