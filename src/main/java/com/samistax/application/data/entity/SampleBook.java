package com.samistax.application.data.entity;

import java.time.LocalDate;
import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class SampleBook extends AbstractEntity {

    @Nonnull
    @Lob
    @Column(length = 1000000)
    private byte[] image;
    @Nonnull
    private String name;
    @Nonnull
    private String author;
    private LocalDate publicationDate;
    @Nonnull
    private Integer pages;
    @Nonnull
    private String isbn;
    @Nonnull
    private Integer qty;
    @Nonnull
    private Float price;

    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
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
    public SampleBook clone() {
        SampleBook copy = new SampleBook();
        copy.setImage(this.getImage());
        copy.setName(this.getName());
        copy.setAuthor(this.getAuthor());
        copy.setPublicationDate(this.getPublicationDate());
        copy.setPages(this.getPages());
        copy.setIsbn(this.getIsbn());
        copy.setPrice(this.getPrice());
        copy.setQty(this.getQty());
        return copy;
    }

}
