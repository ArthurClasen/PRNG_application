package prng.app.prng_application.service.primeChecker;

import prng.app.prng_application.service.prng.IsaacPRNG;

import java.math.BigInteger;

public class MillerRabin implements PrimalityTest {
    private final IsaacPRNG rnd = new IsaacPRNG(437);

    private BigInteger uniformRandom(BigInteger bottom, BigInteger top) {
        BigInteger res;
        do {
            res = rnd.nextBigInteger(32);
        } while (res.compareTo(bottom) < 0 || res.compareTo(top) > 0);
        return res;
    }

    @Override
    public boolean isProbablePrime(BigInteger n, int rounds) {
        if (n.compareTo(BigInteger.TWO) < 0) return false;
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) return true; // 2 ou 3 são primos
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) return false; // se "n" é par

        // definir os valores de "d" e "s" em "n-1 = 2^s * d"
        BigInteger d = n.subtract(BigInteger.ONE);
        int s = 0;
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.shiftRight(1);
            s++;
        }

        for (int i = 0; i < rounds; i++) {
            BigInteger a = uniformRandom(BigInteger.TWO, n.subtract(BigInteger.TWO)); // escolher valor arbitrário para "a"
            BigInteger x = a.modPow(d, n); // testa quando s = 0 e define a parte "a^d"
            // se for igual a 1 ou n-1 continua o teste
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) continue;
            boolean cont = false; // primo (a princípio não considera)
            for (int r = 1; r < s; r++) { // elevar a todos os valores possíveis de "s" a partir de "1" (0 já foi verificado)
                x = x.modPow(BigInteger.TWO, n); // eleva a 2 para testar todas as potências
                if (x.equals(n.subtract(BigInteger.ONE))) { cont = true; break; } // se igualar à n-1 continua o teste e considera como primo
            }
            if (cont) continue; //
            return false; // composto
        }
        return true; // provável primo
    }
}

