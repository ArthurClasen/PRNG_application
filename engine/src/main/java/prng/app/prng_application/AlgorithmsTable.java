package prng.app.prng_application;

import com.github.freva.asciitable.AsciiTable;

public class AlgorithmsTable {
    private final String[] header = {"Algoritmo", "Tamanho do Número", "Número gerado", "Tempo para Gerar (µs)", "Prime", "Qtd 0's", "Qtd 1's", "runs", "ideal runs"};
    private final String data[][];

    public AlgorithmsTable(String[][] data) {
        this.data = data;
    }

    public String form_table() {
        return AsciiTable.getTable(header, data);
    }
}
