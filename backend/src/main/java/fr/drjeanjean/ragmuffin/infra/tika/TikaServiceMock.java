package fr.drjeanjean.ragmuffin.infra.tika;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@Profile("test")
public class TikaServiceMock implements TikaService {

    @Override
    public String extract(Path filePath) {
        return "Mocked text extraction for " + filePath.getFileName();
    }
}
