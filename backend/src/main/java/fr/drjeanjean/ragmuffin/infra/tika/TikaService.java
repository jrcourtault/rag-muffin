package fr.drjeanjean.ragmuffin.infra.tika;

import java.nio.file.Path;

public interface TikaService {

    String extract(Path filePath);
}
