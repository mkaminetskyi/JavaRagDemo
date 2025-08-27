package com.michael.semanticsearchdemo.controller;

import com.michael.semanticsearchdemo.dto.ChatResponse;
import com.michael.semanticsearchdemo.dto.DocumentRequest;
import com.michael.semanticsearchdemo.utils.DocumentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
public class RagController {
    private final JdbcTemplate jdbcTemplate;
    private final JdbcTemplate h2JdbcTemplate;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagController(JdbcTemplate jdbcTemplate, @Qualifier("h2JdbcTemplate") JdbcTemplate h2JdbcTemplate, VectorStore vectorStore, ChatClient chatClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.h2JdbcTemplate = h2JdbcTemplate;
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    @PostMapping("/rag-text")
    public ChatResponse ragText(@RequestBody DocumentRequest request) {
        List<Document> docs = vectorStore.similaritySearch(SearchRequest.builder()
                .query(request.content())
                .topK(3)
                .build());
        String context = docs.stream().map(Document::getText).reduce("", (a, b) -> a + "\n" + b).trim();
        String answer = chatClient.prompt().system(context).user(request.content()).call().content();
        return new ChatResponse(answer, docs.stream().map(Document::getText).toList());
    }

    @PostMapping("/rag-table")
    public ChatResponse ragTable(@RequestBody DocumentRequest request) {
        List<String> rows = h2JdbcTemplate.query("select name, info from items", (rs, i) ->
                rs.getString("name") + " " + rs.getString("info"));
        String context = String.join("\n", rows);
        String answer = chatClient.prompt().system(context).user(request.content()).call().content();
        return new ChatResponse(answer, rows);
    }

    @PostMapping("/rag-table-sql")
    public ChatResponse ragTableSql(@RequestBody DocumentRequest request) {
        String sql = chatClient.prompt()
                .system("Write sql for h2 table items with columns id,name,info")
                .user(request.content())
                .call()
                .content();
        List<String> rows = h2JdbcTemplate.query(sql, (rs, i) ->
                rs.getString("name") + " " + rs.getString("info"));
        String data = String.join("\n", rows);
        String answer = chatClient.prompt().system(data).user(request.content()).call().content();
        return new ChatResponse(answer, List.of(sql));
    }

    @PostMapping("/add-document")
    public void addDocumentToVectorStore(@RequestBody DocumentRequest request) {
        vectorStore.add(List.of(new Document(request.content())));
        log.info("added document to vector store");
    }

    @PostMapping("/upload-csv-file")
    public ResponseEntity<String> uploadCsvFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a file");
        }
        List<Document> documents = DocumentUtils.readCsvAsDocuments(file);
        vectorStore.add(documents);
        return ResponseEntity.ok("Successfully embedded and stored " + documents.size() + " chunks");
    }

    @PostMapping("/upload-text-file")
    public ResponseEntity<String> uploadTextFile(@RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        List<String> chunks = DocumentUtils.splitIntoChunks(content, 256);
        List<Document> documents = chunks.stream().map(Document::new).toList();
        vectorStore.add(documents);
        return ResponseEntity.ok("Successfully embedded and stored " + documents.size() + " chunks");
    }

    @PostMapping("/add-default-documents")
    public void addDefaultDocumentToVectorStore() {
        List<String> facts = List.of(
                "Albert Einstein was a physicist known for the theory of relativity.",
                "Marie Curie won two Nobel Prizes in Physics and Chemistry.",
                "Isaac Newton formulated the laws of motion and gravity.",
                "Nikola Tesla invented the alternating current system.",
                "Ada Lovelace is considered the first computer programmer.",
                "Galileo was an astronomer who supported heliocentrism.",
                "Leonardo da Vinci painted the Mona Lisa.",
                "Vincent van Gogh was a Dutch post-impressionist painter.",
                "Mozart was a prolific and influential composer of the classical era.",
                "Beethoven composed music even after losing his hearing.",
                "Einstein was not known for his musical talents.",
                "Curie did not work on computer science."
        );
        List<Document> documents = facts.stream().map(Document::new).toList();
        vectorStore.add(documents);
        log.info("added default documents");
    }

    @DeleteMapping("/delete-all-documents")
    public ResponseEntity<String> deleteAllDocuments() {
        jdbcTemplate.execute("DELETE FROM vector_store");
        log.info("deleted all documents from vector store");
        return ResponseEntity.ok("All documents deleted");
    }
}
