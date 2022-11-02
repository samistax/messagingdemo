package com.samistax.application.data.entity.astra.cdc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.avro.reflect.AvroSchema;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookCDC {
    BookCDC.Key key;
    BookCDC.Value value;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Key {
        String isbn;
        Long updatedAt;
        public String toString() {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(updatedAt);

            return "Isbn: " + isbn + " update (" + df.format(cal.getTimeInMillis())  +")";
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Value {
        private byte[] image;
        private Integer qty;
        private Float price;
        private String name;
        private String author;
        @AvroSchema("{\"type\":\"int\",\"logicalType\":\"date\"}")
        private LocalDate publicationdate;
        private Integer pages;
        @AvroSchema("{\"type\":\"map\",\"values\":[\"null\",\"string\"],\"default\":{}}")
        private Map<String, String> updatedvalues;

        public String toString() {
            String parsedString = "";
            // Iterate all changed values to returned String.
            Iterator<Map.Entry<String, String>> iterator = updatedvalues.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> e = iterator.next();
                parsedString = parsedString.concat(e.getKey() + " ->" + e.getValue());
                if ( iterator.hasNext() ) {
                    parsedString = parsedString.concat(System.lineSeparator());
                }
            }
            //return name + ": " + author + " " + publicationdate +" (Qty:" + qty +" pcs, price = "+ price +")";
            return parsedString;
        }
    }
}
