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

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class PrngApplication implements ApplicationRunner {
    private final int bitsArray[] = {40, 56, 80, 128, 168, 224, 256, 512, 1024, 2048, 4096};
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
        long sum = 0;
        List<String[]> dataList = new ArrayList<>();
        ObjectAnalysisPRNG analysisPRNGISAAC = null;
        ObjectAnalysisPRNG analysisPRNGReingold = null;
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
            sum = 0;
            for (int i = 0; i < 4; i++) {
                analysisPRNGISAAC = isaacPRNG.nextBigInteger(j);
                sum += analysisPRNGISAAC.getTimeGenerator();
            } // gerar 4 números aleatórios
            boolean ideal = false;
            int zerosCount = 0;
            int onesCount = 0;
            int runs = 0;
            while (!ideal) { // enquanto o número não tiver um valor de run ideal, continuar a buscar um valor aleatório excelente
                analysisPRNGISAAC = isaacPRNG.nextBigInteger(j);
                onesCount = analysisPRNGISAAC.getRandomNumber().bitCount();
                zerosCount = j - onesCount;
                runs = calculateRuns(analysisPRNGISAAC.getRandomNumber().toString(2));
                ideal = idealRuns(zerosCount, onesCount, runs);
            }
            sum += analysisPRNGISAAC.getTimeGenerator();
            isaacArray.add(analysisPRNGISAAC);
            String[] data = {
                    analysisPRNGISAAC.getAlgorithm(),
                    String.valueOf(analysisPRNGISAAC.getSize()),
                    String.valueOf(analysisPRNGISAAC.getRandomNumber()),
                    String.valueOf(sum / 5000),
                    String.valueOf(analysisPRNGISAAC.isPrime()),
                    String.valueOf(zerosCount),
                    String.valueOf(onesCount),
                    String.valueOf(runs),
                    String.valueOf(idealRuns(zerosCount, onesCount, runs))
            };
            dataList.add(data);
        }
        for (int k : bitsArray) { // gerar tabela de números aleatórios usando NaorReingold
            sum = 0;
            for (int i = 0; i < 4; i++) {
                analysisPRNGReingold = naorReingoldPRF.nextBigInteger(k);
                sum += analysisPRNGReingold.getTimeGenerator();
            }
            boolean ideal = false;
            int zerosCount = 0;
            int onesCount = 0;
            int runs = 0;
            while (!ideal) {
                analysisPRNGReingold = naorReingoldPRF.nextBigInteger(k);
                onesCount = analysisPRNGReingold.getRandomNumber().bitCount();
                zerosCount = k - onesCount;
                runs = calculateRuns(analysisPRNGReingold.getRandomNumber().toString(2));
                ideal = idealRuns(zerosCount, onesCount, runs);
            }
            sum +=  analysisPRNGReingold.getTimeGenerator();
            naorArray.add(analysisPRNGReingold);
            String[] data = {
                    analysisPRNGReingold.getAlgorithm(),
                    String.valueOf(analysisPRNGReingold.getSize()),
                    String.valueOf(analysisPRNGReingold.getRandomNumber()),
                    String.valueOf(sum / 5000),
                    String.valueOf(analysisPRNGReingold.isPrime()),
                    String.valueOf(zerosCount),
                    String.valueOf(onesCount),
                    String.valueOf(runs),
                    String.valueOf(idealRuns(zerosCount, onesCount, runs))
            };
            dataList.add(data);
        }
        sum = 0;
        // testadores de números primos
        for (ObjectAnalysisPRNG l : isaacArray) { // gerar tabela de números primos (teste de Fermat) gerados por ISAAC
            ObjectAnalysisPRNG copy = new ObjectAnalysisPRNG(l);
            long startTime = System.nanoTime();
            millerRabin.isProbablePrime(copy, 5);
            long endTime = System.nanoTime();
            copy.setTimeTester(endTime - startTime);
            String[] data = {
                    copy.getTester(),
                    String.valueOf(copy.getSize()),
                    String.valueOf(copy.getRandomNumber()),
                    String.valueOf(copy.getTimeTester()/1000),
                    String.valueOf(copy.isPrime())
            };
            dataList.add(data);
        }
        for (ObjectAnalysisPRNG l : isaacArray) { // gerar tabela de números primos (teste de MillerRabin) gerados por ISAAC
            long startTime = System.nanoTime();
            fermatTest.isProbablePrime(l, 5);
            long endTime = System.nanoTime();
            l.setTimeTester(endTime - startTime);
            String[] data = {
                    l.getTester(),
                    String.valueOf(l.getSize()),
                    String.valueOf(l.getRandomNumber()),
                    String.valueOf(l.getTimeTester()/1000),
                    String.valueOf(l.isPrime())
            };
            dataList.add(data);
        }
        for (ObjectAnalysisPRNG m : naorArray) { // gerar tabela de números primos (teste de Fermat) gerados por NaorReingold
            ObjectAnalysisPRNG copy = new ObjectAnalysisPRNG(m);
            long startTime = System.nanoTime();
            fermatTest.isProbablePrime(copy, 5);
            long endTime = System.nanoTime();
            copy.setTimeTester(endTime - startTime);
            String[] data = {
                    copy.getTester(),
                    String.valueOf(copy.getSize()),
                    String.valueOf(copy.getRandomNumber()),
                    String.valueOf(copy.getTimeTester()/1000),
                    String.valueOf(copy.isPrime())
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
                    String.valueOf(m.getTimeTester()/1000),
                    String.valueOf(m.isPrime())
            };
            dataList.add(data);
        }

        // testes de fermat (falso positivo) e miller-rabin (negativo corrigido)
        /* uso somente para aquele teste dos números
        ObjectAnalysisPRNG falsePositiveTest = new ObjectAnalysisPRNG("", 10, 0, BigInteger.valueOf(561), false, "Fermat", 0);
        ObjectAnalysisPRNG trueNegativeTest = new ObjectAnalysisPRNG("", 10, 0, BigInteger.valueOf(561), false, "Miller-Rabin", 0);
        fermatTest.isProbablePrime(falsePositiveTest, 5);
        millerRabin.isProbablePrime(trueNegativeTest, 5);
        String[] falsePositiveTestData = {
                falsePositiveTest.getTester(),
                String.valueOf(falsePositiveTest.getSize()),
                String.valueOf(falsePositiveTest.getRandomNumber()),
                String.valueOf(falsePositiveTest.getTimeTester()),
                String.valueOf(falsePositiveTest.isPrime()),
        };
        dataList.add(falsePositiveTestData);
        String[] trueNegativeTestData = {
                trueNegativeTest.getTester(),
                String.valueOf(trueNegativeTest.getSize()),
                String.valueOf(trueNegativeTest.getRandomNumber()),
                String.valueOf(trueNegativeTest.getTimeTester()),
                String.valueOf(trueNegativeTest.isPrime()),
        };
        dataList.add(trueNegativeTestData);
        */
        return dataList;
    }

    // método utilizado para calcular quantidade de runs
    private static int calculateRuns(String binaryString) {
        if (binaryString == null || binaryString.length() == 0) {
            return 0;
        }

        int runs = 1; // Começa com 1, pois a primeira sequência já conta
        char[] bits = binaryString.toCharArray();

        // Percorre a string comparando cada bit com o anterior
        for (int i = 1; i < bits.length; i++) {
            // Se o bit atual for diferente do bit anterior, uma nova sequência (run) começou
            if (bits[i] != bits[i - 1]) {
                runs++;
            }
        }
        return runs;
    }

    // método para verificar qualidade de runs
    private boolean idealRuns(double num0, double num1, double runs) {
        double E = (2*num0*num1)/(num0+num1) + 1; // calcula o valor esperado
        double stdDev = (2*num0*num1)*(2*num0*num1-(num1+num0))/(Math.pow((num0+num1), 2)*(num0+num1-1)); // desvio padrão
        double stdDevRoot = Math.sqrt(stdDev); // variância
        double low = E - 1.96*stdDevRoot; // limite inferior
        double high = E + 1.96*stdDevRoot; // limite superior
        return !(runs < low) && !(runs > high); // se não estiver entre os limites retornar falso, se não verdadeiro
    }
}
