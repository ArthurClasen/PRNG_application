package prng.app.prng_application.service.prng;

import lombok.Getter;
import lombok.Setter;
import prng.app.prng_application.service.ObjectAnalysisPRNG;

import java.math.BigInteger;
import java.util.Random;

@Getter
@Setter
public class IsaacPRNG extends Random implements PRNG {
    private static final int SIZE = 256;
    private final int[] mem = new int[SIZE];
    private final int[] res = new int[SIZE];
    private int a,b,c;
    private int idx;

    public IsaacPRNG(int[] seed) {
        for (int i = 0; i < SIZE; i++) {
            res[i] = seed[i % seed.length];
        }
        init();
    }

    private void init() {
        int i;
        int[] mix = new int[8];
        for (i = 0; i < 8; i++) mix [i] = 0x9e3779b9; // valor (golden ratio) que garantirá melhor aleatoriedade (difusão)
        for (i = 0; i < 4; i++) scramble(mix); // misturar valores 4 vezes para espalhar a semente inicial
        for (i = 0; i < SIZE; i+=8) {
            for (int j = 0; j < 8; j++) mix[j] += res[i + j];
            scramble(mix); // mistura para evitar que possa ter alguma relação/associação entre mem e res
            for (int j = 0; j < 8; j++) mem[i + j] = mix[j];
        }
        generate();
        idx = 0;
    }

    // método para embaralhar os valores do array
    private void scramble(int[] x) {
        x[0] ^= x[1] << 11; x[3] += x[0]; x[1] += x[2];
        x[1] ^= x[2] >>> 2; x[4] += x[1]; x[2] += x[3];
        x[2] ^= x[3] << 8; x[5] += x[2]; x[3] += x[4];
        x[3] ^= x[4] >>> 16; x[6] += x[3]; x[4] += x[5];
        x[4] ^= x[5] << 10; x[7] += x[4]; x[5] += x[6];
        x[5] ^= x[6] >>> 4; x[0] += x[5]; x[6] += x[7];
        x[6] ^= x[7] << 8; x[1] += x[6]; x[7] += x[0];
        x[7] ^= x[0] >>> 9; x[2] += x[7]; x[0] += x[1];
    }

    private void generate() {
        // incremento dos valores 'c' e 'b' para garantir que mesmo que a entrada e
        // saída sejam as mesmas ainda serão gerados valores bem diferentes
        c++;
        b += c;
        // atribuição por meio de operações aos valores de entrada e saída que dependem
        // do seu valor de índice
        for (int i = 0; i < SIZE; i++) {
            int x = mem[i];
            switch (i & 3) {
                case 0 : a ^= (a << 13); break;
                case 1 : a ^= (a >>> 6); break;
                case 2 : a ^= (a << 2); break;
                case 3 : a ^= (a >>> 16); break;
            }
            a += mem[(i + 128) & 0xff];
            int y = mem[(x >>> 2) & 0xff] + a + b;
            mem[i] = y;
            b = mem[(y >>> 10) & 0xff] + x;
            res[i] = b;
        }
    }

    // método para entregar valores gerados
    private int nextIntISAAC() {
        // caso o índice seja maior que o tamanho significa que ele já entregou todos os
        // valores do array, portanto devo gerar novos números e reiniciar o valor de idx
        if (idx >= SIZE) {
            generate();  // gerar novamente os números
            idx = 0;
        }
        return res[idx++]; // valor gerado e incremento de idx
    }

    @Override
    public ObjectAnalysisPRNG nextBigInteger(int bits) { // método que retorna o valor gerado final
        long startTime = System.nanoTime(); // tempo inicial
        if (bits <= 0) throw new IllegalArgumentException("bits must be positive"); // bits tem que ser maior que 0
        int words = (bits + 31) / 32; // conversão de quantidade de bits para quantidade de palavras
        byte[] out = new byte[words*4]; // array de bytes (é o que vai ser utilizado para criar o BigInteger)
        for (int i = 0; i < words; i++) { // vai criar todos os bytes a partir dos valores inteiros
            int val = nextIntISAAC();
            int off = i*4; // multiplica por 4, pois inteiro tem 32 bits ou 4 bytes
            out[off] =  (byte) (val >>> 24);
            out[off + 1] = (byte) (val >>> 16);
            out[off + 2] = (byte) (val >>> 8);
            out[off + 3] = (byte) val;
        }
        BigInteger big = new BigInteger(1, out); // BigInteger positivo pegando os bytes acumulados
        // correção caso o número de bits não seja divisível por 32 (qtd de bits em palavras)
        int shift = words*32 - bits;
        if (shift > 0) big = big.shiftRight(shift);
        long endTime = System.nanoTime(); // tempo final - para medir o tempo de execução
        ObjectAnalysisPRNG analysis = new ObjectAnalysisPRNG("ISAAC", bits, endTime-startTime, big, false, null, 0);
        return analysis;
    }
}


// 11110000010011111000000111100000