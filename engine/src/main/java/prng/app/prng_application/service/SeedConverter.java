package prng.app.prng_application.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

@Getter
@Setter
public class SeedConverter {
    public int[] getSecureIntSeed() {
        // 1. Define o tamanho da semente criptográfica (32 bytes = 256 bits).
        int BYTE_LENGTH = 32;

        // 2. Obtém a entropia verdadeira do sistema operacional (TRNG) via SecureRandom.
        SecureRandom sr = new SecureRandom();
        byte[] secureBytes = new byte[BYTE_LENGTH];
        sr.nextBytes(secureBytes); // Preenche com 32 bytes de alta entropia

        // 3. O resultado deve ser um array de 8 inteiros (32 / 4 = 8).
        int INT_LENGTH = BYTE_LENGTH / 4;
        int[] intSeed = new int[INT_LENGTH];

        // 4. Usa o ByteBuffer para converter de forma segura (lidando com endianness).
        ByteBuffer buffer = ByteBuffer.wrap(secureBytes);

        for (int i = 0; i < INT_LENGTH; i++) {
            // Obtém os 4 bytes seguintes e os converte em um int.
            intSeed[i] = buffer.getInt();
        }

        return intSeed;
    }
}
