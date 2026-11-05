package info.syachiku.fetchpaging;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
            select o from PurchaseOrder o
            left join fetch o.items
            order by o.placedOn desc, o.id asc
            """)
    List<Order> findPageWithItems(Pageable page);

    @Query("""
            select o.id from PurchaseOrder o
            order by o.placedOn desc, o.id asc
            """)
    List<Long> findPageIds(Pageable page);

    @Query("""
            select o from PurchaseOrder o
            left join fetch o.items
            where o.id in :ids
            """)
    List<Order> findWithItems(@Param("ids") List<Long> ids);
}
