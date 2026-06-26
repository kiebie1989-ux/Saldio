package com.bwa.controlling.imports;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface BuchungRepository extends JpaRepository<Buchung, Long> {
    List<Buchung> findByImportBatchId(Long importBatchId);
}

interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    List<ImportBatch> findAllByOrderByImportiertAmDesc();
    java.util.Optional<ImportBatch> findFirstByDateiHash(String dateiHash);
}
