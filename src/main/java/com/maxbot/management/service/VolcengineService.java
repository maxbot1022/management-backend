package com.maxbot.management.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxbot.management.config.VolcengineProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 火山引擎Ark API服务
 */
@Slf4j
@Service
public class VolcengineService {

    @Autowired
    private VolcengineProperties volcengineProperties;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * 提交图片生成任务
     */
    public String submitImageTask(String prompt, String referenceImageUrl) {
        try {
            HttpPost httpPost = new HttpPost(volcengineProperties.getBaseUrl() + "/images/generations");
            httpPost.setHeader("Authorization", "Bearer " + volcengineProperties.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", volcengineProperties.getImageModel());
            
            JSONObject input = new JSONObject();
            input.put("prompt", prompt);
            if (referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
                input.put("image", referenceImageUrl);
            }
            requestBody.put("input", input);

            JSONObject parameters = new JSONObject();
            parameters.put("width", 1024);
            parameters.put("height", 1024);
            requestBody.put("parameters", parameters);

            httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("图片生成任务提交响应: {}", responseBody);
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                // 根据实际API返回格式解析任务ID
                return jsonResponse.getString("id");
            }
        } catch (Exception e) {
            log.error("提交图片生成任务失败", e);
            throw new RuntimeException("提交图片生成任务失败", e);
        }
    }

    /**
     * 查询图片生成任务结果
     */
    public String queryImageTaskResult(String taskId) {
        try {
            // 根据火山引擎API实际接口调整
            HttpPost httpPost = new HttpPost(volcengineProperties.getBaseUrl() + "/images/query");
            httpPost.setHeader("Authorization", "Bearer " + volcengineProperties.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("task_id", taskId);

            httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("图片生成任务查询响应: {}", responseBody);
                return responseBody;
            }
        } catch (Exception e) {
            log.error("查询图片生成任务结果失败", e);
            throw new RuntimeException("查询图片生成任务结果失败", e);
        }
    }

    /**
     * 提交视频生成任务
     */
    public String submitVideoTask(String prompt, String imageUrl, String videoUrl) {
        try {
            HttpPost httpPost = new HttpPost(volcengineProperties.getBaseUrl() + "/videos/submit");
            httpPost.setHeader("Authorization", "Bearer " + volcengineProperties.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", volcengineProperties.getVideoModel());
            
            JSONObject input = new JSONObject();
            input.put("prompt", prompt);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                input.put("first_frame", imageUrl);
            }
            if (videoUrl != null && !videoUrl.isEmpty()) {
                input.put("reference_video", videoUrl);
            }
            requestBody.put("input", input);

            httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("视频生成任务提交响应: {}", responseBody);
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                return jsonResponse.getString("id");
            }
        } catch (Exception e) {
            log.error("提交视频生成任务失败", e);
            throw new RuntimeException("提交视频生成任务失败", e);
        }
    }

    /**
     * 查询视频生成任务结果
     */
    public String queryVideoTaskResult(String taskId) {
        try {
            HttpPost httpPost = new HttpPost(volcengineProperties.getBaseUrl() + "/videos/query");
            httpPost.setHeader("Authorization", "Bearer " + volcengineProperties.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("task_id", taskId);

            httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("视频生成任务查询响应: {}", responseBody);
                return responseBody;
            }
        } catch (Exception e) {
            log.error("查询视频生成任务结果失败", e);
            throw new RuntimeException("查询视频生成任务结果失败", e);
        }
    }

    /**
     * 调用LLM整理小说文本
     */
    public String processNovelText(String text, String prompt) {
        try {
            log.info("准备调用火山引擎小说整理接口, textLength={}, promptLength={}",
                    text == null ? 0 : text.length(),
                    prompt == null ? 0 : prompt.length());

            HttpPost httpPost = new HttpPost(volcengineProperties.getBaseUrl() + "/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + volcengineProperties.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "doubao-seed-2-0-pro-260215"); // 使用豆包大模型
            
            com.alibaba.fastjson.JSONArray messages = new com.alibaba.fastjson.JSONArray();
            
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个专业的小说分镜整理助手。请将用户提供的小说文本整理成结构化的分镜脚本，包含场景、动作、情绪、人物等要素。输出JSON格式。");
            messages.add(systemMsg);
            
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            String content = "整理要求：" + prompt + "\n\n小说文本：\n" + text;
            userMsg.put("content", content);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4000);

            httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("小说整理响应, statusCode={}, body={}", statusCode, responseBody);
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (Exception e) {
            log.error("小说整理失败", e);
            throw new RuntimeException("小说整理失败", e);
        }
    }
}
