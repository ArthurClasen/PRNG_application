package prng.app.prng_application.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
public class ObjectAnalysisPRNG {
    private String algorithm;
    private int size;
    private long timeGenerator;
    private BigInteger randomNumber;
    private boolean isPrime;
    private String tester;
    private long timeTester;
}
