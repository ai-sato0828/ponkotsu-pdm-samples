package info.syachiku.audit;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
            select o
              from PurchaseOrder o
              join fetch o.customer
             order by o.id
            """)
    List<Order> findAllWithCustomer();

    @EntityGraph(attributePaths = "customer")
    List<Order> findByStatusOrderById(OrderStatus status);

    @Query("""
            select new info.syachiku.audit.OrderRow(o.id, c.name, o.totalAmount)
              from PurchaseOrder o
              join o.customer c
             order by o.id
            """)
    List<OrderRow> findOrderRows();
}
