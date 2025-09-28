package prng.app.prng_application.service.primeChecker;

import prng.app.prng_application.service.SeedConverter;
import prng.app.prng_application.service.prng.IsaacPRNG;
import prng.app.prng_application.service.ObjectAnalysisPRNG;


import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class FermatTest implements PrimalityTest {
    private final IsaacPRNG rnd;

    public FermatTest(IsaacPRNG rnd) {
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
        o.setTester("Fermat");
        BigInteger n = o.getRandomNumber();
        if (n.compareTo(BigInteger.TWO) < 0) return; // retornar falso se for menor que 2
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) { // se é par não é primo
            o.setRandomNumber(o.getRandomNumber().add(BigInteger.ONE)); // incrementa 1 (para não gerar número par)
            isProbablePrime(o, rounds); // testa novamente o novo número
            return;
        }
        for (int i = 0; i < rounds; i++) {
            // número qualquer para a (valor que será exponenciado)
            BigInteger a = uniformRandom(BigInteger.TWO, n.subtract(BigInteger.TWO));
            // retorna falso caso seja respeitado o teorema de Fermat
            if (!a.modPow(n.subtract(BigInteger.ONE), n).equals(BigInteger.ONE)) {
                o.setRandomNumber(o.getRandomNumber().add(BigInteger.TWO)); // incrementa 2 (para não gerar número par)
                isProbablePrime(o, rounds); // testa novamente o novo número
                return;
            }
        }
        o.setPrime(true);
    }
}


