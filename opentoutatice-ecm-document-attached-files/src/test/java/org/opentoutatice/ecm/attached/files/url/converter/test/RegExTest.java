package org.opentoutatice.ecm.attached.files.url.converter.test;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.htmlsanitizer.HtmlSanitizerAntiSamyDescriptor;
import org.nuxeo.ecm.platform.htmlsanitizer.HtmlSanitizerServiceImpl;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;

import com.google.inject.Inject;

//@RunWith(FeaturesRunner.class)
//@Features(CoreFeature.class)
public class RegExTest {
    
//    @Inject
//    HtmlSanitizerServiceImpl sanitizer;
    
    
    static final String INTERNAL_PICTURE_INDICATOR = "internalPicture";
    static final Pattern PATTERN_NX_INTERNAL_PICTURE = Pattern.compile("(src=\"nxfile/default/)([a-zA-Z0-9[-]&&[^/]]*)(/ttc:images/[.[^\"]]+\")");
    static final Pattern PATTERN_NX_INTERNAL_PICTURE_PARAM = Pattern.compile("([?]conversationId=[.[^\"]]+)");
    static final Pattern PATTERN_INTERNAL_PICTURE = Pattern.compile("(src=\"nxfile/default/)(".concat(INTERNAL_PICTURE_INDICATOR).concat(")").concat("(/ttc:images/[.[^\"]]*\")"));
    
    static final Pattern IMG_SRC = Pattern.compile("([p{L}p{N}.#@$%+&amp;;-_~,?=/!]+|#(w)+)");
    

    @Test
    public void test_nx_internal_picture() {
        String content = "<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/df253d7f-4811-42d7-b853-512df435c1b0/ttc:images/2/file/chat.jpeg\" />ppp</p> \r\n"
                .concat("<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/df253d7f-8811-42d7-b853-512df435c1b0/ttc:images/0/file/rat.jpg?conversationId=0NXMAIN1\" alt=\"\" />pbp</p>");
        
        Matcher matcher = PATTERN_NX_INTERNAL_PICTURE.matcher(content);
        
        StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String g3 = matcher.group(3);
                Matcher paramMatcher = PATTERN_NX_INTERNAL_PICTURE_PARAM.matcher(g3);
                if(paramMatcher.find()){
                    g3 = StringUtils.remove(g3, paramMatcher.group(1));
                }
                matcher.appendReplacement(sb, "$1" + INTERNAL_PICTURE_INDICATOR + g3);
            }
            sb = matcher.appendTail(sb);
        System.out.println(sb.toString());
    }
    
    @Test
    public void test_internal_picture() {
        String content = "<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/internalPicture/ttc:images/2/file/chat.jpeg\" alt=\"\"/>ppp</p> \r \n\n  \r"
                .concat("<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/internalPicture/ttc:images/0/file/chat222222.jpeg\" alt=\"\" />ppp</p>");
        
        Matcher matcher = PATTERN_INTERNAL_PICTURE.matcher(content);
        final String param = "?conversationId=ONXMAIN18";
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String g3 = matcher.group(3);
            
            g3 = StringUtils.remove(g3, "\"").concat(param).concat("\"");
            
            matcher.appendReplacement(sb, "$1" + "TTTTT" + g3);
        }
        sb = matcher.appendTail(sb);
        System.out.println(sb.toString());
    }
    
//    @Test
//    public void test_img_src_sanitizer() throws PolicyException{
//        String content = "<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/internalPicture/ttc:images/2/file/chat.jpeg\" />ppp</p> \r \n\n  \r";
//        
//        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
//                "antisamy-urls-policy.xml");
//         Policy policy = Policy.getInstance(is);
//        
//        sanitizer.policy = policy;
//        content = sanitizer.sanitizeString(content, "euh");
//        System.out.println(content);
//        
//        Assert.assertNotEquals(content, StringUtils.EMPTY);
//        
//    }

}
