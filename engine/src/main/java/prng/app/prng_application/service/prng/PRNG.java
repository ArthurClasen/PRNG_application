package prng.app.prng_application.service.prng;

import prng.app.prng_application.service.ObjectAnalysisPRNG;

public interface PRNG {
    // retorna o próximo k-bit BigInteger (k > 0) e positivo
    ObjectAnalysisPRNG nextBigInteger(int bits);
}
