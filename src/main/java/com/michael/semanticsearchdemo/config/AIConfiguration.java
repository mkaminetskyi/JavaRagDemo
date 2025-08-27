package com.michael.semanticsearchdemo.config;

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
    public ChatClient ChatClientBean(ChatClient.Builder builder, VectorStore vectorStore) {
       return builder.defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }
}
