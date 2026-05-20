package edu.ruc.platform.knowledge.repository;

import edu.ruc.platform.knowledge.domain.LatestCertTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LatestCertTemplateRepository extends JpaRepository<LatestCertTemplate, Long> {

    List<LatestCertTemplate> findByIsDeletedAndIsActive(Integer isDeleted, Integer isActive);

    List<LatestCertTemplate> findByIsDeletedOrderByIdAsc(Integer isDeleted);

    Optional<LatestCertTemplate> findByTemplateCodeAndIsDeleted(String templateCode, Integer isDeleted);
}
