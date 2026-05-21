package roomescape.store.repository;

import java.util.List;
import roomescape.store.Store;

public interface StoreRepository {
    List<Store> findAll();
}
