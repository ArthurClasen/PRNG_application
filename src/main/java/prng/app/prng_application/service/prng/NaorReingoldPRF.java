package prng.app.prng_application.service.prng;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Implementação didática do esquema Naor–Reingold como PRF -> usado como PRNG.
 */
public class NaorReingoldPRF implements PRNG {
    private final BigInteger p; // prime modulus
    private final BigInteger g; // generator
    private final BigInteger[] a; // secret exponents
    private final SecureRandom rnd = new SecureRandom();

    public NaorReingoldPRF(int tBits, int primeBits) {
        this.p = BigInteger.probablePrime(primeBits, rnd);
        this.g = BigInteger.TWO; // simplificação: usar 2 como gerador em muitos p
        this.a = new BigInteger[tBits];
        for (int i = 0; i < tBits; i++) {
            a[i] = new BigInteger(primeBits-2, rnd).mod(p);
        }
    }

    private BigInteger eval(long x) {
        BigInteger exp = BigInteger.ZERO;
        for (int i = 0; i < a.length; i++) {
            if (((x >>> i) & 1L) == 1L) exp = exp.add(a[i]);
        }
        exp = exp.mod(p.subtract(BigInteger.ONE)); // expoente mod p-1
        return g.modPow(exp, p);
    }

    @Override
    public BigInteger nextBigInteger(int bits) {
        // concatena avaliacoes em x=0,1,2,... até preencher bits
        int needed = bits;
        BigInteger result = BigInteger.ZERO;
        int chunk = Math.min(256, needed); // por ex. hashiza cada output pra 256 bits
        long counter = 0;
        while (needed > 0) {
            BigInteger y = eval(counter++);
            // hash y para obter chunk bits
            byte[] digest = hash(y.toByteArray());
            BigInteger chunkVal = new BigInteger(1, digest);
            if (chunkVal.bitLength() > chunk) chunkVal = chunkVal.shiftRight(chunkVal.bitLength()-chunk);
            result = result.shiftLeft(chunk).or(chunkVal);
            needed -= chunk;
        }
        return result;
    }

    private byte[] hash(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
