package com.site.blog.my.core.controller.common;
import com.site.blog.my.core.entity.Message;
import com.site.blog.my.core.service.MessageService;
import com.site.blog.my.core.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/api/xxxxx")
public class MessageController {

    @Autowired
    private MessageService messageService;


    @GetMapping("/xxxxx")
    public String verify(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        if (SignUtil.checkSignature(signature, timestamp, nonce)) {
            return echostr;
        }
        return "校验失败";
    }

    @PostMapping("/xxxxx")
    public String handleMessage(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder xmlData = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                xmlData.append(line);
            }

            String fromUser = getTagValue(xmlData.toString(), "FromUserName");
            String toUser = getTagValue(xmlData.toString(), "ToUserName");
            String content = getTagValue(xmlData.toString(), "Content");
            String msgType = getTagValue(xmlData.toString(), "MsgType");

            // 存储消息到数据库
            Message message = new Message(fromUser, toUser, content, msgType, new Date());


            return buildReplyMessage(messageService.handleMessage(message));

        } catch (IOException e) {
            e.printStackTrace();
            return "处理失败";
        }
    }

    // 解析XML节点值
    private String getTagValue(String xml, String tagName) {
        int start = xml.indexOf("<" + tagName + ">") + tagName.length() + 2;
        int end = xml.indexOf("</" + tagName + ">");
        return start > end ? "" : xml.substring(start, end);
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

