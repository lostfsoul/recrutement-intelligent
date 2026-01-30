package ma.recrutement.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de Spring AI pour l'intégration avec OpenAI.
 *
 * @author Recrutement Team
 * @version 1.0.0
 */
@Configuration
public class SpringAIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-4}")
    private String chatModel;

    /**
     * Configure le ChatModel pour les interactions avec l'IA.
     *
     * @return le ChatModel
     */
    @Bean
    public ChatModel chatModel() {
        return new OpenAiChatModel(new OpenAiApi(openaiApiKey));
    }

    /**
     * Configure le ChatClient pour les interactions avec l'IA.
     *
     * @param chatModel le modèle de chat
     * @return le ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
