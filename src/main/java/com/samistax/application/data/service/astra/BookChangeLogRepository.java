package com.samistax.application.data.service.astra;

import com.samistax.application.data.entity.astra.BookUpdate;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;

public interface BookChangeLogRepository extends CassandraRepository<BookUpdate,String> {
    List<BookUpdate> findByIsbn(final String isbn);
}