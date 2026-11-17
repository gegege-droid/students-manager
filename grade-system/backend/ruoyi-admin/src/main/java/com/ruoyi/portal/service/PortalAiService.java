package com.ruoyi.portal.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;

/**
 * DeepSeek AI 咨询代理。
 *
 * 前端只调用 /portal/ai/chat，API Key 保存在服务端配置中，不下发到浏览器。
 */
@Service
public class PortalAiService
{
    private static final Logger log = LoggerFactory.getLogger(PortalAiService.class);

    /** 最多保留的历史消息条数（含本轮），防止请求体过大 */
    private static final int MAX_MESSAGES = 20;

    /** 单条消息最大长度 */
    private static final int MAX_CONTENT_LENGTH = 4000;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${deepseek.read-timeout:60000}")
    private int readTimeout;

    public Map<String, Object> status()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("enabled", apiKey != null && !apiKey.trim().isEmpty());
        result.put("model", model);
        return result;
    }

    /**
     * 调用 DeepSeek Chat Completions。
     *
     * @param body 前端请求体，messages 为 [{role, content}] 数组
     * @return {reply, model, usage}
     */
    public Map<String, Object> chat(Map<String, Object> body)
    {
        if (apiKey == null || apiKey.trim().isEmpty())
        {
            throw new ServiceException("AI 助手尚未配置 DeepSeek API Key，请联系管理员在 application.yml 中设置");
        }

        Object rawMessages = body == null ? null : body.get("messages");
        if (!(rawMessages instanceof List) || ((List<?>) rawMessages).isEmpty())
        {
            throw new ServiceException("消息不能为空");
        }

        List<Map<String, String>> messages = normalizeMessages((List<?>) rawMessages);
        if (messages.isEmpty())
        {
            throw new ServiceException("消息不能为空");
        }

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 800);
        payload.put("stream", Boolean.FALSE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        RestTemplate restTemplate = new RestTemplate(factory);

        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";
        HttpEntity<String> requestEntity = new HttpEntity<String>(JSON.toJSONString(payload), headers);

        // 网络偶发抖动（TLS 握手失败、连接重置）时自动重试，最多 3 次
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++)
        {
            try
            {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,
                        requestEntity, String.class);
                return parseResponse(response.getBody());
            }
            catch (HttpStatusCodeException ex)
            {
                String detail = ex.getResponseBodyAsString();
                int status = ex.getRawStatusCode();
                log.warn("DeepSeek 调用失败，status={}，body={}", status, detail);
                // 429 或 5xx 可重试，其余（401/402/400）直接返回，避免无意义重试
                boolean retryable = status == 429 || status >= 500;
                if (retryable && attempt < maxAttempts)
                {
                    sleepQuietly(attempt);
                    continue;
                }
                throw new ServiceException("AI 服务调用失败（" + status + "）：" + briefError(detail));
            }
            catch (RestClientException ex)
            {
                log.warn("DeepSeek 调用异常（第 {} 次）", attempt, ex);
                if (attempt < maxAttempts)
                {
                    sleepQuietly(attempt);
                    continue;
                }
                throw new ServiceException("AI 服务暂时不可用，请稍后再试");
            }
        }
        throw new ServiceException("AI 服务暂时不可用，请稍后再试");
    }

    private void sleepQuietly(int attempt)
    {
        try
        {
            Thread.sleep(400L * attempt);
        }
        catch (InterruptedException ignored)
        {
            Thread.currentThread().interrupt();
        }
    }

    private List<Map<String, String>> normalizeMessages(List<?> raw)
    {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Object item : raw)
        {
            if (!(item instanceof Map))
            {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) item;
            String role = map.get("role") == null ? "" : String.valueOf(map.get("role")).trim();
            String content = map.get("content") == null ? "" : String.valueOf(map.get("content")).trim();
            if (content.isEmpty())
            {
                continue;
            }
            if (!"system".equals(role) && !"user".equals(role) && !"assistant".equals(role))
            {
                continue;
            }
            if (content.length() > MAX_CONTENT_LENGTH)
            {
                content = content.substring(0, MAX_CONTENT_LENGTH);
            }
            Map<String, String> msg = new HashMap<String, String>();
            msg.put("role", role);
            msg.put("content", content);
            list.add(msg);
        }

        // 只保留最近的若干条；若截断后以 assistant 开头，向前补一条 system 保持对话有效
        if (list.size() > MAX_MESSAGES)
        {
            list = new ArrayList<Map<String, String>>(
                    list.subList(list.size() - MAX_MESSAGES, list.size()));
        }
        if (!list.isEmpty() && !"system".equals(list.get(0).get("role")))
        {
            Map<String, String> system = new HashMap<String, String>();
            system.put("role", "system");
            system.put("content", "你是一个中文成绩管理系统助手，回答简洁、准确、友好。");
            list.add(0, system);
        }
        return list;
    }

    private Map<String, Object> parseResponse(String body)
    {
        if (body == null || body.isEmpty())
        {
            throw new ServiceException("AI 服务返回为空，请稍后再试");
        }
        JSONObject json = JSON.parseObject(body);
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            throw new ServiceException("AI 服务未返回有效内容，请稍后再试");
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        String reply = message == null ? "" : message.getString("content");
        if (reply == null || reply.trim().isEmpty())
        {
            throw new ServiceException("AI 服务未返回有效内容，请稍后再试");
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("reply", reply.trim());
        result.put("model", json.getString("model") == null ? model : json.getString("model"));
        JSONObject usage = json.getJSONObject("usage");
        if (usage != null)
        {
            Map<String, Object> u = new LinkedHashMap<String, Object>();
            u.put("promptTokens", usage.getInteger("prompt_tokens"));
            u.put("completionTokens", usage.getInteger("completion_tokens"));
            u.put("totalTokens", usage.getInteger("total_tokens"));
            result.put("usage", u);
        }
        return result;
    }

    private String briefError(String body)
    {
        if (body == null || body.isEmpty())
        {
            return "无响应内容";
        }
        try
        {
            JSONObject json = JSON.parseObject(body);
            JSONObject error = json.getJSONObject("error");
            if (error != null && error.getString("message") != null)
            {
                return error.getString("message");
            }
        }
        catch (Exception ignored)
        {
            // 非 JSON 响应，直接截断返回
        }
        return body.length() > 160 ? body.substring(0, 160) : body;
    }
}
