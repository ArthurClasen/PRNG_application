package prng.app.prng_application.service.primeChecker;

import java.math.BigInteger;

public interface PrimalityTest {
    public boolean isProbablePrime(BigInteger n, int rounds);
}
