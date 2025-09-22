package prng.app.prng_application.service.prng;

import java.math.BigInteger;

public interface PRNG {
    // retorna o próximo k-bit BigInteger (k > 0) e positivo
    BigInteger nextBigInteger(int bits);
}
