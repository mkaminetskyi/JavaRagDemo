package com.michael.ragdemo.config;

import com.michael.ragdemo.service.ProductTools;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class AIConfiguration {

    @Bean
    public ChatClient ChatClientBean(ChatClient.Builder builder, VectorStore vectorStore,
                                     ProductTools productTools) {
       return builder
              // .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
               .defaultSystem("You are an AI assistant answering questions about different products. " +
                       "Answer only based on RAG or function calling data")
              // .defaultTools(productTools)
               .build();
    }
}
