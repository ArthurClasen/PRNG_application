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
import prng.app.prng_application.service.ObjectAnalysisPRNG;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    private List<String[]> formDataPRNG () throws ExecutionException, InterruptedException {
        List<String[]> dataList = new ArrayList<>();
        long seed = System.nanoTime();
        System.out.println("Seed: " + seed);
        IsaacPRNG isaacPRNG = new IsaacPRNG((int) seed);
        NaorReingoldPRF naorReingoldPRF = new NaorReingoldPRF(32, 40);
        FermatTest fermatTest = new FermatTest();
        MillerRabin millerRabin = new MillerRabin();
        for (int j : bitsArray) {
            ObjectAnalysisPRNG analysisPRNGISAAC = isaacPRNG.nextBigInteger(j);
            isaacArray.add(analysisPRNGISAAC);
            String[] data = {
                    analysisPRNGISAAC.getAlgorithm(),
                    String.valueOf(analysisPRNGISAAC.getSize()),
                    String.valueOf(analysisPRNGISAAC.getRandomNumber()),
                    String.valueOf(analysisPRNGISAAC.getTimeGenerator()/1000000),
                    String.valueOf(analysisPRNGISAAC.isPrime()),
            };
            dataList.add(data);
        }
        for (int k : bitsArray) {
            ObjectAnalysisPRNG analysisPRNGReingold = naorReingoldPRF.nextBigInteger(k);
            naorArray.add(analysisPRNGReingold);
            String[] data = {
                    analysisPRNGReingold.getAlgorithm(),
                    String.valueOf(analysisPRNGReingold.getSize()),
                    String.valueOf(analysisPRNGReingold.getRandomNumber()),
                    String.valueOf(analysisPRNGReingold.getTimeGenerator()/1000000),
                    String.valueOf(analysisPRNGReingold.isPrime())
            };
            dataList.add(data);
        }
        /*
        for (ObjectAnalysisPRNG l : isaacArray) {
            fermatTest.isProbablePrime(l, 5);
            String[] data = {
                    l.getTester(),
                    String.valueOf(l.getSize()),
                    String.valueOf(l.getRandomNumber()),
                    String.valueOf(l.getTimeTester()),
                    String.valueOf(l.isPrime())
            };
            dataList.add(data);
        }
        */
        for (ObjectAnalysisPRNG m : naorArray) {
            long start = System.nanoTime();
            while (!m.isPrime()) millerRabin.isProbablePrime(m, 5);
            long end = System.nanoTime();
            m.setTimeTester(end - start);
            String[] data = {
                    m.getTester(),
                    String.valueOf(m.getSize()),
                    String.valueOf(m.getRandomNumber()),
                    String.valueOf(m.getTimeTester()/1000000),
                    String.valueOf(m.isPrime())
            };
            dataList.add(data);
        }
        return dataList;
    }
}
