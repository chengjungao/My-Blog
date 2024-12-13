package com.site.blog.my.core.controller.common;
import com.site.blog.my.core.entity.Message;
import com.site.blog.my.core.service.MessageService;
import com.site.blog.my.core.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/api/xxx")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Value("${token:}")
    private String token;

    @GetMapping("/xxx")
    public String verify(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        if (SignUtil.checkSignature(signature, timestamp, nonce,token)) {
            return echostr;
        }
        return "校验失败";
    }

    @PostMapping("/wechat")
    public String handleMessage(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder xmlData = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                xmlData.append(line);
            }

            String fromUser = extractTagValue(xmlData.toString(), "FromUserName");
            String toUser = extractTagValue(xmlData.toString(), "ToUserName");
            String content = extractTagValue(xmlData.toString(), "Content");
            String msgType = extractTagValue(xmlData.toString(), "MsgType");

            // 存储消息到数据库
            Message message = new Message(fromUser, toUser, content, msgType, new Date());


            return buildReplyMessage(messageService.handleMessage(message));

        } catch (IOException e) {
            e.printStackTrace();
            return "处理失败";
        }
    }

    // 解析XML节点值
    // 提取 XML 标签值的方法
    private String extractTagValue(String xml, String tagName) {
        String startTag = "<" + tagName + "><![CDATA[";
        String endTag = "]]></" + tagName + ">";

        int startIndex = xml.indexOf(startTag);
        int endIndex = xml.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            return xml.substring(startIndex + startTag.length(), endIndex).trim();
        }
        return "未找到 " + tagName + " 标签";
    }

    // 构建回复XML消息
    private String buildReplyMessage(Message reply) {
        return "<xml>" +
                "<ToUserName><![CDATA[" + reply.getToUser() + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + reply.getFromUser() + "]]></FromUserName>" +
                "<CreateTime>" + System.currentTimeMillis() / 1000 + "</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[" + reply.getContent() + "]]></Content>" +
                "</xml>";
    }
}

