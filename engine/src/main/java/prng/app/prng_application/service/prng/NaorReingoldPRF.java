package prng.app.prng_application.service.prng;

import prng.app.prng_application.service.ObjectAnalysisPRNG;
import prng.app.prng_application.service.SeedConverter;
import prng.app.prng_application.service.primeChecker.FermatTest;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NaorReingoldPRF implements PRNG {
    private final BigInteger p; // módulo primo
    private final BigInteger g; // gerador (valor que será exponenciado)
    private final BigInteger[] a; // expoentes secretos
    private final long timeToGenerate; // tempo para inicializar o algoritmo

    public NaorReingoldPRF(int tBits, int primeBits, IsaacPRNG rnd, FermatTest test) throws NoSuchAlgorithmException {
        long startTime = System.nanoTime();
        ObjectAnalysisPRNG primetest = rnd.nextBigInteger(primeBits); // gera um número pseudoaleatório
        test.isProbablePrime(primetest, 5); // usa um teste de Fermat
        // primeBits: tamanho do "p" em bits (ex: 2048)
        // tBits: comprimento do "input" (número de bits usados por avaliação)
        this.p = primetest.getRandomNumber(); // aqui usar um dos algoritmos de verificador de primos
        this.g = BigInteger.TWO; // simplificação: usar 2 como gerador em muitos "p"
        this.a = new BigInteger[tBits]; // arrays de a's que serão usados para exponenciação
        for (int i = 0; i < tBits; i++) {
            a[i] = rnd.nextBigInteger(128).getRandomNumber(); // definição dos valores de aleatórios para 'a', usando ISAAC
        }
        long endTime = System.nanoTime();
        this.timeToGenerate = endTime - startTime;
    }

    // método para realizar a equação
    // algoritmo que executa um 'pedaço' da equação de NaorReingold
    private BigInteger eval(long x) {
        BigInteger exp = BigInteger.ZERO; // valor que será construido o expoente
        for (int i = 0; i < a.length; i++) {
            if (((x >>> i) & 1L) == 1L) exp = exp.add(a[i]); // somando os valores do expoente (as multiplicações)
        }
        exp = exp.mod(p.subtract(BigInteger.ONE)); // expoente mod p-1
        return g.modPow(exp, p); // retornar o valor y
    }

    // construir o valor de bigInteger
    @Override
    public ObjectAnalysisPRNG nextBigInteger(int bits) {
        long startTime = System.nanoTime(); // tempo inicial
        // concatena avaliações em x=0,1,2,... até preencher bits
        int needed = bits; // quantos bits ainda são necessários para gerar
        BigInteger result = BigInteger.ZERO; // resultado acumulado
        int chunk = Math.min(256, needed); // tamanho máximo de cada pedaço
        long counter = 0; // contador de chamadas à PRF
        while (needed > 0) {
            BigInteger y = eval(counter++); // realizará o cálculo do y do chunk
            byte[] digest = hash(y.toByteArray()); // hash y para obter chunk bits
            BigInteger chunkVal = new BigInteger(1, digest); // converte array de bytes para BigInteger
            // correção de valores do chunk (caso tenha mais bits que o necessário sobrando)
            if (chunkVal.bitLength() > chunk) chunkVal = chunkVal.shiftRight(chunkVal.bitLength()-chunk);
            result = result.shiftLeft(chunk).or(chunkVal); // concatena os bits
            needed -= chunk; // diminui o valor de bits necessários
        }
        long endTime = System.nanoTime(); // tempo final - para calcular tempo de execução
        long totalTime = endTime - startTime + timeToGenerate;
        return new ObjectAnalysisPRNG("NaorReingold", bits, totalTime, result, false, null, 0);
    }

    private byte[] hash(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}