package prng.app.prng_application.service.primeChecker;

import prng.app.prng_application.service.prng.IsaacPRNG;
import prng.app.prng_application.service.ObjectAnalysisPRNG;

import java.math.BigInteger;

public class FermatTest implements PrimalityTest {
    private final IsaacPRNG rnd = new IsaacPRNG(347);

    private BigInteger uniformRandom(BigInteger bottom, BigInteger top) {
        BigInteger res;
        do {
            res = rnd.nextBigInteger(8).getRandomNumber();
        } while (res.compareTo(bottom) < 0 || res.compareTo(top) > 0);
        return res;
    }

    @Override
    public void isProbablePrime(ObjectAnalysisPRNG o, int rounds) {
        long startTime = System.nanoTime();
        o.setTester("Fermat");
        BigInteger n = o.getRandomNumber();
        if (n.compareTo(BigInteger.TWO) < 0) return; // retornar falso se for menor que 2
        if (n.equals(BigInteger.TWO)) {
            long endTime = System.nanoTime();
            o.setPrime(true);
            o.setTimeTester(endTime-startTime);
            return;} // 2 é um valor primo
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) { // se é par não é primo
            o.setRandomNumber(o.getRandomNumber().add(BigInteger.ONE)); // incrementa 1 (para não gerar número par)
            isProbablePrime(o, rounds); // testa novamente o novo número
            long endTime = System.nanoTime();
            o.setTimeTester(endTime-startTime);
            return;
        }
        for (int i = 0; i < rounds; i++) {
            // número qualquer para a (valor que será exponenciado)
            BigInteger a = uniformRandom(BigInteger.TWO, n.subtract(BigInteger.TWO));
            // retorna falso caso seja respeitado o teorema de Fermat
            if (!a.modPow(n.subtract(BigInteger.ONE), n).equals(BigInteger.ONE)) {
                o.setRandomNumber(o.getRandomNumber().add(BigInteger.TWO)); // incrementa 2 (para não gerar número par)
                isProbablePrime(o, rounds); // testa novamente o novo número
                long endTime = System.nanoTime();
                o.setTimeTester(endTime-startTime);
                return;
            }
        }
        o.setPrime(true);
        long endTime = System.nanoTime();
        o.setTimeTester(endTime-startTime);
    }
}


