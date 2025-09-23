package prng.app.prng_application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import prng.app.prng_application.service.primeChecker.FermatTest;
import prng.app.prng_application.service.primeChecker.MillerRabin;
import prng.app.prng_application.service.prng.IsaacPRNG;
import prng.app.prng_application.service.prng.NaorReingoldPRF;

import java.math.BigInteger;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class PrngApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(PrngApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            long seed = System.nanoTime();
            System.out.println("Seed: " + seed);
            BigInteger rnd = BigInteger.ONE;
            int algoritmoPRNG = 2;
            int algoritmoCHECK = 2;
            switch (algoritmoPRNG) {
                case 1: // ISAAC
                    IsaacPRNG isaac = new IsaacPRNG((int) seed);
                    rnd = isaac.nextBigInteger(4096);
                    System.out.println(rnd);
                    break;
                case 2: // NaorReingold
                    NaorReingoldPRF naorReingoldPRF = new NaorReingoldPRF(128, 128);
                    rnd = naorReingoldPRF.nextBigInteger(4096);
                    System.out.println(rnd);
                    break;
            }
            if (rnd.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                rnd.subtract(BigInteger.ONE);
            }
            System.out.println("Indo checar primalidade");
            switch (algoritmoCHECK) {
                case 1: // Fermat
                    FermatTest fermatTest = new FermatTest();
                    if (fermatTest.isProbablePrime(BigInteger.valueOf(37), 3)) {
                        System.out.println("é primo.");
                    } else {
                        System.out.println("é composto");
                    }
                    break;
                case 2: // MillerRabin
                    MillerRabin millerRabin = new MillerRabin();
                    if (millerRabin.isProbablePrime(BigInteger.valueOf(37), 3)) {
                        System.out.println("é primo.");
                    } else {
                        System.out.println("é composto.");
                    }
                    break;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
