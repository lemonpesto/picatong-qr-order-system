package lemon.qrordersystem.repository;

import lemon.qrordersystem.entity.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long> {
    Optional<TableEntity> findByTableNum(Integer tableNum);
    Optional<TableEntity> findByTableNumAndAccessKey(Integer tableNum, String accessKey);
}
