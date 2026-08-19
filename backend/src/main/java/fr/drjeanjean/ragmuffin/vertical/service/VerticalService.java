package fr.drjeanjean.ragmuffin.vertical.service;

import fr.drjeanjean.ragmuffin.vertical.Vertical;
import fr.drjeanjean.ragmuffin.vertical.VerticalRepository;
import fr.drjeanjean.ragmuffin.vertical.dto.CreateVerticalRequest;
import fr.drjeanjean.ragmuffin.vertical.dto.VerticalMapper;
import fr.drjeanjean.ragmuffin.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerticalService {
    private final VerticalRepository verticalRepository;
    private final WorkspaceRepository workspaceRepository;

    public Vertical create(CreateVerticalRequest request) {
        return verticalRepository.save(VerticalMapper.INSTANCE.toEntity(request));
    }

    public List<Vertical> findAll() {
        return verticalRepository.findAll();
    }

    public Vertical findById(UUID id) {
        return verticalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void delete(Vertical vertical) {
        if (vertical.isLocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This vertical cannot be deleted");
        }
        if (workspaceRepository.existsByVerticalId(vertical.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vertical is still associated with workspaces");
        }
        verticalRepository.delete(vertical);
    }
}
