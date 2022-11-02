package com.samistax.application.data.service.astra;

import com.samistax.application.data.entity.astra.BookUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookChangeLogService {

    @Autowired

    private final BookChangeLogRepository repository;

    @Autowired
    public BookChangeLogService(BookChangeLogRepository repository) {

        this.repository = repository;
    }

    public Optional<BookUpdate> get(String isbn) {
        return repository.findById(isbn);
    }

    public BookUpdate update(BookUpdate entity) {
        return repository.save(entity);
    }

    public void delete(String isbn) {
        repository.deleteById(isbn);
    }

    public Slice<BookUpdate> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
