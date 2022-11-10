package com.example.restexample.endpoint;

import com.example.restexample.model.Book;
import com.example.restexample.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class BookEndpoint {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;


    @GetMapping("/books")
    public List<Book> getAllBooks() {
        List<Book> all = bookRepository.findAll();
        if (!all.isEmpty()){
            ResponseEntity<HashMap> currency = restTemplate.getForEntity("https://cb.am/latest.json.php?currency=USD", HashMap.class);
            HashMap<String, String> hashMap = currency.getBody();
            if (!hashMap.isEmpty()){
                double usdCurrency = Double.parseDouble(hashMap.get("USD"));
                if (usdCurrency>0) {
                    for (Book book : all) {
                        double price = book.getPrice() / usdCurrency;
                        DecimalFormat df = new DecimalFormat("#.##"); /*rounding double price*/
                        book.setPrice(Double.parseDouble(df.format(price)));
                    }
                }
            }
        }
        return all;
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") int id) {
        Optional<Book> byId = bookRepository.findById(id);
        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(byId.get());
    }

    @PostMapping("/books")
    public ResponseEntity<?> createBook(@RequestBody Book book) {
        bookRepository.save(book);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/books")
    public ResponseEntity<Book> updateBook(@RequestBody Book book) {
        if (book.getId() == 0) {
            return ResponseEntity.badRequest().build();
        }
        bookRepository.save(book);
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBookByID(@PathVariable("id") int id) {
        bookRepository.deleteById(id);
        return ResponseEntity.notFound().build();

    }

}
