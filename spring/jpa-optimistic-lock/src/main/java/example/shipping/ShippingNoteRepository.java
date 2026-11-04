package example.shipping;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShippingNoteRepository extends JpaRepository<ShippingNote, Long> {
}
