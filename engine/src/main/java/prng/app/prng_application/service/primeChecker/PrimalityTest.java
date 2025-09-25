package prng.app.prng_application.service.primeChecker;

import prng.app.prng_application.service.ObjectAnalysisPRNG;

import java.util.concurrent.ExecutionException;

public interface PrimalityTest {
    public void isProbablePrime(ObjectAnalysisPRNG o, int rounds) throws ExecutionException, InterruptedException;
}
