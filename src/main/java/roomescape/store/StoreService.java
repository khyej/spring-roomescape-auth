package roomescape.store;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.store.dto.StoreResponse;
import roomescape.store.repository.StoreRepository;

@Service
public class StoreService {
    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<StoreResponse> findAll() {
        return storeRepository.findAll().stream()
                .map(StoreResponse::from)
                .toList();
    }
}
