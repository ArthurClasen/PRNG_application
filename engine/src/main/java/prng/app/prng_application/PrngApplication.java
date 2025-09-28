package prng.app.prng_application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import prng.app.prng_application.service.SeedConverter;
import prng.app.prng_application.service.primeChecker.FermatTest;
import prng.app.prng_application.service.primeChecker.MillerRabin;
import prng.app.prng_application.service.prng.IsaacPRNG;
import prng.app.prng_application.service.prng.NaorReingoldPRF;
import prng.app.prng_application.service.ObjectAnalysisPRNG;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class PrngApplication implements ApplicationRunner {
    private final int bitsArray[] = {40, 56, 80, 128, 168, 224, 256, 512, 1024, 2048};
    private final List<ObjectAnalysisPRNG> isaacArray;
    private final List<ObjectAnalysisPRNG> naorArray;

    public PrngApplication(List<ObjectAnalysisPRNG> isaacArray, List<ObjectAnalysisPRNG> naorArray) {
        this.isaacArray = isaacArray;
        this.naorArray = naorArray;
    }

    public static void main(String[] args) {
        SpringApplication.run(PrngApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            String[][] dataArray = formDataPRNG().toArray(new String[0][]);
            AlgorithmsTable algorithmsTable = new AlgorithmsTable(dataArray);
            String table = algorithmsTable.form_table();
            System.out.println(table);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String[]> formDataPRNG () throws NoSuchAlgorithmException {
        List<String[]> dataList = new ArrayList<>();
        int[] seed =  new SeedConverter().getSecureIntSeed(); // criação da semente
        System.out.println("Seed:"); // declaração da semente
        for (int s : seed){
            System.out.println(s);
        }
        IsaacPRNG isaacPRNG = new IsaacPRNG(seed); // ISAAC
        NaorReingoldPRF naorReingoldPRF = new NaorReingoldPRF(32, 40, isaacPRNG, new FermatTest(isaacPRNG)); // naorReingold (gerador de números aleatórios para o "a", e o teste de primos para "p")
        FermatTest fermatTest = new FermatTest(isaacPRNG); // teste de Fermat (uso de ISAAC para aleatoriedade de valores de teste)
        MillerRabin millerRabin = new MillerRabin(isaacPRNG); // teste de MillerRabin (ISAAC tem mesmo principio do Fermat)
        for (int j : bitsArray) { // gerar tabela de números aleatórios usando ISAAC
            ObjectAnalysisPRNG analysisPRNGISAAC = isaacPRNG.nextBigInteger(j);
            isaacArray.add(analysisPRNGISAAC);
            String[] data = {
                    analysisPRNGISAAC.getAlgorithm(),
                    String.valueOf(analysisPRNGISAAC.getSize()),
                    String.valueOf(analysisPRNGISAAC.getRandomNumber()),
                    String.valueOf(analysisPRNGISAAC.getTimeGenerator()),
                    String.valueOf(analysisPRNGISAAC.isPrime()),
            };
            dataList.add(data);
        }
        for (int k : bitsArray) { // gerar tabela de números aleatórios usando NaorReingold
            ObjectAnalysisPRNG analysisPRNGReingold = naorReingoldPRF.nextBigInteger(k);
            naorArray.add(analysisPRNGReingold);
            String[] data = {
                    analysisPRNGReingold.getAlgorithm(),
                    String.valueOf(analysisPRNGReingold.getSize()),
                    String.valueOf(analysisPRNGReingold.getRandomNumber()),
                    String.valueOf(analysisPRNGReingold.getTimeGenerator()),
                    String.valueOf(analysisPRNGReingold.isPrime())
            };
            dataList.add(data);
        }
        for (ObjectAnalysisPRNG l : isaacArray) { // gerar tabela de números primos (teste de Fermat) gerados por ISAAC
            long startTime = System.nanoTime();
            fermatTest.isProbablePrime(l, 5);
            long endTime = System.nanoTime();
            l.setTimeTester(endTime - startTime);
            String[] data = {
                    l.getTester(),
                    String.valueOf(l.getSize()),
                    String.valueOf(l.getRandomNumber()),
                    String.valueOf(l.getTimeTester()),
                    String.valueOf(l.isPrime())
            };
            dataList.add(data);
        }
        for (ObjectAnalysisPRNG m : naorArray) { // gerar tabela de números primos (teste de MillerRabin) gerados por NaorReingold
            long startTime = System.nanoTime();
            millerRabin.isProbablePrime(m, 5);
            long endTime = System.nanoTime();
            m.setTimeTester(endTime - startTime);
            String[] data = {
                    m.getTester(),
                    String.valueOf(m.getSize()),
                    String.valueOf(m.getRandomNumber()),
                    String.valueOf(m.getTimeTester()),
                    String.valueOf(m.isPrime())
            };
            dataList.add(data);
        }
        return dataList;
    }
}
