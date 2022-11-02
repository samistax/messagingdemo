package com.samistax.application.data.entity.astra;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Table("book_changelog")
public class BookUpdate {

    @PrimaryKeyColumn(name = "isbn", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.TEXT)
    private String isbn;

    @PrimaryKeyColumn(name = "updatedAt", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private LocalDateTime updatedAt;

    //@Lob
    //@Column(length = 1000000)
    @CassandraType(type = CassandraType.Name.BLOB)
    private byte[] image;
    private Integer qty;
    private Float price;
    private String name;
    private String author;
    private LocalDate publicationDate;
    @CassandraType(type = CassandraType.Name.INT)
    private Integer pages;
    @CassandraType(type = CassandraType.Name.MAP,typeArguments = { CassandraType.Name.TEXT,CassandraType.Name.TEXT})
    private Map<String, String> updatedValues = new HashMap<>();

    public byte[] getImage() {return image;}
    public void setImage(byte[] image) {this.image = image;}
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
    public Integer getPages() {
        return pages;
    }
    public void setPages(Integer pages) {
        this.pages = pages;
    }
    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price;}
    public Integer getQty() {return qty;}
    public void setQty(Integer qty) {this.qty = qty;}
    public Map getUpdatedValues() {return updatedValues;}
    public void setUpdatedValues(Map<String, String> updatedValues) {this.updatedValues = updatedValues;}
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}