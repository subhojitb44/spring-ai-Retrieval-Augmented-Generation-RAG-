package com.subho.projects.airag.controller;

import com.subho.projects.airag.type.Answer;
import com.subho.projects.airag.type.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by subho
 * Date: 4/10/2024
 */
@RestController
@RequestMapping("/ask")
@RequiredArgsConstructor
public class AskController {

    private final ChatClient aiClient;
    private final VectorStore vectorStore;

    @Value("classpath:/rag-prompt-template.st")
    private Resource ragPromptTemplate;


    @PostMapping(value = "/question")
    public Answer ask(@RequestBody Question question) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(question.question()).withTopK(2));
        List<String> contentList = similarDocuments.stream().map(Document::getContent).toList();
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", question.question());
        promptParameters.put("documents", String.join("\n", contentList));
        Prompt prompt = promptTemplate.create(promptParameters);

        ChatResponse response = aiClient.call(prompt);
        return new Answer(response.getResult().getOutput().getContent());
    }

}
