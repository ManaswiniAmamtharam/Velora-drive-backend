
package com.velora.velorabackend.repository;

import com.velora.velorabackend.model.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {
    List<Enquiry> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    List<Enquiry> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
