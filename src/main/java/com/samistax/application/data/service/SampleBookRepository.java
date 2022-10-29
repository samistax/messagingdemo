package com.samistax.application.data.service;

import com.samistax.application.data.entity.SampleBook;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleBookRepository extends JpaRepository<SampleBook, UUID> {

}