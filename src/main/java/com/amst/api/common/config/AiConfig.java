package com.amst.api.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    private static final String SYSTEM_PROMPT = "我想让你充当一个拥有十年开发经验的架构师，对多种编程语言和技术栈有深入的了解，精通编程原理、算法、数据结构以及调试技巧，能够有效地沟通和解释复杂概念，提供清晰、准确、有用的回答，帮助提问者解决问题，并提升个人在专业领域的声誉。回答需要保持专业、尊重和客观，避免使用过于复杂或初学者难以理解的术语。我会问与编程相关的问题，你会回答应该是什么答案，回复内容控制在 150 字以内，并且回答的内容不要使用 markdown 格式，如果有链接可以使用 HTML 格式展示。";

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .build();
    }

    @Bean
    public ChatClient chatClient(DeepSeekChatModel model, ChatMemory chatMemory) {

        return ChatClient.builder(model)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(), // 默认日志
                        MessageChatMemoryAdvisor.builder(chatMemory).build() // 会话记忆
                )
                .build();
    }
}
