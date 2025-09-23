package prng.app.prng_application.service.primeChecker;

import prng.app.prng_application.service.prng.IsaacPRNG;

import java.math.BigInteger;

public class FermatTest implements PrimalityTest {
    private final IsaacPRNG rnd = new IsaacPRNG(347);

    private BigInteger uniformRandom(BigInteger bottom, BigInteger top) {
        BigInteger res;
        do {
            res = rnd.nextBigInteger(32);
        } while (res.compareTo(bottom) < 0 || res.compareTo(top) > 0);
        return res;
    }

    @Override
    public boolean isProbablePrime(BigInteger n, int rounds) {
        if (n.compareTo(BigInteger.TWO) < 0) return false; // retornar falso se for negativo
        if (n.equals(BigInteger.TWO)) return true; // 2 é um valor primo
        for (int i = 0; i < rounds; i++) {
            // número qualquer para a (valor que será exponenciado)
            BigInteger a = uniformRandom(BigInteger.TWO, n.subtract(BigInteger.TWO));
            // retorna falso caso seja respeitado o teorema de Fermat
            if (!a.modPow(n.subtract(BigInteger.ONE), n).equals(BigInteger.ONE)) return false;
        }
        return true;
    }
}


