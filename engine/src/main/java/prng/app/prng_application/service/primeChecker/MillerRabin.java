package prng.app.prng_application.service.primeChecker;

import prng.app.prng_application.service.prng.IsaacPRNG;
import prng.app.prng_application.service.ObjectAnalysisPRNG;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MillerRabin implements PrimalityTest {
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    private final ThreadLocal<IsaacPRNG> rndLocal = ThreadLocal.withInitial(() -> new IsaacPRNG(437)); // evitar condições de corrida

    private BigInteger uniformRandom(BigInteger bottom, BigInteger top) {
        IsaacPRNG rnd = rndLocal.get();
        BigInteger res;
        do {
            res = rnd.nextBigInteger(8).getRandomNumber();
        } while (res.compareTo(bottom) < 0 || res.compareTo(top) > 0);
        return res;
    }

    @Override
    public void isProbablePrime(ObjectAnalysisPRNG o, int rounds) throws ExecutionException, InterruptedException {
        o.setTester("Miller-Rabin");
        BigInteger n = o.getRandomNumber();
        if (n.compareTo(BigInteger.TWO) < 0) return;
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) { // 2 ou 3 são primos
            o.setPrime(true);
            return;
        }
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)){ // se "n" é par
            o.setRandomNumber(o.getRandomNumber().add(BigInteger.ONE)); // incrementa 1 (para não gerar número par)
            return;
        }

        // definir os valores de "d" e "s" em "n-1 = 2^s * d"
        BigInteger d = n.subtract(BigInteger.ONE);
        int s = 0;
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.shiftRight(1);
            s++;
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < rounds; i++){
            BigInteger finalD = d;
            int finalS = s;
            Callable<Boolean> task = () -> {
                return performMillerRabin(o, finalD, finalS);
            };
            futures.add(executor.submit(task));
        }
        for (Future<Boolean> future : futures) {
            try {
                if (future.get() == Boolean.FALSE) {
                    o.setPrime(false); // composto
                    o.setRandomNumber(o.getRandomNumber().subtract(BigInteger.TWO));
                    for (Future<Boolean> f : futures) {
                        f.cancel(true);
                    }
                    return;
                }
            } catch (CancellationException e) {
                break;
            }
        }
        o.setPrime(true); // passou em todos os testes
    }
    private boolean performMillerRabin(ObjectAnalysisPRNG o, BigInteger d, int s){
        BigInteger n = o.getRandomNumber();
        BigInteger a = uniformRandom(BigInteger.TWO, n.subtract(BigInteger.TWO));
        BigInteger x = a.modPow(d, n);
        if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) return true;
        boolean cont = false; // primo (a princípio não considera)
        for (int r = 1; r < s; r++) { // elevar a todos os valores possíveis de "s" a partir de "1" (0 já foi verificado)
            if (Thread.currentThread().isInterrupted()) {
                return true; // thread foi interrompida e retornará verdadeiro (outra thread provavelmente retorna falso caso não seja realmente primo)
            }
            x = x.modPow(BigInteger.TWO, n); // eleva a 2 para testar todas as potências
            if (x.equals(n.subtract(BigInteger.ONE))) { cont = true; break; } // se igualar à n-1 continua o teste e considera como primo
        }
        return cont;
    }
}

