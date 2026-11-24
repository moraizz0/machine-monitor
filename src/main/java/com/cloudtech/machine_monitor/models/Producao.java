package com.cloudtech.machine_monitor.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "maquinaId", "consumoKw", "horarioAcionamento", "pecasProduzidas"})
public class Producao {

    private Long id;
    private Long maquinaId;
    private Double consumoKw;
    private String horarioAcionamento; // formato HH:mm
    private Integer pecasProduzidas;

    public Producao() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMaquinaId() { return maquinaId; }
    public void setMaquinaId(Long maquinaId) { this.maquinaId = maquinaId; }

    public Double getConsumoKw() { return consumoKw; }
    public void setConsumoKw(Double consumoKw) { this.consumoKw = consumoKw; }

    public String getHorarioAcionamento() { return horarioAcionamento; }
    public void setHorarioAcionamento(String horarioAcionamento) { this.horarioAcionamento = horarioAcionamento; }

    public Integer getPecasProduzidas() { return pecasProduzidas; }
    public void setPecasProduzidas(Integer pecasProduzidas) { this.pecasProduzidas = pecasProduzidas; }
}
