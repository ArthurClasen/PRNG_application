package prng.app.prng_application.service.primeChecker;

import prng.app.prng_application.service.prng.IsaacPRNG;
import prng.app.prng_application.service.ObjectAnalysisPRNG;
import java.math.BigInteger;

public class MillerRabin implements PrimalityTest {
    private final IsaacPRNG rnd;

    public MillerRabin(IsaacPRNG rnd) {
        this.rnd = rnd;
    }

    private BigInteger uniformRandom(BigInteger bottom, BigInteger top) { // testar com números aleatórios
        BigInteger res;
        int bitlen = top.bitLength();
        do {
            res = rnd.nextBigInteger(bitlen).getRandomNumber();
        } while (res.compareTo(bottom) < 0 || res.compareTo(top) > 0);
        return res;
    }

    @Override
    public void isProbablePrime(ObjectAnalysisPRNG o, int rounds) {
        o.setTester("Miller-Rabin");
        BigInteger n = o.getRandomNumber();
        if (n.compareTo(BigInteger.TWO) < 0) return;
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)){ // se "n" é par
            o.setRandomNumber(o.getRandomNumber().add(BigInteger.ONE)); // incrementa 1 (para não gerar número par)
            isProbablePrime(o, rounds); // testa novamente o novo número
            return;
        }

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
            if (cont) continue;
            // composto
            o.setRandomNumber(o.getRandomNumber().add(BigInteger.TWO)); // incrementa 2 (para não gerar número par)
            isProbablePrime(o, rounds); // testa novamente o novo número
            return;
        }
        o.setPrime(true); // provável primo
    }
}

